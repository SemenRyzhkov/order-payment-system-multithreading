package dev.sorokin.repository.converter;

import dev.sorokin.repository.entity.Step;
import dev.sorokin.utils.EnumUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter()
public class StepConverter implements AttributeConverter<Step, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Step statusEnum) {
        return statusEnum == null
                ? null
                : statusEnum.getCode();
    }

    @Override
    public Step convertToEntityAttribute(Integer intCode) {
        return intCode == null
                ? null
                : EnumUtils.fromCode(Step.class, intCode);
    }
}
