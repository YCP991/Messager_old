import { defineStore } from 'pinia';
import { ref } from 'vue';

/**
 * 消息类型
 */
export interface Message {
  id: number;
  chatId: string;
  seqId: number;
  fromUid: number;
  fromName?: string;
  fromAvatar?: string;
  toUid?: number;
  groupId?: number;
  content: string;
  msgType: number; // 0-文本,1-图片,2-文件,3-AI摘要,99-AI思考中
  mentionedUsers?: number[];
  isRecalled?: number;
  createTime: string;
}

/**
 * 会话类型
 */
export interface ChatSession {
  chatId: string;
  type: 'private' | 'group';
  targetId: number; // 私聊: userId, 群聊: groupId
  name: string;
  avatar: string;
  lastMessage?: string;
  lastMessageTime?: string;
  unreadCount: number;
}

/**
 * 聊天状态管理
 */
export const useChatStore = defineStore('chat', () => {
  // State
  const sessions = ref<ChatSession[]>([]);
  const currentChatId = ref<string>('');
  const messages = ref<Map<string, Message[]>>(new Map());
  const isWebSocketConnected = ref(false);

  // Actions
  /**
   * 设置当前会话
   */
  function setCurrentChat(chatId: string) {
    currentChatId.value = chatId;
    
    // 标记该会话消息为已读
    const session = sessions.value.find(s => s.chatId === chatId);
    if (session) {
      session.unreadCount = 0;
    }
  }

  /**
   * 添加消息
   */
  function addMessage(message: Message) {
    const chatId = message.chatId;
    
    if (!messages.value.has(chatId)) {
      messages.value.set(chatId, []);
    }
    
    const chatMessages = messages.value.get(chatId)!;
    
    // 二分法插入排序(保证seqId顺序)
    const index = findInsertIndex(chatMessages, message.seqId);
    chatMessages.splice(index, 0, message);
    
    // 更新会话最后一条消息
    updateSessionLastMessage(chatId, message);
  }

  /**
   * 获取会话消息列表
   */
  function getMessages(chatId: string): Message[] {
    return messages.value.get(chatId) || [];
  }

  /**
   * 添加或更新会话
   */
  function addOrUpdateSession(session: ChatSession) {
    const index = sessions.value.findIndex(s => s.chatId === session.chatId);
    
    if (index >= 0) {
      sessions.value[index] = { ...sessions.value[index], ...session };
    } else {
      sessions.value.push(session);
    }
  }

  /**
   * 设置WebSocket连接状态
   */
  function setWebSocketStatus(connected: boolean) {
    isWebSocketConnected.value = connected;
  }

  /**
   * 增加未读数
   */
  function incrementUnreadCount(chatId: string) {
    if (chatId === currentChatId.value) {
      return; // 当前会话不增加未读数
    }
    
    const session = sessions.value.find(s => s.chatId === chatId);
    if (session) {
      session.unreadCount++;
    }
  }

  /**
   * 清空所有数据
   */
  function clear() {
    sessions.value = [];
    currentChatId.value = '';
    messages.value.clear();
    isWebSocketConnected.value = false;
  }

  /**
   * 移除消息（用于撤回）
   */
  function removeMessage(messageId: number) {
    for (const [chatId, chatMessages] of messages.value.entries()) {
      const index = chatMessages.findIndex(m => m.id === messageId);
      if (index !== -1) {
        chatMessages.splice(index, 1);
        // 更新会话最后一条消息
        const lastMessage = chatMessages[chatMessages.length - 1];
        if (lastMessage) {
          updateSessionLastMessage(chatId, lastMessage);
        }
        break;
      }
    }
  }

  /**
   * 二分法查找插入位置
   */
  function findInsertIndex(messages: Message[], seqId: number): number {
    let left = 0;
    let right = messages.length;
    
    while (left < right) {
      const mid = Math.floor((left + right) / 2);
      if (messages[mid].seqId < seqId) {
        left = mid + 1;
      } else {
        right = mid;
      }
    }
    
    return left;
  }

  /**
   * 更新会话最后一条消息
   */
  function updateSessionLastMessage(chatId: string, message: Message) {
    const session = sessions.value.find(s => s.chatId === chatId);
    if (session) {
      session.lastMessage = message.content;
      session.lastMessageTime = message.createTime;
    }
  }

  return {
    // State
    sessions,
    currentChatId,
    messages,
    isWebSocketConnected,
    // Actions
    setCurrentChat,
    addMessage,
    getMessages,
    addOrUpdateSession,
    setWebSocketStatus,
    incrementUnreadCount,
    clear,
    removeMessage
  };
});
