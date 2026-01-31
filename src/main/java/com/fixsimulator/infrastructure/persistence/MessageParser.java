package com.fixsimulator.infrastructure.persistence;

import com.fixsimulator.domain.message.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.Message;
import quickfix.SessionID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageParser {

    public FixMessage parse(Message message) {
        return new FixMessage(message);
    }

    public ParsedMessage toParsedMessage(FixMessage fixMessage, SessionID sessionId) {
        // 获取原始 FIX 报文
        String rawMessage = fixMessage.getOriginalMessage().toString();

        return ParsedMessage.builder()
                .sessionId(sessionId.toString())
                .msgType(fixMessage.getMsgType())
                .symbol(fixMessage.getSymbol())
                .clOrdId(fixMessage.getClOrdID())
                .origClOrdId(fixMessage.getOrigClOrdID())
                .price(fixMessage.getPrice())
                .orderQty(fixMessage.getOrderQty())
                .side(fixMessage.getSide() != null ? fixMessage.getSide().getDisplayName() : null)
                .ordType(fixMessage.getOrdType() != null ? fixMessage.getOrdType().getDisplayName() : null)
                .message(rawMessage)
                .build();
    }
}
