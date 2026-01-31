package com.fixsimulator.domain.autoresponse;

import com.fixsimulator.domain.message.FixMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Condition {
    @JsonProperty("field")
    private String field;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("value")
    private Object value;

    public boolean matches(FixMessage message) {
        return switch (field) {
            case "OrderQty" -> checkOrderQty(message);
            default -> false;
        };
    }

    private boolean checkOrderQty(FixMessage message) {
        BigDecimal orderQty = message.getOrderQty();
        if (orderQty == null) {
            return false;
        }

        return switch (operator) {
            case "==" -> orderQty.compareTo(toBigDecimal(value)) == 0;
            case ">" -> orderQty.compareTo(toBigDecimal(value)) > 0;
            case "<" -> orderQty.compareTo(toBigDecimal(value)) < 0;
            case "between" -> {
                if (value instanceof java.util.List list && list.size() == 2) {
                    BigDecimal min = toBigDecimal(list.get(0));
                    BigDecimal max = toBigDecimal(list.get(1));
                    yield orderQty.compareTo(min) >= 0 && orderQty.compareTo(max) <= 0;
                }
                yield false;
            }
            default -> false;
        };
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        throw new IllegalArgumentException("Cannot convert to BigDecimal: " + value);
    }
}
