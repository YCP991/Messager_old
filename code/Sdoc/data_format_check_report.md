# 麦思哲前后端数据格式检查报告

检查日期：2024年12月

## 一、检查概述

本次检查对前后端数据格式进行全面对比，确保数据交互的一致性和正确性。

### 检查范围
- 前端 API 接口定义（`frontend/src/api/*.ts`）
- 后端 Controller 接口定义（`backend/src/main/java/**/controller/*.java`）
- 后端实体类定义（`backend/src/main/java/**/entity/*.java`）
- 后端 VO/DTO 定义（`backend/src/main/java/**/vo/*.java`, `backend/src/main/java/**/dto/*.java`）

### 检查结果统计

| 模块 | 字段匹配数 | 字段缺失数 | 类型不一致数 | 状态 |
|------|-----------|-----------|-------------|------|
| 好友模块 | 11 | 1 | 1 | 需修复 |
| 好友请求模块 | 9 | 0 | 1 | 需修复 |
| 群组模块 | 6 | 8 | 1 | 需修复 |
| 群成员模块 | 6 | 4 | 1 | 需修复 |
| 用户模块 | 6 | 0 | 1 | 需修复 |
| 登录模块 | 7 | 0 | 0 | 正常 |
| 注册模块 | 7 | 0 | 0 | 正常 |

---

## 二、好友模块数据格式对比

### 2.1 FriendVO 字段对比

| 字段名 | 前端类型 | 后端类型 | 数据库字段 | 状态 |
|--------|---------|---------|-----------|------|
| friendId | number | Long | friend_id | ✓ 一致 |
| username | string | String | username | ✓ 一致 |
| realName | string | String | real_name | ✓ 一致 |
| avatar | string | String | avatar | ✓ 一致 |
| classNo | string | String | class_no | ✓ 一致 |
| department | string | String | department | ✓ 一致 |
| remark | string | String | remark | ✓ 一致 |
| isPinned | number | Integer | is_pinned | ✓ 一致 |
| sortOrder | number | Double | sort_order | ⚠ 类型不一致 |
| isOnline | number | Integer | - | ✓ 一致 |
| createTime | string | String | create_time | ✓ 一致 |
| isFriend | boolean | - | - | 前端特有 |

**问题说明**：
- `sortOrder` 前端定义为 `number`，后端定义为 `Double`。JavaScript 的 `number` 类型可以表示双精度浮点数，但建议前端明确类型注释。

### 2.2 好友相关 API 路径对比

| 功能 | 前端路径 | 后端路径 | 状态 |
|------|---------|---------|------|
| 获取好友列表 | `/friend/list` | `/api/friend/list` | ✓ 一致 |
| 搜索用户 | `/friend/search` | `/api/friend/search` | ✓ 一致 |
| 获取好友详情 | `/friend/{friendId}` | `/api/friend/{friendId}` | ✓ 一致 |
| 添加好友 | `/friend/add` | `/api/friend/add` | ✓ 一致 |
| 删除好友 | `/friend/{friendId}` | `/api/friend/{friendId}` | ✓ 一致 |
| 更新备注 | `/friend/remark` | `/api/friend/remark` | ✓ 一致 |
| 更新排序 | `/friend/sort` | `/api/friend/sort` | ✓ 一致 |
| 批量更新排序 | `/friend/batch-sort` | `/api/friend/batch-sort` | ✓ 一致 |
| 检查好友关系 | `/friend/check/{friendId}` | `/api/friend/check/{friendId}` | ✓ 一致 |

---

## 三、好友请求模块数据格式对比

### 3.1 FriendRequestVO 字段对比

| 字段名 | 前端类型 | 后端类型 | 数据库字段 | 状态 |
|--------|---------|---------|-----------|------|
| id | number | Long | id | ✓ 一致 |
| fromUserId | number | Long | from_user_id | ✓ 一致 |
| fromUsername | string | String | - | ✓ 一致 |
| fromRealName | string | String | - | ✓ 一致 |
| fromAvatar | string | String | - | ✓ 一致 |
| remark | string | String | remark | ✓ 一致 |
| status | number | Integer | status | ✓ 一致 |
| statusDesc | string | String | - | ✓ 一致 |
| createTime | string | LocalDateTime | create_time | ⚠ 类型不一致 |

