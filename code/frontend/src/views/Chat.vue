<template>
  <div class="chat-container">
    <n-layout has-sider style="height: 100vh;">
      <!-- 侧边栏：会话列表 -->
      <n-layout-sider bordered :width="280" show-trigger>
        <div class="sidebar-header">
          <n-space justify="space-between" align="center">
            <n-avatar :size="40" :src="userStore.userAvatar" />
            <span class="user-name">{{ userStore.userName }}</span>
            <n-button size="small" @click="handleLogout">退出</n-button>
          </n-space>
        </div>
        
        <n-divider style="margin: 8px 0" />
        
        <!-- 群组列表 -->
        <div class="group-list">
          <n-scrollbar>
            <n-list hoverable clickable>
              <n-list-item
                v-for="group in groups"
                :key="group.id"
                @click="selectGroup(group)"
                :class="{ active: currentGroupId === group.id }"
              >
                <n-thing>
                  <template #header>
                    <n-space justify="space-between">
                      <span>{{ group.groupName }}</span>
                      <n-tag size="small" :type="getGroupTypeColor(group.groupType)">
                        {{ getGroupTypeName(group.groupType) }}
                      </n-tag>
                    </n-space>
                  </template>
                  <template #description>
                    <n-text depth="3" style="font-size: 12px;">
                      {{ group.memberCount }}人
                    </n-text>
                  </template>
                </n-thing>
              </n-list-item>
            </n-list>
          </n-scrollbar>
        </div>
      </n-layout-sider>

      <!-- 主区域：聊天窗口 -->
      <n-layout-content>
        <div v-if="currentGroupId" class="chat-main">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <n-space align="center">
              <h3>{{ currentGroupName }}</h3>
              <n-tag :type="getGroupTypeColor(currentGroupType)">
                {{ getGroupTypeName(currentGroupType) }}
              </n-tag>
              <n-text depth="3" style="font-size: 12px;">
                {{ wsStatus ? '● 在线' : '○ 离线' }}
              </n-text>
            </n-space>
          </div>

          <n-divider style="margin: 8px 0" />

          <!-- 消息列表 -->
          <div class="message-list">
            <n-scrollbar ref="scrollbarRef">
              <div v-for="msg in messages" :key="msg.id" class="message-item">
                <ChatBubble :message="msg" :is-self="msg.fromUid === userStore.userId" />
              </div>
            </n-scrollbar>
          </div>

          <!-- 消息输入框 -->
          <div class="message-input">
            <MessageInput @send="handleSendMessage" />
          </div>
        </div>

        <!-- 未选择会话时的提示 -->
        <div v-else class="empty-state">
          <n-empty description="请选择一个群组开始聊天" />
        </div>
      </n-layout-content>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useUserStore } from '@/stores/user';
import { useChatStore } from '@/stores/chat';
import WebSocketManager from '@/utils/websocket';
import { getUserGroups, getGroupHistory } from '@/api/auth';
import type { GroupInfo } from '@/api/auth';
import ChatBubble from '@/components/ChatBubble.vue';
import MessageInput from '@/components/MessageInput.vue';

const router = useRouter();
const message = useMessage();
const userStore = useUserStore();
const chatStore = useChatStore();

const groups = ref<GroupInfo[]>([]);
const currentGroupId = ref<number | null>(null);
const ws = ref<WebSocketManager | null>(null);
const wsStatus = ref(false);
const scrollbarRef = ref<any>(null);

// 当前群组信息
const currentGroupName = computed(() => {
  const group = groups.value.find(g => g.id === currentGroupId.value);
  return group?.groupName || '';
});

const currentGroupType = computed(() => {
  const group = groups.value.find(g => g.id === currentGroupId.value);
  return group?.groupType || 0;
});

// 当前会话的消息列表
const messages = computed(() => {
  if (!currentGroupId.value) return [];
  const chatId = `g_${currentGroupId.value}`;
  return chatStore.getMessages(chatId);
});

/**
 * 初始化
 */
onMounted(async () => {
  // 加载群组列表
  await loadGroups();
  
  // 初始化WebSocket
  initWebSocket();
});

onUnmounted(() => {
  // 断开WebSocket
  if (ws.value) {
    ws.value.disconnect();
  }
});

/**
 * 加载群组列表
 */
async function loadGroups() {
  try {
    groups.value = await getUserGroups(userStore.userId);
    
    if (groups.value.length > 0) {
      // 自动选择第一个群组
      selectGroup(groups.value[0]);
    }
  } catch (error) {
    console.error('加载群组失败:', error);
    message.error('加载群组失败');
  }
}

/**
 * 初始化WebSocket
 */
