package com.fixsimulator.service;

import java.time.LocalDateTime;

/**
 * 查询条件类，用于统一消息查询参数
 */
public class QueryCriteria {
    private String clOrdId;
    private String origClOrdId;
    private String symbol;
    private String sessionKey;
    private String msgType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int page = 0;
    private int size = 10;

    // 构造函数
    public QueryCriteria() {}

    // getter 和 setter 方法
    public String getClOrdId() {
        return clOrdId;
    }

    public void setClOrdId(String clOrdId) {
        this.clOrdId = clOrdId;
    }

    public String getOrigClOrdId() {
        return origClOrdId;
    }

    public void setOrigClOrdId(String origClOrdId) {
        this.origClOrdId = origClOrdId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    // 辅助方法
    public boolean hasClOrdId() {
        return clOrdId != null && !clOrdId.isEmpty();
    }

    public boolean hasOrigClOrdId() {
        return origClOrdId != null && !origClOrdId.isEmpty();
    }

    public boolean hasSymbol() {
        return symbol != null && !symbol.isEmpty();
    }

    public boolean hasSessionKey() {
        return sessionKey != null && !sessionKey.isEmpty();
    }

    public boolean hasMsgType() {
        return msgType != null && !msgType.isEmpty();
    }

    public boolean hasTimeRange() {
        return startTime != null && endTime != null;
    }

    public boolean needPagination() {
        return true;
    }
}