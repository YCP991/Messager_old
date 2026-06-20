# 前端Bug修复文档

## 修复日期
2026-06-20

## 修复内容
与后端Bug修复配合的前端相关调整。

---

## 问题1：群组加载失败

### 问题表现
前端控制台错误：
```
加载群组失败: Error: 系统繁忙，请稍后重试
at request.ts:51:29
at async loadGroups (Chat.vue:153:20)
```

### 根本原因
后端实体类字段映射问题，导致API返回错误（已在后端修复）。

### 前端影响
- ❌ 无法显示群组列表
- ❌ 自动选择第一个群组失败
- ❌ WebSocket连接不稳定

### 修复状态
✅ **后端已修复**，前端无需修改代码。

### 验证步骤
1. 重启后端服务
2. 清除浏览器缓存（Ctrl+Shift+Delete）
3. 重新登录系统
4. 检查群组列表是否正常显示

### 前端相关文件
- `src/views/Chat.vue` - 聊天主页面（loadGroups函数）
- `src/api/auth.ts` - API接口定义
- `src/utils/request.ts` - 请求工具类

---

## 问题2：WebSocket不稳定

### 问题表现
控制台日志：
```
websocket.ts:45 正在连接WebSocket...
websocket.ts:50 WebSocket连接成功
websocket.ts:76 WebSocket连接关闭
Chat.vue:180 WebSocket断开连接
websocket.ts:152 3秒后尝试第1次重连...
```

### 根本原因
后端WebSocket处理器中的依赖注入问题（已在后端修复）。

### 前端WebSocket配置
WebSocket连接URL：`ws://localhost:8080/ws/{token}`

```typescript
// websocket.ts
const token = userStore.token;
ws.value = new WebSocket(`${this.url}/${token}`);
```

### 验证步骤
1. 确保后端服务正常运行
2. 检查浏览器控制台是否有WebSocket连接错误
3. 观察WebSocket状态显示（在线/离线）
4. 尝试发送消息，验证实时通信

---

## 前端依赖和版本

### 关键依赖
```json
{
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    "naive-ui": "^2.38.1",
    "axios": "^1.6.7",
    "dayjs": "^1.11.10",
    "@vicons/ionicons5": "^0.13.0"
  }
}
```

### 运行环境要求
- Node.js 18+
- npm 9+

---

## 测试清单

### 基础功能测试
- [ ] 用户登录
- [ ] 群组列表加载
- [ ] 选择群组
- [ ] 加载历史消息
- [ ] 发送消息
- [ ] WebSocket连接

### 异常场景测试
- [ ] Token过期处理
- [ ] 网络断开重连
- [ ] 服务器重启后恢复

---

## 调试技巧

### 查看API响应
在浏览器开发者工具中：
1. 打开Network标签
2. 筛选XHR/Fetch请求
3. 查看 `/api/group/my-groups` 的响应

### 查看WebSocket消息
在浏览器开发者工具中：
1. 打开Network标签
2. 筛选WS（WebSocket）请求
3. 查看消息帧

### 前端日志
已在代码中添加适当的console.log：
- `console.log('正在连接WebSocket...')`
- `console.log('WebSocket连接成功')`
- `console.log('WebSocket断开连接')`
- `console.log('收到WebSocket消息:', wsMessage)`

---

## 相关文档

### 前端文档
- [前端设计方案.md](file://C:/Users/Champion%20Young/Desktop/课设/Messager/rem/前端设计方案.md)

### 后端修复文档
- [Bug修复报告-群组加载失败和WebSocket不稳定.md](../backend/Bdoc/Bug修复报告-群组加载失败和WebSocket不稳定.md)

---

## 常见问题

### Q1: 登录后一直显示"加载中"
**可能原因**：
1. 后端服务未启动
2. 数据库连接失败
3. API地址配置错误

**解决方法**：
1. 检查后端服务是否在8080端口运行
2. 检查数据库是否正常连接
3. 检查vite.config.ts中的代理配置

### Q2: WebSocket一直重连
**可能原因**：
1. 后端WebSocket未正确启动
2. Token无效
3. 网络问题

**解决方法**：
1. 重启后端服务
2. 清除浏览器缓存，重新登录
3. 检查网络连接

### Q3: 消息发送失败
**可能原因**：
1. WebSocket未连接
2. 群组ID无效
3. 消息内容为空

**解决方法**：
1. 确保WebSocket已连接
2. 检查是否选择了群组
3. 输入非空消息内容

---

## 后续优化建议

### 短期优化
1. 添加加载状态指示器（loading spinner）
2. 优化错误提示信息
3. 添加重试机制

### 长期优化
1. 实现消息离线存储
2. 添加消息草稿功能
3. 实现消息已读状态

---

**修复完成日期**：2026-06-20  
**状态**：✅ 等待后端服务重启验证
