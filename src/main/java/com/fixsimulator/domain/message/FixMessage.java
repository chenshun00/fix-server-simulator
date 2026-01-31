package com.fixsimulator.domain.message;

import quickfix.FieldNotFound;
import quickfix.Message;

import java.math.BigDecimal;

public class FixMessage {
    private final Message message;

    public FixMessage(Message message) {
        this.message = message;
    }

    public com.fixsimulator.domain.message.MsgType getMsgType() {
        try {
            return com.fixsimulator.domain.message.MsgType.fromCode(
                message.getHeader().getString(quickfix.field.MsgType.FIELD)
            );
        } catch (FieldNotFound e) {
            throw new IllegalArgumentException("MsgType not found", e);
        }
    }

    public String getSymbol() {
        return getString(quickfix.field.Symbol.FIELD);
    }

    public String getClOrdID() {
        return getString(quickfix.field.ClOrdID.FIELD);
    }

    public String getOrigClOrdID() {
        return getString(quickfix.field.OrigClOrdID.FIELD);
    }

    public BigDecimal getPrice() {
        String value = getString(quickfix.field.Price.FIELD);
        return value != null ? new BigDecimal(value) : null;
    }

    public BigDecimal getOrderQty() {
        String value = getString(quickfix.field.OrderQty.FIELD);
        return value != null ? new BigDecimal(value) : null;
    }

    public Side getSide() {
        String code = getString(quickfix.field.Side.FIELD);
        return code != null ? Side.fromCode(code) : null;
    }

    public OrdType getOrdType() {
        String code = getString(quickfix.field.OrdType.FIELD);
        return code != null ? OrdType.fromCode(code) : null;
    }

    private String getString(int field) {
        try {
            return message.getString(field);
        } catch (FieldNotFound e) {
            return null;
        }
    }

    public Message getOriginalMessage() {
        return message;
    }
}
