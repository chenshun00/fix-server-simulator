package com.fixsimulator.infrastructure.fix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.SessionSettings;

@Slf4j
@Configuration
public class FixConfig {

    @Bean
    public SessionSettings sessionSettings() {
        SessionSettings settings = new SessionSettings();
        try {
            // Basic FIX Acceptor settings
            settings.setString("StartTime", "00:00:00");
            settings.setString("EndTime", "00:00:00");
            settings.setString("HeartBtInt", "30");
            settings.setString("UseDataDictionary", "Y");
            settings.setString("DataDictionary", "FIX42.xml");
            log.info("Created default FIX SessionSettings");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SessionSettings", e);
        }
        return settings;
    }
}
