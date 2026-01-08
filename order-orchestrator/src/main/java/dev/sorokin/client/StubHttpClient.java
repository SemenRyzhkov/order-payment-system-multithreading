package dev.sorokin.client;

import dev.sorokin.api.payment.AuthorizePaymentRequestDto;
import dev.sorokin.api.payment.AuthorizePaymentResponseDto;
import dev.sorokin.api.payment.CapturePaymentRequestDto;
import dev.sorokin.api.payment.CapturePaymentResponseDto;
import dev.sorokin.api.warehouse.CalculatePricingRequestDto;
import dev.sorokin.api.warehouse.CalculatePricingResponseDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface StubHttpClient {

    @PostExchange("/payment/authorize")
    AuthorizePaymentResponseDto authorizePayment(
            @RequestBody AuthorizePaymentRequestDto authorizePaymentRequestDto
    );

    @PostExchange("payment/capture")
    CapturePaymentResponseDto capturePayment(
            @RequestBody CapturePaymentRequestDto capturePaymentRequestDto
    );

    @PostExchange("/warehouse/calculate-price")
    CalculatePricingResponseDto calculatePricing(
            @RequestBody CalculatePricingRequestDto calculatePricingRequest
    );
}
