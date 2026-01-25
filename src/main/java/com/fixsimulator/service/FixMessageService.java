package com.fixsimulator.service;

import com.fixsimulator.entity.FixMessageEntity;
import com.fixsimulator.entity.MessageWithParsedField;
import com.fixsimulator.entity.ParsedFieldEntity;
import com.fixsimulator.repository.FixMessageRepository;
import com.fixsimulator.repository.ParsedFieldRepository;
import com.fixsimulator.util.FixMessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FixMessageService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FixMessageRepository fixMessageRepository;

    @Autowired
    private ParsedFieldRepository parsedFieldRepository;

    /**
     * 保存原始FIX消息到数据库 - 只保存INBOUND消息
     */
    public FixMessageEntity saveFixMessage(String sessionKey, Message message, FixMessageEntity.MessageDirection direction) {
        try {
            // 只保存INBOUND消息，忽略OUTBOUND消息
            if (!FixMessageEntity.MessageDirection.INBOUND.equals(direction)) {
                return null; // 不保存OUTBOUND消息
            }

            FixMessageEntity entity = new FixMessageEntity();
            entity.setSessionKey(sessionKey);
            entity.setMsgSeqNum(message.getHeader().getInt(MsgSeqNum.FIELD));
            entity.setMsgType(message.getHeader().getString(MsgType.FIELD));
            entity.setRawMessage(message.toString());
            entity.setReceiveTime(LocalDateTime.now());
            entity.setDirection(direction);

            FixMessageEntity savedEntity = fixMessageRepository.save(entity);

            // 解析并保存字段
            parseAndSaveFields(savedEntity.getId(), message, direction);

            return savedEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving FIX message", e);
        }
    }

    /**
     * 解析FIX消息并保存到解析字段表
     */
    private void parseAndSaveFields(Long messageId, Message message, FixMessageEntity.MessageDirection direction) {
        try {
            ParsedFieldEntity parsedField = new ParsedFieldEntity();
            parsedField.setMessageId(messageId);
            parsedField.setDirection(direction.name());

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
     * 简化的消息查询方法 - 只返回解析字段数据（使用direction字段过滤INBOUND消息）
     */
    public List<MessageWithParsedField> queryParsedMessages(QueryCriteria criteria) {
        // 构建动态JPQL查询 - 使用direction字段直接过滤INBOUND消息
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT p FROM ParsedFieldEntity p ");
        jpql.append("WHERE p.direction = 'INBOUND' ");

        // 根据参数动态添加查询条件
        Map<String, Object> parameters = new HashMap<>();

        if (criteria.hasClOrdId()) {
            jpql.append("AND p.clOrdId = :clOrdId ");
            parameters.put("clOrdId", criteria.getClOrdId());
        }

        if (criteria.hasSymbol()) {
            jpql.append("AND p.symbol = :symbol ");
            parameters.put("symbol", criteria.getSymbol());
        }

        if (criteria.hasOrigClOrdId()) {
            jpql.append("AND p.origClOrdId = :origClOrdId ");
            parameters.put("origClOrdId", criteria.getOrigClOrdId());
        }

        // 添加排序
        jpql.append("ORDER BY p.createdAt DESC");

        // 执行查询
        TypedQuery<ParsedFieldEntity> query = entityManager.createQuery(jpql.toString(), ParsedFieldEntity.class);

        // 设置参数
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        // 如果需要分页
        if (criteria.needPagination()) {
            int offset = criteria.getPage() * criteria.getSize();
            query.setFirstResult(offset);
            query.setMaxResults(criteria.getSize());
        }

        // 处理查询结果
        List<ParsedFieldEntity> parsedFields = query.getResultList();
        List<MessageWithParsedField> results = new ArrayList<>();

        for (ParsedFieldEntity parsedField : parsedFields) {
            results.add(new MessageWithParsedField(null, parsedField));
        }

        return results;
    }


    /**
     * 根据ID获取特定消息
     */
    public Optional<FixMessageEntity> getMessageById(Long id) {
        return fixMessageRepository.findById(id);
    }
}