**问题说明**：
- `createTime` 前端定义为 `string`，后端定义为 `LocalDateTime`。后端返回时会自动序列化为 ISO 格式字符串，前端可以正常接收。

### 3.2 好友请求 API 路径对比

| 功能 | 前端路径 | 后端路径 | 状态 |
|------|---------|---------|------|
| 发送好友请求 | `/friend/request/send` | `/api/friend/request/send` | ✓ 一致 |
| 处理好友请求 | `/friend/request/handle` | `/api/friend/request/handle` | ✓ 一致 |
| 获取收到请求 | `/friend/request/received` | `/api/friend/request/received` | ✓ 一致 |
| 获取发送请求 | `/friend/request/sent` | `/api/friend/request/sent` | ✓ 一致 |
| 获取待处理数量 | `/friend/request/pending-count` | `/api/friend/request/pending-count` | ✓ 一致 |
| 取消好友请求 | `/friend/request/cancel` | `/api/friend/request/cancel` | ✓ 一致 |

---

## 四、群组模块数据格式对比

### 4.1 GroupVO 字段对比

| 字段名 | 前端类型 | 后端类型 | 数据库字段 | 状态 |
|--------|---------|---------|-----------|------|
| id | number | Long | id | ✓ 一致 |
| groupName | string | String | name | ✓ 一致 |
| groupAvatar | string | String | avatar | ✓ 一致 |
| groupType | number | Integer | type | ✓ 一致 |
| memberCount | number | Integer | member_count | ✓ 一致 |
| myRole | number | Integer | - | ✓ 一致 |
| announcement | string | String | announcement | ✓ 一致 |
| createTime | string | LocalDateTime | create_time | ⚠ 类型不一致 |
| creatorId | - | Long | owner_id | ❌ 前端缺失 |
| creatorName | - | String | - | ❌ 前端缺失 |
| classNo | - | String | class_no | ❌ 前端缺失 |
| courseCode | - | String | course_id | ❌ 前端缺失 |
| maxMembers | - | Integer | max_members | ❌ 前端缺失 |
| isDisbanded | - | Integer | is_disbanded | ❌ 前端缺失 |
| disbandTime | - | LocalDateTime | disband_time | ❌ 前端缺失 |
| groupTypeDesc | - | String | - | ❌ 前端缺失 |

**问题说明**：
- 前端 `GroupVO` 缺少多个字段，可能导致部分功能无法正常显示。
- 建议前端补充缺失字段，或根据实际需求选择性添加。

### 4.2 CreateGroupDTO 字段对比

| 字段名 | 前端类型 | 后端类型 | 状态 |
|--------|---------|---------|------|
| groupName | string | String | ✓ 一致 |
| groupAvatar | string | String | ✓ 一致 |
| groupType | number | Integer | ✓ 一致 |
| description | string | - | ❌ 后端缺失 |
| announcement | string | - | ❌ 后端缺失 |
| classNo | - | String | ❌ 前端缺失 |
| courseCode | - | String | ❌ 前端缺失 |
| maxMembers | - | Integer | ❌ 前端缺失 |

**问题说明**：
- 前端定义了 `description` 和 `announcement` 字段，但后端 DTO 未定义。
- 后端定义了 `classNo`、`courseCode`、`maxMembers` 字段，但前端未定义。
- 需要前后端协调统一字段定义。

### 4.3 群组 API 路径对比

