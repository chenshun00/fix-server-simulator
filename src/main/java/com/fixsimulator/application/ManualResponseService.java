package com.fixsimulator.application;

import com.fixsimulator.domain.session.Session;
import com.fixsimulator.domain.session.SessionRepository;
import com.fixsimulator.interfaces.fix.ExecutionReportFactory;
import com.fixsimulator.interfaces.rest.dto.ManualResponseRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import quickfix.Message;
import quickfix.SessionID;
import quickfix.SessionNotFound;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualResponseService {

    private final SessionRepository sessionRepository;
    private final ExecutionReportFactory reportFactory;

    public void sendManualResponse(ManualResponseRequest request) {
        // 1. 查找 Session
        Session session = sessionRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("会话不存在: " + request.getSessionId()));

        // 2. 构建回报报文
        Message report = reportFactory.createExecutionReport(request);

        // 3. 获取 QuickFIX/J SessionID 并发送
        SessionID qfSessionId = toQuickFixSessionId(session);

        try {
            quickfix.Session.sendToTarget(report, qfSessionId);
            log.info("手动回报发送成功: sessionId={}, clOrdId={}, execType={}",
                    request.getSessionId(), request.getClOrdId(), request.getExecType());
        } catch (SessionNotFound e) {
            log.error("发送失败: Session not found: {}", qfSessionId);
            throw new RuntimeException("会话未连接或已断开: " + qfSessionId);
        }
    }

    private SessionID toQuickFixSessionId(Session session) {
        return new SessionID(
                "FIX.4.2",
                session.getSenderCompId(),
                session.getTargetCompId()
        );
    }
}
