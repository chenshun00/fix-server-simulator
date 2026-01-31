package com.fixsimulator.infrastructure.fix;

import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

public class SLF4JLogFactory implements LogFactory {
    @Override
    public Log create(SessionID sessionID) {
        return new SLF4JLog(sessionID);
    }
}
