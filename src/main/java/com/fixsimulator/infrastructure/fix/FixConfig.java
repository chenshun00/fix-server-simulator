package com.fixsimulator.infrastructure.fix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.SessionID;
import quickfix.SessionSettings;

@Slf4j
@Configuration
public class FixConfig {

    @Bean
    public SessionSettings sessionSettings() {
        SessionSettings settings = new SessionSettings();
        try {
            // Default settings
            settings.setString("StartTime", "00:00:00");
            settings.setString("EndTime", "23:59:59");
            settings.setString("HeartBtInt", "30");
            settings.setString("UseDataDictionary", "Y");
            settings.setString("DataDictionary", "FIX42.xml");
            settings.setString("ReconnectInterval", "5");
            settings.setString("ResetOnLogon", "Y");
            settings.setString("FileLogPath", "log");

            // Create session for acceptor
            SessionID sessionID = new SessionID(
                "FIX.4.2",  // BeginString
                "SIMULATOR",  // SenderCompID
                "GATEWAY"  // TargetCompID
            );

            // Session-specific settings
            settings.setString(sessionID, "ConnectionType", "acceptor");
            settings.setLong(sessionID, "SocketAcceptPort", 9876);
            settings.setString(sessionID, "BeginString", "FIX.4.2");
            settings.setString(sessionID, "SenderCompID", "SIMULATOR");
            settings.setString(sessionID, "TargetCompID", "GATEWAY");

            log.info("Created FIX SessionSettings with session: {}", sessionID);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SessionSettings", e);
        }
        return settings;
    }
}
