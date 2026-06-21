麦思哲项目 Bug 修复完整记录

项目开发过程中遇到的所有问题及解决方案汇总

目录：
1. 环境配置问题
2. 后端问题
3. 前端问题
4. 数据库问题
5. 前后端联调问题
6. WebSocket 问题
7. 常见问题速查


========================================
环境配置问题
========================================

问题1：Spring Boot 端口被占用

错误信息：
Web server failed to start. Port 8080 was already in use.

原因分析：
端口 8080 已被其他进程占用（可能是之前启动的 Spring Boot 应用未正常关闭）。

解决方案：

Windows PowerShell:
1. 查找占用端口的进程
   netstat -ano | findstr :8080

2. 终止进程（将 PID 替换为实际进程ID）
   taskkill /F /PID <PID>

Linux/Mac:
1. 查找占用端口的进程
   lsof -i :8080

2. 终止进程
   kill -9 <PID>

预防措施：
- 使用 Ctrl+C 正确停止 Spring Boot 应用
- 或在 application.yml 中修改端口：
  server:
    port: 8081


----------------------------------------

问题2：Redis 连接失败

错误信息：
io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379

原因分析：
Redis 服务未启动或密码配置不正确。

解决方案：

步骤1：检查 Redis 是否运行
Windows执行：redis-cli ping
应该返回 PONG

步骤2：启动 Redis
Windows (如果安装了 Redis)：redis-server
Linux：sudo systemctl start redis
Mac：brew services start redis

步骤3：配置密码（如果需要）
application.yml 配置：
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password  （如果设置了密码）


----------------------------------------

问题3：MySQL 连接失败

错误信息：
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure

原因分析：
MySQL 服务未启动或数据库配置错误。

解决方案：

步骤1：检查 MySQL 服务
Windows执行：Get-Service MySQL*
如果未运行，启动服务：Start-Service MySQL80

步骤2：验证数据库配置
application.yml 配置：
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/maisizhe?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password


----------------------------------------

问题4：Maven 命令无法识别

错误信息：
mvn : The term 'mvn' is not recognized as the name of a cmdlet

原因分析：
Maven 未安装或未添加到系统环境变量 PATH。

解决方案：

步骤1：安装 Maven
下载地址：https://maven.apache.org/download.cgi
解压到指定目录（如 C:\Program Files\Apache\maven）

步骤2：配置环境变量
添加 MAVEN_HOME：$env:MAVEN_HOME = "C:\Program Files\Apache\maven"
添加到 PATH：$env:Path += ";$env:MAVEN_HOME\bin"

步骤3：验证安装
执行：mvn -version


----------------------------------------

问题5：npm 命令无法识别

错误信息：
npm : The term 'npm' is not recognized

原因分析：
Node.js 未安装或未添加到 PATH。

解决方案：

步骤1：安装 Node.js
下载地址：https://nodejs.org/
推荐下载 LTS 版本（包含 npm）

步骤2：验证安装
执行：node -v
执行：npm -v


========================================
后端问题
========================================

问题6：CORS 配置错误

错误信息：
java.lang.IllegalArgumentException: When allowCredentials is true,
allowedOrigins cannot contain the special value "*"

原因分析：
Spring Security 6 中，当设置 allowCredentials=true 时，不能使用 allowedOrigins=["*"]。

解决方案：

修改 SecurityConfig.java：

修改前（错误）：
configuration.setAllowedOrigins(Arrays.asList("*"));

修改后（正确）：
configuration.setAllowedOriginPatterns(Arrays.asList("*"));

完整配置示例：
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}


----------------------------------------

问题7：WebSocketHandler 依赖注入失败

错误信息：
org.springframework.beans.factory.annotation.Autowired annotation requires
a bean of type 'com.maisizhe.websocket.handler.ChatWebSocketHandler' that could not be found

原因分析：
WebSocketHandler 未注册为 Spring Bean，导致无法自动注入。

解决方案：

方法1：在 WebSocketConfig 中显式注册 Bean

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/{token}")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }
}

方法2：在 Handler 类上添加 @Component

