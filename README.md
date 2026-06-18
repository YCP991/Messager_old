# 麦思哲 (MaiSiZhe) - 智能Agent校园即时通讯系统

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-blue)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

## 📖 项目简介

麦思哲是一个面向校园场景的小型团队即时通讯平台，融合了AI Agent能力，为班级、课程组、实验室、社团等组织提供即时通讯服务和智能协作辅助。

**一句话概括**: 它不是"又一个聊天软件"，而是一个"面向校园组织的、具备AI协作能力的轻量级私有化IM平台"。

### 🎯 核心定位

介于以下产品之间：
- **微信** → 即时通讯（私聊、群聊）
- **钉钉** → 组织协作（权限管理、公告）
- **飞书** → AI办公（AI摘要、提醒）
- **Slack** → 私有部署协作

**聚焦场景**: 班级、实验室、社团、小型研发团队

---

## ✨ 核心亮点

### 1️⃣ 真正的即时通讯系统设计
- ✅ WebSocket长连接 + STOMP协议
- ✅ 消息可靠性保障（断线重连 + HTTP增量拉取补偿）
- ✅ 消息顺序性保证（Snowflake ID + Redis INCR双轴定序）
- ✅ 客户端去重排序（clientMsgId + seq_id）

### 2️⃣ 校园场景建模优秀
- ✅ 三种群类型：普通群、班级群、课程群
- ✅ 课程群支持"一门课关联多个班级"自动拉人
- ✅ 贴近真实校园业务，非简单CRUD

### 3️⃣ 完整权限体系
- ✅ RBAC + 资源权限混合模型
- ✅ 角色 × 群类型 × 群身份三维复合权限
- ✅ AOP切面统一校验（@GroupPermission注解）

### 4️⃣ AI Agent优雅设计 ⭐
- ✅ AI作为特殊用户（user_id=0）入驻群聊
- ✅ 无需数据库特殊处理，完美融入现有体系
- ✅ 支持被@、发消息、参与权限体系

### 5️⃣ AI功能超越聊天机器人
- ✅ 群内问答（@AI 解释红黑树）
- ✅ 群日报（每日扫描生成摘要）
- ✅ 关键词提醒（实验报告、DDL私信提醒）
- ✅ Agent Workflow而非ChatBot

### 6️⃣ 扩展性考虑
- ✅ Redis + MySQL冷热分离
- ✅ ZSet缓存热消息（最近100条）
- ✅ 单表设计 + 联合索引（拒绝按月分表陷阱）

---

## 🏗️ 技术架构

### 总体架构
```
前后端分离 + 实时通讯 + 缓存加速 + AI异步协作
```

### 技术栈

#### 前端
- **框架**: Vue 3.4 + TypeScript 5.x
- **构建**: Vite 5.x
- **状态**: Pinia 2.x
- **UI**: Naive UI 2.x
- **通信**: WebSocket + Axios

#### 后端
- **框架**: Spring Boot 3.2
- **安全**: Spring Security + JWT
- **持久层**: MyBatis-Plus 3.5
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **WebSocket**: Spring WebSocket (原生Handler)
- **消息队列**: Redis Stream

#### AI
- **模型**: DeepSeek / Qwen
- **接入**: 异步线程池 + BlockingQueue削峰

---

## 📁 项目结构

```
Messager/
├── code/                          # 源代码
│   ├── backend/                   # Spring Boot后端
│   │   ├── src/main/java/
│   │   │   └── com/maisizhe/
│   │   │       ├── common/        # 公共模块
│   │   │       ├── config/        # 配置类
│   │   │       ├── security/      # 安全认证
│   │   │       ├── websocket/     # WebSocket网关
│   │   │       ├── modules/       # 业务模块
│   │   │       │   ├── user/      # 用户
│   │   │       │   ├── friend/    # 好友
│   │   │       │   ├── group/     # 群组
│   │   │       │   ├── message/   # 消息
│   │   │       │   └── ai/        # AI
│   │   │       └── infrastructure/# 基础设施
│   │   ├── src/main/resources/
│   │   │   ├── application.yml    # 配置文件
│   │   │   └── mapper/            # MyBatis XML
│   │   ├── pom.xml
│   │   └── README.md
│   │
│   ├── frontend/                  # Vue3前端
│   │   ├── src/
│   │   │   ├── api/               # API接口
│   │   │   ├── components/        # 组件
│   │   │   ├── views/             # 页面
│   │   │   ├── stores/            # Pinia状态
│   │   │   ├── router/            # 路由
│   │   │   └── utils/             # 工具
│   │   ├── package.json
│   │   └── README.md
│   │
│   └── database/                  # 数据库
│       └── init.sql               # 初始化脚本
│
├── rem/                           # 设计文档
│   ├── 前端设计方案.md
│   ├── 后端设计方案.md
│   ├── 数据库设计方案.md
│   └── 开发进度.md
│
├── doc/                           # 参考资料
│   ├── chat.txt
│   ├── gemini.txt
│   └── ori.txt
│
└── README.md                      # 本文件
```

---

## 🚀 快速开始

### 前置要求
- JDK 17+
- Node.js 18+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x

### 1. 数据库初始化

```bash
# 进入数据库目录
cd code/database

# 执行初始化脚本
mysql -u root -p < init.sql
```

