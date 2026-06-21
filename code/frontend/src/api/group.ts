import request from '@/utils/request';

/**
 * 群组信息
 */
export interface GroupVO {
  id: number;
  groupName: string;
  groupAvatar: string;
  groupType: number;
  memberCount: number;
  myRole?: number;
  announcement?: string;
  createTime?: string;
  // 新增字段
  creatorId?: number;
  creatorName?: string;
  classNo?: string;
  courseCode?: string;
  maxMembers?: number;
  isDisbanded?: number;
  disbandTime?: string;
  groupTypeDesc?: string;
}

/**
 * 群成员信息
 */
export interface GroupMemberVO {
  userId: number;
  username: string;
  realName: string;
  avatar: string;
  role: number; // 0普通成员 1管理员 2群主
  groupNickname?: string;
  joinTime?: string;
  // 新增字段
  id?: number;
  inviterId?: number; // 邀请人ID
  inviterName?: string; // 邀请人姓名
  roleDesc?: string;
  isOnline?: boolean;
  isMuted?: boolean; // 是否被禁言
  muteExpireTime?: string; // 禁言截止时间
  isSelfMuted?: boolean; // 是否设置了免打扰
}

/**
 * 创建群组请求
 */
export interface CreateGroupDTO {
  groupName: string;
  groupAvatar?: string;
  groupType: number; // 0普通群 1班级群 2课程群
  description?: string;
  announcement?: string;
  // 新增字段
  classNo?: string;
  courseCode?: string;
  maxMembers?: number;
}

/**
 * 获取我的群组列表
 */
export function getMyGroups(): Promise<GroupVO[]> {
  return request.get('/group/my-groups');
}

/**
 * 获取群组详情
 */
export function getGroupDetail(groupId: number): Promise<GroupVO> {
  return request.get(`/group/${groupId}`);
}

/**
 * 创建群组
 */
export function createGroup(data: CreateGroupDTO): Promise<GroupVO> {
  return request.post('/group/create', data);
}

/**
 * 加入群组
 */
export function joinGroup(groupId: number): Promise<void> {
  return request.post(`/group/${groupId}/join`);
}

/**
 * 退出群组
 */
export function quitGroup(groupId: number): Promise<void> {
  return request.post(`/group/${groupId}/quit`);
}

/**
 * 获取群成员列表
 */
export function getGroupMembers(groupId: number): Promise<GroupMemberVO[]> {
  return request.get(`/group/${groupId}/members`);
}

/**
 * 踢出群成员
 */
export function kickMember(groupId: number, targetUserId: number): Promise<void> {
  return request.post(`/group/${groupId}/kick`, null, { params: { targetUserId } });
}

/**
 * 更新群公告
 */
export function updateAnnouncement(groupId: number, announcement: string): Promise<void> {
  return request.put(`/group/${groupId}/announcement`, null, { params: { announcement } });
}

/**
 * 解散群组
 */
export function disbandGroup(groupId: number): Promise<void> {
  return request.post(`/group/${groupId}/disband`);
}

/**
 * 转让群主
 */
export function transferGroupOwner(groupId: number, toUid: number): Promise<void> {
  return request.post(`/group/${groupId}/transfer`, null, { params: { toUid } });
}

/**
 * 禁言成员
 */
export function muteMember(groupId: number, targetUserId: number, muteMinutes: number = 30): Promise<void> {
  return request.post(`/group/${groupId}/mute`, null, { params: { targetUserId, muteMinutes } });
}

/**
 * 解除禁言
 */
export function unmuteMember(groupId: number, targetUserId: number): Promise<void> {
  return request.post(`/group/${groupId}/unmute`, null, { params: { targetUserId } });
}

/**
 * 邀请用户加入群组
 */
export function inviteMember(groupId: number, targetUserId: number): Promise<void> {
  return request.post(`/group/${groupId}/invite`, null, { params: { targetUserId } });
}

/**
 * 批量邀请用户加入群组
 */
export function inviteMembers(groupId: number, userIds: number[]): Promise<void> {
  return request.post(`/group/${groupId}/invite/batch`, null, { params: { userIds } });
}
