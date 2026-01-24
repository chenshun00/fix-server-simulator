package com.fixsimulator.service;

import com.fixsimulator.entity.FixMessageEntity;
import com.fixsimulator.entity.ParsedFieldEntity;
import com.fixsimulator.repository.ParsedFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.*;

import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ResponseService {
    
    @Autowired
    private FixSessionService fixSessionService;
    
    @Autowired
    private FixMessageService fixMessageService;
    
    @Autowired
    private ParsedFieldRepository parsedFieldRepository;

    /**
     * 从ClOrdId推导SessionKey
     */
    private String deriveSessionKeyFromClOrdId(String clOrdId) {
        // 通过ClOrdId查找相关的解析字段
        List<ParsedFieldEntity> parsedFields = parsedFieldRepository.findByClOrdId(clOrdId);
        if (parsedFields != null && !parsedFields.isEmpty()) {
            // 获取第一个匹配项的消息ID
            Long messageId = parsedFields.get(0).getMessageId();
            // 通过消息ID查找对应的FIX消息，从中获取sessionKey
            Optional<FixMessageEntity> fixMessage = fixMessageService.getMessageById(messageId);
            if (fixMessage.isPresent()) {
                return fixMessage.get().getSessionKey();
            }
        }
        return null;
    }

    /**
     * 发送ExecutionReport回报（V1.0.1: 从报文数据中推导sessionKey）
     */
    public boolean sendExecutionReportWithDerivedSession(String clOrdId, String execType, String ordStatus,
                                      BigDecimal lastQty, BigDecimal cumQty, BigDecimal leavesQty) throws Exception {
        // 从报文数据中推导sessionKey
        String sessionKey = deriveSessionKeyFromClOrdId(clOrdId);
        if (sessionKey == null) {
            throw new IllegalStateException("Could not derive session key from ClOrdId: " + clOrdId);
        }

        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdID作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(execType.charAt(0))); // 执行类型
        executionReport.set(new OrdStatus(ordStatus.charAt(0))); // 订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        executionReport.set(new LeavesQty(leavesQty != null ? leavesQty.doubleValue() : 0.0)); // 剩余数量
        executionReport.set(new CumQty(cumQty != null ? cumQty.doubleValue() : 0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 如果提供了最后成交量，则设置
        if (lastQty != null) {
            executionReport.set(new LastShares(lastQty.doubleValue()));
        }

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }

    /**
     * 发送ExecutionReport回报（保持原有方法用于向后兼容）
     */
    public boolean sendExecutionReport(String sessionKey, String clOrdId, String execType, String ordStatus,
                                      BigDecimal lastQty, BigDecimal cumQty, BigDecimal leavesQty) throws Exception {
        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdID作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(execType.charAt(0))); // 执行类型
        executionReport.set(new OrdStatus(ordStatus.charAt(0))); // 订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        executionReport.set(new LeavesQty(leavesQty != null ? leavesQty.doubleValue() : 0.0)); // 剩余数量
        executionReport.set(new CumQty(cumQty != null ? cumQty.doubleValue() : 0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 如果提供了最后成交量，则设置
        if (lastQty != null) {
            executionReport.setField(new LastShares(lastQty.doubleValue()));
        }

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }
    
    /**
     * 发送Reject回报（V1.0.1: 从报文数据中推导sessionKey）
     */
    public boolean sendRejectWithDerivedSession(String clOrdId, String text) throws Exception {
        // 从报文数据中推导sessionKey
        String sessionKey = deriveSessionKeyFromClOrdId(clOrdId);
        if (sessionKey == null) {
            throw new IllegalStateException("Could not derive session key from ClOrdId: " + clOrdId);
        }

        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建Reject消息
        Reject reject = new Reject();

        // 设置必要字段
        reject.set(new RefSeqNum(0)); // 参考序列号，实际应参考被拒绝的消息
        if (text != null) {
            reject.set(new Text(text));
        }

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(reject.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, reject);
    }

    /**
     * 发送Reject回报（保持原有方法用于向后兼容）
     */
    public boolean sendReject(String sessionKey, String clOrdId, String text) throws Exception {
        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建Reject消息
        Reject reject = new Reject();

        // 设置必要字段
        reject.set(new RefSeqNum(0)); // 参考序列号，实际应参考被拒绝的消息
        if (text != null) {
            reject.set(new Text(text));
        }

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(reject.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, reject);
    }
    
    /**
     * 发送New回报（已报）（V1.0.1: 从报文数据中推导sessionKey）
     */
    public boolean sendNewOrderAckWithDerivedSession(String clOrdId) throws Exception {
        // 从报文数据中推导sessionKey
        String sessionKey = deriveSessionKeyFromClOrdId(clOrdId);
        if (sessionKey == null) {
            throw new IllegalStateException("Could not derive session key from ClOrdId: " + clOrdId);
        }

        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息，表示订单已被接受
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdID作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(ExecType.NEW)); // NEW执行类型
        executionReport.set(new OrdStatus(OrdStatus.NEW)); // NEW订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        executionReport.set(new LeavesQty(0.0)); // 剩余数量
        executionReport.set(new CumQty(0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }

    /**
     * 发送New回报（已报）（保持原有方法用于向后兼容）
     */
    public boolean sendNewOrderAck(String sessionKey, String clOrdId) throws Exception {
        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息，表示订单已被接受
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdID作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(ExecType.NEW)); // NEW执行类型
        executionReport.set(new OrdStatus(OrdStatus.NEW)); // NEW订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        executionReport.set(new LeavesQty(0.0)); // 剩余数量
        executionReport.set(new CumQty(0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }
    
    /**
     * 发送订单修改确认（V1.0.1: 从报文数据中推导sessionKey）
     */
    public boolean sendOrderModifyConfirmationWithDerivedSession(String clOrdId, String execType, String ordStatus) throws Exception {
        // 从报文数据中推导sessionKey
        String sessionKey = deriveSessionKeyFromClOrdId(clOrdId);
        if (sessionKey == null) {
            throw new IllegalStateException("Could not derive session key from ClOrdId: " + clOrdId);
        }

        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息，表示订单修改
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdId作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(execType.charAt(0))); // 执行类型
        executionReport.set(new OrdStatus(ordStatus.charAt(0))); // 订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        // 设置数量字段
        executionReport.set(new LeavesQty(0.0)); // 剩余数量
        executionReport.set(new CumQty(0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }

    /**
     * 发送订单修改确认（保持原有方法用于向后兼容）
     */
    public boolean sendOrderModifyConfirmation(String sessionKey, String clOrdId, String execType, String ordStatus) throws Exception {
        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息，表示订单修改
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdId作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(execType.charAt(0))); // 执行类型
        executionReport.set(new OrdStatus(ordStatus.charAt(0))); // 订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        // 设置数量字段
        executionReport.set(new LeavesQty(0.0)); // 剩余数量
        executionReport.set(new CumQty(0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }
    
    /**
     * 发送订单取消确认（V1.0.1: 从报文数据中推导sessionKey）
     */
    public boolean sendOrderCancelConfirmationWithDerivedSession(String clOrdId, String execType, String ordStatus) throws Exception {
        // 从报文数据中推导sessionKey
        String sessionKey = deriveSessionKeyFromClOrdId(clOrdId);
        if (sessionKey == null) {
            throw new IllegalStateException("Could not derive session key from ClOrdId: " + clOrdId);
        }

        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息，表示订单取消
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdId作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(execType.charAt(0))); // 执行类型
        executionReport.set(new OrdStatus(ordStatus.charAt(0))); // 订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        // 设置数量字段
        executionReport.set(new LeavesQty(0.0)); // 剩余数量
        executionReport.set(new CumQty(0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }

    /**
     * 发送订单取消确认（保持原有方法用于向后兼容）
     */
    public boolean sendOrderCancelConfirmation(String sessionKey, String clOrdId, String execType, String ordStatus) throws Exception {
        if (!fixSessionService.isSessionExistsAndActive(sessionKey)) {
            throw new IllegalStateException("Session does not exist or is not active: " + sessionKey);
        }

        // 获取原始订单信息
        ParsedFieldEntity originalOrder = getParsedFieldByClOrdId(clOrdId);

        // 创建ExecutionReport消息，表示订单取消
        ExecutionReport executionReport = new ExecutionReport();

        // 设置必要字段
        executionReport.set(new OrderID(clOrdId)); // 使用ClOrdId作为OrderID
        executionReport.set(new ExecID(generateExecId())); // 生成执行ID
        executionReport.set(new ExecType(execType.charAt(0))); // 执行类型
        executionReport.set(new OrdStatus(ordStatus.charAt(0))); // 订单状态
        if (originalOrder != null && originalOrder.getSymbol() != null) {
            executionReport.set(new Symbol(originalOrder.getSymbol())); // 使用原始订单的证券代码
        } else {
            executionReport.set(new Symbol("DEFAULT_SYMBOL")); // 默认证券代码
        }
        if (originalOrder != null && originalOrder.getSide() != null && !originalOrder.getSide().isEmpty()) {
            executionReport.set(new Side(originalOrder.getSide().charAt(0))); // 使用原始订单的买卖方向
        } else {
            executionReport.set(new Side(Side.BUY)); // 默认买入
        }
        // 设置数量字段
        executionReport.set(new LeavesQty(0.0)); // 剩余数量
        executionReport.set(new CumQty(0.0)); // 累计成交量
        executionReport.set(new AvgPx(BigDecimal.ZERO.doubleValue())); // 平均价格

        // 设置ClOrdID
        executionReport.set(new ClOrdID(clOrdId));

        // 设置消息头
        SessionID targetSessionId = getSessionIdFromKey(sessionKey);
        setHeaderForOutboundMessage(executionReport.getHeader(), targetSessionId);

        // 发送消息
        return fixSessionService.sendMessageToSession(sessionKey, executionReport);
    }
    
    // 辅助方法
    
    private String generateExecId() {
        // 生成执行ID，使用时间戳
        return "EXEC_" + System.currentTimeMillis();
    }
    
    private ParsedFieldEntity getParsedFieldByClOrdId(String clOrdId) {
        // 从数据库中查询ClOrdId对应的解析字段
        List<ParsedFieldEntity> entities = parsedFieldRepository.findByClOrdId(clOrdId);
        if (entities != null && !entities.isEmpty()) {
            return entities.get(0); // 返回第一个匹配项
        }
        return null;
    }
    
    private SessionID getSessionIdFromKey(String sessionKey) throws Exception {
        // 从sessionKey解析出SessionID
        // sessionKey格式: BeginString|SenderCompID|TargetCompID
        String[] parts = sessionKey.split("\\|");
        if (parts.length != 3) {
            throw new Exception("Invalid session key format: " + sessionKey);
        }
        
        return new SessionID(parts[0], parts[1], parts[2]);
    }
    
    private void setHeaderForOutboundMessage(quickfix.Message.Header header, SessionID targetSessionId) throws FieldNotFound {
        // 设置消息头，交换Sender和Target
        header.setField(new SenderCompID(targetSessionId.getTargetCompID()));
        header.setField(new TargetCompID(targetSessionId.getSenderCompID()));
        header.setField(new BeginString(targetSessionId.getBeginString()));
    }
}