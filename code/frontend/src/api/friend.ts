import request from '@/utils/request';

/**
 * 好友信息VO
 */
export interface FriendVO {
  friendId: number;
  username: string;
  realName: string;
  avatar: string;
  classNo?: string;
  department?: string;
  remark?: string;
  isPinned: number;
  isMuted: number; // 是否免打扰:0-否,1-是
  sortOrder: number;
  isOnline: number;
  createTime?: string;
  isFriend?: boolean; // 是否已是好友（用于搜索结果）
}

/**
 * 添加好友请求
 */
export interface AddFriendDTO {
  targetUserId: number;
  remark?: string;
}

/**
 * 更新好友备注请求
 */
export interface UpdateFriendRemarkDTO {
  friendId: number;
  remark: string;
}

/**
 * 更新好友排序请求
 */
export interface UpdateFriendSortDTO {
  friendId: number;
  sortOrder: number;
  isPinned: number;
}

/**
 * 获取好友列表
 */
export function getFriends(): Promise<FriendVO[]> {
  return request.get('/friend/list');
}

/**
 * 搜索用户
 */
export function searchUsers(keyword: string): Promise<FriendVO[]> {
  return request.get('/friend/search', { params: { keyword } });
}

/**
 * 获取好友详情
 */
export function getFriendDetail(friendId: number): Promise<FriendVO> {
  return request.get(`/friend/${friendId}`);
}

/**
 * 添加好友
 */
export function addFriend(data: AddFriendDTO): Promise<void> {
  return request.post('/friend/add', data);
}

/**
 * 删除好友
 */
export function deleteFriend(friendId: number): Promise<void> {
  return request.delete(`/friend/${friendId}`);
}

/**
 * 更新好友备注
 */
export function updateFriendRemark(data: UpdateFriendRemarkDTO): Promise<void> {
  return request.put('/friend/remark', data);
}

/**
 * 更新好友排序/置顶状态
 */
export function updateFriendSort(data: UpdateFriendSortDTO): Promise<void> {
  return request.put('/friend/sort', data);
}

/**
 * 批量更新好友排序
 */
export function batchUpdateFriendSort(data: UpdateFriendSortDTO[]): Promise<void> {
  return request.put('/friend/batch-sort', data);
}

/**
 * 检查是否为好友关系
 */
export function checkFriend(friendId: number): Promise<boolean> {
  return request.get(`/friend/check/${friendId}`);
}

// ==================== 好友请求相关API ====================

/**
 * 好友请求VO
 */
export interface FriendRequestVO {
  id: number;
  fromUserId: number;
  fromUsername: string;
  fromRealName: string;
  fromAvatar: string;
  remark: string;
  status: number;
  statusDesc: string;
  createTime: string;
  expiresTime?: string; // 过期时间
  isOnline?: number; // 在线状态
}

/**
 * 发送好友请求
 */
export function sendFriendRequest(targetUserId: number, remark?: string): Promise<void> {
  return request.post('/friend/request/send', { targetUserId, remark });
}

/**
 * 处理好友请求
 */
export function handleFriendRequest(requestId: number, accept: boolean): Promise<void> {
  return request.post('/friend/request/handle', null, { params: { requestId, accept } });
}

/**
 * 获取收到的好友请求列表
 */
export function getReceivedRequests(): Promise<FriendRequestVO[]> {
  return request.get('/friend/request/received');
}

/**
 * 获取发送的好友请求列表
 */
export function getSentRequests(): Promise<FriendRequestVO[]> {
  return request.get('/friend/request/sent');
}

/**
 * 获取待处理的好友请求数量
 */
export function getPendingRequestCount(): Promise<number> {
  return request.get('/friend/request/pending-count');
}

/**
 * 取消好友请求
 */
export function cancelFriendRequest(requestId: number): Promise<void> {
  return request.post('/friend/request/cancel', null, { params: { requestId } });
}
