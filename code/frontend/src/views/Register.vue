<template>
  <div class="register-container">
    <n-card title="麦思哲(MaiSiZhe) - 注册" class="register-card">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名(4-20位)" />
        </n-form-item>
        
        <n-form-item label="密码" path="password">
          <n-input v-model:value="formData.password" type="password" placeholder="请输入密码(6-20位)" />
        </n-form-item>
        
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="formData.confirmPassword" type="password" placeholder="请再次输入密码" />
        </n-form-item>
        
        <n-form-item label="学号" path="studentNo">
          <n-input v-model:value="formData.studentNo" placeholder="请输入学号/工号" />
        </n-form-item>
        
        <n-form-item label="姓名" path="realName">
          <n-input v-model:value="formData.realName" placeholder="请输入真实姓名" />
        </n-form-item>
        
        <n-form-item label="班级" path="classNo">
          <n-input v-model:value="formData.classNo" placeholder="请输入班级号(可选)" />
        </n-form-item>
        
        <n-space vertical :size="12">
          <n-button type="primary" block @click="handleRegister" :loading="loading">
            注册
          </n-button>
          <n-button block @click="goToLogin">
            已有账号？去登录
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
import { register } from '@/api/auth';

const router = useRouter();
const message = useMessage();

const formRef = ref<FormInst | null>(null);
const loading = ref(false);

const formData = ref({
  username: '',
  password: '',
  confirmPassword: '',
  studentNo: '',
  realName: '',
  classNo: ''
});

// 自定义验证规则：确认密码
const validatePasswordSame = (_rule: any, value: string) => {
  return value === formData.value.password;
};

const rules: FormRules = {
  username: {
    required: true,
    min: 4,
    max: 20,
    message: '用户名长度为4-20位',
    trigger: 'blur'
  },
  password: {
    required: true,
    min: 6,
    max: 20,
    message: '密码长度为6-20位',
    trigger: 'blur'
  },
  confirmPassword: {
    required: true,
    validator: validatePasswordSame,
    message: '两次密码输入不一致',
    trigger: 'blur'
  },
  studentNo: {
    required: true,
    message: '请输入学号',
    trigger: 'blur'
  },
  realName: {
    required: true,
    message: '请输入姓名',
    trigger: 'blur'
  }
};

/**
 * 处理注册
 */
async function handleRegister() {
  try {
    await formRef.value?.validate();
    
    loading.value = true;
    
    await register({
      username: formData.value.username,
      password: formData.value.password,
      studentNo: formData.value.studentNo,
      realName: formData.value.realName,
      classNo: formData.value.classNo || undefined,
      role: 0 // 学生
    });
    
    message.success('注册成功，请登录');
    
    // 跳转到登录页
    router.push('/login');
    
  } catch (error: any) {
    console.error('注册失败:', error);
    message.error(error.message || '注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

/**
 * 跳转到登录页
 */
function goToLogin() {
  router.push('/login');
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 500px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
</style>
