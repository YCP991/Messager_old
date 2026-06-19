import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// API响应类型
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data } = response.data;
    
    if (code === 200) {
      return data;
    } else if (code === 401) {
      // Token过期，跳转到登录页
      localStorage.removeItem('token');
      window.location.href = '/login';
      return Promise.reject(new Error(message));
    } else {
      // 显示错误消息（使用console代替naive-ui的message）
      console.error('API错误:', message || '请求失败');
      return Promise.reject(new Error(message));
    }
  },
  (error) => {
    console.error('响应错误:', error);
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.error('未授权，请重新登录');
          localStorage.removeItem('token');
          window.location.href = '/login';
          break;
        case 403:
          console.error('权限不足');
          break;
        case 404:
          console.error('请求的资源不存在');
          break;
        case 500:
          console.error('服务器错误');
          break;
        default:
          console.error(error.message || '网络错误');
      }
    } else {
      console.error('网络连接失败');
    }
    
    return Promise.reject(error);
  }
);

export default request;
