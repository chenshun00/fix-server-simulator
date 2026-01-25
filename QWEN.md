# FIX Server Simulator 项目上下文

## 项目概述

FIX Server Simulator 是一个用于模拟FIX协议服务器行为的Java应用程序，主要功能包括：
- 支持FIX 4.2协议的消息处理和会话管理
- 自动根据委托数量触发相应的回报序列（V1.0.5新增）
- 提供REST API接口用于会话管理和监控
- 具备完整的报文接收、存储和查询能力
- 支持人工和规则化的模拟响应

该项目基于Spring Boot和QuickFIX/J实现，采用MySQL作为数据存储。

## 核心技术栈

- **后端框架**: Spring Boot 2.7.0
- **FIX引擎**: QuickFIX/J 2.3.1 (仅支持Acceptor模式)
- **数据库**: MySQL
- **协议版本**: FIX 4.2
- **Java版本**: Java 11
- **构建工具**: Maven

## 项目结构

```
src/main/java/com/fixsimulator/
├── FixServerSimulatorApplication.java  # Spring Boot启动类
├── config/                            # 配置类
├── controller/                        # REST控制器
├── entity/                           # JPA实体类
├── repository/                       # 数据访问层
├── service/                          # 业务逻辑层
└── util/                             # 工具类
```

## 核心功能特性

### 1. FIX消息处理
- 支持FIX 4.2协议标准消息格式
- 处理各种FIX消息类型，包括订单请求、执行报告等
- 消息验证和解析功能

### 2. 会话管理
- 管理FIX会话生命周期
- 提供会话状态查询接口
- 支持多会话并发处理

### 3. 自动响应机制 (V1.0.5)
- 根据委托数量自动触发相应的回报序列
- 支持多种执行类型(NEW, PARTIAL_FILL, FILL, REJECTED)
- 智能执行状态管理

### 4. 报文存储与查询
- 完整保存原始FIX报文作为唯一事实源
- 解析并存储常用业务字段作为派生数据
- 支持按ClOrdID和Symbol进行查询

### 5. REST API接口
- 提供HTTP接口用于会话管理和监控
- 健康检查端点
- 实时状态查询

## 构建和运行

### 环境要求
- Java 11
- Maven 3.x
- MySQL数据库

### 构建命令
```bash
# 编译项目
mvn clean compile

# 打包项目
mvn package

# 启动应用
java -jar target/fix-simulator.jar

# 或直接使用Maven运行
mvn spring-boot:run
```

### 默认配置
- 应用端口：8192
- FIX服务器端口：随机会话端口
- 默认会话配置：FIX.4.2协议

## 数据库设计

### 表结构
1. **fix_messages表** (事实数据表) - 存储原始FIX报文及相关元数据
2. **parsed_fields表** (派生数据表) - 存储解析后的业务字段

### 数据分类原则
- **事实数据**: 原始FIX报文内容(raw_message)是系统的唯一事实源
- **派生数据**: 从原始报文中解析的业务字段，仅用于查询与展示

## 版本特性 (V1.0.5)

### 自动回报策略
- 委托数量100：回复已报（NEW）报文
- 委托数量200：回复已报（NEW）报文 + 部成100股报文
- 委托数量300：回复已报（NEW）报文 + 部成100股报文 + 部成200股报文 + 全成300股报文
- 委托数量400：回复拒绝报文
- 其他委托数量：无自动回报

## 开发约定

- 遵循FIX 4.2协议规范
- 会话管理严格遵循协议要求
- 报文序列号自动维护
- 支持上海时区，允许01:00-23:00连接
- 心跳间隔30秒