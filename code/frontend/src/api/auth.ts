import request from '@/utils/request';

// 登录请求参数
export interface LoginParams {
  username: string;
  password: string;
}

// 注册请求参数
export interface RegisterParams {
  username: string;
  password: string;
  studentNo: string;
  realName: string;
  classNo?: string;
  department?: string;
  role?: number;
}

// 用户信息
export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  avatar: string;
  role: number;
  classNo?: string;  // 与后端保持一致
  isOnline?: number; // 在线状态:0-离线,1-在线
  lastOnlineTime?: string; // 最后在线时间
}

// 登录响应
export interface LoginResponse {
  token: string;
  userInfo: UserInfo;
}

/**
 * 用户登录
 */
export function login(data: LoginParams): Promise<LoginResponse> {
  return request.post('/auth/login', data);
}

/**
 * 用户注册
 */
export function register(data: RegisterParams): Promise<void> {
  return request.post('/auth/register', data);
}

/**
 * 获取用户信息
 */
export function getUserInfo(userId: number): Promise<UserInfo> {
  return request.get(`/auth/user/${userId}`);
}

/**
 * 更新用户资料
 */
export function updateProfile(userId: number, data: UserInfo): Promise<void> {
  return request.put(`/auth/user/${userId}`, data);
}

/**
 * 获取群组列表
 * 从JWT Token中获取当前用户ID
 */
export interface GroupInfo {
  id: number;
  groupName: string;
  groupAvatar: string;
  groupType: number;
  memberCount: number;
  myRole?: number;
}

export function getUserGroups(): Promise<GroupInfo[]> {
  return request.get('/group/my-groups');
}
