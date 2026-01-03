package dev.sorokin.api;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderDto(
        UUID id,
        String address // todo остальные поля
) { }
