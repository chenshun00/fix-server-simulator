package com.fixsimulator.config;

import com.fixsimulator.entity.FixMessageEntity;
import com.fixsimulator.service.FixMessageService;
import com.fixsimulator.service.FixSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;
import quickfix.field.BeginString;

@Component
public class FixMessageAdapter extends ApplicationAdapter {
    
    @Autowired
    private FixMessageService fixMessageService;
    
    @Autowired
    private FixSessionService fixSessionService;
    
    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // 处理管理消息
        System.out.println("Received admin message from: " + sessionId);
        super.fromAdmin(message, sessionId);
    }
    
    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        // 处理应用消息
        System.out.println("Received app message from: " + sessionId);
        
        try {
            // 获取会话键
            String sessionKey = fixSessionService.getSessionKey(sessionId);
            
            // 保存消息到数据库
            fixMessageService.saveFixMessage(sessionKey, message, FixMessageEntity.MessageDirection.INBOUND);
            
            // 根据消息类型进行处理
            String msgType = message.getHeader().getString(MsgType.FIELD);
            System.out.println("Processing message type: " + msgType);
            
        } catch (Exception e) {
            System.err.println("Error processing app message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onCreate(SessionID sessionId) {
        System.out.println("Session created: " + sessionId);
        // 添加到活跃会话列表
        fixSessionService.addActiveSession(sessionId);
    }
    
    @Override
    public void onLogon(SessionID sessionId) {
        System.out.println("Session logged on: " + sessionId);
        // 更新会话状态
        fixSessionService.addActiveSession(sessionId);
    }
    
    @Override
    public void onLogout(SessionID sessionId) {
        System.out.println("Session logged out: " + sessionId);
        // 从活跃会话列表中移除
        fixSessionService.removeActiveSession(sessionId);
    }
    
    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        // 发送到管理端的消息
        System.out.println("Sending admin message to: " + sessionId);
        super.toAdmin(message, sessionId);
    }
    
    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        // 发送到应用端的消息
        System.out.println("Sending app message to: " + sessionId);
        
        try {
            // 获取会话键
            String sessionKey = fixSessionService.getSessionKey(sessionId);
            
            // 保存发送的消息到数据库
            fixMessageService.saveFixMessage(sessionKey, message, FixMessageEntity.MessageDirection.OUTBOUND);
        } catch (Exception e) {
            System.err.println("Error saving outbound message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}