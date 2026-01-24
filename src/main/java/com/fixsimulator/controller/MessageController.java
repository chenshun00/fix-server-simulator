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
     * 根据ClOrdId查询消息
     */
    @GetMapping("/clordid/{clOrdId}")
    public ResponseEntity<List<FixMessageEntity>> getMessagesByClOrdId(
            @PathVariable String clOrdId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 首先通过解析字段找到相关的消息ID
        List<ParsedFieldEntity> parsedFields = parsedFieldService.getByClOrdId(clOrdId);
        List<Long> messageIds = parsedFields.stream()
                .map(ParsedFieldEntity::getMessageId)
                .toList();
        
        // 然后获取对应的消息
        List<FixMessageEntity> messages = fixMessageService.getAllMessages().stream()
                .filter(msg -> messageIds.contains(msg.getId()))
                .toList();
        
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
     * 根据Symbol查询消息
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<FixMessageEntity>> getMessagesBySymbol(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 首先通过解析字段找到相关的消息ID
        List<ParsedFieldEntity> parsedFields = parsedFieldService.getBySymbol(symbol);
        List<Long> messageIds = parsedFields.stream()
                .map(ParsedFieldEntity::getMessageId)
                .toList();
        
        // 然后获取对应的消息
        List<FixMessageEntity> messages = fixMessageService.getAllMessages().stream()
                .filter(msg -> messageIds.contains(msg.getId()))
                .toList();
        
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