# API接口定义文档

## 📋 接口规范

### 基础URL
- 开发环境: `http://localhost:8080/api`
- 生产环境: `https://api.maisizhe.com/api`

### 认证方式
在请求头中携带JWT Token:
```
Authorization: Bearer <token>
```

### 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

**响应码**:
- `200`: 成功
- `401`: 未认证
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器错误

---

## 🔐 认证接口

### 1. 用户登录
**POST** `/auth/login`

**请求体**:
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1001,
      "username": "zhangsan",
      "realName": "张三",
      "avatar": "/avatar/1.jpg",
      "role": 0,
      "className": "22计科1班"
    }
  }
}
```

---

### 2. 用户注册
**POST** `/auth/register`

**请求体**:
```json
{
  "username": "newuser",
  "password": "123456",
  "studentNo": "2022004",
  "realName": "新用户",
  "className": "22计科1班",
  "role": 0
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### 3. 刷新Token
**POST** `/auth/refresh`

**请求头**:
```
Authorization: Bearer <old_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

## 👤 用户接口

### 4. 获取用户信息
**GET** `/user/info`

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1001,
    "username": "zhangsan",
    "realName": "张三",
    "avatar": "/avatar/1.jpg",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "className": "22计科1班",
    "department": "计算机学院",
    "role": 0
  }
}
```

---

### 5. 更新个人资料
**PUT** `/user/profile`

**请求体**:
```json
{
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000"
}
```

---

### 6. 上传头像
**POST** `/user/avatar`

**请求**: `multipart/form-data`
- `file`: 图片文件

**响应**:
```json
{
  "code": 200,
  "data": {
    "avatarUrl": "/uploads/avatars/1001.jpg"
  }
}
```

---

### 7. 搜索用户
**GET** `/user/search?keyword=张三`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1001,
      "username": "zhangsan",
      "realName": "张三",
      "avatar": "/avatar/1.jpg",
      "className": "22计科1班"
    }
  ]
}
```

---

## 👥 好友接口

### 8. 好友列表
**GET** `/friend/list`

**响应**:
```json
{
  "code": 200,
  "data": {
    "pinned": [
      {
        "userId": 1002,
        "username": "lisi",
        "realName": "李四",
        "avatar": "/avatar/2.jpg",
        "remark": "小李",
        "online": true
      }
    ],
    "others": [...]
  }
}
```

---

### 9. 添加好友
**POST** `/friend/add`

**请求体**:
```json
{
  "friendId": 1002,
  "remark": "小李"
}
```

---

### 10. 删除好友
**DELETE** `/friend/{friendId}`

---

### 11. 修改备注
**PUT** `/friend/{friendId}/remark`

**请求体**:
```json
{
  "remark": "新备注"
}
```

---

### 12. 置顶/取消置顶
**PUT** `/friend/{friendId}/pin`

**请求体**:
```json
{
  "isPinned": true
}
```

---

## 👨‍👩‍👧‍👦 群组接口

### 13. 群组列表
**GET** `/group/list`

**响应**:
```json
{
  "code": 200,
  "data": {
    "pinned": [
      {
        "id": 5001,
        "name": "22计科1班交流群",
        "type": 1,
        "avatar": "/group/5001.jpg",
        "memberCount": 45,
        "lastMessage": {
          "content": "明天交实验报告",
          "time": "2024-01-15 10:30:00"
        },
        "unreadCount": 5
      }
    ],
    "others": [...]
  }
}
```

---

### 14. 创建普通群
**POST** `/group/create/normal`

**请求体**:
```json
{
  "name": "学习小组",
  "memberIds": [1002, 1003]
}
```

---

### 15. 创建班级群
**POST** `/group/create/class`

**请求体**:
```json
{
  "name": "22计科1班",
  "classNo": "22计科1班"
}
```

**说明**: 自动拉入该班级所有学生

---

### 16. 创建课程群
**POST** `/group/create/course`

**请求体**:
```json
{
  "name": "数据库原理",
  "courseId": 101,
  "semester": "2024-2025-1"
}
```

**说明**: 自动拉入选课的所有学生

---

### 17. 获取群详情
**GET** `/group/{groupId}`

---

### 18. 更新群信息
**PUT** `/group/{groupId}`

**请求体**:
```json
{
  "name": "新群名",
  "announcement": "群公告内容"
}
```

---

### 19. 群成员列表
**GET** `/group/{groupId}/members`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "userId": 1001,
      "username": "zhangsan",
      "realName": "张三",
      "role": 2,
      "groupNickname": "班长",
      "joinTime": "2024-01-01 10:00:00"
    }
  ]
}
```

---

### 20. 添加群成员
**POST** `/group/{groupId}/members`

**请求体**:
```json
{
  "memberIds": [1004, 1005]
}
```

---

### 21. 移除群成员
**DELETE** `/group/{groupId}/members/{userId}`

---

### 22. 设置管理员
**PUT** `/group/{groupId}/members/{userId}/role`

**请求体**:
```json
{
  "role": 1
}
```

---

### 23. 转让群主
**PUT** `/group/{groupId}/transfer`

**请求体**:
```json
{
  "newOwnerId": 1002
}
```

---

### 24. 退出群组
**POST** `/group/{groupId}/quit`

---

## 💬 消息接口

### 25. 获取历史消息
**GET** `/message/history?chatId=g_5001&lastSeq=100&limit=50`

