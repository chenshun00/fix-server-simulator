package com.fixsimulator.domain.message;

import lombok.Getter;

@Getter
public enum OrdType {
    MARKET("1", "MARKET"),
    LIMIT("2", "LIMIT");

    private final String code;
    private final String displayName;

    OrdType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static OrdType fromCode(String code) {
        for (OrdType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown OrdType: " + code);
    }
}
