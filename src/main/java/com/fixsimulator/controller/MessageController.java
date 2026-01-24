package com.fixsimulator.controller;

import com.fixsimulator.entity.FixMessageEntity;
import com.fixsimulator.entity.ParsedFieldEntity;
import com.fixsimulator.service.FixMessageService;
import com.fixsimulator.service.ParsedFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
     * 分页获取所有消息
     */
    @GetMapping
    public ResponseEntity<List<FixMessageEntity>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        List<FixMessageEntity> messages = fixMessageService.getAllMessages();
        
        // 由于JPA Repository没有提供分页方法，这里手动实现分页
        int start = Math.min(page * size, messages.size());
        int end = Math.min((page + 1) * size, messages.size());
        
        if (start >= end) {
            return ResponseEntity.ok(List.of()); // 返回空列表而不是null
        }
        
        List<FixMessageEntity> pagedMessages = messages.subList(start, end);
        return ResponseEntity.ok(pagedMessages);
    }
    
    /**
     * 根据ID获取特定消息
     */
    @GetMapping("/{id}")
    public ResponseEntity<FixMessageEntity> getMessageById(@PathVariable Long id) {
        Optional<FixMessageEntity> message = fixMessageService.getMessageById(id);
        return message.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据会话键获取消息
     */
    @GetMapping("/session/{sessionKey}")
    public ResponseEntity<List<FixMessageEntity>> getMessagesBySession(
            @PathVariable String sessionKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<FixMessageEntity> messages = fixMessageService.getMessagesBySessionKey(sessionKey);
        
        // 手动实现分页
        int start = Math.min(page * size, messages.size());
        int end = Math.min((page + 1) * size, messages.size());
        
        if (start >= end) {
            return ResponseEntity.ok(List.of());
        }
        
        List<FixMessageEntity> pagedMessages = messages.subList(start, end);
        return ResponseEntity.ok(pagedMessages);
    }
    
    /**
     * 根据消息类型获取消息
     */
    @GetMapping("/type/{msgType}")
    public ResponseEntity<List<FixMessageEntity>> getMessagesByType(
            @PathVariable String msgType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<FixMessageEntity> messages = fixMessageService.getMessagesByMsgType(msgType);
        
        // 手动实现分页
        int start = Math.min(page * size, messages.size());
        int end = Math.min((page + 1) * size, messages.size());
        
        if (start >= end) {
            return ResponseEntity.ok(List.of());
        }
        
        List<FixMessageEntity> pagedMessages = messages.subList(start, end);
        return ResponseEntity.ok(pagedMessages);
    }
    
    /**
     * 根据时间范围获取消息
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<FixMessageEntity>> getMessagesByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<FixMessageEntity> messages = fixMessageService.getMessagesByTimeRange(start, end);
        
        // 手动实现分页
        int startIdx = Math.min(page * size, messages.size());
        int endIdx = Math.min((page + 1) * size, messages.size());
        
        if (startIdx >= endIdx) {
            return ResponseEntity.ok(List.of());
        }
        
        List<FixMessageEntity> pagedMessages = messages.subList(startIdx, endIdx);
        return ResponseEntity.ok(pagedMessages);
    }
    
    /**
     * 根据ClOrdId查询消息（V1.0.1: 支持可选查询）
     */
    @GetMapping("/clordid")
    public ResponseEntity<List<FixMessageEntity>> getMessagesByClOrdId(
            @RequestParam(required = false) String clOrdId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<FixMessageEntity> messages;

        if (clOrdId != null && !clOrdId.isEmpty()) {
            // 如果提供了ClOrdId，则按ClOrdId查询
            List<ParsedFieldEntity> parsedFields = parsedFieldService.getByClOrdId(clOrdId);
            List<Long> messageIds = parsedFields.stream()
                    .map(ParsedFieldEntity::getMessageId)
                    .toList();

            messages = fixMessageService.getAllMessages().stream()
                    .filter(msg -> messageIds.contains(msg.getId()))
                    .toList();
        } else {
            // 如果没有提供ClOrdId，则查询最近一天的数据
            messages = fixMessageService.getRecentMessages();
        }

        // 手动实现分页
        int start = Math.min(page * size, messages.size());
        int end = Math.min((page + 1) * size, messages.size());

        if (start >= end) {
            return ResponseEntity.ok(List.of());
        }

        List<FixMessageEntity> pagedMessages = messages.subList(start, end);
        return ResponseEntity.ok(pagedMessages);
    }

    /**
     * 根据Symbol查询消息（V1.0.1: 支持可选查询）
     */
    @GetMapping("/symbol")
    public ResponseEntity<List<FixMessageEntity>> getMessagesBySymbol(
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<FixMessageEntity> messages;

        if (symbol != null && !symbol.isEmpty()) {
            // 如果提供了Symbol，则按Symbol查询
            List<ParsedFieldEntity> parsedFields = parsedFieldService.getBySymbol(symbol);
            List<Long> messageIds = parsedFields.stream()
                    .map(ParsedFieldEntity::getMessageId)
                    .toList();

            messages = fixMessageService.getAllMessages().stream()
                    .filter(msg -> messageIds.contains(msg.getId()))
                    .toList();
        } else {
            // 如果没有提供Symbol，则查询最近一天的数据
            messages = fixMessageService.getRecentMessages();
        }

        // 手动实现分页
        int start = Math.min(page * size, messages.size());
        int end = Math.min((page + 1) * size, messages.size());

        if (start >= end) {
            return ResponseEntity.ok(List.of());
        }

        List<FixMessageEntity> pagedMessages = messages.subList(start, end);
        return ResponseEntity.ok(pagedMessages);
    }

    /**
     * 根据ClOrdId和Symbol查询消息（V1.0.1: 支持可选参数组合）
     */
    @GetMapping("/search")
    public ResponseEntity<List<FixMessageEntity>> getMessagesByClOrdIdAndSymbol(
            @RequestParam(required = false) String clOrdId,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<FixMessageEntity> messages;

        if ((clOrdId == null || clOrdId.isEmpty()) && (symbol == null || symbol.isEmpty())) {
            // 两者都为空，查询最近一天的数据
            messages = fixMessageService.getRecentMessages();
        } else if (clOrdId != null && !clOrdId.isEmpty() && symbol != null && !symbol.isEmpty()) {
            // 两者都不为空，查询两者的交集
            messages = fixMessageService.getMessagesByClOrdIdAndSymbol(clOrdId, symbol);
        } else if (clOrdId != null && !clOrdId.isEmpty()) {
            // 只有ClOrdId不为空
            List<ParsedFieldEntity> parsedFields = parsedFieldService.getByClOrdId(clOrdId);
            List<Long> messageIds = parsedFields.stream()
                    .map(ParsedFieldEntity::getMessageId)
                    .toList();

            messages = fixMessageService.getAllMessages().stream()
                    .filter(msg -> messageIds.contains(msg.getId()))
                    .toList();
        } else {
            // 只有Symbol不为空
            List<ParsedFieldEntity> parsedFields = parsedFieldService.getBySymbol(symbol);
            List<Long> messageIds = parsedFields.stream()
                    .map(ParsedFieldEntity::getMessageId)
                    .toList();

            messages = fixMessageService.getAllMessages().stream()
                    .filter(msg -> messageIds.contains(msg.getId()))
                    .toList();
        }

        // 手动实现分页
        int start = Math.min(page * size, messages.size());
        int end = Math.min((page + 1) * size, messages.size());

        if (start >= end) {
            return ResponseEntity.ok(List.of());
        }

        List<FixMessageEntity> pagedMessages = messages.subList(start, end);
        return ResponseEntity.ok(pagedMessages);
    }
}