# 麦思哲前端开发日志 - Day 6

## 📅 日期: 2024-01-XX

---

## ✅ 今日完成工作

### 前端项目初始化 (30%)

今日完成了Vue3前端项目的**基础架构搭建**，包括项目配置、WebSocket管理器、API封装等核心工具。

---

## 📁 新增文件清单

### 1. 项目配置文件 (5个文件)

| 文件 | 行数 | 说明 |
|------|------|------|
| package.json | 33 | 依赖配置(Vue3 + Vite + Naive UI) |
| vite.config.ts | 48 | Vite配置(代理、插件、别名) |
| tsconfig.json | 32 | TypeScript配置 |
| tsconfig.node.json | 11 | Node环境TS配置 |
| index.html | 14 | HTML入口 |

**核心技术栈**:
```json
{
  "vue": "^3.4.21",
  "vue-router": "^4.3.0",
  "pinia": "^2.1.7",
  "naive-ui": "^2.38.1",
  "axios": "^1.6.7",
  "typescript": "^5.4.2",
  "vite": "^5.1.5"
}
```

---

### 2. 核心工具类 (2个文件)

**文件**: `src/utils/websocket.ts` (243行)

**功能**:
- ✅ WebSocket连接管理
- ✅ 自动重连(指数退避算法)
- ✅ 心跳保活(30秒间隔)
- ✅ 消息队列(断线重发)
- ✅ 事件回调机制

**核心方法**:
```typescript
class WebSocketManager {
  connect(): Promise<void>           // 连接
  send(message: WSMessage): void     // 发送
  disconnect(): void                 // 断开
  onConnected(callback): void        // 连接成功回调
  onMessage(callback): void          // 消息接收回调
  onDisconnected(callback): void     // 断开连接回调
}
```

**使用示例**:
```typescript
const ws = new WebSocketManager('ws://localhost:8080/ws', token);

ws.onConnected(() => {
  console.log('WebSocket连接成功');
});

ws.onMessage((message) => {
  if (message.type === 'GROUP_MESSAGE') {
    // 处理群聊消息
  }
});

ws.connect();

// 发送消息
ws.send({
  type: 'GROUP_MESSAGE',
  data: { groupId: 5001, content: 'Hello' }
});
```

---

**文件**: `src/utils/request.ts` (88行)

**功能**:
- ✅ Axios实例创建
- ✅ 请求拦截器(自动添加Token)
- ✅ 响应拦截器(统一错误处理)
- ✅ 401自动跳转登录

**配置**:
```typescript
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});

// 自动添加Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

---

### 3. API接口定义 (1个文件)

**文件**: `src/api/auth.ts` (63行)

**接口列表**:
- `login()` - 用户登录
- `register()` - 用户注册
- `getUserInfo()` - 获取用户信息
- `updateProfile()` - 更新用户资料

**TypeScript类型定义**:
```typescript
export interface LoginParams {
  username: string;
  password: string;
}

export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  avatar: string;
  role: number;
}
```

---

## 🎯 技术亮点

### 1. WebSocket自动重连(指数退避)

```typescript
private attemptReconnect(): void {
  const delay = this.reconnectDelay * Math.pow(2, this.reconnectCount);
  // 3秒 → 6秒 → 12秒 → 24秒 → ...
  
  this.reconnectTimer = window.setTimeout(() => {
    this.reconnectCount++;
    this.connect();
  }, delay);
}
```

**优势**:
- ✅ 避免频繁重连导致服务器压力
- ✅ 网络恢复后快速重连
- ✅ 最大重连次数限制(10次)

---

### 2. 消息队列保证不丢失

```typescript
send(message: WSMessage): void {
  if (this.isConnected) {
    this.ws.send(JSON.stringify(message));
  } else {
    // 未连接时加入队列
    this.messageQueue.push(message);
  }
}

private flushMessageQueue(): void {
  // 连接成功后发送所有队列消息
  while (this.messageQueue.length > 0) {
    const message = this.messageQueue.shift();
    this.ws.send(JSON.stringify(message));
  }
}
```

---

### 3. Vite代理配置

```typescript
server: {
  port: 3000,
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

**优势**:
- ✅ 避免跨域问题
- ✅ 开发环境无缝对接后端
- ✅ 无需修改API路径

---

### 4. Naive UI按需加载

```typescript
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

plugins: [
  AutoImport({
    imports: [
      'vue',
      {
        'naive-ui': [
          'useDialog',
          'useMessage',
          'useNotification',
          'useLoadingBar'
        ]
      }
    ]
  }),
  Components({
    resolvers: [NaiveUiResolver()]
  })
]
```

**优势**:
- ✅ 自动导入组件和API
- ✅ 减少bundle体积
- ✅ 提升开发效率

---

## 📊 代码统计

### 今日新增文件: **8个**

| 模块 | 文件数 | 行数 | 说明 |
|------|--------|------|------|
| 配置文件 | 5 | 138 | package.json, vite.config等 |
| utils | 2 | 331 | WebSocket + Request |
| api | 1 | 63 | Auth API |
| **总计** | **8** | **532** | - |

---

## ⏭️ 下一步计划

### Phase 1: Pinia状态管理 (预计2小时)

需要创建:
1. `stores/user.ts` - 用户状态(Token、UserInfo)
2. `stores/chat.ts` - 聊天状态(消息列表、会话)
3. `stores/group.ts` - 群组状态

---

### Phase 2: 路由和页面 (预计3小时)

需要创建:
1. `router/index.ts` - 路由配置
2. `views/Login.vue` - 登录页面
3. `views/Register.vue` - 注册页面
4. `views/Chat.vue` - 聊天主页面

---

### Phase 3. 核心组件 (预计5小时)

需要创建:
1. `components/ChatBubble.vue` - 消息气泡
2. `components/MessageInput.vue` - 消息输入框
3. `components/Sidebar.vue` - 侧边栏(会话列表)
4. `components/GroupList.vue` - 群组列表
5. `components/AIAssistant.vue` - AI助手面板

---

## 🔍 使用指南

### 1. 安装依赖

```bash
cd code/frontend
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

---

## 📝 注意事项

### 1. 环境变量

创建 `.env.development`:
```
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

### 2. TypeScript类型检查

```bash
npm run build  # 包含类型检查
```

### 3. 代码格式化

建议安装VS Code插件:
- ESLint
- Prettier
- Volar (Vue语言支持)

---

## 🎉 总结

Day 6成功完成了**前端项目初始化**，搭建了坚实的基础架构：

**核心能力**:
- ✅ Vue3 + TypeScript + Vite项目结构
- ✅ WebSocket管理器(自动重连、心跳、消息队列)
- ✅ Axios请求封装(Token自动注入、错误处理)
- ✅ Auth API接口定义
- ✅ Naive UI按需加载配置

**技术亮点**:
- ✅ 指数退避重连算法
- ✅ 消息队列保证不丢失
- ✅ Vite代理避免跨域
- ✅ TypeScript类型安全

**总体进度**: 前端约30%，后端85%

下一步将开发**Pinia状态管理**和**核心页面组件**，实现完整的聊天界面。
