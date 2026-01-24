# FIX Server Simulator

FIX Server Simulator是一个用于模拟FIX协议服务器行为的应用程序，支持FIX 4.2协议版本的消息处理和会话管理。

## 核心功能

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

### 4. REST API接口
- 提供HTTP接口用于会话管理和监控
- 健康检查端点
- 实时状态查询

## 应用启动

### 环境要求
- Java 8或更高版本
- Maven 3.x

### 启动步骤

1. 克隆项目代码
```bash
git clone <repository-url>
```

2. 进入项目目录
```bash
cd fix-all
```

3. 编译项目
```bash
mvn clean compile
```

4. 打包项目
```bash
mvn package
```

5. 启动应用
```bash
java -jar target/fix-simulator.jar
```

或者直接使用Maven运行：
```bash
mvn spring-boot:run
```

### 默认配置
- 应用端口：8192
- FIX服务器端口：随机会话端口
- 默认会话配置：FIX.4.2协议

## Gateway接入方式

### 1. FIX协议连接
Gateway通过标准FIX 4.2协议与Simulator建立连接：

- 配置FIX客户端连接参数
- 设置TargetCompID和SenderCompID
- 使用TCP/IP连接到Simulator服务器端口

### 2. REST API集成
Simulator提供REST API用于会话管理和监控：

#### 健康检查
```
GET http://localhost:8192/api/health
```

#### 查询会话列表
```
GET http://localhost:8192/api/sessions
```

#### 查询特定会话状态
```
GET http://localhost:8192/api/sessions/{sessionId}
```

### 3. 消息交互流程
1. Gateway发送New Order Single (MsgType=D)消息
2. Simulator根据委托数量自动触发相应回报序列：
   - 委托数量100：返回NEW状态执行报告
   - 委托数量200：返回NEW + 部分成交100股执行报告
   - 委托数量300：返回NEW + 部分成交100股 + 部分成交200股 + 完全成交300股执行报告
   - 委托数量400：返回拒绝执行报告
3. Gateway接收并处理执行报告消息

### 4. 配置示例
在Gateway的FIX配置文件中添加Simulator作为FIX服务器：
```
[SESSION]
BeginString=FIX.4.2
SenderCompID=YOUR_GATEWAY_ID
TargetCompID=SIMULATOR_ID
SocketConnectHost=localhost
SocketConnectPort=[Simulator分配的端口]
HeartBtInt=30
```

## API参考

### 会话管理API
- `GET /api/sessions` - 获取所有会话信息
- `GET /api/sessions/{sessionId}` - 获取特定会话详情
- `GET /api/health` - 健康检查

### 返回格式
API返回JSON格式数据，包含会话状态、连接信息等。

## 注意事项
- 确保防火墙允许相应端口通信
- 检查FIX协议版本兼容性
- 监控应用日志以排查连接问题