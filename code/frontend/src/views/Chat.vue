<template>
  <div class="private-chat-container">
    <n-layout has-sider style="height: 100vh;">
      <!-- 侧边栏 -->
      <n-layout-sider bordered :width="280" show-trigger>
        <div class="sidebar-header">
          <n-space justify="space-between" align="center">
            <n-avatar :size="40" :src="userStore.userAvatar" />
            <span class="user-name">{{ userStore.userName }}</span>
            <n-space>
              <n-button quaternary circle size="small" @click="goToContacts">
                <template #icon>
                  <n-icon :component="PeopleOutline" />
                </template>
              </n-button>
              <n-button quaternary circle size="small" @click="handleLogout">
                <template #icon>
                  <n-icon :component="LogOutOutline" />
                </template>
              </n-button>
            </n-space>
          </n-space>
        </div>
        
        <n-divider style="margin: 8px 0" />
        
        <!-- 搜索 -->
        <div style="padding: 0 16px; margin-bottom: 8px;">
          <n-input 
            v-model:value="searchKeyword" 
            placeholder="搜索好友或群组"
            size="small"
            clearable
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
        </div>

        <!-- 标签页 -->
        <n-tabs v-model:value="activeTab" type="line" size="small">
          <n-tab name="private">私聊</n-tab>
          <n-tab name="group">
            群聊
            <n-button quaternary circle size="tiny" @click.stop="showCreateGroupModal = true" style="margin-left: 4px;">
              <template #icon>
                <n-icon :component="AddOutline" />
              </template>
            </n-button>
          </n-tab>
        </n-tabs>
        
        <!-- 私聊列表 -->
        <div v-if="activeTab === 'private'" class="session-list">
          <n-scrollbar>
            <n-list hoverable clickable>
              <n-list-item
                v-for="friend in filteredFriends"
                :key="friend.friendId"
                @click="selectFriend(friend)"
                :class="{ active: currentFriendId === friend.friendId }"
              >
                <n-thing>
                  <template #header>
                    <n-space justify="space-between">
                      <span>{{ friend.remark || friend.realName || friend.username }}</span>
                      <n-badge :dot="friend.isOnline === 1" :color="onlineColor" />
                    </n-space>
                  </template>
                  <template #description>
                    <n-text depth="3" style="font-size: 12px;">
                      {{ friend.isOnline === 1 ? '在线' : '离线' }}
                    </n-text>
                  </template>
                </n-thing>
              </n-list-item>
              <n-list-item v-if="filteredFriends.length === 0">
                <n-empty description="暂无好友" size="small">
                  <template #extra>
                    <n-button size="small" @click="goToContacts">添加好友</n-button>
                  </template>
                </n-empty>
              </n-list-item>
            </n-list>
          </n-scrollbar>
        </div>
        
        <!-- 群聊列表 -->
        <div v-else class="session-list">
          <n-scrollbar>
            <n-list hoverable clickable>
              <n-list-item
                v-for="group in filteredGroups"
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
              <n-list-item v-if="filteredGroups.length === 0">
                <n-empty description="暂无群组" size="small" />
              </n-list-item>
            </n-list>
          </n-scrollbar>
        </div>
      </n-layout-sider>

      <!-- 主区域：聊天窗口 -->
      <n-layout-content>
        <!-- 私聊 -->
        <div v-if="currentFriendId && activeTab === 'private'" class="chat-main">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <n-space align="center">
              <n-badge :dot="currentFriend?.isOnline === 1" :color="onlineColor" :offset="[-5, 5]">
                <n-avatar :size="40" :src="currentFriend?.avatar || '/avatar/default.png'" round />
              </n-badge>
              <div>
                <h3>{{ currentFriend?.remark || currentFriend?.realName || currentFriend?.username }}</h3>
                <n-text depth="3" style="font-size: 12px;">
                  {{ currentFriend?.isOnline === 1 ? '在线' : '离线' }}
                </n-text>
              </div>
            </n-space>
            <n-space>
              <n-tag :type="wsStatus ? 'success' : 'warning'" size="small">
                {{ wsStatus ? '● 在线' : '○ 离线' }}
              </n-tag>
            </n-space>
          </div>

          <n-divider style="margin: 8px 0" />

          <!-- 消息列表 -->
          <div class="message-list">
            <n-scrollbar ref="scrollbarRef">
              <div v-for="msg in currentMessages" :key="msg.id" class="message-item">
                <ChatBubble
                  :message="msg"
                  :is-self="msg.fromUid === userStore.userId"
                  @recall="handleRecallMessage"
                />
              </div>
            </n-scrollbar>
          </div>

          <!-- 消息输入框 -->
          <div class="message-input">
            <MessageInput @send="handleSendMessage" />
          </div>
        </div>

        <!-- 群聊 -->
        <div v-else-if="currentGroupId && activeTab === 'group'" class="chat-main">
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
            <n-space>
              <n-button quaternary @click="showGroupMembers = true">
                <template #icon>
                  <n-icon :component="PeopleOutline" />
                </template>
                成员
              </n-button>
              <n-button quaternary @click="openGroupSettings">
                <template #icon>
                  <n-icon :component="SettingsOutline" />
                </template>
                设置
              </n-button>
            </n-space>
          </div>

          <n-divider style="margin: 8px 0" />

          <div class="message-list">
            <n-scrollbar ref="groupScrollbarRef">
              <div v-for="msg in currentMessages" :key="msg.id" class="message-item">
                <ChatBubble
                  :message="msg"
                  :is-self="msg.fromUid === userStore.userId"
                  @recall="handleRecallMessage"
                />
              </div>
            </n-scrollbar>
          </div>

          <div class="message-input">
            <MessageInput @send="handleSendGroupMessage" />
          </div>
        </div>

        <!-- 未选择会话时的提示 -->
        <div v-else class="empty-state">
          <n-empty description="请选择一个好友或群组开始聊天" />
        </div>
      </n-layout-content>
    </n-layout>

    <!-- 群成员弹窗 -->
    <n-modal v-model:show="showGroupMembers" preset="card" title="群成员" style="width: 600px;">
      <n-scrollbar style="max-height: 400px;">
        <n-list>
          <n-list-item v-for="member in groupMembers" :key="member.userId">
            <n-space>
              <n-avatar :size="40" :src="member.avatar || '/avatar/default.png'" round />
              <div>
                <n-text>{{ member.groupNickname || member.realName || member.username }}</n-text>
                <n-tag v-if="member.role === 2" type="warning" size="small" style="margin-left: 8px;">群主</n-tag>
                <n-tag v-if="member.role === 1" type="info" size="small" style="margin-left: 8px;">管理员</n-tag>
              </div>
            </n-space>
          </n-list-item>
        </n-list>
      </n-scrollbar>
    </n-modal>

    <!-- 创建群聊弹窗 -->
    <CreateGroupModal 
      v-model:show="showCreateGroupModal" 
      @created="handleGroupCreated"
    />

    <!-- 群设置面板 -->
    <GroupSettingsPanel
      v-model:show="showGroupSettings"
      :group-info="currentGroupInfo"
      :group-id="currentGroupId || 0"
      @quit="handleQuitGroup"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useMessage } from 'naive-ui';
