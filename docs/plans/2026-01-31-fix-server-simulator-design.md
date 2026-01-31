# FIX Server Simulator 设计文档

**日期**: 2026-01-31
**版本**: 2.0 (完全重构)
**作者**: Claude & 用户协作设计

---

## 一、项目概述

FIX Server Simulator 是一个用于模拟 FIX 4.2 协议服务器行为的应用程序，支持自动回报功能和会话管理。

### 核心功能
- FIX 4.2 协议消息接收与处理
- 基于委托数量的自动回报（PRD V1.0.5）
- 多会话并发管理
- REST API 接口
- Vue 3 前端界面

---

## 二、技术架构

### 2.1 技术栈
| 层级 | 技术选型 |
|------|----------|
| 后端框架 | Java + Spring Boot |
| FIX 协议库 | QuickFIX/J (简化配置) |
| 数据库 | MySQL |
| 前端框架 | Vue 3 + Element Plus |
| 状态管理 | Pinia |
| 构建工具 | Vite |

### 2.2 分层架构 (DDD)

```
┌─────────────────────────────────────────────────────────┐
│                    接口层 (Interfaces)                   │
│  ┌──────────────────┐        ┌──────────────────┐      │
│  │  REST Controller │        │ FIX Message      │      │
│  │  (会话/消息查询)  │        │ Handler          │      │
│  └──────────────────┘        └──────────────────┘      │
└─────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    应用层 (Application)                  │
│  ┌──────────────────┐  ┌──────────────────────────────┐ │
│  │ Session          │  │ AutoResponse                 │ │
│  │ ApplicationService│ │ Orchestrator                 │ │
│  └──────────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    领域层 (Domain)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │  Session    │  │ FixMessage  │  │ RuleEngine      │  │
│  │  聚合根     │  │  值对象     │  │  (规则引擎)     │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────┐
│                  基础设施层 (Infrastructure)              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │ QuickFIX/J  │  │ MySQL       │  │ Rule Config     │  │
│  │ Adapter     │  │ Repository  │  │ (JSON)          │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 三、数据库设计

### 3.1 sessions 表（会话管理）

```sql
CREATE TABLE sessions (
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
```

### 3.2 parsed_messages 表（解析后的消息）

```sql
CREATE TABLE parsed_messages (
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
```

---

## 四、领域模型

### 4.1 核心领域对象

```java
// 会话聚合根
class Session {
    SessionId id;
    SenderCompID sender;
    TargetCompID target;
    SessionStatus status;  // CONNECTED, DISCONNECTED, LOGGED_OUT
    LocalDateTime connectedAt;
}

// FIX 消息值对象（不可变）
class FixMessage {
    MsgType type;
    Map<Integer, String> fields;

    String getField(int tag);
    String getSymbol();        // Tag 55
    String getClOrdID();       // Tag 11
    String getOrigClOrdID();   // Tag 41
    BigDecimal getPrice();     // Tag 44
    BigDecimal getOrderQty();  // Tag 38
    Side getSide();            // Tag 54
    OrdType getOrdType();      // Tag 40
}

// 解析后的消息实体
class ParsedMessage {
    Long id;
    String sessionId;
    MsgType msgType;           // D, G, F
    String symbol;
    String clOrdID;
    String origClOrdID;
    BigDecimal price;
    BigDecimal orderQty;
    Side side;                 // BUY, SELL
    OrdType ordType;           // LIMIT, MARKET
    LocalDateTime receivedAt;
}
```

### 4.2 枚举定义

```java
enum MsgType {
    NEW_ORDER_SINGLE("D"),     // 下单
    ORDER_CANCEL_REQUEST("F"), // 撤单
    ORDER_CANCEL_REPLACE_REQUEST("G"); // 改单
}

enum Side {
    BUY("1"),
    SELL("2");
}

enum OrdType {
    MARKET("1"),
    LIMIT("2");
}

enum SessionStatus {
    CONNECTED,
    DISCONNECTED,
    LOGGED_OUT
}
```

---

## 五、规则引擎设计

### 5.1 规则配置结构 (resources/rules.json)

```json
{
  "rules": [
    {
      "id": "100",
      "name": "委托数量100 - 仅回报已报",
      "condition": {
        "field": "OrderQty",
        "operator": "==",
        "value": 100
      },
      "actions": [
        {
          "type": "SEND_REPORT",
          "execType": "0",
          "ordStatus": "0",
          "delayMs": 0
        }
      ]
    },
    {
      "id": "200",
      "name": "委托数量200 - 已报 + 部成100",
      "condition": {
        "field": "OrderQty",
        "operator": "==",
        "value": 200
      },
      "actions": [
        {
          "type": "SEND_REPORT",
          "execType": "0",
          "ordStatus": "0",
          "delayMs": 0
        },
        {
          "type": "SEND_REPORT",
          "execType": "1",
          "ordStatus": "1",
          "lastQty": 100,
          "delayMs": 10
        }
      ]
    },
    {
      "id": "300",
      "name": "委托数量300 - 已报 + 部成100 + 部成200 + 全成",
      "condition": {
        "field": "OrderQty",
        "operator": "==",
        "value": 300
      },
      "actions": [
        {
          "type": "SEND_REPORT",
          "execType": "0",
          "ordStatus": "0",
          "delayMs": 0
        },
        {
          "type": "SEND_REPORT",
          "execType": "1",
          "ordStatus": "1",
          "lastQty": 100,
          "delayMs": 10
        },
        {
          "type": "SEND_REPORT",
          "execType": "1",
          "ordStatus": "1",
          "lastQty": 100,
          "delayMs": 10
        },
        {
          "type": "SEND_REPORT",
          "execType": "2",
          "ordStatus": "2",
          "delayMs": 10
        }
      ]
    },
    {
      "id": "400",
      "name": "委托数量400 - 拒绝",
      "condition": {
        "field": "OrderQty",
        "operator": "==",
        "value": 400
      },
      "actions": [
        {
          "type": "SEND_REPORT",
          "execType": "8",
          "ordStatus": "8",
          "text": "rejected by simulator, don't worry",
          "delayMs": 0
        }
      ]
    }
  ]
}
```

### 5.2 规则引擎核心类

```java
class RuleEngine {
    List<Rule> rules;

    Optional<Rule> match(FixMessage message);
}

class Rule {
    String id;
    String name;
    Condition condition;
    List<Action> actions;
}

class Condition {
    String field;      // "OrderQty"
    String operator;   // "==", ">", "<", "between"
    Object value;

    boolean matches(FixMessage message);
}

class Action {
    String type;       // "SEND_REPORT"
    Map<String, Object> params;
    long delayMs;      // 延迟执行时间
}
```

---

## 六、自动回报流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    1. 接收消息                                   │
│  Gateway → New Order Single (MsgType=D, OrderQty=200)           │
│  → FixMessageApplicationAdapter.fromApp()                       │
└─────────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    2. 解析并存储                                 │
│  MessageParser.parse() → ParsedMessage                          │
│  → ParsedMessageRepository.save()                               │
└─────────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    3. 规则匹配                                   │
│  AutoResponseOrchestrator                                       │
│  → RuleEngine.match(message)                                    │
│  → 匹配到规则 200 (OrderQty=200)                                │
└─────────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    4. 生成回报序列                               │
│  ExecutionReportGenerator.generate(rule.actions)                │
│  → NEW 报文 (ExecType=0, OrdStatus=0)                           │
│  → PARTIAL_FILL 报文 (ExecType=1, OrdStatus=1, LastQty=100)     │
└─────────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    5. 发送回报                                   │
│  Session.send(NEW) → 等待 10ms → Session.send(PARTIAL_FILL)     │
└─────────────────────────────────────────────────────────────────┘
```

### 关键时间参数
- 报文发送间隔：**10ms**
- 异常情况：OrderQty=400 直接返回 REJECTED

---

## 七、QuickFIX/J 集成

### 7.1 配置文件 (resources/acceptor.properties)

用户提供的配置文件，包含：
- BeginString=FIX.4.2
- SenderCompID, TargetCompID
- HeartBtInt=30
- SocketAcceptPort 等参数

### 7.2 核心适配器

```java
@Component
public class FixMessageApplicationAdapter implements Application {

    @Autowired
    private MessageParser messageParser;

    @Autowired
    private ParsedMessageRepository messageRepository;

    @Autowired
    private AutoResponseOrchestrator autoResponseOrchestrator;

    @Override
    public void fromApp(Message message, SessionID sessionId) {
        // 1. 解析消息
        FixMessage fixMessage = messageParser.parse(message);

        // 2. 存储到数据库
        ParsedMessage parsed = messageParser.toParsedMessage(fixMessage, sessionId);
        messageRepository.save(parsed);

        // 3. 触发自动回报
        autoResponseOrchestrator.process(fixMessage, sessionId);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
        // 发送消息前的处理（如有需要）
    }
}
```

---

## 八、REST API 设计

### 8.1 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| GET | `/api/sessions` | 查询所有会话 |
| GET | `/api/sessions/{sessionId}` | 查询会话详情 |
| GET | `/api/messages` | 查询消息列表（支持搜索） |

### 8.2 消息查询接口

**请求参数**：
```
GET /api/messages?symbol=600519&clOrdId=CLORD001&page=0&size=20
```

**返回格式**：
```json
{
  "content": [
    {
      "id": 1,
      "msgType": "D",
      "symbol": "600519",
      "clOrdId": "CLORD001",
      "origClOrdId": null,
      "price": "123.45",
      "orderQty": "100",
      "side": "BUY",
      "ordType": "LIMIT",
      "receivedAt": "2026-01-25T10:30:15"
    }
  ],
  "totalElements": 45,
  "page": 0,
  "size": 20
}
```

---

## 九、前端设计（Vue 3）

### 9.1 技术选型
- **框架**: Vue 3 + Composition API
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **HTTP 客户端**: Axios
- **构建工具**: Vite

### 9.2 页面结构

1. **会话列表页** (`/sessions`)
   - 表格：会话ID、SenderCompID、状态、连接时间
   - 自动刷新：每 5 秒

2. **消息查询页** (`/messages`)
   - 搜索表单：Symbol、ClOrdID
   - 表格：展示所有字段
   - 分页组件

### 9.3 目录结构

```
frontend/
├── src/
│   ├── views/
│   │   ├── Sessions.vue
│   │   └── Messages.vue
│   ├── components/
│   │   ├── SessionTable.vue
│   │   └── MessageTable.vue
│   ├── stores/
│   │   ├── session.ts
│   │   └── message.ts
│   ├── api/
│   │   └── client.ts
│   ├── App.vue
│   └── main.ts
├── package.json
└── vite.config.ts
```

---

## 十、项目结构

```
fix-server-simulator/
├── domain/                          # 领域层
│   ├── session/
│   │   ├── Session.java
│   │   ├── SessionStatus.java
│   │   └── SessionRepository.java
│   ├── message/
│   │   ├── FixMessage.java
│   │   ├── ParsedMessage.java
│   │   ├── MessageParser.java
│   │   └── ParsedMessageRepository.java
│   └── autoresponse/
│       ├── RuleEngine.java
│       ├── Rule.java
│       ├── Condition.java
│       └── Action.java
├── application/                     # 应用层
│   ├── SessionApplicationService.java
│   ├── MessageApplicationService.java
│   └── AutoResponseOrchestrator.java
├── infrastructure/                  # 基础设施层
│   ├── fix/
│   │   ├── FixConfig.java
│   │   └── FixMessageApplicationAdapter.java
│   ├── persistence/
│   │   ├── JpaSessionRepository.java
│   │   └── JpaParsedMessageRepository.java
│   └── ruleengine/
│       └── JsonRuleLoader.java
├── interfaces/                      # 接口层
│   ├── rest/
│   │   ├── HealthController.java
│   │   ├── SessionController.java
│   │   └── MessageController.java
│   └── fix/
│       └── ExecutionReportGenerator.java
├── config/                         # 配置
│   └── acceptor.properties
├── resources/
│   ├── application.yml             # MySQL 配置
│   └── rules.json                  # 自动回报规则
├── pom.xml
└── frontend/                       # 前端项目
    └── (Vue 3 项目结构)
```

---

## 十一、关键依赖 (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- QuickFIX/J -->
    <dependency>
        <groupId>quickfixj</groupId>
        <artifactId>quickfixj-core</artifactId>
        <version>2.3.1</version>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 十二、验收标准 (PRD V1.0.5)

| 委托数量 | 预期回报 |
|---------|----------|
| 100 | 1个 NEW 报文 |
| 200 | NEW + 部成100股 |
| 300 | NEW + 部成100股 + 部成200股 + 全成 |
| 400 | REJECTED 报文 |
| 其他 | 无自动回报 |

---

## 十三、下一步

设计确认后，实施计划：
1. 创建项目骨架（Maven + Spring Boot）
2. 创建数据库表
3. 实现领域层（Session、Message、RuleEngine）
4. 实现 QuickFIX/J 集成
5. 实现自动回报流程
6. 实现 REST API
7. 实现 Vue 3 前端
8. 集成测试
