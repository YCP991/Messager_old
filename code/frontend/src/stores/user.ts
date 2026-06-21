import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserInfo } from '@/api/auth';
import { getUserInfo } from '@/api/auth';

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);
  const isRefreshing = ref(false);

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
      // 尝试刷新用户信息
      refreshUserInfo().catch(() => {
        // 刷新失败，可能Token已过期
        console.warn('用户信息刷新失败');
      });
    }
  }

  /**
   * 刷新用户信息
   */
  async function refreshUserInfo(): Promise<void> {
    if (isRefreshing.value) {
      return;
    }
    
    try {
      isRefreshing.value = true;
      // 假设用户ID存储在localStorage中
      const storedUserId = localStorage.getItem('userId');
      if (storedUserId) {
        const info = await getUserInfo(parseInt(storedUserId));
        userInfo.value = info;
      }
    } catch (error) {
      console.error('刷新用户信息失败:', error);
      // 如果刷新失败，可能是Token过期，清除登录状态
      logout();
    } finally {
      isRefreshing.value = false;
    }
  }

  /**
   * 保存用户ID到localStorage
   */
  function saveUserId(userId: number): void {
    localStorage.setItem('userId', userId.toString());
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
    restoreFromStorage,
    refreshUserInfo,
    saveUserId
  };
});
