# 前后端同步更新文档

## 更新日期
2026-06-21

## 数据库更新内容
本次更新主要针对 `init.sql` 优化的同步，涉及以下数据库改动：

### 1. 用户表 (`user`)
- 新增 `is_online` 字段：在线状态 (0-离线, 1-在线)
- 新增 `last_online_time` 字段：最后在线时间
- 新增 `deleted` 字段：逻辑删除标记

### 2. 好友表 (`friend`)
- 新增 `is_muted` 字段：是否免打扰
- `sort_order` 字段类型改为 `double`
- 新增 `deleted` 字段：逻辑删除标记

### 3. 好友请求表 (`friend_request`)
- 新增 `expires_time` 字段：过期时间
- 新增 `deleted` 字段：逻辑删除标记

### 4. 群组表 (`im_group`)
- 新增 `description` 字段：群描述

### 5. 群成员表 (`group_member`)
- 新增 `inviter_id` 字段：邀请人ID
- `is_muted` 字段：是否免打扰
- `sort_order` 字段类型改为 `double`
- 新增 `deleted` 字段：逻辑删除标记

### 6. 消息表 (`im_message`)
- 新增 `msg_status` 字段：消息状态 (0-发送中, 1-已发送, 2-已送达, 3-已读, 4-发送失败)

### 7. 新增会话表 (`chat_session`)
- 用于管理用户的会话列表

## 后端更新

### 1. FriendRequestVO.java
**文件**: `backend/src/main/java/com/maisizhe/modules/friend/vo/FriendRequestVO.java`
**改动**: 新增 `expiresTime` 字段
```java
/**
 * 过期时间
 */
private LocalDateTime expiresTime;
```

### 2. FriendRequestServiceImpl.java
**文件**: `backend/src/main/java/com/maisizhe/modules/friend/service/impl/FriendRequestServiceImpl.java`
**改动**:
- 新增 `LocalDateTime` 导入
- `sendRequest` 方法中设置默认过期时间（7天后）
- `convertToVOList` 方法中添加 `expiresTime` 字段转换

```java
// 创建好友请求（默认7天后过期）
FriendRequest request = FriendRequest.builder()
        .fromUserId(fromUserId)
        .toUserId(toUserId)
        .remark(remark)
        .status(STATUS_PENDING)
        .expiresTime(LocalDateTime.now().plusDays(7))
        .build();
```

### 3. GroupServiceImpl.java
**文件**: `backend/src/main/java/com/maisizhe/modules/group/service/impl/GroupServiceImpl.java`
**改动**: `inviteMembers` 方法中添加 `inviterId` 字段设置

```java
// 3. 批量添加新成员（inviterId为操作者，即邀请人）
if (!newUserIds.isEmpty()) {
    List<GroupMember> newMembers = newUserIds.stream()
        .map(uid -> GroupMember.builder()
            .groupId(groupId)
            .userId(uid)
            .inviterId(operatorId)  // 新增
            .role(MemberRoleEnum.MEMBER.getCode())
            .joinTime(LocalDateTime.now())
            .build())
        .collect(Collectors.toList());
```

### 4. GroupMemberMapper.java
**文件**: `backend/src/main/java/com/maisizhe/modules/group/mapper/GroupMemberMapper.java`
**改动**: `insertBatchSomeColumn` 方法的 SQL 添加缺失字段

```java
@Insert("<script>" +
        "INSERT INTO group_member (group_id, user_id, inviter_id, role, is_pinned, is_muted, sort_order, join_time) VALUES " +
        "<foreach collection='list' item='item' separator=','>" +
        "(#{item.groupId}, #{item.userId}, #{item.inviterId}, #{item.role}, #{item.isPinned}, #{item.isMuted}, #{item.sortOrder}, #{item.joinTime})" +
        "</foreach>" +
        "</script>")
int insertBatchSomeColumn(List<GroupMember> members);
```

## 前端更新

### 1. friend.ts
**文件**: `frontend/src/api/friend.ts`
**改动**: `FriendRequestVO` 接口添加 `expiresTime` 字段

```typescript
export interface FriendRequestVO {
  id: number;
  fromUserId: number;
  fromUsername: string;
  fromRealName: string;
  fromAvatar: string;
  remark: string;
  status: number;
  statusDesc: string;
  createTime: string;
  expiresTime?: string; // 过期时间 (新增)
  isOnline?: number; // 在线状态
}
```

## 检查确认的同步项

### 后端实体类（已确认与数据库一致）
- `User.java`: `isOnline`, `lastOnlineTime`, `deleted`
- `Friend.java`: `isMuted`, `sortOrder`(Double), `deleted`
- `FriendRequest.java`: `expiresTime`, `deleted`
- `Group.java`: `description`
- `GroupMember.java`: `inviterId`, `isMuted`, `sortOrder`(Double), `deleted`
- `Message.java`: `msgStatus`

### 后端VO类（已确认与数据库一致）
- `FriendVO.java`: `isMuted`, `sortOrder`(Double), `isOnline`
- `FriendRequestVO.java`: `expiresTime` (已修复)
- `GroupVO.java`: `description`
- `GroupMemberVO.java`: `inviterId`, `inviterName`, `isMuted`
- `MessageVO.java`: `msgStatus`

### 前端API类型定义（已确认与后端一致）
- `friend.ts`: `FriendVO`, `FriendRequestVO` (已添加 `expiresTime`)
- `group.ts`: `GroupVO`, `GroupMemberVO`
- `message.ts`: `MessageVO`, `MessageStatus`

### 前端组件（已确认支持新字段）
- `GroupSettingsPanel.vue`: 支持显示 `description` 字段
- `CreateGroupModal.vue`: 支持输入 `description` 字段

## 备注
- 会话表 (`chat_session`) 已定义但前后端尚未实现相关功能，待后续开发
- 消息已读状态功能 (`msg_status`) 已添加字段，具体业务逻辑待完善
- 在线状态功能 (`is_online`, `last_online_time`) 已添加字段，需配合WebSocket实现状态同步
