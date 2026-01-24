package com.fixsimulator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import quickfix.Acceptor;
import quickfix.ConfigError;

import javax.annotation.PreDestroy;

@Component
public class FixStarter {
    
    @Autowired
    private Acceptor fixAcceptor;
    
    @EventListener(ContextRefreshedEvent.class)
    public void startFixAcceptor() {
        try {
            System.out.println("Starting FIX Acceptor...");
            fixAcceptor.start();
            System.out.println("FIX Acceptor started successfully.");
        } catch (ConfigError e) {
            System.err.println("Error starting FIX Acceptor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @PreDestroy
    public void stopFixAcceptor() {
        System.out.println("Stopping FIX Acceptor...");
        if (fixAcceptor != null && fixAcceptor.isLoggedOn()) {
            fixAcceptor.stop();
        }
        System.out.println("FIX Acceptor stopped.");
    }
}