# FIX Server Simulator v2.0 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 从头实现一个 FIX 4.2 协议服务器模拟器，支持基于委托数量的自动回报功能。

**Architecture:** 采用 DDD 四层架构（接口层、应用层、领域层、基础设施层），使用 QuickFIX/J 处理 FIX 协议，规则引擎实现自动回报，Vue 3 实现前端界面。

**Tech Stack:** Java 17, Spring Boot 3.x, QuickFIX/J 2.3.1, MySQL 8.x, Vue 3 + Element Plus

---

## Phase 1: 项目初始化

### Task 1.1: 清理旧代码和创建项目结构

**Files:**
- Delete: `src/` (整个旧目录)
- Delete: `pom.xml` (旧的)
- Create: `pom.xml` (新的)
- Create: `src/main/java/com/fixsimulator/` (目录结构)

**Step 1: 删除旧代码**

```bash
rm -rf src/
rm -f pom.xml
rm -f *.iml
```

**Step 2: 创建新的目录结构**

```bash
mkdir -p src/main/java/com/fixsimulator/{domain/{session,message,autoresponse},application,infrastructure/{fix,persistence,ruleengine},interfaces/{rest,fix}}
mkdir -p src/main/resources
mkdir -p src/test/java/com/fixsimulator
```

**Step 3: 创建新的 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.fixsimulator</groupId>
    <artifactId>fix-server-simulator</artifactId>
    <version>2.0.0</version>
    <name>FIX Server Simulator</name>
    <description>FIX 4.2 Protocol Server Simulator with Auto-Response</description>

    <properties>
        <java.version>17</java.version>
        <quickfixj.version>2.3.1</quickfixj.version>
    </properties>

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
            <version>${quickfixj.version}</version>
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

        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 4: Commit**

```bash
git add pom.xml
git commit -m "feat: initialize project structure with Maven dependencies

- Spring Boot 3.2.0
- QuickFIX/J 2.3.1
- MySQL + JPA
- Lombok
- Remove old code"
```

---

### Task 1.2: 创建 Spring Boot 主类和配置

**Files:**
- Create: `src/main/java/com/fixsimulator/FixServerSimulatorApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/acceptor.properties`

**Step 1: 创建主类**

```java
package com.fixsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FixServerSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixServerSimulatorApplication.class, args);
    }
}
```

**Step 2: 创建 application.yml**

```yaml
server:
  port: 8192

spring:
  application:
    name: fix-server-simulator

  datasource:
    url: jdbc:mysql://localhost:3306/fix_simulator?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
    username: root
    password: chenshun
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

logging:
  level:
    com.fixsimulator: DEBUG
    quickfix: DEBUG
```

**Step 3: 创建 acceptor.properties（占位符）**

```properties
# FIX Acceptor Configuration
# BeginString=FIX.4.2
# SenderCompID=FIXSIMULATOR
# 更多配置将由用户提供
```

**Step 4: 验证编译**

```bash
mvn clean compile
```

**Step 5: Commit**

```bash
git add src/main/java/com/fixsimulator/FixServerSimulatorApplication.java
git add src/main/resources/application.yml
git add src/main/resources/acceptor.properties
git commit -m "feat: add Spring Boot main class and basic configuration"
```

---

## Phase 2: 领域层实现

### Task 2.1: 创建枚举类型

**Files:**
- Create: `src/main/java/com/fixsimulator/domain/message/MsgType.java`
- Create: `src/main/java/com/fixsimulator/domain/message/Side.java`
- Create: `src/main/java/com/fixsimulator/domain/message/OrdType.java`
- Create: `src/main/java/com/fixsimulator/domain/session/SessionStatus.java`

**Step 1: 创建 MsgType 枚举**

```java
package com.fixsimulator.domain.message;

import lombok.Getter;

@Getter
public enum MsgType {
    NEW_ORDER_SINGLE("D", "New Order Single"),
    ORDER_CANCEL_REQUEST("F", "Order Cancel Request"),
    ORDER_CANCEL_REPLACE_REQUEST("G", "Order Cancel Replace Request");

    private final String code;
    private final String description;

    MsgType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MsgType fromCode(String code) {
        for (MsgType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MsgType: " + code);
    }
}
```

**Step 2: 创建 Side 枚举**

```java
package com.fixsimulator.domain.message;

import lombok.Getter;

@Getter
public enum Side {
    BUY("1", "BUY"),
    SELL("2", "SELL");

    private final String code;
    private final String displayName;

    Side(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static Side fromCode(String code) {
        for (Side side : values()) {
            if (side.code.equals(code)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown Side: " + code);
    }
}
```

**Step 3: 创建 OrdType 枚举**

```java
package com.fixsimulator.domain.message;

import lombok.Getter;

@Getter
public enum OrdType {
    MARKET("1", "MARKET"),
    LIMIT("2", "LIMIT");

    private final String code;
    private final String displayName;

    OrdType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static OrdType fromCode(String code) {
        for (OrdType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown OrdType: " + code);
    }
}
```

