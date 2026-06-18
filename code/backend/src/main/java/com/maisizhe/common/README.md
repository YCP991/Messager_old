# Common模块使用说明

## 📁 模块概述

Common模块是麦思哲后端系统的公共基础模块，提供了统一响应、异常处理、枚举定义等核心功能。

---

## 📦 模块结构

```
common/
├── response/
│   └── Result.java              # 统一响应类
├── enums/
│   ├── RoleEnum.java            # 用户角色枚举
│   ├── GroupTypeEnum.java       # 群组类型枚举
│   ├── MessageTypeEnum.java     # 消息类型枚举
│   └── MemberRoleEnum.java      # 群成员角色枚举
├── constants/
│   └── RedisKeyConstants.java   # Redis Key常量
└── exception/
    ├── BusinessException.java         # 业务异常
    ├── GroupPermissionException.java  # 群组权限异常
    └── GlobalExceptionHandler.java    # 全局异常处理器
```

---

## 🔧 使用指南

### 1. 统一响应 Result

#### 导入
```java
import com.maisizhe.common.response.Result;
```

#### 成功响应
```java
// 无数据
return Result.success();

// 带数据
UserVO userVO = new UserVO();
return Result.success(userVO);

// 自定义消息
return Result.success("操作成功", data);
```

#### 失败响应
```java
// 一般错误
return Result.error("系统繁忙");

// 指定错误码
return Result.error(400, "参数错误");

// 未授权
return Result.unauthorized("Token已过期");

// 禁止访问
return Result.forbidden("无权操作");

// 资源不存在
return Result.notFound("用户不存在");
```

#### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": 1705334400000
}
```

---

### 2. 枚举类使用

#### RoleEnum - 用户角色
```java
import com.maisizhe.common.enums.RoleEnum;

// 获取枚举
RoleEnum role = RoleEnum.getByCode(0); // STUDENT

// 获取代码
Integer code = RoleEnum.STUDENT.getCode(); // 0

// 获取描述
String desc = RoleEnum.TEACHER.getDescription(); // "教师"
```

**枚举值**:
- `STUDENT(0, "学生")`
- `TEACHER(1, "教师")`
- `ADMIN(2, "管理员")`

---

#### GroupTypeEnum - 群组类型
```java
import com.maisizhe.common.enums.GroupTypeEnum;

GroupTypeEnum type = GroupTypeEnum.getByCode(1); // CLASS

// 获取策略名称(用于工厂模式)
String strategy = GroupTypeEnum.COURSE.getStrategyName(); // "courseGroupStrategy"
```

**枚举值**:
- `NORMAL(0, "普通群", "normalGroupStrategy")`
- `CLASS(1, "班级群", "classGroupStrategy")`
- `COURSE(2, "课程群", "courseGroupStrategy")`

---

#### MessageTypeEnum - 消息类型
```java
import com.maisizhe.common.enums.MessageTypeEnum;

MessageTypeEnum type = MessageTypeEnum.getByCode(0); // TEXT
```

**枚举值**:
- `TEXT(0, "文本")`
- `IMAGE(1, "图片")`
- `FILE(2, "文件")`
- `AI_SUMMARY(3, "AI摘要")`
- `AI_PLANNING(99, "AI思考中")`

---

#### MemberRoleEnum - 群成员角色
```java
import com.maisizhe.common.enums.MemberRoleEnum;

MemberRoleEnum role = MemberRoleEnum.getByCode(2); // OWNER
```

**枚举值**:
- `MEMBER(0, "普通成员")`
- `ADMIN(1, "管理员")`
- `OWNER(2, "群主")`

---

### 3. Redis Key常量

#### 导入
```java
import com.maisizhe.common.constants.RedisKeyConstants;
```

#### 使用示例
```java
// 用户在线状态
String key = RedisKeyConstants.ONLINE_USER + userId;
// 结果: "online:user:1001"

