/**
 * WebSocket消息类型定义
 */
export interface WSMessage {
  type: string;
  data?: any;
  clientMsgId?: string;
  timestamp?: number;
}

/**
 * WebSocket管理器
 * 负责连接管理、自动重连、心跳保活、消息队列
 */
class WebSocketManager {
  private url: string;
  private token: string;
  private ws: WebSocket | null = null;
  private reconnectCount: number = 0;
  private maxReconnectCount: number = 10;
  private reconnectDelay: number = 3000; // 3秒
  private heartbeatInterval: number = 30000; // 30秒
  private reconnectTimer: number | null = null;
  private heartbeatTimer: number | null = null;
  private messageQueue: WSMessage[] = [];
  private isConnected: boolean = false;
  
  // 事件回调
  private onConnectedCallback: (() => void) | null = null;
  private onDisconnectedCallback: (() => void) | null = null;
  private onMessageCallback: ((message: WSMessage) => void) | null = null;
  private onErrorCallback: ((error: Event) => void) | null = null;

  constructor(url: string, token: string) {
    this.url = url;
    this.token = token;
  }

  /**
   * 连接WebSocket
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        console.log('正在连接WebSocket...');
        this.ws = new WebSocket(`${this.url}?token=${this.token}`);

        this.ws.onopen = () => {
          console.log('WebSocket连接成功');
          this.isConnected = true;
          this.reconnectCount = 0;
          this.startHeartbeat();
          this.flushMessageQueue();
          
          if (this.onConnectedCallback) {
            this.onConnectedCallback();
          }
          resolve();
        };

        this.ws.onmessage = (event) => {
          try {
            const message: WSMessage = JSON.parse(event.data);
            console.log('收到消息:', message);
            
            if (this.onMessageCallback) {
              this.onMessageCallback(message);
            }
          } catch (error) {
            console.error('解析消息失败:', error);
          }
        };

        this.ws.onclose = () => {
          console.log('WebSocket连接关闭');
          this.isConnected = false;
          this.stopHeartbeat();
          
          if (this.onDisconnectedCallback) {
            this.onDisconnectedCallback();
          }
          
          this.attemptReconnect();
        };

        this.ws.onerror = (error) => {
          console.error('WebSocket错误:', error);
          
          if (this.onErrorCallback) {
            this.onErrorCallback(error);
          }
          reject(error);
        };

      } catch (error) {
        console.error('创建WebSocket连接失败:', error);
        reject(error);
      }
    });
  }

  /**
   * 发送消息
   */
  send(message: WSMessage): void {
    if (!message.clientMsgId) {
      message.clientMsgId = `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    }
    
    if (!message.timestamp) {
      message.timestamp = Date.now();
    }

    if (this.isConnected && this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
      console.log('发送消息:', message);
    } else {
      console.warn('WebSocket未连接，消息已加入队列');
      this.messageQueue.push(message);
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.stopHeartbeat();
    this.clearReconnectTimer();
    
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    
    this.isConnected = false;
    this.messageQueue = [];
  }

  /**
   * 尝试重连
   */
  private attemptReconnect(): void {
    if (this.reconnectCount >= this.maxReconnectCount) {
      console.error('达到最大重连次数，停止重连');
      return;
    }

    this.clearReconnectTimer();
    
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectCount); // 指数退避
    console.log(`${delay / 1000}秒后尝试第${this.reconnectCount + 1}次重连...`);
    
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectCount++;
      this.connect().catch(err => {
        console.error('重连失败:', err);
      });
    }, delay);
  }

  /**
   * 启动心跳
   */
  private startHeartbeat(): void {
    this.stopHeartbeat();
    
    this.heartbeatTimer = window.setInterval(() => {
      this.send({ type: 'HEARTBEAT' });
      console.debug('发送心跳');
    }, this.heartbeatInterval);
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 清空重连定时器
   */
  private clearReconnectTimer(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  /**
   * 刷新消息队列
   */
  private flushMessageQueue(): void {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift();
      if (message && this.ws) {
        this.ws.send(JSON.stringify(message));
        console.log('发送队列消息:', message);
      }
    }
  }

  /**
   * 获取连接状态
   */
  getConnectStatus(): boolean {
    return this.isConnected;
  }

  /**
   * 设置连接成功回调
   */
  onConnected(callback: () => void): void {
    this.onConnectedCallback = callback;
  }

  /**
   * 设置断开连接回调
   */
  onDisconnected(callback: () => void): void {
    this.onDisconnectedCallback = callback;
  }

  /**
   * 设置消息接收回调
   */
  onMessage(callback: (message: WSMessage) => void): void {
    this.onMessageCallback = callback;
  }

  /**
   * 设置错误回调
   */
  onError(callback: (error: Event) => void): void {
    this.onErrorCallback = callback;
  }
}

export default WebSocketManager;
