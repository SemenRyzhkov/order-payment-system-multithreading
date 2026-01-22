package dev.sorokin.repository.entity;

import dev.sorokin.utils.EnumUtils;

public enum Step implements EnumUtils.IntEnum {

    AUTH(0),
    REPRICE(1),
    CAPTURE(2);

    private final int value;

    Step(Integer value) {
        this.value = value;
    }

    @Override
    public int getCode() {
        return value;
    }
}