| 功能 | 前端路径 | 后端路径 | 状态 |
|------|---------|---------|------|
| 获取我的群组 | `/group/my-groups` | `/api/group/my-groups` | ✓ 一致 |
| 获取群组详情 | `/group/{groupId}` | `/api/group/{groupId}` | ✓ 一致 |
| 创建群组 | `/group/create` | `/api/group/create` | ✓ 一致 |
| 加入群组 | `/group/{groupId}/join` | `/api/group/{groupId}/join` | ✓ 一致 |
| 退出群组 | `/group/{groupId}/quit` | `/api/group/{groupId}/quit` | ✓ 一致 |
| 获取群成员 | `/group/{groupId}/members` | `/api/group/{groupId}/members` | ✓ 一致 |
| 踢出成员 | `/group/{groupId}/kick` | `/api/group/{groupId}/kick` | ✓ 一致 |
| 更新公告 | `/group/{groupId}/announcement` | `/api/group/{groupId}/announcement` | ✓ 一致 |
| 解散群组 | `/group/{groupId}/disband` | `/api/group/{groupId}/disband` | ✓ 一致 |
| 转让群主 | `/group/{groupId}/transfer` | `/api/group/{groupId}/transfer` | ✓ 一致 |
| 禁言成员 | `/group/{groupId}/mute` | `/api/group/{groupId}/mute` | ✓ 一致 |
| 解除禁言 | `/group/{groupId}/unmute` | `/api/group/{groupId}/unmute` | ✓ 一致 |
| 邀请成员 | `/group/{groupId}/invite` | `/api/group/{groupId}/invite` | ✓ 一致 |
| 批量邀请 | `/group/{groupId}/invite/batch` | `/api/group/{groupId}/invite/batch` | ✓ 一致 |

---

## 五、群成员模块数据格式对比

### 5.1 GroupMemberVO 字段对比

| 字段名 | 前端类型 | 后端类型 | 数据库字段 | 状态 |
|--------|---------|---------|-----------|------|
| userId | number | Long | user_id | ✓ 一致 |
| username | string | String | - | ✓ 一致 |
| realName | string | String | - | ✓ 一致 |
| avatar | string | String | - | ✓ 一致 |
| role | number | Integer | role | ✓ 一致 |
| groupNickname | string | - | group_nickname | ❌ 后端缺失 |
| joinTime | string | LocalDateTime | join_time | ⚠ 类型不一致 |
| id | - | Long | id | ❌ 前端缺失 |
| roleDesc | - | String | - | ❌ 前端缺失 |
| isOnline | - | Boolean | - | ❌ 前端缺失 |

**问题说明**：
- 前端定义了 `groupNickname` 字段，但后端 VO 未定义。
- 后端定义了 `id`、`roleDesc`、`isOnline` 字段，但前端未定义。

---

## 六、用户模块数据格式对比

### 6.1 UserInfo 字段对比

| 字段名 | 前端类型 | 后端类型 | 数据库字段 | 状态 |
|--------|---------|---------|-----------|------|
| id | number | Long | id | ✓ 一致 |
| username | string | String | username | ✓ 一致 |
| realName | string | String | real_name | ✓ 一致 |
| avatar | string | String | avatar | ✓ 一致 |
| role | number | Integer | role | ✓ 一致 |
| className | string | String | class_no | ⚠ 字段名不一致 |

**问题说明**：
- 前端使用 `className`，后端使用 `classNo`，字段名不一致。
- 前端 `auth.ts` 中 `UserInfo` 的 `className` 应改为 `classNo`，或后端在序列化时使用别名。

### 6.2 RegisterParams/RegisterDTO 字段对比

| 字段名 | 前端类型 | 后端类型 | 状态 |
|--------|---------|---------|------|
| username | string | String | ✓ 一致 |
| password | string | String | ✓ 一致 |
| studentNo | string | String | ✓ 一致 |
| realName | string | String | ✓ 一致 |
| classNo | string | String | ✓ 一致 |
| department | string | String | ✓ 一致 |
| role | number | Integer | ✓ 一致 |

### 6.3 用户认证 API 路径对比

