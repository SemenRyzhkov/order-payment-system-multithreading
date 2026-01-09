#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "[SMOKE] Creating order..."
CREATE_JSON=$(curl -sS -X POST "$BASE_URL/orders" \
  -H 'Content-Type: application/json' -H 'Accept: application/json' \
  -d '{"address":"smoke-street","description":"smoke-order"}')

ORDER_ID=$(echo "$CREATE_JSON" | jq -r '.orderId')

if [[ -z "$ORDER_ID" || "$ORDER_ID" == "null" ]]; then
  echo "[SMOKE][FAIL] create returned no orderId. Body: $CREATE_JSON" >&2
  exit 1
fi

echo "[SMOKE] Created orderId=$ORDER_ID. Polling status..."
TERMINAL_STATE_REACHED=0

for i in {1..60}; do
  RESP=$(curl -sS -H 'Accept: application/json' "$BASE_URL/orders/$ORDER_ID" || true)
  STATUS=$(echo "$RESP" | jq -r '.status // empty')
  if [[ "$STATUS" == "CREATED_SUCCESSFULLY" || "$STATUS" == "CANCELLED" ]]; then
    echo "[SMOKE] Final status: $STATUS"
    TERMINAL_STATE_REACHED=1
    break
  fi
  sleep 0.25
done

if [[ $TERMINAL_STATE_REACHED -ne 1 ]]; then
  echo "[SMOKE][FAIL] Order did not reach terminal state in time" >&2
  exit 2
fi

echo "[SMOKE][OK]"

