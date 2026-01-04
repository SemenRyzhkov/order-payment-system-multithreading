package dev.sorokin.api;

import java.math.BigDecimal;

public record OrderCreateRequestDto(
        String address,
        BigDecimal clientEstimate
) { }
