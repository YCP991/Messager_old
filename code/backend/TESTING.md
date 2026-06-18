# Messager 后端单元测试说明

## 📋 测试概览

本项目使用 **JUnit 5** + **Mockito** 进行单元测试，确保代码质量和功能正确性。

---

## 🧪 测试文件清单

### 1. Common模块测试 (3个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `ResultTest` | `com.maisizhe.common.response.ResultTest` | 统一响应类的success/error方法 |
| `RoleEnumTest` | `com.maisizhe.common.enums.RoleEnumTest` | 用户角色枚举的getByCode方法 |
| `MsgTypeEnumTest` | `com.maisizhe.common.enums.MsgTypeEnumTest` | 消息类型枚举的验证 |

### 2. 认证模块测试 (2个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `JwtUtilTest` | `com.maisizhe.security.jwt.JwtUtilTest` | JWT Token生成、验证、解析 |
| `LoginDTOTest` | `com.maisizhe.modules.auth.dto.LoginDTOTest` | 登录DTO的数据验证 |

### 3. 消息模块测试 (1个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `MessageServiceTest` | `com.maisizhe.modules.message.service.MessageServiceTest` | 消息发送、实体创建、DTO验证 |

### 4. 群组模块测试 (1个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `GroupServiceTest` | `com.maisizhe.modules.group.service.GroupServiceTest` | 群组创建、成员管理、权限验证 |

### 5. AI模块测试 (1个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `AiServiceTest` | `com.maisizhe.modules.ai.service.AiServiceTest` | AI对话、摘要生成、意图识别 |

### 6. WebSocket模块测试 (1个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `WSMessageTest` | `com.maisizhe.websocket.message.WSMessageTest` | WebSocket消息序列化/反序列化 |

### 7. 工具类测试 (1个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `RedisUtilTest` | `com.maisizhe.util.RedisUtilTest` | Redis Key格式、chatId生成规则 |

### 8. 应用启动测试 (1个)

| 测试类 | 路径 | 测试内容 |
|--------|------|----------|
| `MessagerApplicationTests` | `com.maisizhe.MessagerApplicationTests` | Spring Boot上下文加载 |

---

## 🚀 运行测试

### 运行所有测试

```bash
mvn test
```

### 运行单个测试类

```bash
mvn test -Dtest=JwtUtilTest
```

### 运行特定模块的测试

```bash
mvn test -Dtest="com.maisizhe.modules.message.*"
```

### 运行测试并生成覆盖率报告

```bash
mvn clean test jacoco:report
```

报告位置: `target/site/jacoco/index.html`

---

## 📊 测试覆盖情况

### 当前测试统计

| 指标 | 数量 |
|------|------|
| 测试类总数 | 11 |
| 测试方法总数 | ~60 |
| 代码覆盖率 | ~40% (估算) |

### 覆盖的核心功能

- ✅ JWT认证流程（Token生成、验证、解析）
- ✅ 统一响应格式（Result.success/error）
- ✅ 枚举类数据访问（RoleEnum, MsgTypeEnum, GroupRoleEnum）
- ✅ DTO数据验证（LoginDTO, SendMessageDTO）
- ✅ Entity实体创建（Message, Group, GroupMember）
- ✅ WebSocket消息序列化
- ✅ Redis Key命名规范
- ✅ AI服务接口调用

---

## 🔧 测试框架说明

### JUnit 5 注解

- `@Test`: 标记测试方法
- `@BeforeEach`: 每个测试前执行（初始化）
- `@AfterEach`: 每个测试后执行（清理）
- `@SpringBootTest`: Spring Boot集成测试

### Mockito 注解

- `@Mock`: 创建Mock对象
- `@InjectMocks`: 自动注入Mock对象
- `when(...).thenReturn(...)`: Mock返回值
- `verify(...)`: 验证方法调用

### 断言方法

- `assertEquals(expected, actual)`: 相等断言
- `assertNotNull(object)`: 非空断言
- `assertTrue(condition)`: 真值断言
- `assertFalse(condition)`: 假值断言
- `assertThrows(Exception.class, () -> {...})`: 异常断言

---

## 📝 测试示例

### JWT工具类测试示例

```java
@Test
void testGenerateAndValidateToken() {
    // Given
    Long userId = 1001L;
    
    // When
    String token = jwtUtil.generateToken(userId);
    
    // Then
    assertNotNull(token);
    assertTrue(jwtUtil.validateToken(token));
    assertEquals(userId, jwtUtil.getUserIdFromToken(token));
}
```

### 消息服务测试示例

```java
@Test
void testSendGroupMessage_Success() {
    // Given
    when(redisUtil.hasKey(anyString())).thenReturn(false);
    when(redisUtil.incr(anyString())).thenReturn(1L);
    when(messageMapper.insert(any(Message.class))).thenReturn(1);
    
    // When
    MessageVO result = messageService.sendGroupMessage(1001L, dto);
    
    // Then
    assertNotNull(result);
    assertEquals("测试消息", result.getContent());
}
```

---

## ⚠️ 注意事项

### 1. 需要外部依赖的测试

以下测试需要真实的外部服务（MySQL、Redis），建议使用`@SpringBootTest`进行集成测试：

- 消息服务的完整流程测试
- 群组服务的数据库操作测试
- Redis工具类的实际操作测试

### 2. Mock对象的使用

对于依赖复杂的Service类，使用Mock对象模拟依赖：

```java
@Mock
private MessageMapper messageMapper;

@InjectMocks
private MessageServiceImpl messageService;
```

### 3. 测试数据隔离

每个测试方法应独立，不依赖其他测试的执行结果。使用`@BeforeEach`初始化测试数据。

---

## 🎯 后续改进计划

### 短期目标（本周）

- [ ] 增加Controller层测试（使用MockMvc）
- [ ] 增加Mapper层测试（使用MyBatis-Plus测试支持）
- [ ] 完善Service层的Mock测试
- [ ] 添加异常场景测试

### 中期目标（本月）

- [ ] 达到60%代码覆盖率
- [ ] 添加集成测试（@SpringBootTest）
- [ ] 添加性能测试（@Benchmark）
- [ ] 配置CI/CD自动运行测试

### 长期目标

- [ ] 达到80%代码覆盖率
- [ ] 添加端到端测试（E2E）
- [ ] 添加压力测试（JMeter）
- [ ] 建立测试质量门禁

---

## 📚 参考资料

- [JUnit 5官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito官方文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [MyBatis-Plus Testing](https://baomidou.com/pages/24112f/)

---

**最后更新**: 2024-01-XX  
**维护者**: Messager开发团队
