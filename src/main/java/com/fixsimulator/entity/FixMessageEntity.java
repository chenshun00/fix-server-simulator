package com.fixsimulator.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fix_messages")
public class FixMessageEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_key", nullable = false)
    private String sessionKey;
    
    @Column(name = "msg_seq_num", nullable = false)
    private Integer msgSeqNum;
    
    @Column(name = "msg_type", nullable = false)
    private String msgType;
    
    @Lob
    @Column(name = "raw_message", nullable = false)
    private String rawMessage;
    
    @Column(name = "receive_time", nullable = false, updatable = false)
    private LocalDateTime receiveTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private MessageDirection direction;
    
    public enum MessageDirection {
        INBOUND, OUTBOUND
    }
    
    // 构造函数
    public FixMessageEntity() {}
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSessionKey() {
        return sessionKey;
    }
    
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
    
    public Integer getMsgSeqNum() {
        return msgSeqNum;
    }
    
    public void setMsgSeqNum(Integer msgSeqNum) {
        this.msgSeqNum = msgSeqNum;
    }
    
    public String getMsgType() {
        return msgType;
    }
    
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    
    public String getRawMessage() {
        return rawMessage;
    }
    
    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }
    
    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }
    
    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    public MessageDirection getDirection() {
        return direction;
    }
    
    public void setDirection(MessageDirection direction) {
        this.direction = direction;
    }
}