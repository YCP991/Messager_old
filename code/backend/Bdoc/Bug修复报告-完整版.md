# Bug修复报告（完整版）

## 修复日期
2026-06-20

## 修复人员
AI Assistant

---

## 一、数据库与实体类映射问题

### Bug 1: 群组加载失败 - "系统繁忙，请稍后重试"

| 属性 | 详情 |
|------|------|
| **问题现象** | 前端调用获取群组列表接口时返回错误提示"系统繁忙，请稍后重试" |
| **根本原因** | 数据库表字段名与Java实体类字段名不匹配，MyBatis无法正确映射查询结果 |
| **严重级别** | 🔴 严重 |
| **影响范围** | Group.java, User.java, Message.java, GroupMember.java |

#### 字段映射问题详情

**Group实体类：**
| 数据库字段 | Java原字段 | 问题 | 修复方案 |
|-----------|----------|------|---------|
| `name` | `groupName` | ❌ 无法自动映射 | `@TableField("name")` |
| `type` | `groupType` | ❌ 无法自动映射 | `@TableField("type")` |
| `owner_id` | `creatorId` | ❌ 无法自动映射 | `@TableField("owner_id")` |
| `avatar` | `groupAvatar` | ❌ 无法自动映射 | `@TableField("avatar")` |
| `is_disbanded` | 缺失字段 | ❌ 实体类缺少字段 | 新增 `isDisbanded` |
| `disband_time` | 缺失字段 | ❌ 实体类缺少字段 | 新增 `disbandTime` |

**Message实体类：**
| 数据库字段 | Java原字段 | 问题 | 修复方案 |
|-----------|----------|------|---------|
| `update_time` | 缺失字段 | ❌ 实体类缺少字段 | 新增 `updateTime` |

**GroupMember实体类：**
| 数据库字段 | Java原字段 | 问题 | 修复方案 |
|-----------|----------|------|---------|
| `last_read_seq_id` | `lastReadSeqId` | ❌ 数据库不存在 | 移除该字段 |
| `is_quit` | `isQuit` | ❌ 数据库不存在 | 移除，改用 `quitTime` |
| `group_nickname` | 缺失字段 | ❌ 实体类缺少字段 | 新增 `groupNickname` |
| `is_pinned` | 缺失字段 | ❌ 实体类缺少字段 | 新增 `isPinned` |
| `sort_order` | 缺失字段 | ❌ 实体类缺少字段 | 新增 `sortOrder` |
| `mute_until` | 缺失字段 | ❌ 实体类缺少字段 | 新增 `muteUntil` |

---

### Bug 2: GroupMember主键类型错误

| 属性 | 详情 |
|------|------|
| **问题现象** | 主键使用雪花算法，但数据库设计为自增 |
| **根本原因** | `@TableId(type = IdType.ASSIGN_ID)` 与数据库AUTO不一致 |
| **严重级别** | 🟡 中等 |
| **影响范围** | GroupMember.java |

**修复方案：** 改为 `@TableId(type = IdType.AUTO)` 自增类型

---

### Bug 3: isQuit字段逻辑转换

| 属性 | 详情 |
|------|------|
| **问题现象** | SQL查询使用不存在的字段 `is_quit` 和 `last_read_seq_id` |
| **根本原因** | 实体类字段与数据库不匹配 |
| **严重级别** | 🔴 严重 |
| **影响范围** | GroupServiceImpl.java |

**修复方案：** 将 `isQuit` 逻辑改为 `quitTime` 判断：
- `isQuit = 0` → `quitTime == null`（未退群）
- `isQuit = 1` → `quitTime != null`（已退群）
- `setIsQuit(0)` → `setQuitTime(null)`
- `setIsQuit(1)` → `setQuitTime(LocalDateTime.now())`

---

## 二、WebSocket依赖注入问题

### Bug 4: WebSocket连接不稳定

| 属性 | 详情 |
|------|------|
| **问题现象** | WebSocket连接成功后立即断开，反复重连 |
| **根本原因** | Spring Boot 3.x中 `@ServerEndpoint` 类无法使用 `@Autowired` 直接注入Bean |
| **严重级别** | 🔴 严重 |
| **影响范围** | WebSocketHandler.java |

#### 依赖注入失败详情

| Bean | 问题 | 影响 |
|------|------|------|
| `JwtUtil` | null | Token验证失败，连接被拒绝 |
| `UserSessionManager` | null | 会话管理失败，无法保存/移除会话 |
| `AiWorkflowService` | null | AI消息处理失败 |
| `MessageService` | null | 普通消息无法持久化 |

**错误日志：**
```
java.lang.NullPointerException: Cannot invoke "com.maisizhe.security.jwt.JwtUtil.validateToken(String)" because "this.jwtUtil" is null
```

**修复方案：** 使用静态ApplicationContext + Setter注入，在方法中手动获取Bean

