package com.fixsimulator.application;

import com.fixsimulator.domain.autoresponse.Action;
import com.fixsimulator.domain.autoresponse.Rule;
import com.fixsimulator.domain.autoresponse.RuleEngine;
import com.fixsimulator.domain.message.FixMessage;
import com.fixsimulator.interfaces.fix.ExecutionReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;

import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoResponseOrchestrator {

    private final RuleEngine ruleEngine;
    private final ExecutionReportGenerator reportGenerator;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void process(FixMessage message, SessionID sessionId) {
        // Only process New Order Single
        if (message.getMsgType() != com.fixsimulator.domain.message.MsgType.NEW_ORDER_SINGLE) {
            log.debug("Ignoring non-NewOrder message: {}", message.getMsgType());
            return;
        }

        // Match rule
        ruleEngine.match(message).ifPresentOrElse(
                rule -> executeActions(rule, message, sessionId),
                () -> log.info("No rule matched for OrderQty: {}", message.getOrderQty())
        );
    }

    private void executeActions(Rule rule, FixMessage message, SessionID sessionId) {
        log.info("Executing rule: {} with {} actions", rule.getName(), rule.getActions().size());

        List<Action> actions = rule.getActions();
        long cumulativeDelay = 0;

        for (Action action : actions) {
            long delay = cumulativeDelay + action.getDelayMs();

            scheduler.schedule(() -> {
                try {
                    Message report = reportGenerator.generate(action, message);
                    Session.sendToTarget(report, sessionId);
                    log.info("Sent execution report: {}", action.getParams());
                } catch (Exception e) {
                    log.error("Failed to send execution report", e);
                }
            }, delay, TimeUnit.MILLISECONDS);

            cumulativeDelay = delay;
        }
    }
}
