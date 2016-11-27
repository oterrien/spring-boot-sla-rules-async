package com.test.rule;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.BiPredicate;
import java.util.function.Function;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Parameter<T extends Comparable> {

    private final String fieldName;

    private final Value<T> value;

    private final boolean isKey;

    public static final Parameter of(String fieldName, String value, String clause, String type, boolean isKey) {
        return new Parameter(fieldName, new Value(value, Clause.valueOf(clause), Type.valueOf(type)), isKey);
    }

    @Data
    public static class Value<T extends Comparable> {

        private final T value;

        private final Clause clause;

        private Value(String value, Clause clause, Type type) {
            this.value = (T) type.apply(value);
            this.clause = clause;
        }
    }

    enum Clause implements BiPredicate<Comparable, Comparable> {

        EQUALS((o1, o2) -> o1.equals(o2)),
        DIFFERENT((o1, o2) -> !o1.equals(o2)),
        GREATER_THAN((o1, o2) -> o1.compareTo(o2) > 0),
        GREATER_THAN_OR_EQUALS((o1, o2) -> o1.compareTo(o2) >= 0),
        LOWER_THAN((o1, o2) -> o1.compareTo(o2) < 0),
        LOWER_THAN_OR_EQUALS((o1, o2) -> o1.compareTo(o2) <= 0),
        LIKE((o1, o2) -> o1.toString().contains(o2.toString())),
        BETWEEN((o1, o2) -> o1.compareTo(o2) == 0);

        private BiPredicate<Comparable, Comparable> comparator;

        Clause(BiPredicate<Comparable, Comparable> comparator) {
            this.comparator = comparator;
        }

        @Override
        public boolean test(Comparable o1, Comparable o2) {
            return comparator.test(o1, o2);
        }
    }

    enum Type implements Function<String, Object> {

        STRING(o -> (String) o),
        INTEGER(Integer::parseInt),
        FLOAT(Float::parseFloat),
        BOOLEAN(Boolean::getBoolean),
        DATE(Type::parseDate),
        DATE_SEGMENT(o -> new Parameter.Segment<>(o, Type::parseDate)),
        INTEGER_SEGMENT(o -> new Parameter.Segment<>(o, Integer::parseInt)),
        FLOAT_SEGMENT(o -> new Parameter.Segment<>(o, Float::parseFloat));

        private Function<String, Object> transformer;

        Type(Function<String, Object> transformer) {
            this.transformer = transformer;
        }

        @Override
        public Object apply(String s) {
            return transformer.apply(s);
        }

        private static LocalDate parseDate(String dateString) {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    private static class Segment<N extends Comparable<? super N>> implements Comparable<N> {

        private N from, to;

        public Segment(String segmentString, Function<String, N> parser) {
            String[] array = segmentString.split(";");
            from = parser.apply(array[0]);
            to = parser.apply(array[1]);
        }

        @Override
        public int compareTo(N o) {

            if (o.compareTo(from) < 0)
                return -1;

            if (o.compareTo(to) > 0)
                return 1;

            return 0;
        }
    }

}
