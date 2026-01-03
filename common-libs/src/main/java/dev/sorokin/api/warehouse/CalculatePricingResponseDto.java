package dev.sorokin.api.warehouse;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ответ на пересчёт: финальная сумма и причина изменений
 */
public record CalculatePricingResponseDto(
        UUID orderId,
        BigDecimal finalAmount,
        String reason
) { }
