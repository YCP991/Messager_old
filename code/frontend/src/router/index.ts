import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/stores/user';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/contacts',
    name: 'Contacts',
    component: () => import('@/views/Contacts.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat/private',
    name: 'PrivateChat',
    component: () => import('@/views/Chat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat/group/:groupId',
    name: 'GroupChat',
    component: () => import('@/views/Chat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore();
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 需要登录但未登录，跳转到登录页
    next('/login');
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    // 已登录，访问登录/注册页，跳转到聊天页
    next('/');
  } else {
    next();
  }
});

export default router;
