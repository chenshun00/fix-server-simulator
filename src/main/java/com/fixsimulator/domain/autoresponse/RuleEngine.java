package com.fixsimulator.domain.autoresponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RuleEngine {

    private List<Rule> rules;
    private final ObjectMapper objectMapper;

    public RuleEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadRules() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("rules.json")) {
            if (inputStream == null) {
                throw new RuntimeException("rules.json not found");
            }
            RuleConfig config = objectMapper.readValue(inputStream, RuleConfig.class);
            this.rules = config.getRules();
            log.info("Loaded {} rules", rules.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rules", e);
        }
    }

    public Optional<Rule> match(com.fixsimulator.domain.message.FixMessage message) {
        return rules.stream()
                .filter(rule -> rule.matches(message))
                .findFirst();
    }

    @lombok.Data
    private static class RuleConfig {
        private List<Rule> rules;
    }
}
