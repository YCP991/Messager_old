# 麦思哲(MaiSiZhe)后端开发指南

## 📁 项目结构

```
backend/
├── pom.xml                                    # Maven配置
├── src/
│   ├── main/
│   │   ├── java/com/maisizhe/
│   │   │   ├── MaiSiZheApplication.java      # 启动类
│   │   │   ├── common/                        # 公共模块
│   │   │   │   ├── constants/                 # 常量
│   │   │   │   ├── enums/                     # 枚举
│   │   │   │   ├── exception/                 # 异常
│   │   │   │   ├── response/                  # 统一响应
│   │   │   │   └── utils/                     # 工具类
│   │   │   ├── config/                        # 配置类
│   │   │   ├── security/                      # 安全认证
│   │   │   ├── websocket/                     # WebSocket网关
│   │   │   ├── modules/                       # 业务模块
│   │   │   │   ├── user/                      # 用户
│   │   │   │   ├── friend/                    # 好友
│   │   │   │   ├── group/                     # 群组
│   │   │   │   ├── message/                   # 消息
│   │   │   │   └── ai/                        # AI
│   │   │   └── infrastructure/                # 基础设施
│   │   └── resources/
│   │       ├── application.yml                # 配置文件
│   │       └── mapper/                        # MyBatis XML
│   └── test/                                  # 测试代码
└── README.md
```

## 🚀 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x

### 2. 数据库初始化
```bash
mysql -u root -p < database/init.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
  data:
    redis:
      password: your_redis_password  # 如果有密码
```

### 4. 运行项目
```bash
mvn clean package
java -jar target/maisizhe-backend-1.0.0.jar
```

或使用Maven直接运行:
```bash
mvn spring-boot:run
```

## 📋 API文档

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/refresh` - 刷新Token

### 用户接口
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/profile` - 更新个人资料
- `POST /api/user/avatar` - 上传头像

### 好友接口
- `GET /api/friend/list` - 好友列表
- `POST /api/friend/add` - 添加好友
- `DELETE /api/friend/{id}` - 删除好友

### 群组接口
- `GET /api/group/list` - 群组列表
- `POST /api/group/create` - 创建群组
- `POST /api/group/{id}/members` - 添加成员

### 消息接口
- `GET /api/message/history` - 历史消息
- `POST /api/message/sync` - 消息同步(补盲)

## 🔑 核心设计

### 1. 消息双轴定序
```
全局唯一ID (Snowflake) + 会话内连续序号 (Redis INCR)
```

### 2. WebSocket消息路由
```
客户端 → WebSocket Handler → Message Dispatcher → Redis Stream → 多节点消费
```

### 3. AI异步工作流
```
@AI触发 → 占位消息 → 任务入队 → AI Worker线程池 → LLM调用 → 推送结果
```

### 4. 权限控制
```java
@GroupPermission(
    requiredRoles = {OWNER, ADMIN},
    allowGroupTypes = {COURSE}
)
```

## 🧪 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 学生 | zhangsan | 123456 | 22计科1班 |
| 学生 | lisi | 123456 | 22计科1班 |
| 教师 | wanglaoshi | 123456 | 课程群主 |
| 管理员 | admin | 123456 | 系统管理员 |
| AI助手 | AI助手 | (无) | user_id=0 |

## 📝 开发规范

### 代码风格
- 使用Lombok简化代码
- 遵循阿里巴巴Java开发手册
- 统一使用Result封装返回

### Git提交
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

## 🔧 常见问题

### Q1: Redis连接失败
检查Redis是否启动，配置文件中的host和port是否正确。

### Q2: JWT Token无效
检查jwt.secret配置，确保前后端使用相同的密钥。

### Q3: WebSocket连接失败
检查WebSocket握手拦截器中的JWT验证逻辑。

## 📞 联系方式

如有问题，请联系开发团队或查看项目文档。

---

**版本**: 1.0.0  
**最后更新**: 2024-01-XX
