<template>
  <div class="login-container">
    <n-card title="麦思哲(MaiSiZhe) - 登录" class="login-card">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" />
        </n-form-item>
        
        <n-form-item label="密码" path="password">
          <n-input v-model:value="formData.password" type="password" placeholder="请输入密码" />
        </n-form-item>
        
        <n-space vertical :size="12">
          <n-button type="primary" block @click="handleLogin" :loading="loading">
            登录
          </n-button>
          <n-button block @click="goToRegister">
            还没有账号？去注册
          </n-button>
        </n-space>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { login } from '@/api/auth';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const message = useMessage();
const userStore = useUserStore();

const formRef = ref<FormInst | null>(null);
const loading = ref(false);

const formData = ref({
  username: '',
  password: ''
});

const rules: FormRules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur'
  }
};

/**
 * 处理登录
 */
async function handleLogin() {
  try {
    await formRef.value?.validate();
    
    loading.value = true;
    
    const response = await login({
      username: formData.value.username,
      password: formData.value.password
    });
    
    // 保存Token和用户信息
    userStore.setToken(response.token, response.userInfo);
    
    message.success('登录成功');
    
    // 跳转到聊天页面
    router.push('/');
    
  } catch (error: any) {
    console.error('登录失败:', error);
    message.error(error.message || '登录失败，请检查用户名和密码');
  } finally {
    loading.value = false;
  }
}

/**
 * 跳转到注册页
 */
function goToRegister() {
  router.push('/register');
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
</style>
