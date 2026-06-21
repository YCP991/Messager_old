# 数据库优化报告

## 一、优化概述

本次对数据库初始化脚本 `init.sql` 进行了全面优化，版本从 v2.0 升级到 v3.0。优化主要集中在以下几个方面：

| 优化类别 | 优化内容 | 影响范围 |
| :--- | :--- | :--- |
| 新增字段 | 用户表在线状态、好友表免打扰、群成员表邀请人等 | 前后端实体类、接口、UI |
| 索引优化 | 为逻辑删除字段、免打扰字段等添加索引 | 查询性能提升 |
| 表结构扩展 | 新增会话表 `chat_session` | 消息会话管理 |
| 数据完整性 | 完善唯一约束、外键关系 | 数据一致性 |

---

## 二、具体优化内容

### 2.1 用户表 (user)

**新增字段：**

| 字段名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `is_online` | tinyint | 0 | 在线状态：0-离线，1-在线 |
| `last_online_time` | datetime | NULL | 最后在线时间 |

**新增索引：**

| 索引名 | 字段 | 说明 |
| :--- | :--- | :--- |
| `idx_is_online` | is_online | 在线状态索引 |

---

### 2.2 好友关系表 (friend)

**新增字段：**

| 字段名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `is_muted` | tinyint | 0 | 是否免打扰：0-否，1-是 |

**新增索引：**

| 索引名 | 字段 | 说明 |
| :--- | :--- | :--- |
| `idx_user_muted` | user_id, is_muted | 用户免打扰查询索引 |

---

### 2.3 群成员表 (group_member)

**新增字段：**

| 字段名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `inviter_id` | bigint | NULL | 邀请人ID（NULL表示自己加入） |
| `is_muted` | tinyint | 0 | 是否免打扰：0-否，1-是 |

**新增索引：**

| 索引名 | 字段 | 说明 |
| :--- | :--- | :--- |
| `idx_inviter_id` | inviter_id | 邀请人索引 |
| `idx_user_muted` | user_id, is_muted | 用户免打扰索引 |

---

### 2.4 消息表 (im_message)

**新增字段：**

| 字段名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `msg_status` | tinyint | 0 | 消息状态：0-发送中，1-已发送，2-已送达，3-已读，4-发送失败 |

**新增索引：**

| 索引名 | 字段 | 说明 |
| :--- | :--- | :--- |
| `idx_msg_status` | msg_status | 消息状态索引 |

---

### 2.5 群组表 (im_group)

**新增字段：**

| 字段名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `description` | text | NULL | 群描述 |

---

### 2.6 好友请求表 (friend_request)

**新增字段：**

| 字段名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `expires_time` | datetime | NULL | 过期时间（默认为发送后7天） |

---

### 2.7 新增会话表 (chat_session)

用于管理用户会话列表，优化会话查询性能。

| 字段名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | bigint | - | 主键ID（自增） |
| `user_id` | bigint | - | 用户ID |
| `chat_id` | varchar(64) | - | 会话ID |
| `chat_type` | tinyint | - | 会话类型：0-私聊，1-群聊，2-AI |
| `target_id` | bigint | - | 目标ID（好友ID或群ID） |
| `unread_count` | int | 0 | 未读消息数 |
| `last_message_id` | bigint | NULL | 最后一条消息ID |
| `last_message` | text | NULL | 最后一条消息内容（预览） |
| `is_pinned` | tinyint | 0 | 是否置顶 |
| `is_muted` | tinyint | 0 | 是否免打扰 |
| `sort_order` | double | 0 | 排序权重 |
| `deleted` | tinyint | 0 | 逻辑删除 |
| `create_time` | datetime | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | CURRENT_TIMESTAMP | 更新时间 |

---

## 三、后端实体类变更

### 3.1 User.java

新增字段：
- `isOnline` (Integer) - 在线状态
- `lastOnlineTime` (LocalDateTime) - 最后在线时间
- `deleted` (Integer) - 逻辑删除

### 3.2 Friend.java

新增字段：
- `isMuted` (Integer) - 是否免打扰
- `deleted` (Integer) - 逻辑删除

### 3.3 GroupMember.java

新增字段：
- `inviterId` (Long) - 邀请人ID
- `isMuted` (Integer) - 是否免打扰
- `deleted` (Integer) - 逻辑删除

### 3.4 Message.java

新增字段：
- `msgStatus` (Integer) - 消息状态
- `deleted` (Integer) - 逻辑删除

### 3.5 Group.java

新增字段：
- `description` (String) - 群描述
- `deleted` (Integer) - 逻辑删除

### 3.6 FriendRequest.java

新增字段：
- `expiresTime` (LocalDateTime) - 过期时间

---

## 四、前端类型定义变更

### 4.1 auth.ts

**UserInfo 接口新增：**
- `isOnline` (number) - 在线状态
- `lastOnlineTime` (string) - 最后在线时间

### 4.2 friend.ts

**FriendVO 接口新增：**
- `isMuted` (number) - 是否免打扰

### 4.3 group.ts

**GroupMemberVO 接口新增：**
- `inviterId` (number) - 邀请人ID
- `inviterName` (string) - 邀请人姓名
- `isSelfMuted` (boolean) - 是否设置了免打扰

### 4.4 message.ts