@Component
@ServerEndpoint("/ws/{token}")
public class ChatWebSocketHandler {
    // ...
}


----------------------------------------

问题8：循环依赖错误

错误信息：
The dependencies of some of the beans in the application context form a cycle:
beanA -> beanB -> beanA

原因分析：
Spring Boot 3.x 默认不允许循环依赖。

解决方案：

临时方案：允许循环依赖
application.yml 配置：
spring:
  main:
    allow-circular-references: true

最佳方案：重构代码消除循环依赖
- 使用 @Lazy 注解延迟加载
- 提取公共逻辑到第三个 Bean
- 使用事件驱动代替直接调用


----------------------------------------

问题9：修改配置后未生效

现象：
修改了 WebSocketConfig 等配置类后重启，仍然报相同的错误。

原因分析：
旧的编译文件（target 目录）未被清理，导致使用的是旧代码。

解决方案：

执行 clean 后重启：
1. 进入后端目录：cd code/backend
2. 清理并重新运行：mvn clean spring-boot:run

或在 IDEA 中：
1. 点击 Build -> Rebuild Project
2. 或使用快捷键 Ctrl+Shift+F9


----------------------------------------

问题10：MyBatis-Plus 自动填充失效

现象：
create_time 和 update_time 字段为 null。

原因分析：
未配置 MetaObjectHandler 或实体类缺少注解。

解决方案：

步骤1：实体类添加注解

@Data
@TableName("user")
public class User {

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

步骤2：创建 MetaObjectHandler

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}


========================================
前端问题
========================================

问题11：Vue inject() 警告

错误信息：
[Vue warn]: inject() can only be used inside setup() or functional components.
TypeError: Cannot read properties of undefined (reading 'error')

原因分析：
在 axios 响应拦截器中使用了 useMessage() from naive-ui，但这是一个 Composition API hook，只能在组件的 setup() 中使用。

解决方案：

方案1：使用 console.error（简单方案）

修改 utils/request.ts：

移除 useMessage 导入
// import { useMessage } from 'naive-ui';

响应拦截器：
request.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      console.error('API错误:', res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    return res;
  },
  error => {
    console.error('请求错误:', error.message);
    return Promise.reject(error);
  }
);

方案2：创建独立消息服务（推荐）

创建 utils/notification.ts：

import { createDiscreteApi } from 'naive-ui';

const { message } = createDiscreteApi(['message']);

export const notify = {
  success: (msg: string) => message.success(msg),
  error: (msg: string) => message.error(msg),
  warning: (msg: string) => message.warning(msg),
  info: (msg: string) => message.info(msg),
};

在拦截器中使用：

import { notify } from '@/utils/notification';

request.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      notify.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    return res;
  },
  error => {
    notify.error(error.message || '网络错误');
    return Promise.reject(error);
  }
);


----------------------------------------

问题12：TypeScript 未使用变量警告

错误信息：
'xxx' is declared but its value is never read. ts(6133)

原因分析：
Vite 构建时对未使用变量进行严格检查。

解决方案：

方案1：删除未使用的变量

修改前（错误）：
const token = localStorage.getItem('token');

修改后（正确）：
直接使用，不赋值给变量
localStorage.getItem('token');

方案2：使用前缀下划线标记

// 表示故意不使用
const _unused = someValue;

方案3：在 tsconfig.json 中禁用检查

{
  "compilerOptions": {
    "noUnusedLocals": false
  }
}


----------------------------------------

问题13：浏览器缓存导致前端更新不生效

现象：
修改前端代码后刷新页面，仍然是旧版本。

原因分析：
浏览器缓存了旧的 JavaScript 和 CSS 文件。

解决方案：

硬刷新：
- Windows：Ctrl + F5
- Mac：Cmd + Shift + R

清除缓存：
1. 打开开发者工具 (F12)
2. 右键点击刷新按钮
3. 选择"清空缓存并硬性重新加载"

使用无痕模式：
- Windows：Ctrl + Shift + N
- Mac：Cmd + Shift + N


========================================
数据库问题
========================================

问题14：数据库表名不匹配