```java
private static ApplicationContext applicationContext;

@Autowired
public void setApplicationContext(ApplicationContext context) {
    WebSocketHandler.applicationContext = context;
}

private JwtUtil getJwtUtil() {
    return applicationContext.getBean(JwtUtil.class);
}
```

---

### Bug 5: WebSocket配置类类型不匹配

| 属性 | 详情 |
|------|------|
| **问题现象** | `addHandler(WebSocketHandler, String)` 方法参数类型不匹配 |
| **根本原因** | Spring WebSocket和Jakarta WebSocket是两种不同的实现方式 |
| **严重级别** | 🟡 中等 |
| **影响范围** | WebSocketConfig.java |

**问题说明：**
- `WebSocketHandlerRegistry.addHandler()` 需要 Spring WebSocket 的 `WebSocketHandler`
- 我们的 `WebSocketHandler` 使用 Jakarta WebSocket 的 `@ServerEndpoint` 注解

**修复方案：** 移除 `WebSocketConfigurer` 实现，只保留 `ServerEndpointExporter`

---

## 三、安全漏洞问题

### Bug 6: 群聊消息广播给所有用户（安全漏洞）

| 属性 | 详情 |
|------|------|
| **问题现象** | 群聊消息会广播给所有在线用户，而非仅群组成员 |
| **根本原因** | `pushGroupMessage` 方法使用 `broadcastMessage` 而非定向推送 |
| **严重级别** | 🔴 严重 - 安全漏洞 |
| **影响范围** | MessageServiceImpl.java |

**问题代码：**
```java
private void pushGroupMessage(Message message) {
    // TODO: 从群组服务获取所有成员ID
    // 这里先简化为广播给所有在线用户
    webSocketHandler.broadcastMessage(wsMessage);  // ⚠️ 安全漏洞！
}
```

**修复方案：** 获取群组成员列表，只向成员推送消息

```java
private void pushGroupMessage(Message message, Long groupId) {
    List<GroupMember> members = groupMemberMapper.selectList(...);
    for (GroupMember member : members) {
        webSocketHandler.sendMessageToUser(member.getUserId(), wsMessage);
    }
}
```

---

### Bug 7: 群聊消息缺少发送者权限验证

| 属性 | 详情 |
|------|------|
| **问题现象** | 任何用户都可以向任意群组发送消息 |
| **根本原因** | `sendGroupMessage` 方法未验证群组成员身份 |
| **严重级别** | 🔴 严重 - 功能缺陷 |
| **影响范围** | MessageServiceImpl.java |

**修复方案：** 添加以下验证：
1. 群组是否存在
2. 群组是否已解散
3. 发送者是否是群组成员
4. 发送者是否被禁言

---

### Bug 8: WebSocket普通消息未持久化

| 属性 | 详情 |
|------|------|
| **问题现象** | 通过WebSocket发送的普通群聊消息不会被保存到数据库 |
| **根本原因** | `handleGroupMessage` 方法中普通消息的保存逻辑未实现 |
| **严重级别** | 🔴 严重 - 功能缺陷 |
| **影响范围** | WebSocketHandler.java |

**问题代码：**
```java
if (content != null && content.contains("@AI")) {
    aiWorkflowService.handleAtAiMessage(...);
} else {
    log.info("普通群聊消息...");
    // TODO: 调用消息服务保存和推送  ❌ 未实现！
}
```

**修复方案：** 调用 `MessageService.sendGroupMessage` 保存消息

---

## 四、功能缺失问题

### Bug 9: 群组解散功能缺失

| 属性 | 详情 |
|------|------|
| **问题现象** | Group实体有解散字段，但服务层没有解散方法 |
| **根本原因** | 功能未实现 |
| **严重级别** | 🟡 中等 |
| **影响范围** | GroupService.java, GroupServiceImpl.java, GroupController.java |

**修复方案：** 新增 `disbandGroup` 方法

---

### Bug 10: 群主转让功能缺失

| 属性 | 详情 |
|------|------|
| **问题现象** | `quitGroup` 提示群主需要先转让，但转让功能未实现 |
| **根本原因** | 功能未实现 |
| **严重级别** | 🟡 中等 |
| **影响范围** | GroupService.java, GroupServiceImpl.java, GroupController.java |

**修复方案：** 新增 `transferOwnership` 方法

---

### Bug 11: 成员禁言功能缺失

| 属性 | 详情 |
|------|------|
| **问题现象** | GroupMember实体有禁言字段，但服务层没有禁言方法 |
| **根本原因** | 功能未实现 |
| **严重级别** | 🟡 中等 |
| **影响范围** | GroupService.java, GroupServiceImpl.java, GroupController.java |

**修复方案：** 新增 `muteMember` 和 `unmuteMember` 方法

---

## 五、轻微问题

### Bug 12: 消息发送者不存在时的处理

