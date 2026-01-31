package com.fixsimulator.domain.message;

import lombok.Getter;

@Getter
public enum MsgType {
    NEW_ORDER_SINGLE("D", "New Order Single"),
    ORDER_CANCEL_REQUEST("F", "Order Cancel Request"),
    ORDER_CANCEL_REPLACE_REQUEST("G", "Order Cancel Replace Request");

    private final String code;
    private final String description;

    MsgType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MsgType fromCode(String code) {
        for (MsgType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MsgType: " + code);
    }
}