### 2. 后端启动

```bash
# 进入后端目录
cd code/backend

# 修改配置文件 src/main/resources/application.yml
# 设置数据库用户名密码和Redis配置

# 编译打包
mvn clean package

# 运行
java -jar target/maisizhe-backend-1.0.0.jar

# 或使用Maven直接运行
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 前端启动

```bash
# 进入前端目录
cd code/frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

前端应用将在 `http://localhost:3000` 启动

### 4. 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 学生 | zhangsan | 123456 | 22计科1班 |
| 学生 | lisi | 123456 | 22计科1班 |
| 教师 | wanglaoshi | 123456 | 课程群主 |
| 管理员 | admin | 123456 | 系统管理员 |
| AI助手 | AI助手 | (无) | user_id=0 |

---

## 📋 核心功能

### 用户认证
- [x] 登录/注册
- [x] JWT Token认证
- [x] 个人资料管理
- [x] 头像上传

### 好友管理
- [x] 添加/删除好友
- [x] 好友备注
- [x] 好友置顶
- [x] 在线状态显示

### 群组管理
- [x] 创建普通群
- [x] 创建班级群（自动拉入同班同学）
- [x] 创建课程群（自动拉入选课学生）
- [x] 群成员管理（邀请、踢人、转让群主）
- [x] 群公告
- [x] 群昵称

### 即时通讯
- [x] 私聊
- [x] 群聊
- [x] 消息撤回
- [x] @提及
- [x] 已读状态
- [x] 历史消息加载
- [x] 离线消息同步

### AI协作
- [x] @AI问答
- [x] 个人AI助手
- [x] 群日报自动生成
- [x] 关键词提醒
- [x] Markdown渲染

### 管理后台
- [x] 用户管理
- [x] 群组管理
- [x] 操作日志

---

## 🔑 核心设计

### 1. 消息双轴定序

```
全局唯一ID (Snowflake) + 会话内连续序号 (Redis INCR)

优势:
- Snowflake ID: 全局唯一,便于检索
- Redis INCR seq_id: 会话内绝对连续,客户端判断丢包
```

### 2. WebSocket消息路由

```
客户端 → WebSocket Handler 
       → Message Dispatcher 
       → Redis Stream (消息总线)
       → 多节点消费
       → UserSessionManager
       → 推送目标用户

优势:
- 支持水平扩展
- 消息不丢失
- 跨节点通信
```

### 3. AI异步削峰

```
@AI触发 → 立即返回占位消息("AI思考中...")
        → 任务入队 (BlockingQueue)
        → AI Worker线程池异步消费
        → 调用LLM API
        → 推送结果到群聊

优势:
- 不阻塞WebSocket I/O线程
- 防止高并发时系统雪崩
- 支持流式输出
```

### 4. 权限控制

```java
@GroupPermission(
    requiredRoles = {OWNER, ADMIN},
    allowGroupTypes = {COURSE}
)
public Result<Void> kickMember(@RequestBody KickDTO dto) {
    // AOP自动校验权限
}

三维权限判定:
1. 全局角色 (学生/教师/管理员)
2. 资源类型 (普通群/班级群/课程群)
3. 群内身份 (群主/管理员/成员)
```

---

## 📊 性能指标

| 指标 | 目标值 |
|------|--------|
| 在线用户数 | 1000+ |
| WebSocket并发连接 | 5000+ |
| 单节点消息吞吐量 | 2000 msg/s |
| 消息平均响应时间 | < 200ms |
| 离线消息恢复时间 | < 2s |
| AI响应时间 | 3~10s |

---

## 🗺️ 开发路线图

### Phase 1: 基础架构 (Day 1-2)
- [x] 数据库设计
- [x] 项目结构搭建
- [ ] JWT认证
- [ ] 用户登录注册

### Phase 2: 核心功能 (Day 3-5)
- [ ] WebSocket长连接
- [ ] 消息收发
- [ ] 好友管理
- [ ] 群组管理

### Phase 3: AI集成 (Day 6-7)
- [ ] AI异步工作流
- [ ] @AI问答
- [ ] 群日报

### Phase 4: 优化交付 (Day 8-10)
- [ ] 前后端联调
- [ ] 性能优化
- [ ] Docker部署
- [ ] 文档整理

---

## 📝 开发文档

详细的设计方案请参考 `rem/` 目录下的文档：

- [前端设计方案](rem/前端设计方案.md)
- [后端设计方案](rem/后端设计方案.md)
- [数据库设计方案](rem/数据库设计方案.md)
- [开发进度跟踪](rem/开发进度.md)

---

## 🤝 团队协作

### 分工
- **杨朝平** (后端负责人): Spring Boot全栈、WebSocket、AI集成
- **张唯栋** (前端负责人): Vue3界面、WebSocket客户端、状态管理
- **赵振凯** (全栈辅助): 数据库设计、Prompt工程、工具类编写

### Git工作流
```bash
# 创建功能分支
git checkout -b feature/xxx

# 提交代码
git commit -m "feat: 添加xxx功能"

# 推送到远程
git push origin feature/xxx

# 合并到主分支
git merge feature/xxx
```

---

## 📄 License

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

---

## 📞 联系方式

如有问题或建议，请联系开发团队。

---

**Made with ❤️ by MaiSiZhe Team**
