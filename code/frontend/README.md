# 麦思哲(MaiSiZhe)前端项目

## 📦 技术栈

- **框架**: Vue 3.4 + TypeScript 5.x
- **构建工具**: Vite 5.x
- **状态管理**: Pinia 2.x
- **路由**: Vue Router 4.x
- **UI组件库**: Naive UI 2.x
- **HTTP客户端**: Axios 1.6.x
- **WebSocket**: 原生WebSocket API

## 🚀 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

输出目录: `dist/`

### 4. 预览生产构建

```bash
npm run preview
```

## 📁 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口定义
│   │   └── auth.ts       # 认证相关API
│   ├── components/       # 可复用组件
│   ├── router/           # 路由配置
│   │   └── index.ts
│   ├── stores/           # Pinia状态管理
│   │   ├── user.ts       # 用户状态
│   │   └── chat.ts       # 聊天状态
│   ├── utils/            # 工具函数
│   │   ├── websocket.ts  # WebSocket管理器
│   │   └── request.ts    # Axios封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   └── Chat.vue      # 聊天主页
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 🔧 开发指南

### API代理配置

在 `vite.config.ts` 中配置了后端代理：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    },
    '/ws': {
      target: 'ws://localhost:8080',
      ws: true
    }
  }
}
```

### 状态管理

使用Pinia进行状态管理，主要包含：

- **User Store**: Token、用户信息、登录状态
- **Chat Store**: 会话列表、消息列表、WebSocket状态

### WebSocket使用

```typescript
import WebSocketManager from '@/utils/websocket';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const ws = new WebSocketManager('ws://localhost:8080/ws', userStore.token);

ws.onConnected(() => {
  console.log('WebSocket连接成功');
});

ws.onMessage((message) => {
  if (message.type === 'GROUP_MESSAGE') {
    // 处理群聊消息
  }
});

ws.connect();
```

## 📝 注意事项

### 环境变量

创建 `.env.development` 文件：

```
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

### TypeScript类型检查

```bash
npm run build  # 包含类型检查
```

### 代码规范

建议安装VS Code插件：
- ESLint
- Prettier
- Volar (Vue语言支持)

## 🎯 核心功能

### 已完成
- ✅ 项目初始化和配置
- ✅ WebSocket管理器(自动重连、心跳、消息队列)
- ✅ Axios请求封装(Token自动注入、错误处理)
- ✅ Pinia状态管理(User Store、Chat Store)
- ✅ 路由配置和守卫
- ✅ 登录页面

### 待完成
- ⏳ 注册页面
- ⏳ 聊天主页面
- ⏳ 消息气泡组件
- ⏳ 消息输入框组件
- ⏳ 侧边栏(会话列表)
- ⏳ 群组列表组件
- ⏳ AI助手面板

## 🔗 相关文档

- [前端设计方案](../../rem/前端设计方案.md)
- [API接口文档](../../rem/API接口文档.md)
- [开发日志-Day6](../../rem/开发日志-Day6.md)

## 📄 License

MIT
