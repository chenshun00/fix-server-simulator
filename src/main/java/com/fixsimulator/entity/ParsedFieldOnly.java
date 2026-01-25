package com.fixsimulator.entity;

/**
 * 只包含解析字段的类（避免JOIN FixMessageEntity）
 */
public class ParsedFieldOnly {
    private ParsedFieldEntity parsedField;

    public ParsedFieldOnly(ParsedFieldEntity parsedField) {
        this.parsedField = parsedField;
    }

    public ParsedFieldEntity getParsedField() {
        return parsedField;
    }

    public void setParsedField(ParsedFieldEntity parsedField) {
        this.parsedField = parsedField;
    }
}