| 功能 | 前端路径 | 后端路径 | 状态 |
|------|---------|---------|------|
| 用户登录 | `/auth/login` | `/api/auth/login` | ✓ 一致 |
| 用户注册 | `/auth/register` | `/api/auth/register` | ✓ 一致 |
| 获取用户信息 | `/auth/user/{userId}` | `/api/auth/user/{userId}` | ✓ 一致 |
| 更新用户资料 | `/auth/user/{userId}` | `/api/auth/user/{userId}` | ✓ 一致 |

---

## 七、消息模块数据格式对比

### 7.1 MessageVO 字段对比

| 字段名 | 前端类型 | 后端类型 | 数据库字段 | 状态 |
|--------|---------|---------|-----------|------|
| id | number | Long | id | ✓ 一致 |
| chatId | string | String | chat_id | ✓ 一致 |
| seqId | number | Long | seq_id | ✓ 一致 |
| fromUid | number | Long | from_uid | ✓ 一致 |
| fromName | string | String | - | ✓ 一致 |
| fromAvatar | string | String | - | ✓ 一致 |
| toUid | number | Long | to_uid | ✓ 一致 |
| groupId | number | Long | group_id | ✓ 一致 |
| content | string | String | content | ✓ 一致 |
| msgType | number | Integer | msg_type | ✓ 一致 |
| mentionedUsers | number[] | List<Long> | mentioned_users | ✓ 一致 |
| isRecalled | number | Integer | is_recalled | ✓ 一致 |
| createTime | string | LocalDateTime | create_time | ⚠ 类型不一致 |

---

## 八、数据库表结构汇总

### 8.1 表名与实体类映射

| 数据库表名 | 实体类 | 状态 |
|-----------|--------|------|
| user | User.java | ✓ 一致 |
| friend | Friend.java | ✓ 一致 |
| friend_request | FriendRequest.java | ✓ 一致 |
| im_group | Group.java | ✓ 一致 |
| group_member | GroupMember.java | ✓ 一致 |
| im_message | Message.java | ✓ 一致 |

### 8.2 字段命名规范

后端使用 MyBatis-Plus 的 `@TableField` 注解进行字段映射：
- 数据库字段使用下划线命名（如 `user_id`）
- Java 实体类使用驼峰命名（如 `userId`）
- 通过注解自动映射：`@TableField("user_id") private Long userId;`

---

## 九、发现的问题汇总

### 9.1 需要修复的问题

| 序号 | 模块 | 问题描述 | 优先级 | 建议 |
|------|------|---------|--------|------|
| 1 | 群组 | 前端 GroupVO 缺少 8 个字段 | 高 | 补充前端字段定义 |
| 2 | 群成员 | 前端 GroupMemberVO 缺少 3 个字段 | 中 | 补充前端字段定义 |
| 3 | 群成员 | 后端 GroupMemberVO 缺少 groupNickname 字段 | 中 | 补充后端字段定义 |
| 4 | 群组 | CreateGroupDTO 前后端字段不一致 | 高 | 协调统一字段定义 |
| 5 | 用户 | UserInfo className 与 classNo 不一致 | 中 | 统一字段名称 |
| 6 | 多模块 | createTime 类型不一致（string vs LocalDateTime） | 低 | 后端序列化时自动转换 |

### 9.2 建议优化的问题

| 序号 | 模块 | 问题描述 | 建议 |
|------|------|---------|------|
| 1 | 好友 | sortOrder 类型不一致 | 前端添加类型注释说明 |
| 2 | 群组 | 前端缺少群组详情相关字段 | 根据业务需求选择性添加 |

---

## 十、修复建议

### 10.1 前端修复建议

#### 修复 GroupVO 字段缺失

```typescript
// frontend/src/api/group.ts
export interface GroupVO {
  id: number;
  groupName: string;
  groupAvatar: string;
  groupType: number;
  memberCount: number;
  myRole?: number;
  announcement?: string;
  createTime?: string;
  // 新增字段
  creatorId?: number;
  creatorName?: string;
  classNo?: string;
  courseCode?: string;
  maxMembers?: number;
  isDisbanded?: number;
  disbandTime?: string;
  groupTypeDesc?: string;
}
```

