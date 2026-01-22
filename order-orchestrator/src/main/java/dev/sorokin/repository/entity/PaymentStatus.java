package dev.sorokin.repository.entity;

import dev.sorokin.utils.EnumUtils;

public enum PaymentStatus implements EnumUtils.IntEnum{
    NEW(0),
    AUTHORIZATION_FAILED(1),
    PRICE_CHANGED_FAILED(2),
    SUCCESS_PAID(3);

    private final int value;

    PaymentStatus(Integer value) {
        this.value = value;
    }

    @Override
    public int getCode() {
        return value;
    }
}