错误信息：
java.sql.SQLSyntaxErrorException: Table 'maisizhe.im_group_member' doesn't exist

原因分析：
- 数据库中实际表名是 group_member
- 但实体类配置的是 @TableName("im_group_member")

解决方案：

修改 GroupMember.java：

修改前（错误）：
@TableName("im_group_member")
public class GroupMember {
    // ...
}

修改后（正确）：
@TableName("group_member")
public class GroupMember {
    // ...
}

检查所有实体类的表名映射：
@TableName("user")          // 确保与数据库一致
@TableName("friend")
@TableName("group_info")
@TableName("message")


----------------------------------------

问题15：MySQL 不支持 ADD COLUMN IF NOT EXISTS

错误信息：
You have an error in your SQL syntax near 'IF NOT EXISTS'

原因分析：
MySQL 不支持 ADD COLUMN IF NOT EXISTS 语法（PostgreSQL 支持）。

解决方案：

方案1：直接添加（首次初始化）

ALTER TABLE group_member
ADD COLUMN last_read_seq_id BIGINT DEFAULT 0 COMMENT '最后读取的消息序号';

方案2：检查列是否存在（脚本化部署）

先检查列是否存在：
SELECT COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'maisizhe'
AND TABLE_NAME = 'group_member'
AND COLUMN_NAME = 'last_read_seq_id';

如果不存在，再执行 ALTER：
ALTER TABLE group_member
ADD COLUMN last_read_seq_id BIGINT DEFAULT 0;


----------------------------------------

问题16：MySQL SOURCE 命令路径问题

错误信息：
Failed to open file 'C:\Users\...\init.sql', error: 2

原因分析：
- Windows 路径转义问题
- 路径中包含空格或特殊字符
- 文件路径不应包含分号

解决方案：

方案1：使用正斜杠
SOURCE C:/Users/zwd05/Desktop/Messager-main/code/database/init.sql

方案2：使用双反斜杠
SOURCE C:\\Users\\zwd05\\Desktop\\Messager-main\\code\\database\\init.sql

方案3：使用 PowerShell 管道（推荐）
Get-Content init.sql | mysql -u root -p maisizhe

注意：
- 路径不要加分号 ;
- 避免路径中包含中文或空格


----------------------------------------

问题17：group_member 表缺少字段

错误信息：
java.sql.SQLSyntaxErrorException: Unknown column 'last_read_seq_id' in 'field list'

原因分析：
数据库表结构与实体类不一致，缺少 last_read_seq_id 字段。

解决方案：

执行 ALTER TABLE 添加字段：
ALTER TABLE group_member
ADD COLUMN last_read_seq_id BIGINT DEFAULT 0 COMMENT '最后读取的消息序号' AFTER role;

验证字段已添加：
DESCRIBE group_member;


----------------------------------------

问题18：im_group 表字段名与实体类不匹配

错误信息：
java.sql.SQLSyntaxErrorException: Unknown column 'owner_id' in 'field list'

原因分析：
实体类使用 ownerId（驼峰命名），但数据库字段可能是 owner_user_id。

解决方案：

方案1：修改数据库字段名
ALTER TABLE im_group CHANGE owner_user_id owner_id BIGINT COMMENT '群主ID';

方案2：在实体类中指定列名
@TableField("owner_user_id")
private Long ownerId;


========================================
前后端联调问题
========================================

问题19：WebSocket 连接失败

错误信息：
WebSocket connection to 'ws://localhost:8080/ws?token=xxx' failed
WebSocket错误: Event {isTrusted: true, type: 'error', ...}

原因分析：
- 前端：使用查询参数传递 token：ws://localhost:8080/ws?token=xxx
- 后端：期望路径参数：@ServerEndpoint("/ws/{token}")
- 两种方式不匹配

解决方案：

修改前端 utils/websocket.ts：

修改前（错误）：
this.ws = new WebSocket(`${this.url}?token=${this.token}`);

修改后（正确）：
this.ws = new WebSocket(`${this.url}/${this.token}`);

后端保持不变：
@ServerEndpoint("/ws/{token}")
public class ChatWebSocketHandler {

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        // token 从路径参数获取
    }
}

