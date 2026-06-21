import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

// 扩展 InternalAxiosRequestConfig 接口以添加 metadata 属性
declare module 'axios' {
  interface InternalAxiosRequestConfig {
    metadata?: {
      requestId: string;
      startTime: number;
    };
  }
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 错误类型枚举
enum ErrorType {
  NETWORK_ERROR = 'NETWORK_ERROR',
  TIMEOUT_ERROR = 'TIMEOUT_ERROR',
  AUTH_ERROR = 'AUTH_ERROR',
  PERMISSION_ERROR = 'PERMISSION_ERROR',
  NOT_FOUND_ERROR = 'NOT_FOUND_ERROR',
  SERVER_ERROR = 'SERVER_ERROR',
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  BUSINESS_ERROR = 'BUSINESS_ERROR',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR'
}

// 错误信息接口
interface ErrorInfo {
  type: ErrorType;
  code: number;
  message: string;
  requestId?: string;
  details?: any;
}

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 生成请求ID
 */
function generateRequestId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
}

/**
 * 处理Token过期
 */
function handleTokenExpired(): void {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  // 避免重复跳转
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

/**
 * 分类错误类型
 */
function classifyError(error: any): ErrorInfo {
  const status = error.response?.status;
  const code = error.response?.data?.code || status || 0;
  const message = error.response?.data?.message || error.message || '未知错误';
  const requestId = error.response?.headers?.['x-request-id'];
  const details = error.response?.data;

  if (error.code === 'ECONNABORTED') {
    return { type: ErrorType.TIMEOUT_ERROR, code: 0, message: '请求超时，请稍后重试', requestId };
  }

  if (!error.response) {
    return { type: ErrorType.NETWORK_ERROR, code: 0, message: '网络连接失败，请检查网络', requestId };
  }

  switch (status) {
    case 400:
      return { type: ErrorType.VALIDATION_ERROR, code, message, requestId, details };
    case 401:
      return { type: ErrorType.AUTH_ERROR, code, message: '未授权，请重新登录', requestId };
    case 403:
      return { type: ErrorType.PERMISSION_ERROR, code, message: '权限不足，无法访问', requestId };
    case 404:
      return { type: ErrorType.NOT_FOUND_ERROR, code, message: '请求的资源不存在', requestId };
    case 500:
      return { type: ErrorType.SERVER_ERROR, code, message: '服务器内部错误', requestId };
    default:
      if (code >= 400 && code < 500) {
        return { type: ErrorType.BUSINESS_ERROR, code, message, requestId, details };
      }
      return { type: ErrorType.UNKNOWN_ERROR, code, message, requestId };
  }
}

/**
 * 格式化日志输出
 */
function logRequest(config: InternalAxiosRequestConfig, requestId: string): void {
  const method = config.method?.toUpperCase() || 'GET';
  const url = config.url || '';
  const params = config.params ? JSON.stringify(config.params) : '';
  const data = config.data ? JSON.stringify(config.data) : '';
  
  console.log(`[${requestId}] >>> ${method} ${url}${params ? ' | params: ' + params : ''}${data ? ' | data: ' + data : ''}`);
}

function logResponse(response: AxiosResponse, requestId: string, duration: number): void {
  const status = response.status;
  const code = response.data?.code || 0;
  
  if (status >= 400) {
    console.warn(`[${requestId}] <<< ${status} (${code}) | ${duration}ms | ${response.data?.message || 'Error'}`);
  } else {
    console.log(`[${requestId}] <<< ${status} (${code}) | ${duration}ms`);
  }
}

function logError(errorInfo: ErrorInfo, requestId: string, duration: number): void {
  const { type, code, message, details } = errorInfo;
  
  if (type === ErrorType.NETWORK_ERROR || type === ErrorType.TIMEOUT_ERROR || type === ErrorType.SERVER_ERROR) {
    console.error(`[${requestId}] <<< ${type} | ${duration}ms | ${message}`, details || '');
  } else {
    console.warn(`[${requestId}] <<< ${type} (${code}) | ${duration}ms | ${message}`, details || '');
  }
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 生成请求ID
    const requestId = generateRequestId();
    config.headers['X-Request-Id'] = requestId;
    config.metadata = { requestId, startTime: Date.now() };
    
    // 记录请求日志
    logRequest(config, requestId);
    
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    console.error('请求配置错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const requestId = response.config.metadata?.requestId || 'unknown';
    const startTime = response.config.metadata?.startTime || Date.now();
    const duration = Date.now() - startTime;
    
    const { code, message, data } = response.data;
    
    // 记录响应日志
    logResponse(response, requestId, duration);
    
    if (code === 200) {
      return data;
    } else if (code === 401) {
      // Token过期，跳转到登录页
      handleTokenExpired();
      return Promise.reject(new Error(message || '未授权'));
    } else {
      // 业务错误
      console.warn(`[${requestId}] 业务错误 (${code}): ${message}`);
      return Promise.reject(new Error(message || '请求失败'));
    }
  },
  (error) => {
    const requestId = error.config?.metadata?.requestId || 'unknown';
    const startTime = error.config?.metadata?.startTime || Date.now();
    const duration = Date.now() - startTime;
    
    // 分类错误
    const errorInfo = classifyError(error);
    
    // 记录错误日志
    logError(errorInfo, requestId, duration);
    
    // 特殊处理
    if (errorInfo.type === ErrorType.AUTH_ERROR) {
      handleTokenExpired();
    }
    
    // 返回格式化的错误信息
    return Promise.reject({
      ...errorInfo,
      originalError: error
    });
  }
);

export default request;
export type { ErrorType, ErrorInfo };
