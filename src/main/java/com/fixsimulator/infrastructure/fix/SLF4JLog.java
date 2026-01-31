package com.fixsimulator.infrastructure.fix;

import lombok.extern.slf4j.Slf4j;
import quickfix.Log;
import quickfix.SessionID;

@Slf4j
public class SLF4JLog implements Log {
    private final SessionID sessionID;

    public SLF4JLog(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public void clear() {
    }

    @Override
    public void onIncoming(String message) {
        log.info("[{}] Incoming: {}", sessionID, message);
    }

    @Override
    public void onOutgoing(String message) {
        log.info("[{}] Outgoing: {}", sessionID, message);
    }

    @Override
    public void onEvent(String message) {
        log.info("[{}] Event: {}", sessionID, message);
    }

    @Override
    public void onErrorEvent(String message) {
        log.error("[{}] Error: {}", sessionID, message);
    }
}