测试连接：
在浏览器控制台手动测试：
const token = localStorage.getItem('token');
const ws = new WebSocket(`ws://localhost:8080/ws/${token}`);

ws.onopen = () => console.log('连接成功');
ws.onerror = (error) => console.error('连接错误:', error);


----------------------------------------

问题20：JWT Token 解析失败

错误信息：
io.jsonwebtoken.JwtException: JWT signature does not match locally computed signature

原因分析：
- 前后端使用的密钥不一致
- Token 被截断或损坏
- Token 已过期

解决方案：

步骤1：检查密钥配置

后端 application.yml：
app:
  jwt:
    secret: your-secret-key-at-least-256-bits-long
    expiration: 86400000  # 24小时

步骤2：前端检查 Token 完整性

utils/websocket.ts：
const token = localStorage.getItem('token');
console.log('Token长度:', token?.length);
console.log('Token前50字符:', token?.substring(0, 50));

if (!token) {
  console.error('Token不存在，请先登录');
  return;
}

步骤3：检查 Token 是否过期

解码 JWT（仅用于调试）：
function decodeJWT(token: string) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const payload = JSON.parse(jsonPayload);
    console.log('Token过期时间:', new Date(payload.exp * 1000));
    return payload;
  } catch (e) {
    console.error('Token解码失败:', e);
    return null;
  }
}


----------------------------------------

问题21：HTTP 请求 401 未授权

错误信息：
HTTP 401 Unauthorized

原因分析：
- Token 未携带
- Token 格式错误
- Token 已过期

解决方案：

检查请求拦截器：

utils/request.ts：
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

检查后端 Security 配置：