**Step 4: 创建 SessionStatus 枚举**

```java
package com.fixsimulator.domain.session;

import lombok.Getter;

@Getter
public enum SessionStatus {
    CONNECTED,
    DISCONNECTED,
    LOGGED_OUT
}
```

**Step 5: 验证编译**

```bash
mvn clean compile
```

**Step 6: Commit**

```bash
git add src/main/java/com/fixsimulator/domain/
git commit -m "feat: add domain enums (MsgType, Side, OrdType, SessionStatus)"
```

---

### Task 2.2: 创建 Session 聚合根

**Files:**
- Create: `src/main/java/com/fixsimulator/domain/session/Session.java`
- Create: `src/main/java/com/fixsimulator/domain/session/SessionRepository.java`

**Step 1: 创建 Session 实体**

```java
package com.fixsimulator.domain.session;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @Column(name = "sender_comp_id", nullable = false)
    private String senderCompId;

    @Column(name = "target_comp_id", nullable = false)
    private String targetCompId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @Column(name = "port")
    private Integer port;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Step 2: 创建 SessionRepository**

```java
package com.fixsimulator.domain.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findBySessionId(String sessionId);

    List<Session> findByStatus(SessionStatus status);

    boolean existsBySessionId(String sessionId);
}
```

**Step 3: 验证编译**

```bash
mvn clean compile
```

**Step 4: Commit**

```bash
git add src/main/java/com/fixsimulator/domain/session/
git commit -m "feat: add Session aggregate root and repository"
```

---

### Task 2.3: 创建 ParsedMessage 实体

**Files:**
- Create: `src/main/java/com/fixsimulator/domain/message/ParsedMessage.java`
- Create: `src/main/java/com/fixsimulator/domain/message/ParsedMessageRepository.java`

**Step 1: 创建 ParsedMessage 实体**

```java
package com.fixsimulator.domain.message;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type", nullable = false)
    private MsgType msgType;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "cl_ord_id")
    private String clOrdId;

    @Column(name = "orig_cl_ord_id")
    private String origClOrdId;

    @Column(name = "price", precision = 18, scale = 4)
    private BigDecimal price;

    @Column(name = "order_qty", precision = 18, scale = 4)
    private BigDecimal orderQty;

    @Enumerated(EnumType.STRING)
    @Column(name = "side")
    private String side;

    @Enumerated(EnumType.STRING)
    @Column(name = "ord_type")
    private String ordType;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}
```

**Step 2: 创建 ParsedMessageRepository**

```java
package com.fixsimulator.domain.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParsedMessageRepository extends JpaRepository<ParsedMessage, Long> {

    @Query("SELECT m FROM ParsedMessage m WHERE " +
           "(:symbol IS NULL OR m.symbol = :symbol) AND " +
           "(:clOrdId IS NULL OR m.clOrdId = :clOrdId)")
    Page<ParsedMessage> searchMessages(
        @Param("symbol") String symbol,
        @Param("clOrdId") String clOrdId,
        Pageable pageable
    );
}
```

**Step 3: 验证编译**

```bash
mvn clean compile
```

**Step 4: Commit**

```bash
git add src/main/java/com/fixsimulator/domain/message/
git commit -m "feat: add ParsedMessage entity and repository"
```

---

### Task 2.4: 创建 FixMessage 值对象

**Files:**
- Create: `src/main/java/com/fixsimulator/domain/message/FixMessage.java`
- Create: `src/main/java/com/fixsimulator/domain/message/FixMessageField.java`

**Step 1: 创建 FixMessageField 辅助类**

```java
package com.fixsimulator.domain.message;

public final class FixMessageField {
    public static final int SYMBOL = 55;
    public static final int CL_ORD_ID = 11;
    public static final int ORIG_CL_ORD_ID = 41;
    public static final int PRICE = 44;
    public static final int ORDER_QTY = 38;
    public static final int SIDE = 54;
    public static final int ORD_TYPE = 40;
    public static final int MSG_TYPE = 35;
}
```

**Step 2: 创建 FixMessage 值对象**

```java
package com.fixsimulator.domain.message;

