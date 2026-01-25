package com.fixsimulator.service;

import com.fixsimulator.entity.ParsedFieldEntity;
import com.fixsimulator.repository.ParsedFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class ParsedFieldService {
    
    @Autowired
    private ParsedFieldRepository parsedFieldRepository;
    
    /**
     * 根据消息ID获取解析字段
     */
    public List<ParsedFieldEntity> getByMessageId(Long messageId) {
        return parsedFieldRepository.findByMessageId(messageId);
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
     * 获取最近一天的解析字段（V1.0.1: 支持可选查询）
     */
    public List<ParsedFieldEntity> getRecentParsedFields() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return parsedFieldRepository.findByCreatedAtAfter(oneDayAgo);
    }

    /**
     * 获取所有解析字段
     */
    public List<ParsedFieldEntity> getAllParsedFields() {
        return parsedFieldRepository.findAll();
    }

    /**
     * 根据ClOrdId和Symbol查询解析字段（V1.0.1: 支持可选参数组合）
     */
    public List<ParsedFieldEntity> getByClOrdIdAndSymbol(String clOrdId, String symbol) {
        if (clOrdId != null && !clOrdId.isEmpty() && symbol != null && !symbol.isEmpty()) {
            // 两个条件都不为空，返回两者的交集
            List<ParsedFieldEntity> clOrdIdFields = parsedFieldRepository.findByClOrdId(clOrdId);
            List<ParsedFieldEntity> symbolFields = parsedFieldRepository.findBySymbol(symbol);

            // 找到两个列表的交集
            List<ParsedFieldEntity> result = new ArrayList<>();
            for (ParsedFieldEntity clField : clOrdIdFields) {
                for (ParsedFieldEntity symField : symbolFields) {
                    if (clField.getMessageId().equals(symField.getMessageId())) {
                        result.add(clField);
                        break;
                    }
                }
            }
            return result;
        } else if (clOrdId != null && !clOrdId.isEmpty()) {
            // 只有ClOrdId不为空
            return parsedFieldRepository.findByClOrdId(clOrdId);
        } else if (symbol != null && !symbol.isEmpty()) {
            // 只有Symbol不为空
            return parsedFieldRepository.findBySymbol(symbol);
        } else {
            // 两者都为空，返回最近一天的数据
            return getRecentParsedFields();
        }
    }
}