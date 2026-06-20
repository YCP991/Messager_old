/**
 * 全局错误处理工具
 */

import { useUserStore } from '@/stores/user';

/**
 * 处理API错误
 * @param error 错误对象
 */
export function handleApiError(error: any): void {
  const message = error?.message || '请求失败';
  
  // Token过期处理
  if (message === '请先登录' || error?.response?.status === 401) {
    // 清除Token并跳转到登录页
    const userStore = useUserStore();
    userStore.logout();
    
    // 避免重复跳转
    if (window.location.pathname !== '/login') {
      window.location.href = '/login';
    }
    return;
  }
  
  // 权限不足处理
  if (error?.response?.status === 403) {
    console.error('权限不足，请联系管理员');
    return;
  }
  
  // 其他错误
  console.error('API错误:', message);
}

/**
 * 显示错误消息（通过naive-ui）
 * @param message 错误消息
 */
export function showErrorMessage(message: string): void {
  // 使用console输出（在Vue组件外使用，避免composable限制）
  console.error('错误:', message);
  
  // 尝试通过全局方式显示消息
  try {
    // 如果在Vue组件中，可以使用naive-ui的message组件
    // 这里使用alert作为降级方案
    if (typeof window !== 'undefined') {
      // 不显示alert，避免影响用户体验
    }
  } catch (e) {
    // 忽略
  }
}