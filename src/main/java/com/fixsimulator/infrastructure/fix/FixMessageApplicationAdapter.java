package com.fixsimulator.infrastructure.fix;

import com.fixsimulator.application.AutoResponseOrchestrator;
import com.fixsimulator.domain.message.FixMessage;
import com.fixsimulator.domain.message.ParsedMessage;
import com.fixsimulator.domain.message.ParsedMessageRepository;
import com.fixsimulator.infrastructure.persistence.MessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.*;
import quickfix.field.MsgType;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixMessageApplicationAdapter implements Application {

    private final MessageParser messageParser;
    private final ParsedMessageRepository messageRepository;
    private final AutoResponseOrchestrator autoResponseOrchestrator;

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("Session created: {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("Session logged on: {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("Session logged out: {}", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        log.debug("To admin: {}", message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
        log.debug("From admin: {}", message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
        log.debug("To app: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {
        try {
            log.info("Received message: {} from {}", message, sessionId);

            // 解析消息
            FixMessage fixMessage = messageParser.parse(message);

            // 存储到数据库
            ParsedMessage parsed = messageParser.toParsedMessage(fixMessage, sessionId);
            messageRepository.save(parsed);
            log.info("Saved parsed message: {}", parsed);

            // 触发自动回报
            autoResponseOrchestrator.process(fixMessage, sessionId);

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