function initWebSocket() {
  const token = userStore.token;
  ws.value = new WebSocketManager(`ws://localhost:8080/ws`, token);
  
  ws.value.onConnected(() => {
    console.log('WebSocket连接成功');
    wsStatus.value = true;
    chatStore.setWebSocketStatus(true);
    message.success('实时通信已连接');
  });
  
  ws.value.onDisconnected(() => {
    console.log('WebSocket断开连接');
    wsStatus.value = false;
    chatStore.setWebSocketStatus(false);
  });
  
  ws.value.onMessage((wsMessage) => {
    handleWebSocketMessage(wsMessage);
  });
  
  ws.value.connect().catch(err => {
    console.error('WebSocket连接失败:', err);
    message.error('实时通信连接失败');
  });
}

/**
 * 处理WebSocket消息
 */
function handleWebSocketMessage(wsMessage: any) {
  console.log('收到WebSocket消息:', wsMessage);
  
  if (wsMessage.type === 'GROUP_MESSAGE') {
    const msgData = wsMessage.data;
    
    // 添加到聊天store
    chatStore.addMessage({
      id: msgData.id,
      chatId: msgData.chatId,
      seqId: msgData.seqId,
      fromUid: msgData.fromUid,
      fromName: msgData.fromName,
      fromAvatar: msgData.fromAvatar,
      groupId: msgData.groupId,
      content: msgData.content,
      msgType: msgData.msgType,
      createTime: msgData.createTime
    });
    
    // 如果是当前会话，滚动到底部
    if (msgData.groupId === currentGroupId.value) {
      nextTick(() => {
        scrollToBottom();
      });
    } else {
      // 其他会话增加未读数
      chatStore.incrementUnreadCount(msgData.chatId);
    }
  } else if (wsMessage.type === 'HEARTBEAT' && wsMessage.data === 'pong') {
    // 心跳响应，无需处理
  }
}

/**
 * 选择群组
 */
async function selectGroup(group: GroupInfo) {
  currentGroupId.value = group.id;
  chatStore.setCurrentChat(`g_${group.id}`);
  
  // 加载历史消息
  try {
    const history = await getGroupHistory({ groupId: group.id, limit: 50 });
    
    // 清空旧消息
    chatStore.messages.clear();
    
    // 添加历史消息
    history.forEach((msg: any) => {
      chatStore.addMessage({
        id: msg.id,
        chatId: msg.chatId,
        seqId: msg.seqId,
        fromUid: msg.fromUid,
        fromName: msg.fromName,
        fromAvatar: msg.fromAvatar,
        groupId: msg.groupId,
        content: msg.content,
        msgType: msg.msgType,
        createTime: msg.createTime
      });
    });
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom();
    });
  } catch (error) {
    console.error('加载历史消息失败:', error);
  }
}

/**
 * 发送消息
 */
function handleSendMessage(content: string) {
  if (!currentGroupId.value || !ws.value) {
    message.warning('请先选择群组');
    return;
  }
  
  // 通过WebSocket发送
  ws.value.send({
    type: 'GROUP_MESSAGE',
    data: {
      groupId: currentGroupId.value,
      content: content
    }
  });
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom();
  });
}

/**
 * 滚动到底部
 */
function scrollToBottom() {
  if (scrollbarRef.value) {
    scrollbarRef.value.scrollTo({ top: 999999, behavior: 'smooth' });
  }
}

/**
 * 退出登录
 */
function handleLogout() {
  userStore.logout();
  chatStore.clear();
  if (ws.value) {
    ws.value.disconnect();
  }
  router.push('/login');
  message.success('已退出登录');
}

/**
 * 获取群组类型名称
 */
function getGroupTypeName(type: number): string {
  const types: Record<number, string> = {
    0: '普通群',
    1: '班级群',
    2: '课程群'
  };
  return types[type] || '未知';
}

/**
 * 获取群组类型颜色
 */
function getGroupTypeColor(type: number): 'default' | 'primary' | 'success' {
  const colors: Record<number, 'default' | 'primary' | 'success'> = {
    0: 'default',
    1: 'primary',
    2: 'success'
  };
  return colors[type] || 'default';
}
</script>

<style scoped>
.chat-container {
  width: 100%;
  height: 100vh;
}

.sidebar-header {
  padding: 16px;
}

.user-name {
  font-weight: bold;
  flex: 1;
}

.group-list {
  height: calc(100vh - 80px);
}

.active {
  background-color: #f0f0f0;
}

.chat-main {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.chat-header {
  padding: 16px;
  background: #fff;
}

.message-list {
  flex: 1;
  overflow: hidden;
  padding: 16px;
  background: #f5f5f5;
}

.message-item {
  margin-bottom: 12px;
}

.message-input {
  padding: 16px;
  background: #fff;
  border-top: 1px solid #e0e0e0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>
