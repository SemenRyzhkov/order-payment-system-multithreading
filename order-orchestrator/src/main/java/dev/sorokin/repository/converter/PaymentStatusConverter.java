package dev.sorokin.repository.converter;

import dev.sorokin.repository.entity.PaymentStatus;
import dev.sorokin.utils.EnumUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter()
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PaymentStatus statusEnum) {
        return statusEnum == null
                ? null
                : statusEnum.getCode();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(Integer intCode) {
        return intCode == null
                ? null
                : EnumUtils.fromCode(PaymentStatus.class, intCode);
    }
}
