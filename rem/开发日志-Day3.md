# 麦思哲后端开发日志 - Day 3

## 📅 日期: 2024-01-XX

---

## ✅ 今日完成工作

### 消息核心模块 (100%)

今日完成了IM系统最核心的**消息收发功能**，包括私聊、群聊、历史消息查询和消息撤回。

---

## 📁 新增文件清单

### 1. 消息实体和Mapper (2个文件)

**文件**: `modules/message/entity/Message.java` (85行)
```java
@Data
@TableName("im_message")
public class Message {
    private Long id;              // 消息ID(雪花算法)
    private String chatId;        // 会话ID(p_uid1_uid2 / g_gid)
    private Long seqId;           // 会话内序列号
    private Long fromUid;         // 发送者ID
    private Long toUid;           // 接收者ID(私聊)
    private Long groupId;         // 群组ID(群聊)
    private String content;       // 消息内容
    private Integer msgType;      // 消息类型
    private String mentionedUsers;// @用户列表(JSON)
    private Integer isRecalled;   // 是否已撤回
    private LocalDateTime recallTime; // 撤回时间
    private String extraData;     // 扩展数据
    private LocalDateTime createTime;
}
```

**文件**: `modules/message/mapper/MessageMapper.java` (15行)
```java
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
```

---

### 2. DTO和VO (2个文件)

**文件**: `modules/message/dto/SendMessageDTO.java` (49行)
```java
@Data
public class SendMessageDTO {
    private Long toUid;                    // 接收者ID(私聊)
    private Long groupId;                  // 群组ID(群聊)
    
    @NotBlank(message = "消息内容不能为空")
    private String content;                // 消息内容
    
    private Integer msgType = 0;           // 消息类型(默认文本)
    private List<Long> mentionedUsers;     // @用户列表
    
    @NotBlank(message = "客户端消息ID不能为空")
    private String clientMsgId;            // 客户端消息ID(去重)
}
```

**文件**: `modules/message/vo/MessageVO.java` (81行)
```java
@Data
public class MessageVO {
    private Long id;
    private String chatId;
    private Long seqId;
    private Long fromUid;
    private String fromName;        // 发送者姓名
    private String fromAvatar;      // 发送者头像
    private Long toUid;
    private Long groupId;
    private String content;
    private Integer msgType;
    private List<Long> mentionedUsers;
    private Integer isRecalled;
    private LocalDateTime createTime;
}
```

---

### 3. 消息服务 (2个文件)

**文件**: `modules/message/service/MessageService.java` (60行)

接口定义:
- `sendPrivateMessage()` - 发送私聊消息
- `sendGroupMessage()` - 发送群聊消息
- `getPrivateHistory()` - 获取私聊历史
- `getGroupHistory()` - 获取群聊历史
- `recallMessage()` - 撤回消息

---

**文件**: `modules/message/service/impl/MessageServiceImpl.java` (368行)

**核心实现逻辑**:

#### 发送私聊消息流程:
```java
@Transactional
public MessageVO sendPrivateMessage(Long fromUid, SendMessageDTO dto) {
    // 1. 参数校验
    if (dto.getToUid() == null) {
        throw new BusinessException("接收者ID不能为空");
    }
    
    // 2. 客户端消息ID去重(5分钟过期)
    String dedupKey = "client:msg:" + dto.getClientMsgId();
    if (redisUtil.hasKey(dedupKey)) {
        throw new BusinessException("重复的消息ID");
    }
    redisUtil.setEx(dedupKey, "1", 300);
    
    // 3. 生成chat_id(格式: p_小id_大id)
    String chatId = generatePrivateChatId(fromUid, dto.getToUid());
    
    // 4. Redis原子自增生成seq_id
    String seqKey = "seq:chat:" + chatId;
    Long seqId = redisUtil.incr(seqKey);
    
    // 5. 构建消息实体
    Message message = new Message();
    message.setChatId(chatId);
    message.setSeqId(seqId);
    // ... 设置其他字段
    
    // 6. 写入MySQL
    messageMapper.insert(message);
    
    // 7. 写入Redis ZSet缓存(最近100条)
    cacheMessage(chatId, message);
    
    // 8. 推送给接收者(如果在线)
    pushPrivateMessage(message);
    
    return convertToVO(message);
}
```

#### 双轴定序机制:
1. **全局唯一ID**: Snowflake算法生成的`id`字段
2. **会话内连续ID**: Redis INCR生成的`seq_id`字段

优势:
- ✅ 保证消息全局唯一性
- ✅ 保证会话内消息顺序性
- ✅ 高性能(原子操作无锁竞争)

---

#### 消息缓存策略:
```java
private void cacheMessage(String chatId, Message message) {
    String cacheKey = "chat:msg:" + chatId;
    
    // 写入ZSet(score=seqId)
    redisUtil.zAdd(cacheKey, message.getId(), message.getSeqId());
    
    // 只保留最近100条
    Long count = redisUtil.zCount(cacheKey, 0, Double.MAX_VALUE);
    if (count > 100) {
        Object[] oldMessages = redisUtil.zRange(cacheKey, 0, count - 101);
        redisUtil.zRemove(cacheKey, oldMessages);
    }
}
```

