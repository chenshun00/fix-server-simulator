# FIX Server Simulator 项目问题总结

## 问题1: AutoResponseService 回报报文缺少ExecTransType字段
- **问题描述**: AutoResponseService中生成的所有回报报文都缺少ExecTransType字段
- **解决方案**: 为所有回报报文添加ExecTransType字段，值设置为'0'（NEW）
- **涉及文件**: `/src/main/java/com/fixsimulator/service/AutoResponseService.java`

## 问题2: 前端"发送回报"按钮无响应
- **问题1**: 前后端API端点不匹配，前端调用`/api/response/*`，后端提供`/api/responses/*`
- **问题2**: AngularJS作用域问题，回报模态框不在MessageQueryController作用域内
- **解决方案1**: 将前端API调用路径从`/api/response`改为`/api/responses`
- **解决方案2**: 将回报模态框移至message-query视图内部，使用Angular的`ng-if`控制显示
- **涉及文件**: 
  - `/src/main/resources/static/js/services/reply.service.js`
  - `/src/main/resources/static/index.html`
  - `/src/main/resources/static/js/controllers/message-query.controller.js`

## 问题3: API接口返回数据不完整
- **问题描述**: `/api/messages/search`接口未返回ClOrdId、Symbol等解析字段数据
- **根本原因**: 后端返回的FixMessageEntity对象不包含解析字段信息
- **解决方案**: 创建MessageWithParsedField类，将FixMessageEntity和ParsedFieldEntity结合返回
- **涉及文件**: 
  - `/src/main/java/com/fixsimulator/entity/MessageWithParsedField.java`
  - `/src/main/java/com/fixsimulator/controller/MessageController.java`
  - `/src/main/java/com/fixsimulator/service/FixMessageService.java`

## 问题4: API端点冗余
- **问题描述**: 存在多个相似功能的API端点，如`/api/messages/clordid`、`/api/messages/symbol`等
- **解决方案**: 将多个端点整合为统一的`/api/messages`端点，通过参数区分查询方式
- **涉及文件**: `/src/main/java/com/fixsimulator/controller/MessageController.java`

## 问题5: 前后端数据显示不一致
- **问题描述**: 前端表格显示字段与后端API返回字段不匹配
- **解决方案**: 调整前端模板，使用正确的字段路径显示数据
- **涉及文件**: `/src/main/resources/static/index.html`

## 问题6: 需求变更 - 增加更多显示字段并过滤OUTBOUND数据
- **问题1**: 需要在前端表格中增加OrigClOrdId、OrderQty、Price、Side字段
- **问题2**: Side字段需要转换为人类可读格式（1=买，2=卖）
- **问题3**: 只显示INBOUND数据，不显示和记录OUTBOUND数据
- **解决方案**: 
  - 修改前端表格显示新字段
  - 添加Side字段格式化显示
  - 在保存和查询时过滤OUTBOUND数据
- **涉及文件**: 
  - `/src/main/resources/static/index.html`
  - `/src/main/java/com/fixsimulator/controller/MessageController.java`
  - `/src/main/java/com/fixsimulator/service/FixMessageService.java`

## 问题7: 查询方法优化
- **问题描述**: FixMessageService中存在多个独立的查询方法，代码冗余
- **解决方案**: 创建统一的QueryCriteria类和queryParsedMessages方法，通过参数组合实现不同查询
- **涉及文件**: 
  - `/src/main/java/com/fixsimulator/service/QueryCriteria.java`
  - `/src/main/java/com/fixsimulator/service/FixMessageService.java`

## 问题8: JOIN操作优化
- **问题描述**: 查询中使用JOIN操作可能影响性能
- **解决方案**: 优化查询逻辑，使用子查询而非JOIN操作
- **涉及文件**: `/src/main/java/com/fixsimulator/service/FixMessageService.java`

## 问题9: 未使用方法清理
- **问题描述**: FixMessageService中存在未使用的方法
- **解决方案**: 移除未使用的方法，精简代码
- **涉及文件**: `/src/main/java/com/fixsimulator/service/FixMessageService.java`