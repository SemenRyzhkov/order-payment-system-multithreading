package dev.sorokin.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Utility methods for working with enums that implement IntEnum or StringEnum.
 */
public final class EnumUtils {

    private EnumUtils() {}

    /**
     * Marker interface for enums with an integer code representation.
     */
    public interface IntEnum {
        /** @return integer code for JSON/DB */
        int getCode();
    }

    /**
     * Marker interface for enums with a string value representation.
     */
    public interface StringEnum {
        /** @return string value for JSON/DB */
        String getValue();
    }

    /**
     * Find enum constant by its integer code.
     *
     * @param enumClass class of the enum implementing IntEnum
     * @param code      integer code
     * @return matching enum constant
     * @throws IllegalArgumentException if no matching constant found
     */
    public static <E extends Enum<E> & IntEnum> E fromCode(
            Class<E> enumClass,
            int code
    ) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("Unknown code %d for enum %s", code, enumClass.getSimpleName())
        );
    }

    /**
     * Find enum constant by its string value.
     *
     * @param enumClass class of the enum implementing StringEnum
     * @param value     string value
     * @return matching enum constant
     * @throws IllegalArgumentException if no matching constant found
     */
    public static <E extends Enum<E> & StringEnum> E fromValue(
            Class<E> enumClass,
            String value
    ) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("Unknown value '%s' for enum %s", value, enumClass.getSimpleName())
        );
    }

    /**
     * Check that all values extracted by the given extractor are unique within the enum.
     *
     * @param enumClass class of the enum
     * @param extractor function extracting the representative value
     * @param <E>       enum type
     * @param <V>       value type
     * @throws IllegalStateException if duplicates are found
     */
    public static <E extends Enum<E>, V> void checkUniqueValues(
            Class<E> enumClass,
            Function<E, V> extractor
    ) {
        Set<V> seen = new HashSet<>();
        for (E e : enumClass.getEnumConstants()) {
            V val = extractor.apply(e);
            if (!seen.add(val)) {
                throw new IllegalStateException(
                        String.format("Duplicate value '%s' in enum %s", val, enumClass.getSimpleName())
                );
            }
        }
    }

    /**
     * Convenience: check unique integer codes in an IntEnum.
     */
    public static <E extends Enum<E> & IntEnum> void checkUniqueValues(Class<E> enumClass) {
        checkUniqueValues(enumClass, IntEnum::getCode);
    }

    /**
     * Convenience: check unique string values in a StringEnum.
     */
    public static <E extends Enum<E> & StringEnum> void checkUniqueStringValues(Class<E> enumClass) {
        checkUniqueValues(enumClass, StringEnum::getValue);
    }
}