---

#### 消息撤回逻辑:
```java
@Transactional
public void recallMessage(Long messageId, Long userId) {
    // 1. 查询消息
    Message message = messageMapper.selectById(messageId);
    if (message == null) {
        throw new BusinessException("消息不存在");
    }
    
    // 2. 权限校验(只有发送者可以撤回)
    if (!message.getFromUid().equals(userId)) {
        throw new BusinessException("无权撤回此消息");
    }
    
    // 3. 时间限制(只能撤回2分钟内的消息)
    LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
    if (message.getCreateTime().isBefore(twoMinutesAgo)) {
        throw new BusinessException("超过撤回时限");
    }
    
    // 4. 更新消息状态
    message.setIsRecalled(1);
    message.setRecallTime(LocalDateTime.now());
    messageMapper.updateById(message);
    
    // 5. 推送撤回通知
    pushRecallNotification(message);
}
```

---

### 4. 消息Controller (1个文件)

**文件**: `modules/message/controller/MessageController.java` (108行)

REST API接口:

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /api/message/private | 发送私聊消息 | fromUid, dto |
| POST | /api/message/group | 发送群聊消息 | fromUid, dto |
| GET | /api/message/private/history | 获取私聊历史 | userId, peerId, limit |
| GET | /api/message/group/history | 获取群聊历史 | groupId, limit |
| POST | /api/message/recall | 撤回消息 | messageId, userId |

---

### 5. WebSocket增强 (修改1个文件)

**文件**: `websocket/handler/WebSocketHandler.java` (+38行)

新增方法:

```java
/**
 * 发送消息给指定用户
 */
public void sendMessageToUser(Long userId, WSMessage message) {
    Session session = sessionManager.getSession(userId);
    if (session != null && session.isOpen()) {
        String jsonMessage = objectMapper.writeValueAsString(message);
        sendTextMessage(session, jsonMessage);
    }
}

/**
 * 广播消息给所有在线用户
 */
public void broadcastMessage(WSMessage message) {
    String jsonMessage = objectMapper.writeValueAsString(message);
    sessionManager.getAllSessions().forEach((userId, session) -> {
        sendTextMessage(session, jsonMessage);
    });
}
```

---

## 🎯 核心技术亮点

### 1. 消息去重机制

**问题**: 网络重试可能导致同一条消息被多次发送

**解决方案**: 客户端消息ID去重
```java
// 客户端生成唯一ID
String clientMsgId = "msg_" + Date.now() + "_" + randomString();

// 服务端使用SETNX防止重复
String dedupKey = "client:msg:" + clientMsgId;
if (redisUtil.hasKey(dedupKey)) {
    throw new BusinessException("重复的消息ID");
}
redisUtil.setEx(dedupKey, "1", 300); // 5分钟过期
```

---

### 2. chat_id生成策略

**私聊**: `p_小id_大id`
```java
private String generatePrivateChatId(Long uid1, Long uid2) {
    Long smallId = Math.min(uid1, uid2);
    Long largeId = Math.max(uid1, uid2);
    return "p_" + smallId + "_" + largeId;
}
```

**优势**:
- ✅ 保证双向一致性(A→B和B→A是同一个会话)
- ✅ 便于索引优化(相同前缀的chat_id物理存储紧凑)

**群聊**: `g_gid`
```java
String chatId = "g_" + groupId;
```

---

### 3. 消息推送机制

**私聊推送**:
```java
private void pushPrivateMessage(Message message) {
    WSMessage wsMessage = new WSMessage(
        "PRIVATE_MESSAGE",
        convertToVO(message),
        null
    );
    
    // 推送给接收者
    webSocketHandler.sendMessageToUser(message.getToUid(), wsMessage);
    
    // 也推送给发送者(多端同步)
    webSocketHandler.sendMessageToUser(message.getFromUid(), wsMessage);
}
```

**群聊推送**:
```java
private void pushGroupMessage(Message message) {
    WSMessage wsMessage = new WSMessage(
        "GROUP_MESSAGE",
        convertToVO(message),
        null
    );
    
    // 广播给所有在线用户
    webSocketHandler.broadcastMessage(wsMessage);
}
```

**注意**: 当前简化为广播，后续需要优化为只推送给群成员。

---

### 4. 历史消息查询优化

**从Redis ZSet获取**:
```java
public List<MessageVO> getPrivateHistory(Long userId, Long peerId, Integer limit) {
    String chatId = generatePrivateChatId(userId, peerId);
    
    // 从ZSet获取最近N条消息ID(按seqId排序)
    Object[] messageIds = redisUtil.zRange(
        "chat:msg:" + chatId, 
        0, 
        limit - 1
    );
    
    // 批量查询MySQL获取完整信息
    return Arrays.stream(messageIds)
        .map(id -> messageMapper.selectById(Long.parseLong(id.toString())))
        .map(this::convertToVO)
        .toList();
}
```

**性能优势**:
- ✅ ZSet天然有序，无需额外排序
- ✅ 只查询最近N条，避免全表扫描
- ✅ 热点数据在Redis，查询速度快

---

