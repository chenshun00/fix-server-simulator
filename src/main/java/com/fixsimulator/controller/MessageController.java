package com.fixsimulator.controller;

import com.fixsimulator.entity.FixMessageEntity;
import com.fixsimulator.entity.MessageWithParsedField;
import com.fixsimulator.entity.ParsedFieldEntity;
import com.fixsimulator.service.FixMessageService;
import com.fixsimulator.service.ParsedFieldService;
import com.fixsimulator.service.QueryCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private FixMessageService fixMessageService;

    @Autowired
    private ParsedFieldService parsedFieldService;

    /**
     * 统一的消息查询端点 - 根据不同参数组合查询消息
     * - 不传参数：返回INBOUND消息（默认最近一天）
     * - 传clOrdId：按ClOrdId查询
     * - 传symbol：按Symbol查询
     * - 同时传clOrdId和symbol：按两者的交集查询
     * - 传origClOrdId：按OrigClOrdId查询
     */
    @GetMapping
    public ResponseEntity<List<MessageWithParsedField>> getMessages(
            @RequestParam(required = false) String clOrdId,
            @RequestParam(required = false) String origClOrdId,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 创建查询条件对象
        QueryCriteria criteria = new QueryCriteria();
        criteria.setClOrdId(clOrdId);
        criteria.setOrigClOrdId(origClOrdId);
        criteria.setSymbol(symbol);
        criteria.setPage(page);
        criteria.setSize(size);

        // 使用优化的查询方法，只查询解析字段
        List<MessageWithParsedField> result = fixMessageService.queryParsedMessages(criteria);

        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据ID获取特定消息
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageWithParsedField> getMessageById(@PathVariable Long id) {
        Optional<FixMessageEntity> messageOpt = fixMessageService.getMessageById(id);
        if (messageOpt.isPresent()) {
            FixMessageEntity fixMessage = messageOpt.get();
            ParsedFieldEntity parsedField = parsedFieldService.getByMessageId(fixMessage.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            MessageWithParsedField result = new MessageWithParsedField(fixMessage, parsedField);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}