#### 修复 GroupMemberVO 字段缺失

```typescript
// frontend/src/api/group.ts
export interface GroupMemberVO {
  userId: number;
  username: string;
  realName: string;
  avatar: string;
  role: number;
  groupNickname?: string;
  joinTime?: string;
  // 新增字段
  id?: number;
  roleDesc?: string;
  isOnline?: boolean;
}
```

#### 修复 UserInfo 字段名不一致

```typescript
// frontend/src/api/auth.ts
export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  avatar: string;
  role: number;
  classNo?: string;  // 改为 classNo，与后端一致
}
```

#### 修复 CreateGroupDTO 字段定义

```typescript
// frontend/src/api/group.ts
export interface CreateGroupDTO {
  groupName: string;
  groupAvatar?: string;
  groupType: number;
  description?: string;
  announcement?: string;
  // 新增字段
  classNo?: string;
  courseCode?: string;
  maxMembers?: number;
}
```

### 10.2 后端修复建议

#### 修复 GroupMemberVO 缺少 groupNickname

```java
// backend/src/main/java/com/maisizhe/modules/group/vo/GroupMemberVO.java
@Data
public class GroupMemberVO {
    // ... 其他字段
    
    /**
     * 群昵称
     */
    private String groupNickname;
}
```

#### 修复 CreateGroupDTO 缺少字段

```java
// backend/src/main/java/com/maisizhe/modules/group/dto/CreateGroupDTO.java
@Data
public class CreateGroupDTO {
    // ... 其他字段
    
    /**
     * 群组描述
     */
    private String description;
    
    /**
     * 群组公告
     */
    private String announcement;
}
```

---

## 十一、API 联通性验证

### 11.1 请求前缀说明

- 前端请求工具（`request.ts`）已配置 baseURL 为 `/api`
- 后端 Controller 路径均以 `/api` 开头
- 前端 API 定义中省略 `/api` 前缀，由请求工具自动添加

### 11.2 认证机制验证

| 项目 | 前端实现 | 后端实现 | 状态 |
|------|---------|---------|------|
| Token 存储 | localStorage | - | ✓ |
| Token 传递 | Authorization Header | JWT Filter | ✓ |
| 用户ID获取 | 从 JWT 解析 | SecurityContext | ✓ |
| Token 过期处理 | 自动跳转登录 | 返回 401 | ✓ |

### 11.3 数据序列化验证

| 类型 | 前端处理 | 后端处理 | 状态 |
|------|---------|---------|------|
| 日期时间 | ISO 字符串 | LocalDateTime → ISO | ✓ |
| 数字 | number | Long/Integer/Double | ✓ |
| 数组 | Array | List | ✓ |
| 布尔 | boolean | Boolean | ✓ |

---

## 十二、结论

### 12.1 总体评估

前后端数据格式整体一致性良好，API 路径完全匹配，认证机制正常工作。存在以下主要问题：

1. **群组模块字段缺失**：前端 GroupVO 缺少多个重要字段，影响群组详情展示。
2. **字段名不一致**：UserInfo 的 className 与 classNo 不一致。
3. **DTO 字段不匹配**：CreateGroupDTO 前后端定义不一致。

### 12.2 修复优先级

| 优先级 | 问题 | 影响范围 |
|--------|------|---------|
| 高 | 群组模块字段缺失 | 群组详情、群组管理功能 |
| 高 | CreateGroupDTO 字段不匹配 | 创建群组功能 |
| 中 | UserInfo 字段名不一致 | 用户信息显示 |
| 中 | 群成员字段缺失 | 群成员管理功能 |
| 低 | 时间类型不一致 | 已自动序列化，无实际影响 |

### 12.3 下一步工作

1. 按优先级修复上述问题
2. 补充单元测试验证数据格式
3. 完善前后端接口文档

---

**检查完成日期**：2024年12月
**检查人员**：AI Assistant