import { 
  SearchOutline, 
  PeopleOutline, 
  LogOutOutline,
  AddOutline,
  SettingsOutline
} from '@vicons/ionicons5';
import { useUserStore } from '@/stores/user';
import { useChatStore } from '@/stores/chat';
import WebSocketManager from '@/utils/websocket';
import { getUserGroups, type GroupInfo } from '@/api/auth';
import { getGroupHistory, recallMessage } from '@/api/message';
import { getFriends, type FriendVO } from '@/api/friend';
import ChatBubble from '@/components/ChatBubble.vue';
import MessageInput from '@/components/MessageInput.vue';
import CreateGroupModal from '@/components/CreateGroupModal.vue';
import GroupSettingsPanel from '@/components/GroupSettingsPanel.vue';

const router = useRouter();
const route = useRoute();
const message = useMessage();
const userStore = useUserStore();
const chatStore = useChatStore();

// 状态
const friends = ref<FriendVO[]>([]);
const groups = ref<GroupInfo[]>([]);
const activeTab = ref<'private' | 'group'>('private');
const searchKeyword = ref('');
const onlineColor = '#18a058';

// WebSocket
const ws = ref<WebSocketManager | null>(null);
const wsStatus = ref(false);

// 当前聊天对象
const currentFriendId = ref<number | null>(null);
const currentFriend = ref<FriendVO | null>(null);
const currentGroupId = ref<number | null>(null);

