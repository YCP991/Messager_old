import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserInfo } from '@/api/auth';

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);

  // Getters
  const isLoggedIn = computed(() => !!token.value);
  const userId = computed(() => userInfo.value?.id || 0);
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '');
  const userAvatar = computed(() => userInfo.value?.avatar || '/avatar/default.png');
  const userRole = computed(() => userInfo.value?.role || 0);

  // Actions
  /**
   * 设置Token和用户信息
   */
  function setToken(newToken: string, info?: UserInfo) {
    token.value = newToken;
    localStorage.setItem('token', newToken);
    
    if (info) {
      userInfo.value = info;
    }
  }

  /**
   * 设置用户信息
   */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info;
  }

  /**
   * 登出
   */
  function logout() {
    token.value = '';
    userInfo.value = null;
    localStorage.removeItem('token');
  }

  /**
   * 从localStorage恢复登录状态
   */
  function restoreFromStorage() {
    const storedToken = localStorage.getItem('token');
    if (storedToken) {
      token.value = storedToken;
      // TODO: 可以调用API获取最新用户信息
    }
  }

  return {
    // State
    token,
    userInfo,
    // Getters
    isLoggedIn,
    userId,
    userName,
    userAvatar,
    userRole,
    // Actions
    setToken,
    setUserInfo,
    logout,
    restoreFromStorage
  };
});
