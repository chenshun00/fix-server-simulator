package com.fixsimulator.domain.message;

import lombok.Getter;

@Getter
public enum Side {
    BUY("1", "BUY"),
    SELL("2", "SELL");

    private final String code;
    private final String displayName;

    Side(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static Side fromCode(String code) {
        for (Side side : values()) {
            if (side.code.equals(code)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown Side: " + code);
    }
}