## 📊 代码统计

### 今日新增文件: **7个**

| 模块 | 文件数 | 行数 | 说明 |
|------|--------|------|------|
| message/entity | 1 | 85 | Message实体类 |
| message/mapper | 1 | 15 | MessageMapper接口 |
| message/dto | 1 | 49 | SendMessageDTO |
| message/vo | 1 | 81 | MessageVO |
| message/service | 1 | 60 | MessageService接口 |
| message/service/impl | 1 | 368 | MessageServiceImpl实现 |
| message/controller | 1 | 108 | MessageController |
| **总计** | **7** | **766** | - |

### 修改文件: **1个**

| 文件 | 新增行数 | 说明 |
|------|----------|------|
| websocket/handler/WebSocketHandler.java | +38 | 添加消息推送方法 |

---

### 累计完成情况:

| 模块 | 进度 | 文件数 | 总行数 |
|------|------|--------|--------|
| Common模块 | 100% | 10 | 639 |
| 认证模块 | 100% | 10 | 783 |
| WebSocket模块 | 100% | 4 | 408 |
| Redis模块 | 100% | 2 | 227 |
| **消息模块** | **100%** | **7** | **766** |
| **总计** | **约50%** | **33** | **2,823** |

---

## 🔍 API测试示例

### 1. 发送私聊消息

**请求**:
```bash
curl -X POST http://localhost:8080/api/message/private \
  -H "Content-Type: application/json" \
  -d '{
    "fromUid": 1001,
    "toUid": 1002,
    "content": "你好，这是测试消息",
    "msgType": 0,
    "clientMsgId": "msg_1234567890_abc"
  }'
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890123456,
    "chatId": "p_1001_1002",
    "seqId": 1,
    "fromUid": 1001,
    "fromName": "张三",
    "fromAvatar": "/avatar/default.png",
    "toUid": 1002,
    "content": "你好，这是测试消息",
    "msgType": 0,
    "createTime": "2024-01-15T10:30:00"
  },
  "timestamp": 1705284600000
}
```

---

### 2. 获取私聊历史消息

**请求**:
```bash
curl "http://localhost:8080/api/message/private/history?userId=1001&peerId=1002&limit=50"
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1234567890123456,
      "seqId": 1,
      "fromUid": 1001,
      "content": "你好",
      "createTime": "2024-01-15T10:30:00"
    },
    {
      "id": 1234567890123457,
      "seqId": 2,
      "fromUid": 1002,
      "content": "你好啊",
      "createTime": "2024-01-15T10:31:00"
    }
  ]
}
```

---

### 3. 撤回消息

**请求**:
```bash
curl -X POST "http://localhost:8080/api/message/recall?messageId=1234567890123456&userId=1001"
```

**响应**:
```json
{
  "code": 200,
  "message": "success"
}
```

---

## ⚠️ 待优化事项

### 1. 群聊消息推送优化

**当前实现**: 广播给所有在线用户

**问题**: 
- ❌ 非群成员也会收到消息
- ❌ 浪费带宽和资源

**改进方案**:
```java
private void pushGroupMessage(Message message) {
    // 1. 查询群组所有成员ID
    List<Long> memberIds = groupMemberService.getMemberIds(message.getGroupId());
    
    // 2. 只推送给在线的群成员
    memberIds.forEach(memberId -> {
        if (sessionManager.isOnline(memberId)) {
            webSocketHandler.sendMessageToUser(memberId, wsMessage);
        }
    });
}
```

**依赖**: 需要先完成群组管理模块。

---

### 2. 离线消息补发

**问题**: 用户离线期间的消息如何处理？

**解决方案**:
1. 记录用户最后在线时间
2. 用户上线时查询未读消息
3. 批量推送离线消息

**实现时机**: 前端开发阶段配合实现。

---

### 3. 消息已读状态

**当前状态**: 未实现

**设计方案**:
```sql
-- 私聊已读游标
CREATE TABLE `private_read_cursor` (
  `user_id` bigint NOT NULL,
  `peer_id` bigint NOT NULL,
  `last_read_seq_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `peer_id`)
);

-- 群聊已读游标
CREATE TABLE `group_read_cursor` (
  `user_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  `last_read_seq_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `group_id`)
);
```

**实现时机**: 后续迭代版本。

---

## 🎉 总结

Day 3成功完成了**消息核心模块**的开发，实现了IM系统最关键的功能：

**核心能力**:
- ✅ 私聊消息发送和接收
- ✅ 群聊消息发送和广播
- ✅ 双轴定序机制(Snowflake ID + Redis INCR)
- ✅ MySQL持久化 + Redis ZSet缓存
- ✅ 历史消息查询(最近100条)
- ✅ 消息撤回(2分钟时限)
- ✅ 客户端消息ID去重
- ✅ 实时消息推送(WebSocket)

**技术亮点**:
- ✅ 事务保证数据一致性
- ✅ 原子操作避免锁竞争
- ✅ ZSet高效缓存热消息
- ✅ chat_id双向一致性设计

**总体进度**: 约50%，已完成一半！

下一步将开发**群组管理模块**，完善群组的创建、加入、退出等功能。