import quickfix.Field;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FixMessage {
    private final Message message;
    private final Map<Integer, String> fields;

    public FixMessage(Message message) {
        this.message = message;
        this.fields = new HashMap<>();
        extractFields();
    }

    private void extractFields() {
        try {
            for (Field field : message) {
                fields.put(field.getTag(), field.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract fields from FIX message", e);
        }
    }

    public MsgType getMsgType() {
        try {
            return MsgType.fromCode(message.getHeader().getString(MsgType.FIELD));
        } catch (FieldNotFound e) {
            throw new IllegalArgumentException("MsgType not found", e);
        }
    }

    public String getSymbol() {
        return getField(FixMessageField.SYMBOL);
    }

    public String getClOrdID() {
        return getField(FixMessageField.CL_ORD_ID);
    }

    public String getOrigClOrdID() {
        return getField(FixMessageField.ORIG_CL_ORD_ID);
    }

    public BigDecimal getPrice() {
        String value = getField(FixMessageField.PRICE);
        return value != null ? new BigDecimal(value) : null;
    }

    public BigDecimal getOrderQty() {
        String value = getField(FixMessageField.ORDER_QTY);
        return value != null ? new BigDecimal(value) : null;
    }

    public Side getSide() {
        String code = getField(FixMessageField.SIDE);
        return code != null ? Side.fromCode(code) : null;
    }

    public OrdType getOrdType() {
        String code = getField(FixMessageField.ORD_TYPE);
        return code != null ? OrdType.fromCode(code) : null;
    }

    public String getField(int tag) {
        return fields.get(tag);
    }

    public Message getOriginalMessage() {
        return message;
    }
}
```

**Step 3: 验证编译**

```bash
mvn clean compile
```

**Step 4: Commit**

```bash
git add src/main/java/com/fixsimulator/domain/message/FixMessage.java
git add src/main/java/com/fixsimulator/domain/message/FixMessageField.java
git commit -m "feat: add FixMessage value object and field constants"
```

---

## Phase 3: 规则引擎实现

### Task 3.1: 创建规则配置模型

**Files:**
- Create: `src/main/java/com/fixsimulator/domain/autoresponse/Rule.java`
- Create: `src/main/java/com/fixsimulator/domain/autoresponse/Condition.java`
- Create: `src/main/java/com/fixsimulator/domain/autoresponse/Action.java`

**Step 1: 创建 Condition 类**

```java
package com.fixsimulator.domain.autoresponse;

import com.fixsimulator.domain.message.FixMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Condition {
    @JsonProperty("field")
    private String field;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("value")
    private Object value;

    public boolean matches(FixMessage message) {
        return switch (field) {
            case "OrderQty" -> checkOrderQty(message);
            default -> false;
        };
    }

    private boolean checkOrderQty(FixMessage message) {
        BigDecimal orderQty = message.getOrderQty();
        if (orderQty == null) {
            return false;
        }

        return switch (operator) {
            case "==" -> orderQty.compareTo(toBigDecimal(value)) == 0;
            case ">" -> orderQty.compareTo(toBigDecimal(value)) > 0;
            case "<" -> orderQty.compareTo(toBigDecimal(value)) < 0;
            case "between" -> {
                if (value instanceof java.util.List list && list.size() == 2) {
                    BigDecimal min = toBigDecimal(list.get(0));
                    BigDecimal max = toBigDecimal(list.get(1));
                    yield orderQty.compareTo(min) >= 0 && orderQty.compareTo(max) <= 0;
                }
                yield false;
            }
            default -> false;
        };
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        throw new IllegalArgumentException("Cannot convert to BigDecimal: " + value);
    }
}
```

**Step 2: 创建 Action 类**

```java
package com.fixsimulator.domain.autoresponse;

import lombok.Data;

import java.util.Map;

@Data
public class Action {
    private String type;  // "SEND_REPORT"
    private Map<String, Object> params;
    private long delayMs;  // 延迟执行时间（毫秒）
}
```

**Step 3: 创建 Rule 类**

```java
package com.fixsimulator.domain.autoresponse;

import com.fixsimulator.domain.message.FixMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Rule {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("condition")
    private Condition condition;

    @JsonProperty("actions")
    private List<Action> actions;

    public boolean matches(FixMessage message) {
        return condition.matches(message);
    }
}
```

**Step 4: 验证编译**

```bash
mvn clean compile
```

**Step 5: Commit**

```bash
git add src/main/java/com/fixsimulator/domain/autoresponse/
git commit -m "feat: add rule engine domain models (Rule, Condition, Action)"
```

---

### Task 3.2: 创建规则引擎

**Files:**
- Create: `src/main/java/com/fixsimulator/domain/autoresponse/RuleEngine.java`
- Create: `src/main/resources/rules.json`

**Step 1: 创建规则引擎类**

```java
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
```

**Step 2: 创建 rules.json 配置文件**

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

**Step 3: 验证编译**

```bash
mvn clean compile
```

**Step 4: Commit**

```bash
git add src/main/java/com/fixsimulator/domain/autoresponse/RuleEngine.java
git add src/main/resources/rules.json
git commit -m "feat: add RuleEngine with auto-response rules configuration"
```

---

## Phase 4: 基础设施层实现

### Task 4.1: 创建 MessageParser

**Files:**
- Create: `src/main/java/com/fixsimulator/infrastructure/persistence/MessageParser.java`

**Step 1: 创建 MessageParser**

```java
package com.fixsimulator.infrastructure.persistence;

import com.fixsimulator.domain.message.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.Message;
import quickfix.SessionID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageParser {

    public FixMessage parse(Message message) {
        return new FixMessage(message);
    }

    public ParsedMessage toParsedMessage(FixMessage fixMessage, SessionID sessionId) {
        return ParsedMessage.builder()
                .sessionId(sessionId.toString())
                .msgType(fixMessage.getMsgType())
                .symbol(fixMessage.getSymbol())
                .clOrdId(fixMessage.getClOrdID())
                .origClOrdId(fixMessage.getOrigClOrdID())
                .price(fixMessage.getPrice())
                .orderQty(fixMessage.getOrderQty())
                .side(fixMessage.getSide() != null ? fixMessage.getSide().getDisplayName() : null)
                .ordType(fixMessage.getOrdType() != null ? fixMessage.getOrdType().getDisplayName() : null)
                .build();
    }
}
```

**Step 2: 验证编译**

```bash
mvn clean compile
```

**Step 3: Commit**

```bash
git add src/main/java/com/fixsimulator/infrastructure/persistence/MessageParser.java
git commit -m "feat: add MessageParser for converting FIX messages to domain objects"
```

---

### Task 4.2: 创建 QuickFIX/J 配置

**Files:**
- Create: `src/main/java/com/fixsimulator/infrastructure/fix/FixConfig.java`

**Step 1: 创建 FixConfig**

```java
package com.fixsimulator.infrastructure.fix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.SessionSettings;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

@Slf4j
@Configuration
public class FixConfig {

    @Bean
    public SessionSettings sessionSettings() {
        try {
            SessionSettings settings = new SessionSettings();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("acceptor.properties");

            if (inputStream == null) {
                log.warn("acceptor.properties not found, using default settings");
                return createDefaultSettings();
            }

            settings.load(inputStream);
            log.info("Loaded FIX settings from acceptor.properties");
            return settings;
        } catch (Exception e) {
            log.error("Failed to load acceptor.properties, using defaults", e);
            return createDefaultSettings();
        }
    }

    private SessionSettings createDefaultSettings() {
        SessionSettings settings = new SessionSettings();
        try {
            // 创建默认设置
            settings.setString("StartTime", "00:00:00");
            settings.setString("EndTime", "00:00:00");
            settings.setString("HeartBtInt", "30");
            settings.setBool("UseDataDictionary", "Y");
            settings.setString("DataDictionary", "FIX42.xml");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default settings", e);
        }
        return settings;
    }
}
```

**Step 2: 验证编译**

```bash
mvn clean compile
```

**Step 3: Commit**

```bash
git add src/main/java/com/fixsimulator/infrastructure/fix/FixConfig.java
git commit -m "feat: add FixConfig for QuickFIX/J settings"
```

---

### Task 4.3: 创建 FixMessageApplicationAdapter

**Files:**
- Create: `src/main/java/com/fixsimulator/infrastructure/fix/FixMessageApplicationAdapter.java`

**Step 1: 创建适配器**

```java
package com.fixsimulator.infrastructure.fix;

import com.fixsimulator.application.AutoResponseOrchestrator;
import com.fixsimulator.domain.message.FixMessage;
import com.fixsimulator.domain.message.ParsedMessage;
import com.fixsimulator.domain.message.ParsedMessageRepository;
import com.fixsimulator.infrastructure.persistence.MessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.*;
import quickfix.field.MsgType;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixMessageApplicationAdapter implements Application {

    private final MessageParser messageParser;
    private final ParsedMessageRepository messageRepository;
    private final AutoResponseOrchestrator autoResponseOrchestrator;

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("Session created: {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("Session logged on: {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("Session logged out: {}", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        log.debug("To admin: {}", message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
        log.debug("From admin: {}", message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
        log.debug("To app: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {
        try {
            log.info("Received message: {} from {}", message, sessionId);

            // 解析消息
            FixMessage fixMessage = messageParser.parse(message);

            // 存储到数据库
            ParsedMessage parsed = messageParser.toParsedMessage(fixMessage, sessionId);
            messageRepository.save(parsed);
            log.info("Saved parsed message: {}", parsed);

            // 触发自动回报
            autoResponseOrchestrator.process(fixMessage, sessionId);

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
```

**Step 2: 验证编译**

```bash
mvn clean compile
```

**Step 3: Commit**

```bash
git add src/main/java/com/fixsimulator/infrastructure/fix/FixMessageApplicationAdapter.java
git commit -m "feat: add FixMessageApplicationAdapter for handling FIX messages"
```

---

## Phase 5: 应用层实现

### Task 5.1: 创建 AutoResponseOrchestrator

**Files:**
- Create: `src/main/java/com/fixsimulator/application/AutoResponseOrchestrator.java`

**Step 1: 创建编排器**

```java
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
        // 只处理 New Order Single
        if (message.getMsgType() != com.fixsimulator.domain.message.MsgType.NEW_ORDER_SINGLE) {
            log.debug("Ignoring non-NewOrder message: {}", message.getMsgType());
            return;
        }

        // 匹配规则
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
```

**Step 2: 验证编译**

```bash
mvn clean compile
```

**Step 3: Commit**

```bash
git add src/main/java/com/fixsimulator/application/AutoResponseOrchestrator.java
git commit -m "feat: add AutoResponseOrchestrator for coordinating auto-response flow"
```

---

### Task 5.2: 创建 ExecutionReportGenerator

**Files:**
- Create: `src/main/java/com/fixsimulator/interfaces/fix/ExecutionReportGenerator.java`

**Step 1: 创建生成器**

```java
package com.fixsimulator.interfaces.fix;

import com.fixsimulator.domain.autoresponse.Action;
import com.fixsimulator.domain.message.FixMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.Message;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class ExecutionReportGenerator {

    private static final DateTimeFormatter FIX_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    public Message generate(Action action, FixMessage originalMessage) {
        String execId = "EXEC_" + System.currentTimeMillis();
        String orderId = "ORD_" + System.currentTimeMillis();

        ExecutionReport report = new ExecutionReport(
                new OrderID(orderId),
                new ExecID(execId),
                new ExecType(action.getParams().get("execType").toString()),
                new OrdStatus(action.getParams().get("ordStatus").toString()),
                new Side(originalMessage.getSide().getCode()),
                new LeavesQty(originalMessage.getOrderQty().intValue()),
                new CumQty(0),
                new AvgPx(0)
        );

        // 设置基本字段
        report.set(new ClOrdID(originalMessage.getClOrdID()));
        report.set(new Symbol(originalMessage.getSymbol()));
        report.set(new OrderQty(originalMessage.getOrderQty()));
        report.set(new TransactTime());
        report.set(new SendingTime(LocalDateTime.now().format(FIX_TIME_FORMATTER)));

        // 根据动作类型设置额外字段
        Object execType = action.getParams().get("execType");
        if ("1".equals(execType)) { // PARTIAL_FILL
            Object lastQty = action.getParams().get("lastQty");
            if (lastQty != null) {
                report.set(new LastQty(new BigDecimal(lastQty.toString())));
            }
        }

        if ("8".equals(execType)) { // REJECTED
            Object text = action.getParams().get("text");
            if (text != null) {
                report.set(new Text(text.toString()));
            }
        }

        return report;
    }
}
```

**Step 2: 修复导入**

```java
// 在 ExecutionReportGenerator.java 顶部添加
import java.math.BigDecimal;
```

**Step 3: 验证编译**

```bash
mvn clean compile
```

**Step 4: Commit**

```bash
git add src/main/java/com/fixsimulator/interfaces/fix/ExecutionReportGenerator.java
git commit -m "feat: add ExecutionReportGenerator for creating FIX execution reports"
```

---

## Phase 6: REST API 实现

### Task 6.1: 创建 DTO 类

**Files:**
- Create: `src/main/java/com/fixsimulator/interfaces/rest/dto/SessionResponse.java`
- Create: `src/main/java/com/fixsimulator/interfaces/rest/dto/MessageResponse.java`
- Create: `src/main/java/com/fixsimulator/interfaces/rest/dto/HealthResponse.java`

**Step 1: 创建 SessionResponse**

```java
package com.fixsimulator.interfaces.rest.dto;

import com.fixsimulator.domain.session.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private String sessionId;
    private String senderCompId;
    private String targetCompId;
    private SessionStatus status;
    private Integer port;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Step 2: 创建 MessageResponse**

```java
package com.fixsimulator.interfaces.rest.dto;

import com.fixsimulator.domain.message.MsgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private MsgType msgType;
    private String symbol;
    private String clOrdId;
    private String origClOrdId;
    private BigDecimal price;
    private BigDecimal orderQty;
    private String side;
    private String ordType;
    private LocalDateTime receivedAt;
}
```

**Step 3: 创建 HealthResponse**

```java
package com.fixsimulator.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
```

**Step 4: 验证编译**

```bash
mvn clean compile
```

**Step 5: Commit**

```bash
git add src/main/java/com/fixsimulator/interfaces/rest/dto/
git commit -m "feat: add REST API DTOs (SessionResponse, MessageResponse, HealthResponse)"
```

---

### Task 6.2: 创建 Application Service

**Files:**
- Create: `src/main/java/com/fixsimulator/application/SessionApplicationService.java`
- Create: `src/main/java/com/fixsimulator/application/MessageApplicationService.java`

**Step 1: 创建 SessionApplicationService**

```java
package com.fixsimulator.application;

import com.fixsimulator.domain.session.Session;
import com.fixsimulator.domain.session.SessionRepository;
import com.fixsimulator.interfaces.rest.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionApplicationService {

    private final SessionRepository sessionRepository;

    public List<SessionResponse> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SessionResponse getSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return toResponse(session);
    }

    private SessionResponse toResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .senderCompId(session.getSenderCompId())
                .targetCompId(session.getTargetCompId())
                .status(session.getStatus())
                .port(session.getPort())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
```

**Step 2: 创建 MessageApplicationService**

```java
package com.fixsimulator.application;

import com.fixsimulator.domain.message.ParsedMessage;
import com.fixsimulator.domain.message.ParsedMessageRepository;
import com.fixsimulator.interfaces.rest.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageApplicationService {

    private final ParsedMessageRepository messageRepository;

    public Page<MessageResponse> searchMessages(String symbol, String clOrdId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ParsedMessage> messages = messageRepository.searchMessages(symbol, clOrdId, pageRequest);
        return messages.map(this::toResponse);
    }

    private MessageResponse toResponse(ParsedMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .msgType(message.getMsgType())
                .symbol(message.getSymbol())
                .clOrdId(message.getClOrdId())
                .origClOrdId(message.getOrigClOrdId())
                .price(message.getPrice())
                .orderQty(message.getOrderQty())
                .side(message.getSide())
                .ordType(message.getOrdType())
                .receivedAt(message.getReceivedAt())
                .build();
    }
}
```

**Step 3: 验证编译**

```bash
mvn clean compile
```

**Step 4: Commit**

```bash
git add src/main/java/com/fixsimulator/application/
git commit -m "feat: add application services (SessionApplicationService, MessageApplicationService)"
```

---

### Task 6.3: 创建 REST Controllers

**Files:**
- Create: `src/main/java/com/fixsimulator/interfaces/rest/HealthController.java`
- Create: `src/main/java/com/fixsimulator/interfaces/rest/SessionController.java`
- Create: `src/main/java/com/fixsimulator/interfaces/rest/MessageController.java`

**Step 1: 创建 HealthController**

```java
package com.fixsimulator.interfaces.rest;

import com.fixsimulator.interfaces.rest.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return HealthResponse.builder()
                .status("UP")
                .message("FIX Server Simulator is running")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

**Step 2: 创建 SessionController**

```java
package com.fixsimulator.interfaces.rest;

import com.fixsimulator.application.SessionApplicationService;
import com.fixsimulator.interfaces.rest.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionApplicationService sessionApplicationService;

    @GetMapping
    public List<SessionResponse> getAllSessions() {
        return sessionApplicationService.getAllSessions();
    }

    @GetMapping("/{sessionId}")
    public SessionResponse getSession(@PathVariable String sessionId) {
        return sessionApplicationService.getSession(sessionId);
    }
}
```

**Step 3: 创建 MessageController**

```java
package com.fixsimulator.interfaces.rest;

import com.fixsimulator.application.MessageApplicationService;
import com.fixsimulator.interfaces.rest.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService messageApplicationService;

    @GetMapping
    public Page<MessageResponse> searchMessages(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String clOrdId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageApplicationService.searchMessages(symbol, clOrdId, page, size);
    }
}
```

**Step 4: 验证编译**

```bash
mvn clean compile
```

**Step 5: Commit**

```bash
git add src/main/java/com/fixsimulator/interfaces/rest/
git commit -m "feat: add REST controllers (Health, Session, Message)"
```

---

## Phase 7: QuickFIX/J 完整集成

### Task 7.1: 创建 FixStarter 配置

**Files:**
- Create: `src/main/java/com/fixsimulator/infrastructure/fix/FixStarter.java`

**Step 1: 创建 FixStarter**

```java
package com.fixsimulator.infrastructure.fix;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import quickfix.*;
import quickfix.mina.acceptor.SocketAcceptor;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixStarter {

    private final FixMessageApplicationAdapter application;
    private final SessionSettings settings;
    private SocketAcceptor acceptor;

    @EventListener(ApplicationReadyEvent.class)
    public void startAcceptor() {
        try {
            MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();
            LogFactory logFactory = new SLF4JLogFactory();
            MessageFactory messageFactory = new DefaultMessageFactory();

            acceptor = new SocketAcceptor(
                    application,
                    messageStoreFactory,
                    settings,
                    logFactory,
                    messageFactory
            );

            acceptor.start();
            log.info("FIX Acceptor started successfully");

        } catch (Exception e) {
            log.error("Failed to start FIX Acceptor", e);
            throw new RuntimeException("Failed to start FIX Acceptor", e);
        }
    }

    public void stopAcceptor() {
        if (acceptor != null) {
            acceptor.stop();
            log.info("FIX Acceptor stopped");
        }
    }
}
```

**Step 2: 创建 SLF4JLogFactory**

```java
package com.fixsimulator.infrastructure.fix;

import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

public class SLF4JLogFactory implements LogFactory {
    @Override
    public Log create(SessionID sessionID) {
        return new SLF4JLog(sessionID);
    }
}
```

**Step 3: 创建 SLF4JLog**

```java
package com.fixsimulator.infrastructure.fix;

import lombok.extern.slf4j.Slf4j;
import quickfix.Log;
import quickfix.SessionID;

@Slf4j
public class SLF4JLog implements Log {
    private final SessionID sessionID;

    public SLF4JLog(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public void clear() {
    }

    @Override
    public void onIncoming(String message) {
        log.info("[{}] Incoming: {}", sessionID, message);
    }

    @Override
    public void onOutgoing(String message) {
        log.info("[{}] Outgoing: {}", sessionID, message);
    }

    @Override
    public void onEvent(String message) {
        log.info("[{}] Event: {}", sessionID, message);
    }
}
```

**Step 4: 验证编译**

```bash
mvn clean compile
```

**Step 5: Commit**

```bash
git add src/main/java/com/fixsimulator/infrastructure/fix/
git commit -m "feat: add FixStarter with SLF4J logging integration"
```

---

### Task 7.2: 完善 acceptor.properties

**Files:**
- Modify: `src/main/resources/acceptor.properties`

**Step 1: 用用户提供的内容替换 acceptor.properties**

```properties
# FIX Acceptor Configuration
[DEFAULT]
ConnectionType=acceptor
StartTime=00:00:00
EndTime=15:30:00
StartDay=monday
EndDay=friday
HeartBtInt=30
ReconnectInterval=5
ResetOnLogon=Y
FileLogPath=log
UseDataDictionary=Y
DataDictionary=FIX42.xml

[SESSION]
BeginString=FIX.4.2
SenderCompID=FIXSIMULATOR
TargetCompID=GATEWAY
SocketAcceptPort=9876
```

**Step 2: Commit**

```bash
git add src/main/resources/acceptor.properties
git commit -m "feat: update acceptor.properties with full FIX configuration"
```

---

## Phase 8: 前端实现 (Vue 3)

### Task 8.1: 初始化前端项目

**Files:**
- Create: `frontend/` (目录结构)
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`

**Step 1: 创建前端目录**

```bash
mkdir -p frontend/src/{views,components,stores,api,assets}
```

**Step 2: 创建 package.json**

```json
{
  "name": "fix-simulator-frontend",
  "version": "2.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "axios": "^1.6.0",
    "element-plus": "^2.5.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

**Step 3: 创建 vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8192',
        changeOrigin: true
      }
    }
  }
})
```

**Step 4: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FIX Server Simulator</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.ts"></script>
</body>
</html>
```

**Step 5: Commit**

```bash
git add frontend/
git commit -m "feat: initialize Vue 3 frontend project structure"
```

---

### Task 8.2: 创建前端主文件

**Files:**
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/api/client.ts`

**Step 1: 创建 main.ts**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

**Step 2: 创建 App.vue**

```vue
<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>FIX Server Simulator</h1>
        <el-menu mode="horizontal" :default-active="activeMenu" router>
          <el-menu-item index="/sessions">会话管理</el-menu-item>
          <el-menu-item index="/messages">消息查询</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
#app {
  min-height: 100vh;
}
.el-header {
  background-color: #409eff;
  color: white;
}
.el-header h1 {
  margin: 0;
  padding: 10px 0;
  float: left;
}
.el-menu {
  float: right;
}
</style>
```

**Step 3: 创建 API 客户端**

```typescript
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export default api
```

**Step 4: Commit**

```bash
git add frontend/src/
git commit -m "feat: add Vue main files (main.ts, App.vue, API client)"
```

---

### Task 8.3: 创建路由和 Pinia stores

**Files:**
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/stores/session.ts`
- Create: `frontend/src/stores/message.ts`

**Step 1: 创建路由**

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import Sessions from '../views/Sessions.vue'
import Messages from '../views/Messages.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/sessions' },
    { path: '/sessions', component: Sessions },
    { path: '/messages', component: Messages }
  ]
})

export default router
```

**Step 2: 创建 session store**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/client'

export interface Session {
  id: number
  sessionId: string
  senderCompId: string
  targetCompId: string
  status: string
  port?: number
  createdAt: string
  updatedAt: string
}

export const useSessionStore = defineStore('session', () => {
  const sessions = ref<Session[]>([])

  async function fetchSessions() {
    const response = await api.get('/sessions')
    sessions.value = response.data
  }

  return { sessions, fetchSessions }
})
```

**Step 3: 创建 message store**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/client'

export interface Message {
  id: number
  msgType: string
  symbol: string
  clOrdId?: string
  origClOrdId?: string
  price?: string
  orderQty?: string
  side?: string
  ordType?: string
  receivedAt: string
}

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const total = ref(0)

  async function searchMessages(params: { symbol?: string; clOrdId?: string; page?: number; size?: number }) {
    const response = await api.get('/messages', { params })
    messages.value = response.data.content
    total.value = response.data.totalElements
  }

  return { messages, total, searchMessages }
})
```

**Step 4: Commit**

```bash
git add frontend/src/router/ frontend/src/stores/
git commit -m "feat: add Vue router and Pinia stores"
```

---

### Task 8.4: 创建页面组件

**Files:**
- Create: `frontend/src/views/Sessions.vue`
- Create: `frontend/src/views/Messages.vue`

**Step 1: 创建 Sessions.vue**

```vue
<template>
  <div>
    <h2>会话列表</h2>
    <el-table :data="sessionStore.sessions" stripe>
      <el-table-column prop="sessionId" label="会话ID" />
      <el-table-column prop="senderCompId" label="SenderCompID" />
      <el-table-column prop="targetCompId" label="TargetCompID" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="port" label="端口" />
      <el-table-column prop="createdAt" label="连接时间" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useSessionStore } from '../stores/session'

const sessionStore = useSessionStore()

function getStatusType(status: string) {
  return status === 'CONNECTED' ? 'success' : 'info'
}

async function refresh() {
  await sessionStore.fetchSessions()
}

onMounted(() => {
  refresh()
  const timer = setInterval(refresh, 5000)
  onUnmounted(() => clearInterval(timer))
})
</script>
```

**Step 2: 创建 Messages.vue**

```vue
<template>
  <div>
    <h2>消息查询</h2>
    <el-form :inline="true" @submit.prevent="handleSearch">
      <el-form-item label="股票代码">
        <el-input v-model="searchForm.symbol" placeholder="如: 600519" clearable />
      </el-form-item>
      <el-form-item label="ClOrdID">
        <el-input v-model="searchForm.clOrdId" placeholder="客户订单ID" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="messageStore.messages" stripe>
      <el-table-column prop="msgType" label="消息类型" width="80" />
      <el-table-column prop="symbol" label="股票代码" width="100" />
      <el-table-column prop="clOrdId" label="ClOrdID" width="150" />
      <el-table-column prop="origClOrdId" label="OrigClOrdID" width="150" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="orderQty" label="数量" width="100" />
      <el-table-column prop="side" label="方向" width="80" />
      <el-table-column prop="ordType" label="类型" width="100" />
      <el-table-column prop="receivedAt" label="接收时间" />
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="messageStore.total"
      layout="total, prev, pager, next"
      @current-change="handleSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useMessageStore } from '../stores/message'

const messageStore = useMessageStore()
const searchForm = reactive({ symbol: '', clOrdId: '' })
const pagination = reactive({ page: 0, size: 20 })

function handleSearch() {
  messageStore.searchMessages({
    symbol: searchForm.symbol || undefined,
    clOrdId: searchForm.clOrdId || undefined,
    page: pagination.page,
    size: pagination.size
  })
}

handleSearch()
</script>
```

**Step 3: Commit**

```bash
git add frontend/src/views/
git commit -m "feat: add Vue views (Sessions, Messages)"
```

---

## Phase 9: 测试和验证

### Task 9.1: 创建数据库初始化脚本

**Files:**
- Create: `src/main/resources/schema.sql`

**Step 1: 创建数据库初始化脚本**

```sql
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
```

**Step 2: Commit**

```bash
git add src/main/resources/schema.sql
git commit -m "feat: add database initialization script"
```

---

### Task 9.2: 编译和运行验证

**Step 1: 编译项目**

```bash
cd /Users/chenshun/open/fix-server-simulator/.worktrees/fix-simulator-v2
mvn clean package -DskipTests
```

**Step 2: 启动应用**

```bash
java -jar target/fix-server-simulator-2.0.0.jar
```

**Step 3: 验证健康检查**

```bash
curl http://localhost:8192/api/health
```

预期输出：
```json
{
  "status": "UP",
  "message": "FIX Server Simulator is running",
  "timestamp": "2026-01-31T..."
}
```

**Step 4: 安装前端依赖**

```bash
cd frontend
npm install
npm run dev
```

**Step 5: 访问前端**

打开浏览器访问 `http://localhost:3000`

---

## 验收标准

### 后端验收

1. 应用可以正常启动，FIX Acceptor 监听在 9876 端口
2. `/api/health` 返回 UP 状态
3. `/api/sessions` 返回会话列表
4. `/api/messages` 支持按 symbol 和 clOrdId 搜索

### 自动回报验收 (PRD V1.0.5)

| 委托数量 | 预期回报 |
|---------|----------|
| 100 | 1个 NEW 报文 |
| 200 | NEW + 部成100股 |
| 300 | NEW + 部成100股 + 部成200股 + 全成 |
| 400 | REJECTED 报文 |

### 前端验收

1. 会话列表页面正常显示
2. 消息查询页面支持搜索
3. 表格数据正确展示

---

## 完成检查清单

- [ ] Phase 1: 项目初始化完成
- [ ] Phase 2: 领域层实现完成
- [ ] Phase 3: 规则引擎实现完成
- [ ] Phase 4: 基础设施层实现完成
- [ ] Phase 5: 应用层实现完成
- [ ] Phase 6: REST API 实现完成
- [ ] Phase 7: QuickFIX/J 集成完成
- [ ] Phase 8: 前端实现完成
- [ ] Phase 9: 测试和验证完成

---

**总任务数**: 25 个 Tasks
**预计时间**: 完整实现需要按顺序执行所有任务
