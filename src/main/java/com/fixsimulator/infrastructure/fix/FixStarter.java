package com.fixsimulator.infrastructure.fix;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import quickfix.*;
import quickfix.Acceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixStarter {

    private final FixMessageApplicationAdapter application;
    private final SessionSettings settings;
    private Acceptor acceptor;

    @EventListener(ApplicationReadyEvent.class)
    public void startAcceptor() {
        try {
            MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();
            LogFactory logFactory = new SLF4JLogFactory();
            MessageFactory messageFactory = new DefaultMessageFactory();

            acceptor = new SocketAcceptor(
                    application,
                    messageStoreFactory,
                    settings,
                    logFactory,
                    messageFactory
            );

            acceptor.start();
            log.info("FIX Acceptor started successfully");

        } catch (Exception e) {
            log.error("Failed to start FIX Acceptor", e);
            throw new RuntimeException("Failed to start FIX Acceptor", e);
        }
    }

    public void stopAcceptor() {
        if (acceptor != null) {
            acceptor.stop();
            log.info("FIX Acceptor stopped");
        }
    }
}
