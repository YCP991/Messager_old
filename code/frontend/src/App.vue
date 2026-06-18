<template>
  <n-config-provider :theme="theme">
    <n-message-provider>
      <n-dialog-provider>
        <router-view />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { darkTheme } from 'naive-ui';
import type { GlobalTheme } from 'naive-ui';

// 主题配置(null为浅色模式，darkTheme为深色模式)
const theme = ref<GlobalTheme | null>(null);
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 
    'Helvetica Neue', Arial, 'PingFang SC', 'Hiragino Sans GB', 
    'Microsoft YaHei', sans-serif;
}

#app {
  width: 100%;
  height: 100vh;
}
</style>