**参数**:
- `chatId`: 会话ID (私聊: p_uid1_uid2, 群聊: g_gid)
- `lastSeq`: 最后一条消息的seq_id (首次加载传0)
- `limit`: 每次加载数量 (默认50)

**响应**:
```json
{
  "code": 200,
  "data": {
    "messages": [
      {
        "id": 200001,
        "chatId": "g_5001",
        "seqId": 101,
        "fromUid": 1001,
        "fromName": "张三",
        "fromAvatar": "/avatar/1.jpg",
        "content": "@AI 帮我解释红黑树",
        "msgType": 0,
        "mentionedUsers": [0],
        "createTime": "2024-01-15 10:30:00"
      }
    ],
    "hasMore": true
  }
}
```

---

### 26. 消息同步(补盲)
**POST** `/message/sync`

**请求体**:
```json
{
  "chatId": "g_5001",
  "lastSeq": 105
}
```

**说明**: 断线重连后获取错过的消息

---

### 27. 撤回消息
**POST** `/message/recall`

**请求体**:
```json
{
  "messageId": 200001
}
```

**限制**: 仅2分钟内可撤回

---

### 28. 标记已读
**POST** `/message/read`

**请求体**:
```json
{
  "chatId": "g_5001",
  "seqId": 110
}
```

---

### 29. 搜索消息
**GET** `/message/search?chatId=g_5001&keyword=实验报告`

---

## 🤖 AI接口

### 30. 个人AI对话
**POST** `/ai/chat`

**请求体**:
```json
{
  "sessionId": 1,
  "message": "帮我解释红黑树"
}
```

**响应** (流式):
```
data: {"chunk": "红"}
data: {"chunk": "黑"}
data: {"chunk": "树"}
...
```

---

### 31. AI会话列表
**GET** `/ai/sessions`

---

### 32. 创建AI会话
**POST** `/ai/sessions`

**请求体**:
```json
{
  "title": "算法讨论"
}
```

---

### 33. 关键词提醒设置
**GET** `/ai/keywords`

**POST** `/ai/keywords`

**请求体**:
```json
{
  "keyword": "实验报告"
}
```

**DELETE** `/ai/keywords/{id}`

---

## 📁 文件接口

### 34. 上传文件
**POST** `/file/upload`

**请求**: `multipart/form-data`
- `file`: 文件

**响应**:
```json
{
  "code": 200,
  "data": {
    "url": "/uploads/files/xxx.pdf",
    "name": "xxx.pdf",
    "size": 1024000
  }
}
```

---

## 🔧 管理员接口

### 35. 用户列表
**GET** `/admin/users?page=1&size=20`

---

### 36. 禁用/启用用户
**PUT** `/admin/users/{userId}/status`

**请求体**:
```json
{
  "status": 0
}
```

---

### 37. 群组列表
**GET** `/admin/groups`

---

### 38. 解散群组
**DELETE** `/admin/groups/{groupId}`

---

### 39. 操作日志
**GET** `/admin/logs?page=1&size=20`

---

## 🔌 WebSocket协议

### 连接地址
```
ws://localhost:8080/ws?token=<jwt_token>
```

### 消息格式
```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {...},
  "clientMsgId": "msg_xxx"
}
```

### 消息类型

#### 客户端 → 服务端

1. **私聊消息**
```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {
    "toUserId": 1002,
    "content": "你好",
    "clientMsgId": "msg_xxx"
  }
}
```

2. **群聊消息**
```json
{
  "type": "GROUP_MESSAGE",
  "data": {
    "groupId": 5001,
    "content": "@AI 帮我总结",
    "clientMsgId": "msg_xxx"
  }
}
```

3. **心跳**
```json
{
  "type": "PING",
  "data": null
}
```

---

#### 服务端 → 客户端

1. **私聊消息**
```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {
    "id": 100001,
    "chatId": "p_1001_1002",
    "seqId": 1,
    "fromUid": 1001,
    "content": "你好",
    "msgType": 0,
    "createTime": "2024-01-15 10:30:00"
  }
}
```

2. **群聊消息**
```json
{
  "type": "GROUP_MESSAGE",
  "data": {...}
}
```

3. **消息撤回**
```json
{
  "type": "MESSAGE_RECALL",
  "data": {
    "messageId": 100001,
    "chatId": "g_5001"
  }
}
```

4. **用户上线**
```json
{
  "type": "USER_ONLINE",
  "data": {
    "userId": 1002
  }
}
```

5. **用户下线**
```json
{
  "type": "USER_OFFLINE",
  "data": {
    "userId": 1002
  }
}
```

6. **心跳响应**
```json
{
  "type": "PONG",
  "data": null
}
```

---

## 📝 枚举值说明

### 角色 (role)
- `0`: 学生
- `1`: 教师
- `2`: 管理员

### 群组类型 (group.type)
- `0`: 普通群
- `1`: 班级群
- `2`: 课程群

### 群成员角色 (group_member.role)
- `0`: 普通成员
- `1`: 管理员
- `2`: 群主

### 消息类型 (message.msg_type)
- `0`: 文本
- `1`: 图片
- `2`: 文件
- `3`: AI摘要
- `99`: AI思考中

---

## 🎯 开发建议

1. **前端**: 使用TypeScript定义接口类型
2. **后端**: 使用Swagger生成在线文档
3. **测试**: 使用Postman保存接口集合
4. **Mock**: 后端未完成时使用Mock数据

---

**版本**: 1.0.0  
**最后更新**: 2024-01-XX
