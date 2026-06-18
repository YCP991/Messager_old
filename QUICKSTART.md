# 快速开始指南

## 🚀 5分钟启动项目

### 前置检查

确保已安装:
```bash
java -version    # 需要 JDK 17+
mvn -v          # 需要 Maven 3.8+
mysql --version # 需要 MySQL 8.0+
redis-server --version # 需要 Redis 7.x
```

---

## 第一步: 初始化数据库 (2分钟)

```bash
# 1. 确保MySQL已启动
# Windows: 在服务中启动MySQL
# Linux: sudo systemctl start mysql

# 2. 执行初始化脚本
cd C:\Users\Champion Young\Desktop\课设\Messager\code\database
mysql -u root -p < init.sql

# 输入MySQL密码后,看到"数据库初始化完成!"即成功
```

验证:
```sql
mysql -u root -p
USE maisizhe;
SHOW TABLES;  -- 应该看到11张表
SELECT * FROM user;  -- 应该看到6个测试用户
```

---

## 第二步: 启动Redis (1分钟)

```bash
# Windows
redis-server

# Linux/Mac
sudo systemctl start redis

# 验证
redis-cli ping  # 应返回 PONG
```

---

## 第三步: 配置后端 (1分钟)

编辑 `code/backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: root      # 改为你的MySQL用户名
    password: root      # 改为你的MySQL密码
  
  data:
    redis:
      host: localhost   # Redis地址
      port: 6379        # Redis端口
      password:         # Redis密码(如果没有留空)
```

---

## 第四步: 启动后端 (1分钟)

```bash
cd C:\Users\Champion Young\Desktop\课设\Messager\code\backend

# 方式1: 使用Maven运行(推荐开发时使用)
mvn spring-boot:run

# 方式2: 打包后运行
mvn clean package
java -jar target/maisizhe-backend-1.0.0.jar
```

看到以下输出表示成功:
```
========================================
   麦思哲(MaiSiZhe)后端服务启动成功!
========================================
```

验证:
```bash
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}'
```

应返回JWT Token。

---

## 第五步: 测试API

使用Postman或curl测试登录接口:

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456"
}
```

预期响应:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1001,
      "username": "zhangsan",
      "realName": "张三"
    }
  }
}
```

---

## ✅ 验证清单

- [ ] MySQL已启动且数据库初始化成功
- [ ] Redis已启动且可以连接
- [ ] 后端配置文件已修改(数据库密码等)
- [ ] 后端服务已成功启动(看到启动成功提示)
- [ ] 登录接口可以正常调用并返回Token

---

## 🐛 常见问题

### Q1: MySQL连接失败
**错误**: `Communications link failure`

**解决**:
1. 检查MySQL是否启动
2. 确认用户名密码正确
3. 检查URL中的时区设置

### Q2: Redis连接失败
**错误**: `Cannot get Jedis connection`

**解决**:
1. 检查Redis是否启动
2. 确认host和port配置正确
3. 如果有密码,确认password配置正确

### Q3: 端口被占用
**错误**: `Port 8080 was already in use`

**解决**:
```bash
# Windows查找占用端口的进程
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# 或修改application.yml中的端口
server:
  port: 8081
```

### Q4: Maven依赖下载慢
**解决**: 配置阿里云镜像

编辑 `~/.m2/settings.xml`:
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>*</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

---

## 📚 下一步

后端启动成功后,继续:

1. **完成后端核心模块** (参考开发进度.md)
2. **初始化前端项目** 
   ```bash
   cd code/frontend
   npm create vue@latest
   ```
3. **前后端联调**

详细开发步骤请参考:
- [后端设计方案](../rem/后端设计方案.md)
- [前端设计方案](../rem/前端设计方案.md)
- [开发进度跟踪](../rem/开发进度.md)

---

## 💡 提示

- 开发时使用 `mvn spring-boot:run` 支持热重载
- 查看日志定位问题: `tail -f logs/maisizhe.log`
- 使用Postman保存常用接口,提高测试效率
- 定期提交Git,避免代码丢失

---

**祝开发顺利!** 🎉
