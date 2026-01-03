package dev.sorokin.api;

import dev.sorokin.api.warehouse.CalculatePricingRequestDto;
import dev.sorokin.api.warehouse.CalculatePricingResponseDto;
import dev.sorokin.config.WarehouseStubProperties;
import dev.sorokin.utils.StubUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * HTTP-stub сервиса пересчёта цен
 */
@Slf4j
@RestController
@RequestMapping("/warehouse")
@AllArgsConstructor
public class WarehouseStubController {

    private final WarehouseStubProperties properties;

    @PostMapping("/calculate-price")
    public ResponseEntity<CalculatePricingResponseDto> calculatePricing(
            @RequestBody CalculatePricingRequestDto calculatePricingRequest
    ) {
        log.info("Calculate price called: request={}", calculatePricingRequest);
        StubUtils.randomSafeSleepMs(
                properties.getLatencyMinMillis(),
                properties.getLatencyMaxMillis()
        );

        boolean isThrowException = properties.isExceptionEnabled()
                && StubUtils.chance(properties.getExceptionProbability());

        if (isThrowException) {
            throw new RuntimeException("Warehouse pricing unavailable (simulated)");
        }

        var amount = StubUtils.getRandomBetween(
                properties.getFinalAmountMin(),
                properties.getFinalAmountMax()
        );

        return ResponseEntity.ok(
                new CalculatePricingResponseDto(
                        calculatePricingRequest.orderId(),
                        BigDecimal.valueOf(amount),
                        "Price calculated by stub"
                )
        );
    }

}
