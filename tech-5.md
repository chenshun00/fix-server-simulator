# FIX Server Simulator - V1.0.5 技术设计文档

## 一、概述

本文档描述了FIX Server Simulator V1.0.5版本中自动回报功能的技术实现方案。该功能将根据委托数量自动生成相应的回报报文，无需手动干预。

## 二、设计目标

- 实现基于委托数量的自动回报逻辑
- 保持与现有系统的兼容性
- 确保自动回报功能与手动回报功能可共存
- 不改变现有API接口

## 三、技术架构

### 3.1 核心组件

1. **AutoResponseService**：自动回报服务，负责根据委托数量生成相应的回报序列
2. **ResponseGenerationStrategy**：回报策略生成器，定义不同委托数量对应的回报逻辑
3. **MessageSender**：消息发送器，用于发送生成的回报报文

### 3.2 类图设计

```
+------------------------+
|   AutoResponseService  |
+------------------------+
| - ResponseStrategy     |
|                        |
| + processOrder()       |
| + generateResponses()  |
+------------------------+
           |
           | implements
           v
+------------------------+
| ResponseGenerationStrategy |
+------------------------+
| + getResponseSequence()|
| + createNewResponse()  |
| + createPartialFill()  |
| + createFullFill()     |
| + createRejected()     |
+------------------------+
```

## 四、详细设计

### 4.1 AutoResponseService

```java
@Service
public class AutoResponseService {
    
    public void processOrder(Message orderMessage) {
        // 从订单消息中提取OrderQty字段
        int orderQty = extractOrderQty(orderMessage);
        
        // 根据OrderQty获取回报序列
        List<Message> responses = generateResponses(orderQty, orderMessage);
        
        // 发送回报消息
        for (Message response : responses) {
            sendMessage(response);
        }
    }
    
    private List<Message> generateResponses(int orderQty, Message originalOrder) {
        ResponseGenerationStrategy strategy = getStrategyByQuantity(orderQty);
        return strategy.getResponseSequence(originalOrder);
    }
    
    private ResponseGenerationStrategy getStrategyByQuantity(int quantity) {
        switch(quantity) {
            case 100: return new Quantity100Strategy();
            case 200: return new Quantity200Strategy();
            case 300: return new Quantity300Strategy();
            case 400: return new Quantity400Strategy();
            default: return new NoAutoResponseStrategy(); // 其他数量不自动回报
        }
    }
}
```

### 4.2 ResponseGenerationStrategy接口及其实现

```java
public interface ResponseGenerationStrategy {
    List<Message> getResponseSequence(Message originalOrder);
}

// 数量100策略：返回NEW报文
@Component
public class Quantity100Strategy implements ResponseGenerationStrategy {
    @Override
    public List<Message> getResponseSequence(Message originalOrder) {
        List<Message> responses = new ArrayList<>();
        Message newResponse = createNewResponse(originalOrder);
        responses.add(newResponse);
        return responses;
    }
    
    private Message createNewResponse(Message originalOrder) {
        // 创建NEW报文 (ExecType=0, OrdStatus=0)
        Message response = new Message();
        // 设置必要字段...
        return response;
    }
}

// 数量200策略：返回NEW报文 + 部成100股报文
@Component
public class Quantity200Strategy implements ResponseGenerationStrategy {
    @Override
    public List<Message> getResponseSequence(Message originalOrder) {
        List<Message> responses = new ArrayList<>();
        // 添加NEW报文
        responses.add(createNewResponse(originalOrder));
        // 添加部成报文 (LastShares=100, CumQty=100, LeavesQty=100)
        responses.add(createPartialFillResponse(originalOrder, 100, 100, 100));
        return responses;
    }
    
    // 实现创建各种类型回报的方法...
}

// 数量300策略：返回NEW报文 + 2个部成报文 + 全成报文
@Component
public class Quantity300Strategy implements ResponseGenerationStrategy {
    @Override
    public List<Message> getResponseSequence(Message originalOrder) {
        List<Message> responses = new ArrayList<>();
        // 添加NEW报文
        responses.add(createNewResponse(originalOrder));
        // 添加第一个部成报文 (LastShares=100, CumQty=100, LeavesQty=200)
        responses.add(createPartialFillResponse(originalOrder, 100, 100, 200));
        // 添加第二个部成报文 (LastShares=100, CumQty=200, LeavesQty=100)
        responses.add(createPartialFillResponse(originalOrder, 100, 200, 100));
        // 添加全成报文 (CumQty=300, LeavesQty=0)
        responses.add(createFullFillResponse(originalOrder, 300, 0));
        return responses;
    }
    
    // 实现创建各种类型回报的方法...
}

// 数量400策略：返回拒绝报文
@Component
public class Quantity400Strategy implements ResponseGenerationStrategy {
    @Override
    public List<Message> getResponseSequence(Message originalOrder) {
        List<Message> responses = new ArrayList<>();
        // 添加拒绝报文 (ExecType=8, OrdStatus=8)
        responses.add(createRejectedResponse(originalOrder));
        return responses;
    }
    
    private Message createRejectedResponse(Message originalOrder) {
        // 创建拒绝报文 (ExecType=8, OrdStatus=8, Text="rejected by simulator, don't worry")
        Message response = new Message();
        // 设置必要字段...
        return response;
    }
}

// 默认策略：不自动回报
@Component
public class NoAutoResponseStrategy implements ResponseGenerationStrategy {
    @Override
    public List<Message> getResponseSequence(Message originalOrder) {
        // 返回空列表，表示不自动回报
        return new ArrayList<>();
    }
}
```

### 4.3 消息处理器集成

需要修改现有的消息处理器，使其能够识别New Order单据并触发自动回报：

```java
@Component
public class FixMessageHandler extends quickfix.MessageCracker implements quickfix.Application {
    // ...
    
    @Autowired
    private AutoResponseService autoResponseService;
    
    // 处理New Order单据
    public void onMessage(quickfix.fix44.NewOrderSingle newOrderSingle, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        // 调用自动回报服务处理订单
        autoResponseService.processOrder(newOrderSingle);
        
        // 原有的处理逻辑...
    }
    
    // ...
}
```

## 五、配置选项

可以通过配置文件启用/禁用自动回报功能：

```yaml
fix:
  simulator:
    auto-response:
      enabled: true  # 是否启用自动回报功能
      trigger-messages:  # 触发自动回报的消息类型
        - "D"  # NewOrderSingle
```

## 六、测试策略

不需要进行测试

## 七、部署考虑

- 自动回报功能默认关闭，可通过配置启用
- 提供开关控制，允许在运行时开启/关闭此功能
- 确保向后兼容性，不影响现有功能

## 八、风险与缓解

- **性能风险**：内部系统不需要考虑性能问题
- **兼容性风险**：不需要考虑，由用户来保证

## 九、重点

- 是否还有歧义，如果有歧义不得进入下一步
- 没有歧义，严格按照技术文档进行实现，不得引入新框架，不能修改原有设计