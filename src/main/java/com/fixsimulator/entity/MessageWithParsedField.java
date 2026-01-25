package com.fixsimulator.entity;

/**
 * 包含FIX消息及其解析字段的复合类
 * 注意：为避免JOIN，这个类只包含解析字段
 */
public class MessageWithParsedField {
    private ParsedFieldEntity parsedField;

    public MessageWithParsedField(Object fixMessage, ParsedFieldEntity parsedField) {
        // 为了兼容现有代码，我们只使用解析字段
        this.parsedField = parsedField;
    }

    // 为了兼容前端代码，我们添加一个空的fixMessage对象
    public Object getFixMessage() {
        // 返回一个包含必要字段的虚拟对象
        return new Object() {
            public Long getId() {
                return parsedField != null ? parsedField.getMessageId() : null;
            }

            public String getMsgType() {
                return parsedField != null ? parsedField.getMsgType() : null;
            }

            public String getDirection() {
                // 假设所有解析字段都来自INBOUND消息
                return "INBOUND";
            }

            public java.time.LocalDateTime getReceiveTime() {
                return parsedField != null ? parsedField.getCreatedAt() : null;
            }
        };
    }

    public ParsedFieldEntity getParsedField() {
        return parsedField;
    }

    public void setParsedField(ParsedFieldEntity parsedField) {
        this.parsedField = parsedField;
    }
}