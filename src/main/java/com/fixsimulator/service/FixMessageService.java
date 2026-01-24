package com.fixsimulator.service;

import com.fixsimulator.entity.FixMessageEntity;
import com.fixsimulator.entity.ParsedFieldEntity;
import com.fixsimulator.repository.FixMessageRepository;
import com.fixsimulator.repository.ParsedFieldRepository;
import com.fixsimulator.util.FixMessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FixMessageService {
    
    @Autowired
    private FixMessageRepository fixMessageRepository;
    
    @Autowired
    private ParsedFieldRepository parsedFieldRepository;
    
    /**
     * 保存原始FIX消息到数据库
     */
    public FixMessageEntity saveFixMessage(String sessionKey, Message message, FixMessageEntity.MessageDirection direction) {
        try {
            FixMessageEntity entity = new FixMessageEntity();
            entity.setSessionKey(sessionKey);
            entity.setMsgSeqNum(message.getHeader().getInt(MsgSeqNum.FIELD));
            entity.setMsgType(message.getHeader().getString(MsgType.FIELD));
            entity.setRawMessage(message.toString());
            entity.setReceiveTime(LocalDateTime.now());
            entity.setDirection(direction);
            
            FixMessageEntity savedEntity = fixMessageRepository.save(entity);
            
            // 解析并保存字段
            parseAndSaveFields(savedEntity.getId(), message);
            
            return savedEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving FIX message", e);
        }
    }
    
    /**
     * 解析FIX消息并保存到解析字段表
     */
    private void parseAndSaveFields(Long messageId, Message message) {
        try {
            ParsedFieldEntity parsedField = new ParsedFieldEntity();
            parsedField.setMessageId(messageId);
            
            // 解析消息类型
            if (message.getHeader().isSetField(MsgType.FIELD)) {
                parsedField.setMsgType(message.getHeader().getString(MsgType.FIELD));
            }
            
            // 解析ClOrdID
            if (message.isSetField(ClOrdID.FIELD)) {
                parsedField.setClOrdId(message.getString(ClOrdID.FIELD));
            }
            
            // 解析OrigClOrdID
            if (message.isSetField(OrigClOrdID.FIELD)) {
                parsedField.setOrigClOrdId(message.getString(OrigClOrdID.FIELD));
            }
            
            // 解析Symbol
            if (message.isSetField(Symbol.FIELD)) {
                parsedField.setSymbol(message.getString(Symbol.FIELD));
            }
            
            // 解析OrderQty
            if (message.isSetField(OrderQty.FIELD)) {
                parsedField.setOrderQty(new java.math.BigDecimal(message.getString(OrderQty.FIELD)));
            }
            
            // 解析Price
            if (message.isSetField(Price.FIELD)) {
                parsedField.setPrice(new java.math.BigDecimal(message.getString(Price.FIELD)));
            }
            
            // 解析Side
            if (message.isSetField(Side.FIELD)) {
                parsedField.setSide(message.getString(Side.FIELD));
            }
            
            // 解析SenderCompID
            if (message.getHeader().isSetField(SenderCompID.FIELD)) {
                parsedField.setSenderCompId(message.getHeader().getString(SenderCompID.FIELD));
            }
            
            // 解析TargetCompID
            if (message.getHeader().isSetField(TargetCompID.FIELD)) {
                parsedField.setTargetCompId(message.getHeader().getString(TargetCompID.FIELD));
            }
            
            parsedFieldRepository.save(parsedField);
        } catch (Exception e) {
            // 记录错误但不抛出异常，因为解析失败不应影响原始消息的保存
            System.err.println("Error parsing FIX message fields: " + e.getMessage());
        }
    }
    
    /**
     * 根据会话键获取消息
     */
    public List<FixMessageEntity> getMessagesBySessionKey(String sessionKey) {
        return fixMessageRepository.findBySessionKey(sessionKey);
    }
    
    /**
     * 根据消息方向获取消息
     */
    public List<FixMessageEntity> getMessagesByDirection(FixMessageEntity.MessageDirection direction) {
        return fixMessageRepository.findByDirection(direction);
    }
    
    /**
     * 根据消息类型获取消息
     */
    public List<FixMessageEntity> getMessagesByMsgType(String msgType) {
        return fixMessageRepository.findByMsgType(msgType);
    }
    
    /**
     * 根据时间范围获取消息
     */
    public List<FixMessageEntity> getMessagesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return fixMessageRepository.findByReceiveTimeBetween(startTime, endTime);
    }
    
    /**
     * 获取所有消息
     */
    public List<FixMessageEntity> getAllMessages() {
        return fixMessageRepository.findAll();
    }
    
    /**
     * 根据ID获取特定消息
     */
    public Optional<FixMessageEntity> getMessageById(Long id) {
        return fixMessageRepository.findById(id);
    }
}