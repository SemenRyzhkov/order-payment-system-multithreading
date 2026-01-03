package dev.sorokin.api.warehouse;

import java.util.UUID;

/**
 * Запрос на пересчёт цены заказа по его идентификатору
 */
public record CalculatePricingRequestDto (
        UUID orderId
) { }