// 消息序列号
String seqKey = RedisKeyConstants.SEQ_ID_PREFIX + chatId;
// 结果: "seq:chat:g_5001"

// 会话消息缓存
String msgKey = RedisKeyConstants.CHAT_MSG_PREFIX + chatId;
// 结果: "chat:msg:g_5001"
```

**常量列表**:
- `ONLINE_USER` - 用户在线状态
- `SEQ_ID_PREFIX` - 消息序列号
- `CHAT_MSG_PREFIX` - 会话消息缓存
- `CLIENT_MSG_ID` - 客户端消息ID去重
- `PRIVATE_READ_CURSOR` - 私聊已读游标
- `GROUP_READ_CURSOR` - 群聊已读游标
- `AI_CONTEXT_PREFIX` - AI会话上下文
- `TOKEN_BLACKLIST` - Token黑名单

---

### 4. 异常处理

#### 抛出业务异常
```java
import com.maisizhe.common.exception.BusinessException;

// 简单用法
throw new BusinessException("用户名已存在");

// 指定错误码
throw new BusinessException(400, "参数错误");
```

#### 抛出权限异常
```java
import com.maisizhe.common.exception.GroupPermissionException;

throw new GroupPermissionException("您不是群主，无权操作");
```

#### 全局自动处理
无需手动捕获，GlobalExceptionHandler会自动处理并返回统一格式：

```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null,
  "timestamp": 1705334400000
}
```

---

## 💡 最佳实践

### 1. Controller层使用
```java
@RestController
@RequestMapping("/user")
public class UserController {
    
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        UserVO user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return Result.success(user);
    }
    
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }
}
```

### 2. Service层使用
```java
@Service
public class UserServiceImpl implements UserService {
    
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }
    
    public void checkPermission(Long userId, Long groupId) {
        GroupMember member = groupMemberMapper.selectOne(...);
        if (member == null) {
            throw new GroupPermissionException("您不是群组成员");
        }
    }
}
```

### 3. 枚举转换
```java
// 数据库存储的是Integer code
Integer roleCode = user.getRole();

// 转换为枚举
RoleEnum role = RoleEnum.getByCode(roleCode);

// 判断角色
if (role == RoleEnum.ADMIN) {
    // 管理员逻辑
}
```

---

## ⚠️ 注意事项

1. **Result不要直接new**
   ```java
   // ❌ 错误
   return new Result<>(200, "success", data);
   
   // ✅ 正确
   return Result.success(data);
   ```

2. **异常消息要友好**
   ```java
   // ❌ 不友好
   throw new BusinessException("Error");
   
   // ✅ 友好
   throw new BusinessException("用户名或密码错误");
   ```

3. **枚举使用前检查null**
   ```java
   RoleEnum role = RoleEnum.getByCode(code);
   if (role == null) {
       throw new BusinessException("无效的角色类型");
   }
   ```

4. **Redis Key使用常量拼接**
   ```java
   // ❌ 硬编码
   String key = "online:user:" + userId;
   
   // ✅ 使用常量
   String key = RedisKeyConstants.ONLINE_USER + userId;
   ```

---

## 🧪 测试示例

### 单元测试
```java
@SpringBootTest
class ResultTest {
    
    @Test
    void testSuccess() {
        Result<String> result = Result.success("test");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test", result.getData());
    }
    
    @Test
    void testError() {
        Result<Void> result = Result.error("error");
        assertEquals(500, result.getCode());
        assertEquals("error", result.getMessage());
    }
}

@SpringBootTest
class EnumTest {
    
    @Test
    void testRoleEnum() {
        RoleEnum role = RoleEnum.getByCode(0);
        assertEquals(RoleEnum.STUDENT, role);
        assertEquals("学生", role.getDescription());
    }
}
```

---

## 📚 相关文档

- [后端设计方案](../rem/后端设计方案.md)
- [API接口文档](../rem/API接口文档.md)
- [开发进度](../rem/开发进度.md)

---

**版本**: 1.0.0  
**最后更新**: 2024-01-XX
