package dev.sorokin.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки поведения stub'а пересчета цен
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "stub.warehouse")
public class WarehouseStubProperties {

    @PositiveOrZero
    @Max(30_000)
    private int latencyMinMillis;

    @PositiveOrZero
    @Max(30_000)
    private int latencyMaxMillis;

    /**
     * Включить симуляцию 5xx/исключений
     */
    private boolean exceptionEnabled;

    /**
     * Вероятность бросить исключение (0..1)
     */
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double exceptionProbability;

    @PositiveOrZero
    private int finalAmountMin;

    @PositiveOrZero
    private int finalAmountMax;
}
