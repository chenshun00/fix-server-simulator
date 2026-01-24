package com.fixsimulator.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_fields")
public class ParsedFieldEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    
    @Column(name = "msg_type")
    private String msgType;
    
    @Column(name = "cl_ord_id")
    private String clOrdId;
    
    @Column(name = "orig_cl_ord_id")
    private String origClOrdId;
    
    @Column(name = "symbol")
    private String symbol;
    
    @Column(name = "order_qty", precision = 15, scale = 4)
    private BigDecimal orderQty;
    
    @Column(name = "price", precision = 15, scale = 4)
    private BigDecimal price;
    
    @Column(name = "side", length = 1)
    private String side;
    
    @Column(name = "sender_comp_id", length = 50)
    private String senderCompId;
    
    @Column(name = "target_comp_id", length = 50)
    private String targetCompId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public ParsedFieldEntity() {}
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMessageId() {
        return messageId;
    }
    
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
    
    public String getMsgType() {
        return msgType;
    }
    
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    
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
    
    public BigDecimal getOrderQty() {
        return orderQty;
    }
    
    public void setOrderQty(BigDecimal orderQty) {
        this.orderQty = orderQty;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getSide() {
        return side;
    }
    
    public void setSide(String side) {
        this.side = side;
    }
    
    public String getSenderCompId() {
        return senderCompId;
    }
    
    public void setSenderCompId(String senderCompId) {
        this.senderCompId = senderCompId;
    }
    
    public String getTargetCompId() {
        return targetCompId;
    }
    
    public void setTargetCompId(String targetCompId) {
        this.targetCompId = targetCompId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}