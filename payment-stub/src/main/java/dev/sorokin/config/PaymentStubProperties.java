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
 * Настройки поведения платежного stub'а
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "stub.payment")
public class PaymentStubProperties {

    /**
     * Включить симуляцию отклонений по карте
     */
    private boolean declineEnabled;

    /**
     * Вероятность отклонения (0..1)
     */
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double declineProbability;

    @PositiveOrZero
    @Max(30_000)
    private int authorizeLatencyMinMillis;

    @PositiveOrZero
    @Max(30_000)
    private int authorizeLatencyMaxMillis;

    @PositiveOrZero
    @Max(30_000)
    private int captureLatencyMinMillis;

    @PositiveOrZero
    @Max(30_000)
    private int captureLatencyMaxMillis;

    /**
     * Включить симуляцию 5xx/исключений.
     */
    private boolean exceptionEnabled;

    /**
     * Вероятность бросить исключение (0..1)
     */
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double exceptionProbability;
}
