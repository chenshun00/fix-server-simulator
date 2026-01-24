package com.fixsimulator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

import java.io.InputStream;

@Configuration
public class FixConfig {

    @Autowired
    private FixMessageAdapter fixMessageAdapter;

    @Bean
    public SessionSettings sessionSettings() throws ConfigError {
        try {
            // 从classpath加载配置文件
            InputStream configStream = getClass().getClassLoader().getResourceAsStream("acceptor.properties");
            if (configStream == null) {
                throw new RuntimeException("Could not find acceptor.properties file in classpath");
            }
            return new SessionSettings(configStream);
        } catch (Exception e) {
            throw new ConfigError("Error loading session settings from acceptor.properties", e);
        }
    }

    @Bean
    public MessageStoreFactory messageStoreFactory(SessionSettings settings) {
        return new FileStoreFactory(settings);
    }

    @Bean
    public LogFactory logFactory(SessionSettings settings) {
        return new ScreenLogFactory(settings);
    }

    @Bean
    public MessageFactory messageFactory() {
        return new DefaultMessageFactory();
    }

    @Bean
    public Acceptor fixAcceptor(SessionSettings settings,
                                MessageStoreFactory messageStoreFactory,
                                LogFactory logFactory,
                                MessageFactory messageFactory) throws ConfigError {
        return new SocketAcceptor(
                (Application) fixMessageAdapter,
                messageStoreFactory,
                settings,
                logFactory,
                messageFactory
        );
    }
}