| 属性 | 详情 |
|------|------|
| **问题现象** | 用户不存在时返回空的用户信息 |
| **根本原因** | 缺少空值判断 |
| **严重级别** | 🔵 轻微 |
| **影响范围** | MessageServiceImpl.java |

**修复方案：** 添加默认值处理
- AI机器人 (fromUid=0) → 显示 "AI助手"
- 用户不存在 → 显示 "[已注销用户]"

---

## Bug汇总表

| No. | Bug名称 | 严重级别 | 类型 | 状态 |
|-----|--------|---------|------|------|
| 1 | 数据库-实体字段映射不匹配 | 🔴 严重 | 数据映射 | ✅ 已修复 |
| 2 | GroupMember主键类型错误 | 🟡 中等 | 数据映射 | ✅ 已修复 |
| 3 | isQuit字段逻辑转换 | 🔴 严重 | 数据映射 | ✅ 已修复 |
| 4 | WebSocket依赖注入失败 | 🔴 严重 | 依赖注入 | ✅ 已修复 |
| 5 | WebSocket配置类型不匹配 | 🟡 中等 | 配置错误 | ✅ 已修复 |
| 6 | 群聊消息广播安全漏洞 | 🔴 严重 | 安全漏洞 | ✅ 已修复 |
| 7 | 群聊消息权限验证缺失 | 🔴 严重 | 功能缺陷 | ✅ 已修复 |
| 8 | WebSocket消息未持久化 | 🔴 严重 | 功能缺陷 | ✅ 已修复 |
| 9 | 群组解散功能缺失 | 🟡 中等 | 功能缺失 | ✅ 已添加 |
| 10 | 群主转让功能缺失 | 🟡 中等 | 功能缺失 | ✅ 已添加 |
| 11 | 成员禁言功能缺失 | 🟡 中等 | 功能缺失 | ✅ 已添加 |
| 12 | 消息发送者空值处理 | 🔵 轻微 | 边界处理 | ✅ 已优化 |

---

## 修复统计

| 类别 | 数量 |
|------|------|
| 🔴 严重问题 | 7 |
| 🟡 中等问题 | 4 |
| 🔵 轻微问题 | 1 |
| **总计** | **12** |

---

## 修复文件清单

| 文件 | 修复内容 |
|------|---------|
| [Group.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\group\entity\Group.java) | 添加@TableField注解 + 新增解散字段 |
| [User.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\user\entity\User.java) | 添加@TableField注解 |
| [Message.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\message\entity\Message.java) | 添加@TableField注解 + 新增updateTime |
| [GroupMember.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\group\entity\GroupMember.java) | 字段调整 + 主键类型修复 |
| [GroupServiceImpl.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\group\service\impl\GroupServiceImpl.java) | isQuit→quitTime + 新增功能 |
| [GroupService.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\group\service\GroupService.java) | 新增接口方法 |
| [GroupController.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\group\controller\GroupController.java) | 新增API接口 |
| [MessageServiceImpl.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\modules\message\service\impl\MessageServiceImpl.java) | 安全修复 + 权限验证 |
| [WebSocketHandler.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\websocket\handler\WebSocketHandler.java) | 依赖注入 + 消息持久化 |
| [WebSocketConfig.java](file:///c:\Users\Champion Young\Desktop\课设\Messager\Messager\code\backend\src\main\java\com\maisizhe\config\WebSocketConfig.java) | 配置修复 |

---

## 新增API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/group/{groupId}/disband` | POST | 解散群组（仅群主） |
| `/api/group/{groupId}/transfer` | POST | 转让群主身份 |
| `/api/group/{groupId}/mute` | POST | 禁言成员 |
| `/api/group/{groupId}/unmute` | POST | 解除禁言 |

---

## 测试验证

### 单元测试结果
```
Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 编译验证
```
mvn compile
BUILD SUCCESS
```

---

## 经验教训

### 1. 数据库与实体类一致性
- 必须使用 `@TableField` 注解显式指定字段映射
- 实体类字段必须与数据库表完全一致
- 不能有数据库不存在的字段，也不能缺少数据库存在的字段

### 2. WebSocket依赖注入
- Spring Boot 3.x中 `@ServerEndpoint` 类无法使用 `@Autowired`
- 必须通过 ApplicationContext 手动获取 Bean
- 使用 ObjectProvider 可以避免循环依赖

### 3. 安全验证
- 群聊消息必须验证发送者权限
- 消息推送必须定向发送，不能广播
- 所有操作前必须验证资源状态（如群组是否解散）

### 4. 功能完整性
- 实体类字段必须有对应的服务方法
- TODO注释必须及时实现，不能遗留
- 功能设计要考虑边界情况（如群主退群需要转让）

---

**修复完成日期**：2026-06-20  
**修复版本**：v1.0.0-complete  
**状态**：✅ 已完成并验证通过