// 滚动条引用
const scrollbarRef = ref<any>(null);
const groupScrollbarRef = ref<any>(null);

// 群成员
const showGroupMembers = ref(false);
const groupMembers = ref<any[]>([]);

// 创建群组相关
const showCreateGroupModal = ref(false);

// 群设置面板
const showGroupSettings = ref(false);
const currentGroupInfo = ref<any>(null);

// 计算属性
const currentGroupName = computed(() => {
  const group = groups.value.find(g => g.id === currentGroupId.value);
  return group?.groupName || '';
});

const currentGroupType = computed(() => {
  const group = groups.value.find(g => g.id === currentGroupId.value);
  return group?.groupType || 0;
});

const currentMessages = computed(() => {
  if (activeTab.value === 'private' && currentFriendId.value) {
    const chatId = `p_${Math.min(userStore.userId, currentFriendId.value)}_${Math.max(userStore.userId, currentFriendId.value)}`;
    return chatStore.getMessages(chatId);
  } else if (activeTab.value === 'group' && currentGroupId.value) {
    const chatId = `g_${currentGroupId.value}`;
    return chatStore.getMessages(chatId);
  }
  return [];
});

const filteredFriends = computed(() => {
  if (!searchKeyword.value.trim()) {
    return friends.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return friends.value.filter(f => 
    (f.username?.toLowerCase().includes(keyword)) ||
    (f.realName?.toLowerCase().includes(keyword)) ||
    (f.remark?.toLowerCase().includes(keyword))
  );
});

const filteredGroups = computed(() => {
  if (!searchKeyword.value.trim()) {
    return groups.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return groups.value.filter(g => 
    g.groupName?.toLowerCase().includes(keyword)
  );
});

// 初始化
onMounted(async () => {
  await Promise.all([loadFriends(), loadGroups()]);
  initWebSocket();
  
  // 检查路由参数
  if (route.query.friendId) {
    const friendId = parseInt(route.query.friendId as string);
    const friend = friends.value.find(f => f.friendId === friendId);
    if (friend) {
      selectFriend(friend);
    }
  }
});

onUnmounted(() => {
  if (ws.value) {
    ws.value.disconnect();
  }
});

// 监听路由变化
watch(() => route.query, (query) => {
  if (query.friendId) {
    const friendId = parseInt(query.friendId as string);
    const friend = friends.value.find(f => f.friendId === friendId);
    if (friend) {
      activeTab.value = 'private';
      selectFriend(friend);
    }
  }
});

// 加载好友列表
async function loadFriends() {
  try {
    friends.value = await getFriends();
  } catch (error) {
    console.error('加载好友列表失败:', error);
  }
}

// 加载群组列表
async function loadGroups() {
  try {
    groups.value = await getUserGroups();
  } catch (error) {
    console.error('加载群组列表失败:', error);
    message.error('加载群组列表失败');
  }
}

// 初始化WebSocket
function initWebSocket() {
  const token = userStore.token;
  ws.value = new WebSocketManager(`ws://localhost:8080/ws`, token);
  
  ws.value.onConnected(() => {
    console.log('WebSocket连接成功');
    wsStatus.value = true;
    chatStore.setWebSocketStatus(true);
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
  });
}

// 处理WebSocket消息
function handleWebSocketMessage(wsMessage: any) {
  console.log('收到WebSocket消息:', wsMessage);
  
  if (wsMessage.type === 'PRIVATE_MESSAGE' || wsMessage.type === 'GROUP_MESSAGE') {
    const msgData = wsMessage.data;
    
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
    const isCurrentChat = activeTab.value === 'private' 
      ? msgData.fromUid === currentFriendId.value || msgData.toUid === currentFriendId.value
      : msgData.groupId === currentGroupId.value;
      
    if (isCurrentChat) {
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

// 选择好友
async function selectFriend(friend: FriendVO) {
  currentFriendId.value = friend.friendId;
  currentFriend.value = friend;
  activeTab.value = 'private';
  chatStore.setCurrentChat(`p_${Math.min(userStore.userId, friend.friendId)}_${Math.max(userStore.userId, friend.friendId)}`);
  
  // TODO: 加载私聊历史消息
  // await loadPrivateHistory(friend.friendId);
  
  nextTick(() => {
    scrollToBottom();
  });
}

// 选择群组
async function selectGroup(group: GroupInfo) {
  currentGroupId.value = group.id;
  activeTab.value = 'group';
  chatStore.setCurrentChat(`g_${group.id}`);
  
  // 加载历史消息
  try {
    const history = await getGroupHistory(group.id, 50);
    chatStore.messages.clear();
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
  } catch (error) {
    console.error('加载历史消息失败:', error);
  }
  
  nextTick(() => {
    scrollToBottom();
  });
}

// 打开群设置面板
function openGroupSettings() {
  const group = groups.value.find(g => g.id === currentGroupId.value);
  if (group) {
    currentGroupInfo.value = group;
    showGroupSettings.value = true;
  }
}

// 处理群创建成功
async function handleGroupCreated(group: any) {
  await loadGroups();
  // 自动选择新创建的群组
  selectGroup(group);
}

// 退出群聊后刷新
async function handleQuitGroup() {
  await loadGroups();
  currentGroupId.value = null;
  currentGroupInfo.value = null;
  chatStore.setCurrentChat('');
}

// 发送私聊消息
function handleSendMessage(content: string) {
  if (!currentFriendId.value || !ws.value) {
    message.warning('请先选择好友');
    return;
  }
  
  ws.value.send({
    type: 'PRIVATE_MESSAGE',
    data: {
      toUid: currentFriendId.value,
      content: content
    }
  });
  
  nextTick(() => {
    scrollToBottom();
  });
}

// 发送群聊消息
function handleSendGroupMessage(content: string) {
  if (!currentGroupId.value || !ws.value) {
    message.warning('请先选择群组');
    return;
  }
  
  ws.value.send({
    type: 'GROUP_MESSAGE',
    data: {
      groupId: currentGroupId.value,
      content: content
    }
  });
  
  nextTick(() => {
    scrollToBottom();
  });
}

// 滚动到底部
function scrollToBottom() {
  const scrollbar = activeTab.value === 'private' ? scrollbarRef.value : groupScrollbarRef.value;
  if (scrollbar) {
    scrollbar.scrollTo({ top: 999999, behavior: 'smooth' });
  }
}

// 撤回消息
async function handleRecallMessage(messageId: number) {
  try {
    await recallMessage(messageId, userStore.userId);
    message.success('消息已撤回');
    // 从聊天记录中移除该消息
    chatStore.removeMessage(messageId);
  } catch (error) {
    console.error('撤回消息失败:', error);
    message.error('撤回消息失败');
  }
}

// 跳转到通讯录
function goToContacts() {
  router.push('/contacts');
}

// 退出登录
function handleLogout() {
  userStore.logout();
  chatStore.clear();
  if (ws.value) {
    ws.value.disconnect();
  }
  router.push('/login');
  message.success('已退出登录');
}

// 获取群组类型名称
function getGroupTypeName(type: number): string {
  const types: Record<number, string> = {
    0: '普通群',
    1: '班级群',
    2: '课程群'
  };
  return types[type] || '未知';
}

// 获取群组类型颜色
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
.private-chat-container {
  width: 100%;
  height: 100vh;
}

.sidebar-header {
  padding: 16px;
}

.user-name {
  font-weight: bold;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-list {
  height: calc(100vh - 250px);
}

.active {
  background-color: #e8f5e9;
}

.chat-main {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.chat-header {
  padding: 16px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  font-size: 18px;
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