**新增枚举：**
```typescript
enum MessageStatus {
  SENDING = 0,   // 发送中
  SENT = 1,      // 已发送
  DELIVERED = 2, // 已送达
  READ = 3,      // 已读
  FAILED = 4     // 发送失败
}
```

**MessageVO 接口新增：**
- `msgStatus` (MessageStatus) - 消息状态

---

## 五、优化效果分析

### 5.1 功能增强

| 新增功能 | 说明 |
| :--- | :--- |
| 在线状态显示 | 支持显示好友在线/离线状态 |
| 消息状态追踪 | 支持显示消息发送中/已发送/已送达/已读状态 |
| 免打扰功能 | 支持好友和群聊的免打扰设置 |
| 邀请人记录 | 记录群成员邀请人信息 |
| 会话管理 | 新增会话表优化会话列表查询 |

### 5.2 性能优化

| 优化项 | 预期效果 |
| :--- | :--- |
| 在线状态索引 | 快速查询在线用户 |
| 免打扰索引 | 快速筛选免打扰会话 |
| 消息状态索引 | 快速统计消息状态 |
| 会话表 | 减少消息表查询压力 |

### 5.3 数据完整性

| 优化项 | 说明 |
| :--- | :--- |
| 逻辑删除字段 | 统一所有表的逻辑删除字段 |
| 唯一约束增强 | 包含删除状态的唯一约束 |
| 外键关系 | 完善表间关联关系 |

---

## 六、迁移注意事项

### 6.1 数据库迁移

如需从旧版本升级，执行以下 SQL：

```sql
-- 用户表新增字段
ALTER TABLE `user` ADD COLUMN `is_online` tinyint NOT NULL DEFAULT 0 AFTER `status`;
ALTER TABLE `user` ADD COLUMN `last_online_time` datetime DEFAULT NULL AFTER `is_online`;
ALTER TABLE `user` ADD COLUMN `deleted` tinyint NOT NULL DEFAULT 0 AFTER `update_time`;
ALTER TABLE `user` ADD KEY `idx_is_online` (`is_online`);

-- 好友表新增字段
ALTER TABLE `friend` ADD COLUMN `is_muted` tinyint NOT NULL DEFAULT 0 AFTER `is_pinned`;
ALTER TABLE `friend` ADD COLUMN `deleted` tinyint NOT NULL DEFAULT 0 AFTER `update_time`;
ALTER TABLE `friend` ADD KEY `idx_user_muted` (`user_id`, `is_muted`);

-- 群成员表新增字段
ALTER TABLE `group_member` ADD COLUMN `inviter_id` bigint DEFAULT NULL AFTER `user_id`;
ALTER TABLE `group_member` ADD COLUMN `is_muted` tinyint NOT NULL DEFAULT 0 AFTER `is_pinned`;
ALTER TABLE `group_member` ADD COLUMN `deleted` tinyint NOT NULL DEFAULT 0 AFTER `quit_time`;
ALTER TABLE `group_member` ADD KEY `idx_inviter_id` (`inviter_id`);
ALTER TABLE `group_member` ADD KEY `idx_user_muted` (`user_id`, `is_muted`);

-- 消息表新增字段
ALTER TABLE `im_message` ADD COLUMN `msg_status` tinyint NOT NULL DEFAULT 0 AFTER `msg_type`;
ALTER TABLE `im_message` ADD COLUMN `deleted` tinyint NOT NULL DEFAULT 0 AFTER `update_time`;
ALTER TABLE `im_message` ADD KEY `idx_msg_status` (`msg_status`);

-- 群组表新增字段
ALTER TABLE `im_group` ADD COLUMN `description` text AFTER `announcement`;
ALTER TABLE `im_group` ADD COLUMN `deleted` tinyint NOT NULL DEFAULT 0 AFTER `update_time`;

-- 好友请求表新增字段
ALTER TABLE `friend_request` ADD COLUMN `expires_time` datetime DEFAULT NULL AFTER `remark`;

-- 创建会话表
CREATE TABLE `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `chat_id` varchar(64) NOT NULL,
  `chat_type` tinyint NOT NULL,
  `target_id` bigint NOT NULL,
  `unread_count` int NOT NULL DEFAULT 0,
  `last_message_id` bigint DEFAULT NULL,
  `last_message` text,
  `is_pinned` tinyint NOT NULL DEFAULT 0,
  `is_muted` tinyint NOT NULL DEFAULT 0,
  `sort_order` double NOT NULL DEFAULT 0,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_chat` (`user_id`, `chat_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_chat_id` (`chat_id`),
  KEY `idx_user_pinned` (`user_id`, `is_pinned`),
  KEY `idx_user_muted` (`user_id`, `is_muted`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 6.2 代码迁移

1. 更新后端实体类，添加新增字段
2. 更新前端类型定义，添加新增字段和枚举
3. 更新相关 Service/Controller 层代码
4. 更新前端组件以使用新字段

---

## 七、总结

本次数据库优化主要实现了以下目标：

1. **功能完善**：新增在线状态、消息状态、免打扰等功能支持
2. **性能提升**：添加必要索引，优化查询性能
3. **架构扩展**：新增会话表，优化会话管理
4. **数据一致性**：统一逻辑删除字段，完善约束

建议在部署前进行充分测试，确保数据迁移和代码变更的正确性。

---

**文档版本**: v1.0  
**生成时间**: 2024年  
**作者**: MaiSiZhe Team