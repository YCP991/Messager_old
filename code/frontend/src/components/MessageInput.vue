<template>
  <div class="message-input-container">
    <n-input
      v-model:value="content"
      type="textarea"
      :placeholder="placeholder"
      :autosize="{ minRows: 2, maxRows: 6 }"
      @keydown="handleKeydown"
    />
    
    <div class="input-actions">
      <n-space justify="space-between" align="center">
        <n-text depth="3" style="font-size: 12px;">
          Enter发送 · Shift+Enter换行
        </n-text>
        
        <n-button
          type="primary"
          :disabled="!content.trim()"
          :loading="sending"
          @click="handleSend"
        >
          发送
        </n-button>
      </n-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useMessage } from 'naive-ui';

const message = useMessage();

const props = defineProps<{
  placeholder?: string;
}>();

const emit = defineEmits<{
  (e: 'send', content: string): void;
}>();

const content = ref('');
const sending = ref(false);

/**
 * 处理键盘事件
 */
function handleKeydown(event: KeyboardEvent) {
  // Enter发送，Shift+Enter换行
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    handleSend();
  }
}

/**
 * 发送消息
 */
async function handleSend() {
  const text = content.value.trim();
  
  if (!text) {
    message.warning('请输入消息内容');
    return;
  }
  
  if (text.length > 2000) {
    message.error('消息内容不能超过2000字');
    return;
  }
  
  try {
    sending.value = true;
    
    // 触发父组件的send事件
    emit('send', text);
    
    // 清空输入框
    content.value = '';
    
  } catch (error) {
    console.error('发送失败:', error);
    message.error('发送失败，请稍后重试');
  } finally {
    sending.value = false;
  }
}
</script>

<style scoped>
.message-input-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-actions {
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
</style>
