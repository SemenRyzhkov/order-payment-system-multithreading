import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

export const options = {
    scenarios: {
        create_and_poll: {
            executor: 'constant-vus',
            exec: 'default',
            vus: 60,
            duration: '10s',
        },
    },
};

const createLatency = new Trend('create_latency_ms');
const terminalLatency = new Trend('terminal_latency_ms');
const ordersCreated = new Counter('orders_created_total');

const baseUrl = __ENV.BASE || 'http://host.docker.internal:8080';
const jsonHeaders = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
};

const reqTimeout = __ENV.HTTP_TIMEOUT || '5s';

function getFieldFromJson(resp, fieldName) {
    try {
        return resp.json(fieldName);
    } catch (_) {
        return null;
    }
}

export default function () {
    const address = `Moscow City, VU-${__VU}, ITER-${__ITER}`;
    const clientEstimate = 9000.5 + __VU * 0.01 + __ITER * 0.001;
    const customerId = 1000 + __VU;

    const createPayload = JSON.stringify({
        address: address,
        clientEstimate: clientEstimate,
        customerId: customerId,
    });

    const t0 = Date.now();
    const createResp = http.post(`${baseUrl}/order`, createPayload, {
        headers: jsonHeaders,
        timeout: reqTimeout,
    });

    createLatency.add(createResp.timings.duration);

    const createdOk = check(createResp, {
        'create 201': (r) => r.status === 201,
        'create JSON': (r) => (r.headers['Content-Type'] || '').includes('application/json'),
    });

    if (!createdOk) {
        fail(`Create failed: status=${createResp.status}, body=${createResp.body}`);
    }

    const id = getFieldFromJson(createResp, 'id');
    if (!id) {
        fail(`No id in response: body=${createResp.body}`);
    }

    ordersCreated.add(1);


    // 2) Поллить до терминального статуса и измерить «от создания до терминала»
  const terminal = new Set(['AUTHORIZATION_FAILED', 'PRICE_CHANGED_FAILED', 'SUCCESS_PAID']);

  // экспоненциальный backoff: 1ms → 2ms → 4ms → 8ms → ... (максимум 15 итераций)
  const maxPollIters = 15;
  let sleepMs = 1;

  let finalized = false;
  let lastStatus = null;

  for (let i = 0; i < maxPollIters; i++) {
    const pollResp = http.get(`${baseUrl}/order/${id}`, {
      headers: jsonHeaders,
      timeout: reqTimeout,
    });

    const ok = check(pollResp, { 'poll 200/404': (r) => r.status === 200 || r.status === 404 });
    if (!ok || pollResp.status === 404) {
      sleep(sleepMs / 1000);   // sleep принимает секунды → конвертируем миллисекунды
      sleepMs *= 2;            // экспоненциальное увеличение
      continue;
    }

    lastStatus = getFieldFromJson(pollResp, 'paymentStatus');

    if (terminal.has(lastStatus)) {
      const t1 = Date.now();
      terminalLatency.add(t1 - t0, { status: lastStatus }); // метрика end-to-end
      finalized = true;
      break;
    }

    sleep(sleepMs / 1000);     // задержка между поллингом
    sleepMs *= 2;              // удваиваем задержку
  }

  check(finalized, {
    'order reached terminal state': (v) => v === true
  });
}
