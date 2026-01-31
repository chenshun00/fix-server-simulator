package com.fixsimulator.application;

import com.fixsimulator.domain.message.FixMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.SessionID;

@Slf4j
@Component
public class AutoResponseOrchestrator {

    public void process(FixMessage message, SessionID sessionId) {
        // Only process New Order Single
        if (message.getMsgType() != com.fixsimulator.domain.message.MsgType.NEW_ORDER_SINGLE) {
            log.debug("Ignoring non-NewOrder message: {}", message.getMsgType());
            return;
        }

        log.info("Processing NewOrderSingle with OrderQty: {}", message.getOrderQty());
        // TODO: Implement rule matching and execution report generation
    }
}
