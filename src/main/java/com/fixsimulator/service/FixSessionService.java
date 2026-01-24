package com.fixsimulator.service;

import org.springframework.stereotype.Service;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;
import quickfix.field.BeginString;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class FixSessionService {
    
    // 存储活跃的会话
    private final Map<String, SessionID> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * 根据SessionID获取会话键
     */
    public String getSessionKey(SessionID sessionId) {
        return sessionId.getBeginString() + "|" + sessionId.getSenderCompID() + "|" + sessionId.getTargetCompID();
    }
    
    /**
     * 获取会话状态
     */
    public boolean isSessionActive(SessionID sessionId) {
        return Session.lookupSession(sessionId) != null && Session.lookupSession(sessionId).isLoggedOn();
    }
    
    /**
     * 添加活跃会话
     */
    public void addActiveSession(SessionID sessionId) {
        String sessionKey = getSessionKey(sessionId);
        activeSessions.put(sessionKey, sessionId);
    }
    
    /**
     * 移除活跃会话
     */
    public void removeActiveSession(SessionID sessionId) {
        String sessionKey = getSessionKey(sessionId);
        activeSessions.remove(sessionKey);
    }
    
    /**
     * 获取所有活跃会话键
     */
    public Map<String, SessionID> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }
    
    /**
     * 检查会话是否存在且活跃
     */
    public boolean isSessionExistsAndActive(String sessionKey) {
        SessionID sessionId = activeSessions.get(sessionKey);
        if (sessionId != null) {
            Session session = Session.lookupSession(sessionId);
            return session != null && session.isLoggedOn();
        }
        return false;
    }
    
    /**
     * 发送消息到指定会话
     */
    public boolean sendMessageToSession(String sessionKey, Message message) throws SessionNotFound {
        SessionID sessionId = activeSessions.get(sessionKey);
        if (sessionId != null) {
            return Session.sendToTarget(message, sessionId);
        }
        return false;
    }
}