package dev.sorokin.repository.entity;


import dev.sorokin.utils.EnumUtils;

public enum TaskStatus implements EnumUtils.IntEnum {

    NEW(0),
    IN_PROGRESS(1),
    SUCCEEDED(2),
    FAILED_RETRYABLE(3),
    FAILED_NON_RETRYABLE(4);

    private final int value;

    TaskStatus(Integer value) {
        this.value = value;
    }

    @Override
    public int getCode() {
        return value;
    }
}