SecurityConfig.java：
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/api/auth/**").permitAll()  // 放行认证接口
        .anyRequest().authenticated()
        .and()
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
}


----------------------------------------

问题22：注册成功后跳转失败

现象：
注册成功后停留在注册页面，未跳转到登录页。

原因分析：
- 路由跳转逻辑错误
- 未正确处理注册成功的响应

解决方案：

修改 views/Register.vue：

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { register } from '@/api/auth';

const router = useRouter();

const handleRegister = async () => {
  try {
    const res = await register(formData);
    if (res.code === 200) {
      // 显示成功提示
      notify.success('注册成功，请登录');

      // 跳转到登录页
      router.push('/login');
    }
  } catch (error) {
    notify.error('注册失败: ' + error.message);
  }
};
</script>


========================================
WebSocket 问题
========================================

问题23：WebSocket 心跳断开

现象：
WebSocket 连接一段时间后自动断开。

原因分析：
- 未实现心跳机制
- 防火墙或代理超时
- 服务端主动关闭空闲连接

解决方案：

前端实现心跳：

utils/websocket.ts：
export class WebSocketManager {
  private heartbeatTimer: number | null = null;
  private readonly HEARTBEAT_INTERVAL = 30000; // 30秒

  connect() {
    this.ws = new WebSocket(`${this.url}/${this.token}`);

    this.ws.onopen = () => {
      console.log('WebSocket连接成功');
      this.startHeartbeat();
    };

    this.ws.onclose = () => {
      console.log('WebSocket连接关闭');
      this.stopHeartbeat();
      this.reconnect();
    };
  }

  private startHeartbeat() {
    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'HEARTBEAT', timestamp: Date.now() });
        console.log('发送心跳');
      }
    }, this.HEARTBEAT_INTERVAL);
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  private reconnect() {
    setTimeout(() => {
      console.log('尝试重连...');
      this.connect();
    }, 5000);
  }
}

后端处理心跳：

@OnMessage
public void onMessage(Session session, String message) {
    JSONObject json = JSON.parseObject(message);
    String type = json.getString("type");

    if ("HEARTBEAT".equals(type)) {
        // 回复心跳
        JSONObject response = new JSONObject();
        response.put("type", "HEARTBEAT_ACK");
        response.put("timestamp", System.currentTimeMillis());
        session.getAsyncRemote().sendText(response.toJSONString());
        return;
    }

    // 处理其他消息类型...
}


----------------------------------------

问题24：WebSocket 消息顺序错乱

现象：
接收到的消息顺序与发送顺序不一致。

原因分析：
- 异步推送导致时序问题
- 客户端未正确排序

解决方案：

后端使用 seq_id 保证顺序：

@Service
public class MessageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void sendMessage(Long groupId, Message message) {
        // 使用 Redis INCR 生成会话内连续序号
        String key = "message:seq:" + groupId;
        Long seqId = redisTemplate.opsForValue().increment(key);

        message.setSeqId(seqId);
        messageMapper.insert(message);

        // 推送到 WebSocket
        pushToGroup(groupId, message);
    }
}

前端按 seq_id 排序：

stores/chat.ts：
function addMessage(message: Message) {
  const index = messages.value.findIndex(m => m.id === message.id);

  if (index === -1) {
    // 新消息，按 seq_id 插入正确位置
    const insertIndex = messages.value.findIndex(m => m.seqId > message.seqId);
    if (insertIndex === -1) {
      messages.value.push(message);
    } else {
      messages.value.splice(insertIndex, 0, message);
    }
  }
}


----------------------------------------

问题25：WebSocket 关闭代码异常

错误信息：
WebSocket closed with code: 1011 (Internal Error)

原因分析：
使用了不存在的关闭代码 INTERNAL_ERROR。

解决方案：

使用标准的关闭代码：

正确的关闭代码：
const CloseCodes = {
  NORMAL: 1000,
  GOING_AWAY: 1001,
  PROTOCOL_ERROR: 1002,
  UNSUPPORTED_DATA: 1003,
  UNEXPECTED_CONDITION: 1011,  // 使用这个代替 INTERNAL_ERROR
};

关闭连接：
ws.close(CloseCodes.UNEXPECTED_CONDITION, 'Unexpected error');

标准关闭代码参考：
1000 - 正常关闭
1001 - 离开（页面关闭、服务器宕机）
1002 - 协议错误
1003 - 不支持的数据类型
1011 - 意外情况（服务器内部错误）


========================================
常见问题速查
========================================

启动相关问题：

问题                    快速解决
端口 8080 被占用        netstat -ano | findstr :8080 -> taskkill /F /PID <PID>
Redis 连接失败          启动 Redis：redis-server
MySQL 连接失败          启动 MySQL 服务：Start-Service MySQL80
Maven 命令找不到        安装 Maven 并配置环境变量
npm 命令找不到          安装 Node.js


后端常见问题：

问题                    快速解决
CORS 错误               使用 setAllowedOriginPatterns 代替 setAllowedOrigins
Bean 注入失败           检查 @Component、@Service 等注解
循环依赖                配置 allow-circular-references: true
配置修改未生效          执行 mvn clean spring-boot:run
表名不匹配              检查 @TableName 注解


前端常见问题：

问题                    快速解决
inject() 警告           不要在非组件文件中使用 Composition API hooks
TypeScript 警告         删除未使用变量或使用 _ 前缀
缓存问题                硬刷新 Ctrl+F5
路由跳转失败            检查 router.push() 调用


数据库常见问题：

问题                    快速解决
表不存在                执行 init.sql 初始化脚本
字段不存在              ALTER TABLE ADD COLUMN
SOURCE 命令失败         使用 Get-Content file.sql | mysql -u root -p
字段名不匹配            检查 @TableField 注解


WebSocket 常见问题：

问题                    快速解决
连接失败                检查 token 传递方式（路径参数 vs 查询参数）
频繁断开                实现心跳机制
消息乱序                使用 seq_id 排序
401 未授权              检查 Token 是否有效


========================================
问题统计
========================================

按类别统计：

类别          数量    占比
环境配置      5       20%
后端问题      5       20%
前端问题      3       12%
数据库问题    3       12%
前后端联调    3       12%
WebSocket    6       24%
总计         25      100%

========================================
2024年12月新增修复记录
========================================

问题26：前端 TypeScript 类型错误（未使用变量）

错误信息：
error TS6133: 'loading' is declared but its value is never read.
error TS6133: 'showFriendRequestModal' is declared but its value is never read.

原因分析：
Vite + TypeScript 构建时启用了严格检查模式，不允许声明未使用的变量。

解决方案：
方案1：删除未使用的变量（推荐）

修改 GroupSettingsPanel.vue：
// 删除未使用的 loading 变量
const actionLoading = ref(false);

修改 Contacts.vue：
// 删除未使用的 showFriendRequestModal
const friendRequests = ref<FriendRequestVO[]>([]);
const pendingRequestCount = ref(0);

方案2：使用下划线前缀标记故意不使用的变量
const _unusedVariable = someValue;


----------------------------------------

问题27：前端 TypeScript 隐式 any 类型错误

错误信息：
error TS7006: Parameter 'key' implicitly has an 'any' type.

原因分析：
事件处理函数参数缺少类型注解，TypeScript 无法推断类型。

解决方案：
修改 GroupSettingsPanel.vue：
// 添加类型注解
@select="(key: string) => handleMemberAction(key, member)"


----------------------------------------

问题28：前端 API 接口字段缺失

错误信息：
error TS2339: Property 'announcement' does not exist on type 'CreateGroupDTO'.

原因分析：
CreateGroupDTO 接口定义不完整，缺少 announcement 字段。

解决方案：
修改 api/group.ts：
export interface CreateGroupDTO {
  groupName: string;
  groupAvatar?: string;
  groupType: number;
  description?: string;
  announcement?: string;  // 添加缺失字段
}


----------------------------------------

问题29：添加好友直接添加而非请求模式

现象：
用户点击添加好友后直接建立好友关系，无需对方确认。

原因分析：
前端使用了 addFriend() 接口直接添加好友，而非 sendFriendRequest() 发送请求。

解决方案：
修改 Contacts.vue：

// 修改前（错误）
await addFriend({
  targetUserId: user.friendId,
  remark: ''
});

// 修改后（正确）
await sendFriendRequest(user.friendId, '');


----------------------------------------

问题30：添加好友缺少确认对话框

现象：
点击添加好友按钮后立即发送请求，容易误操作。

原因分析：
缺少二次确认机制，用户可能误触发送好友请求。

解决方案：
在 Contacts.vue 中添加确认对话框：

// 添加状态变量
const showConfirmAddModal = ref(false);
const confirmTargetUser = ref<FriendVO | null>(null);

// 修改发送请求逻辑
async function handleSendAddFriendRequest(user: FriendVO) {
  if (user.isFriend) {
    message.info('已经是好友了');
    return;
  }
  // 显示确认对话框
  showConfirmAddModal.value = true;
  confirmTargetUser.value = user;
}

// 确认发送请求
async function confirmSendRequest() {
  if (!confirmTargetUser.value) return;
  
  try {
    await sendFriendRequest(confirmTargetUser.value.friendId, '');
    message.success('已发送好友请求，请等待对方确认');
    // ...
  } finally {
    showConfirmAddModal.value = false;
    confirmTargetUser.value = null;
  }
}

模板中添加确认对话框组件：
<n-modal v-model:show="showConfirmAddModal" preset="card" title="发送好友请求">
  <!-- 显示目标用户信息 -->
  <template #footer>
    <n-button @click="showConfirmAddModal = false">取消</n-button>
    <n-button type="primary" @click="confirmSendRequest">发送请求</n-button>
  </template>
</n-modal>


----------------------------------------

问题31：缺少好友请求通知功能

现象：
用户收到好友请求后无法及时查看和处理。

原因分析：
前端缺少好友请求列表展示和处理界面。

解决方案：
在 Contacts.vue 中添加好友请求标签页：

// 添加好友请求列表
const friendRequests = ref<FriendRequestVO[]>([]);
const pendingRequestCount = ref(0);

// 加载好友请求
async function loadFriendRequests() {
  friendRequests.value = await getReceivedRequests();
  pendingRequestCount.value = friendRequests.value.length;
}

// 处理好友请求
async function handleAcceptRequest(request: FriendRequestVO) {
  await handleFriendRequest(request.id, true);
  await loadFriendRequests();
  await loadFriends();
}

async function handleRejectRequest(request: FriendRequestVO) {
  await handleFriendRequest(request.id, false);
  await loadFriendRequests();
}

模板中添加好友请求标签：
<n-tabs v-model:value="activeTab">
  <n-tab name="friends">好友列表</n-tab>
  <n-tab name="requests">
    好友请求
    <n-badge v-if="pendingRequestCount > 0" :value="pendingRequestCount" type="warning" />
  </n-tab>
</n-tabs>

<!-- 好友请求列表 -->
<div v-if="activeTab === 'requests'">
  <div v-for="request in friendRequests" :key="request.id">
    <!-- 显示请求信息 -->
    <n-button @click="handleAcceptRequest(request)">同意</n-button>
    <n-button @click="handleRejectRequest(request)">拒绝</n-button>
  </div>
</div>


========================================
问题统计（更新后）
========================================

按类别统计：

类别          数量    占比
环境配置      5       16%
后端问题      5       16%
前端问题      9       29%
数据库问题    3       10%
前后端联调    3       10%
WebSocket    6       19%
总计         31      100%


按严重程度统计：

级别      数量    说明
严重      5       导致系统无法启动或核心功能不可用
中等      12      影响部分功能但不阻塞主要流程
轻微      8       警告信息或体验优化类问题


========================================
已修复问题标记
========================================

以下问题已在代码中修复：

1. ✅ 问题7：WebSocketHandler 依赖注入失败
   - 修复方式：使用 @Component 注解注册 Bean
   - 状态：已修复

2. ✅ 问题8：循环依赖错误
   - 修复方式：在 application.yml 中配置 allow-circular-references: true
   - 状态：已修复（临时方案）

3. ✅ 问题11：Vue inject() 警告
   - 修复方式：在组件中正确使用 useMessage()
   - 状态：已修复

4. ✅ 问题19：WebSocket 连接失败
   - 修复方式：使用路径参数传递 token：ws://localhost:8080/ws/${token}
   - 状态：已修复

5. ✅ GroupController 用户ID参数问题
   - 问题描述：多个接口需要从URL参数获取用户ID，应该从JWT Token自动获取
   - 修复方式：添加 getCurrentUserId() 方法，从 SecurityContext 获取用户ID
   - 状态：已修复


========================================
新增功能记录
========================================

1. 好友请求处理功能
   - 发送好友请求
   - 处理好友请求（同意/拒绝）
   - 获取收到/发送的请求列表
   - 待处理请求数量统计
   - 取消好友请求

2. 群成员邀请功能
   - 单用户邀请
   - 批量邀请
   - 权限验证（仅管理员/群主可操作）

3. 日志和错误处理增强
   - 后端请求日志拦截器
   - 全局异常处理器增强
   - 前端日志管理工具
   - WebSocket 日志输出增强


========================================
最佳实践总结
========================================

1. 开发规范

- 修改配置后执行 mvn clean
- 提交代码前检查是否有编译错误
- 数据库变更同步更新实体类
- 前后端接口变更及时更新文档
- 使用 JWT Token 进行用户身份验证，避免在URL中传递用户ID

2. 调试技巧

- 使用浏览器开发者工具查看 Network 和 Console
- 后端日志级别设置为 DEBUG
- 使用 Postman 测试 API 接口
- WebSocket 使用浏览器控制台手动测试
- 检查 Redis 缓存状态：redis-cli ping

3. 问题排查思路

1. 看日志：后端日志、前端控制台、浏览器 Network
2. 查配置：数据库连接、Redis 配置、端口配置
3. 验环境：JDK、Node.js、Maven、MySQL、Redis 是否正常
4. 搜错误：复制错误信息到搜索引擎
5. 问 AI：提供完整错误信息和上下文


========================================
更新记录
========================================

日期          更新内容                        负责人
2026-06-20    初始版本，整理 25 个常见问题    MaiSiZhe Team
2026-06-20    修复 GroupController 用户ID获取方式，新增好友请求和群邀请功能    MaiSiZhe Team


持续更新中...

如有新问题，请及时补充到此文档。
