package com.fixsimulator.service;

import com.fixsimulator.entity.ParsedFieldEntity;
import com.fixsimulator.repository.ParsedFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParsedFieldService {
    
    @Autowired
    private ParsedFieldRepository parsedFieldRepository;
    
    /**
     * 根据消息ID获取解析字段
     */
    public ParsedFieldEntity getByMessageId(Long messageId) {
        List<ParsedFieldEntity> entities = parsedFieldRepository.findByMessageId(messageId);
        if (entities != null && !entities.isEmpty()) {
            return entities.get(0); // 返回第一个匹配项
        }
        return null;
    }
    
    /**
     * 根据ClOrdId获取解析字段
     */
    public List<ParsedFieldEntity> getByClOrdId(String clOrdId) {
        return parsedFieldRepository.findByClOrdId(clOrdId);
    }
    
    /**
     * 根据Symbol获取解析字段
     */
    public List<ParsedFieldEntity> getBySymbol(String symbol) {
        return parsedFieldRepository.findBySymbol(symbol);
    }
    
    /**
     * 根据消息类型获取解析字段
     */
    public List<ParsedFieldEntity> getByMsgType(String msgType) {
        return parsedFieldRepository.findByMsgType(msgType);
    }
    
    /**
     * 根据时间范围获取解析字段
     */
    public List<ParsedFieldEntity> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return parsedFieldRepository.findByCreatedAtBetween(startTime, endTime);
    }
    
    /**
     * 根据ClOrdId或Symbol获取解析字段
     */
    public List<ParsedFieldEntity> getByClOrdIdOrSymbol(String clOrdId, String symbol) {
        return parsedFieldRepository.findByClOrdIdOrSymbol(clOrdId, symbol);
    }
    
    /**
     * 获取所有解析字段
     */
    public List<ParsedFieldEntity> getAllParsedFields() {
        return parsedFieldRepository.findAll();
    }
}