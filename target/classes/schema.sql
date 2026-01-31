-- 创建数据库
CREATE DATABASE IF NOT EXISTS fix_simulator
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE fix_simulator;

-- sessions 表
CREATE TABLE IF NOT EXISTS sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) UNIQUE NOT NULL COMMENT 'FIX会话ID',
    sender_comp_id VARCHAR(64) NOT NULL COMMENT '发送方ID',
    target_comp_id VARCHAR(64) NOT NULL COMMENT '接收方ID',
    status VARCHAR(32) NOT NULL COMMENT '状态: CONNECTED/DISCONNECTED/LOGGED_OUT',
    port INT COMMENT '监听端口',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_status (status)
) COMMENT='FIX会话表';

-- parsed_messages 表
CREATE TABLE IF NOT EXISTS parsed_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL COMMENT '关联会话ID',
    msg_type VARCHAR(10) NOT NULL COMMENT '消息类型: D=下单, G=改单, F=撤单',
    symbol VARCHAR(32) NOT NULL COMMENT '股票代码',
    cl_ord_id VARCHAR(64) COMMENT '客户端订单ID',
    orig_cl_ord_id VARCHAR(64) COMMENT '原始客户端订单ID',
    price DECIMAL(18,4) COMMENT '委托价格',
    order_qty DECIMAL(18,4) COMMENT '委托数量',
    side VARCHAR(10) COMMENT '买卖方向: BUY/SELL',
    ord_type VARCHAR(10) COMMENT '订单类型: LIMIT/MARKET',
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
    INDEX idx_session (session_id),
    INDEX idx_symbol (symbol),
    INDEX idx_clordid (cl_ord_id),
    INDEX idx_received (received_at)
) COMMENT='解析后的FIX消息';
