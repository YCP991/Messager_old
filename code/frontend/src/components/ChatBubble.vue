<template>
  <div :class="['chat-bubble', isSelf ? 'self' : 'other']" @contextmenu.prevent="showMenu = true">
    <!-- 头像 -->
    <n-avatar
      v-if="!isSelf"
      :size="40"
      :src="message.fromAvatar || '/avatar/default.png'"
      class="avatar"
    />

    <!-- 消息内容区 -->
    <div class="bubble-content">
      <!-- 发送者姓名(仅群聊) -->
      <div v-if="!isSelf" class="sender-name">
        <n-text depth="3" style="font-size: 12px;">
          {{ message.fromName || '未知用户' }}
        </n-text>
        <n-text depth="5" style="font-size: 10px; margin-left: 8px;">
          {{ formatTime(message.createTime) }}
        </n-text>
      </div>

      <!-- 消息气泡 -->
      <div :class="['bubble', getMessageTypeClass(message.msgType)]">
        <!-- AI思考中 -->
        <div v-if="message.msgType === 99" class="ai-thinking">
          <n-spin size="small" />
          <span style="margin-left: 8px;">AI正在思考中...</span>
        </div>

        <!-- 普通文本消息 -->
        <div v-else-if="message.msgType === 0" class="text-message">
          {{ message.content }}
        </div>

        <!-- AI摘要 -->
        <div v-else-if="message.msgType === 3" class="ai-summary">
          <n-icon :component="DocumentTextOutline" style="margin-right: 4px;" />
          <div v-html="renderMarkdown(message.content)"></div>
        </div>

        <!-- 其他类型消息 -->
        <div v-else class="unknown-message">
          [暂不支持的消息类型]
        </div>
      </div>

      <!-- 时间戳(自己发送的消息) -->
      <div v-if="isSelf" class="timestamp">
        <n-text depth="5" style="font-size: 10px;">
          {{ formatTime(message.createTime) }}
        </n-text>
      </div>
    </div>

    <!-- 自己的头像 -->
    <n-avatar
      v-if="isSelf"
      :size="40"
      :src="userStore.userAvatar"
      class="avatar"
    />

    <!-- 消息操作菜单 -->
    <n-dropdown
      v-model:show="showMenu"
      trigger="manual"
      placement="bottom-start"
      :x="menuX"
      :y="menuY"
      :options="menuOptions"
      @select="handleMenuSelect"
      @clickoutside="showMenu = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { DocumentTextOutline } from '@vicons/ionicons5';
import MarkdownIt from 'markdown-it';
import dayjs from 'dayjs';
import type { Message } from '@/stores/chat';

const userStore = useUserStore();

const props = defineProps<{
  message: Message;
  isSelf: boolean;
}>();

const emit = defineEmits<{
  (e: 'recall', messageId: number): void;
}>();

const showMenu = ref(false);
const menuX = ref(0);
const menuY = ref(0);

const md = new MarkdownIt();

/**
 * 格式化时间
 */
function formatTime(time: string): string {
  return dayjs(time).format('HH:mm');
}

/**
 * 渲染Markdown
 */
function renderMarkdown(content: string): string {
  return md.render(content);
}

/**
 * 获取消息类型样式类
 */
function getMessageTypeClass(msgType: number): string {
  const types: Record<number, string> = {
    0: 'text-bubble',
    3: 'summary-bubble',
    99: 'ai-bubble'
  };
  return types[msgType] || 'text-bubble';
}

// 右键菜单选项
const menuOptions = computed(() => {
  const options = [];

  // 自己发送的消息可以撤回（2分钟内）
  if (props.isSelf) {
    const messageTime = dayjs(props.message.createTime);
    const now = dayjs();
    const diffMinutes = now.diff(messageTime, 'minute');

    if (diffMinutes <= 2) {
      options.push({
        label: '撤回',
        key: 'recall'
      });
    }
  }

  // 所有人都可以复制
  options.push({
    label: '复制',
    key: 'copy'
  });

  return options;
});

/**
 * 处理菜单选择
 */
function handleMenuSelect(key: string) {
  showMenu.value = false;

  switch (key) {
    case 'recall':
      emit('recall', props.message.id);
      break;
    case 'copy':
      navigator.clipboard.writeText(props.message.content);
      break;
  }
}
</script>

<style scoped>
.chat-bubble {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.chat-bubble.self {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
}

.bubble-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.chat-bubble.self .bubble-content {
  align-items: flex-end;
}

.sender-name {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.bubble {
  padding: 10px 14px;
  border-radius: 8px;
  word-wrap: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.text-bubble {
  background: #fff;
  color: #333;
}

.chat-bubble.self .text-bubble {
  background: #18a058;
  color: #fff;
}

.ai-bubble {
  background: #f0f0ff;
  color: #666;
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-bubble {
  background: #fffbe6;
  color: #333;
  border-left: 3px solid #faad14;
}

.summary-bubble :deep(p) {
  margin: 4px 0;
}

.summary-bubble :deep(ul) {
  margin: 4px 0;
  padding-left: 20px;
}

.unknown-message {
  color: #999;
  font-style: italic;
}

.timestamp {
  margin-top: 4px;
}
</style>
