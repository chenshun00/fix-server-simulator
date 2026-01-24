package com.fixsimulator.controller;

import com.fixsimulator.service.FixSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    
    @Autowired
    private FixSessionService fixSessionService;
    
    /**
     * 获取所有活跃会话
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSessions() {
        Map<String, quickfix.SessionID> activeSessions = fixSessionService.getActiveSessions();
        Map<String, Object> response = new java.util.HashMap<>();
        
        for (Map.Entry<String, quickfix.SessionID> entry : activeSessions.entrySet()) {
            quickfix.Session session = quickfix.Session.lookupSession(entry.getValue());
            Map<String, Object> sessionInfo = new java.util.HashMap<>();
            sessionInfo.put("sessionKey", entry.getKey());
            sessionInfo.put("connected", session != null && session.isLoggedOn());
            sessionInfo.put("nextExpectedMsgSeqNum", session != null ? session.getExpectedTargetNum() : null);
            sessionInfo.put("nextSenderMsgSeqNum", session != null ? session.getExpectedSenderNum() : null);
            
            response.put(entry.getKey(), sessionInfo);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取特定会话信息
     */
    @GetMapping("/{sessionKey}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionKey) {
        quickfix.SessionID sessionId = null;
        // 从sessionKey重建SessionID对象
        String[] parts = sessionKey.split("\\|");
        if (parts.length == 3) {
            sessionId = new quickfix.SessionID(parts[0], parts[1], parts[2]);
        }
        
        if (sessionId != null) {
            quickfix.Session session = quickfix.Session.lookupSession(sessionId);
            if (session != null) {
                Map<String, Object> sessionInfo = new java.util.HashMap<>();
                sessionInfo.put("sessionKey", sessionKey);
                sessionInfo.put("connected", session.isLoggedOn());
                sessionInfo.put("nextExpectedMsgSeqNum", session.getExpectedTargetNum());
                sessionInfo.put("nextSenderMsgSeqNum", session.getExpectedSenderNum());
                
                return ResponseEntity.ok(sessionInfo);
            }
        }
        
        return ResponseEntity.notFound().build();
    }
}