-- FIX Server Simulator 数据库初始化脚本

-- 创建fix_messages表 (事实数据表)
CREATE TABLE IF NOT EXISTS fix_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_key VARCHAR(120) NOT NULL COMMENT 'Session唯一标识：BeginString+SenderCompID+TargetCompID',
    msg_seq_num INT NOT NULL COMMENT '消息序列号',
    msg_type VARCHAR(10) NOT NULL COMMENT '消息类型',
    raw_message TEXT NOT NULL COMMENT '原始FIX报文内容',
    receive_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
    direction ENUM('INBOUND', 'OUTBOUND') NOT NULL COMMENT '报文方向：入站(INBOUND)或出站(OUTBOUND)'
);

-- 创建parsed_fields表 (派生数据表)
CREATE TABLE IF NOT EXISTS parsed_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL COMMENT '关联报文ID',
    msg_type VARCHAR(10) COMMENT '消息类型',
    cl_ord_id VARCHAR(50) COMMENT '客户订单ID',
    orig_cl_ord_id VARCHAR(50) COMMENT '原始客户订单ID',
    symbol VARCHAR(50) COMMENT '证券代码',
    order_qty DECIMAL(15,4) COMMENT '订单数量',
    price DECIMAL(15,4) COMMENT '价格',
    side CHAR(1) COMMENT '买卖方向',
    sender_comp_id VARCHAR(50) COMMENT '发送方公司ID',
    target_comp_id VARCHAR(50) COMMENT '目标方公司ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建查询所需的索引
CREATE INDEX IF NOT EXISTS idx_fix_msg_session_key ON fix_messages(session_key);
CREATE INDEX IF NOT EXISTS idx_fix_msg_receive_time ON fix_messages(receive_time);
CREATE INDEX IF NOT EXISTS idx_fix_msg_direction ON fix_messages(direction);
CREATE INDEX IF NOT EXISTS idx_parsed_field_cl_ord_id ON parsed_fields(cl_ord_id);
CREATE INDEX IF NOT EXISTS idx_parsed_field_symbol ON parsed_fields(symbol);
CREATE INDEX IF NOT EXISTS idx_parsed_field_created_at ON parsed_fields(created_at);