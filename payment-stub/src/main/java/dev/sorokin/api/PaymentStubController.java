package dev.sorokin.api;

import dev.sorokin.api.payment.AuthorizationStatus;
import dev.sorokin.api.payment.AuthorizePaymentRequestDto;
import dev.sorokin.api.payment.AuthorizePaymentResponseDto;
import dev.sorokin.api.payment.CapturePaymentRequestDto;
import dev.sorokin.api.payment.CapturePaymentResponseDto;
import dev.sorokin.api.payment.CaptureStatus;
import dev.sorokin.config.PaymentStubProperties;
import dev.sorokin.utils.StubUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * HTTP-stub платежного шлюза: авторизация и списание
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@AllArgsConstructor
public class PaymentStubController {

    private final PaymentStubProperties properties;

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizePaymentResponseDto> authorizePayment(
            @RequestBody AuthorizePaymentRequestDto authorizePaymentRequest
    ) {
        log.info("Authorize called: request={}", authorizePaymentRequest);
        StubUtils.randomSafeSleepMs(
                properties.getAuthorizeLatencyMinMillis(),
                properties.getAuthorizeLatencyMaxMillis()
        );

        var isThrowException = properties.isExceptionEnabled()
                && StubUtils.chance(properties.getExceptionProbability());

        if (isThrowException) {
            throw new RuntimeException("Payment gateway unavailable (simulated)");
        }

        boolean isAuthorizeDeclined = properties.isDeclineEnabled()
                && StubUtils.chance(properties.getDeclineProbability());

        if (isAuthorizeDeclined) {
            log.info("Authorize declined");
            return ResponseEntity.ok(
                    new AuthorizePaymentResponseDto(
                            null,
                            authorizePaymentRequest.amount(),
                            AuthorizationStatus.DECLINED,
                            "Card was declined by stub"
                    )
            );
        }

        var authId = UUID.randomUUID();

        log.info("Authorize approved: authId={}", authId);
        return ResponseEntity.ok(
                new AuthorizePaymentResponseDto(
                        authId,
                        authorizePaymentRequest.amount(),
                        AuthorizationStatus.AUTHORIZED,
                        null
                )
        );
    }

    @PostMapping("/capture")
    public ResponseEntity<CapturePaymentResponseDto> capturePayment(
            @RequestBody CapturePaymentRequestDto capturePaymentRequest
    ) {
        log.info("Capture called: request={}", capturePaymentRequest);
        StubUtils.randomSafeSleepMs(
                properties.getCaptureLatencyMinMillis(),
                properties.getCaptureLatencyMaxMillis()
        );

        var isThrowException = properties.isExceptionEnabled()
                && StubUtils.chance(properties.getExceptionProbability());

        if (isThrowException) {
            throw new RuntimeException("Payment gateway capture failed (simulated)");
        }

        var captureId = UUID.randomUUID();

        log.info("Capture succeeded: captureId={}", captureId);

        return ResponseEntity.ok(
                new CapturePaymentResponseDto(
                        captureId,
                        capturePaymentRequest.captureAmount(),
                        CaptureStatus.CAPTURED,
                        null
                )
        );
    }

}
