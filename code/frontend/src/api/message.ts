import request from '@/utils/request';

/**
 * 消息类型枚举
 */
export enum MessageType {
  TEXT = 0,      // 文本消息
  IMAGE = 1,     // 图片消息
  FILE = 2,      // 文件消息
  VOICE = 3,     // 语音消息
  VIDEO = 4,     // 视频消息
  CARD = 5,      // 名片消息
  SYSTEM = 99   // 系统消息
}

/**
 * 消息状态枚举
 */
export enum MessageStatus {
  SENDING = 0,   // 发送中
  SENT = 1,      // 已发送
  DELIVERED = 2, // 已送达
  READ = 3,      // 已读
  FAILED = 4     // 发送失败
}

/**
 * 消息VO
 */
export interface MessageVO {
  id: number;
  chatId: string;
  seqId: number;
  fromUid: number;
  fromName: string;
  fromAvatar: string;
  toUid?: number;
  groupId?: number;
  content: string;
  msgType: MessageType;
  msgStatus: MessageStatus; // 消息状态
  mentionedUsers?: number[];
  isRecalled?: number;
  createTime: string;
}

/**
 * 发送消息请求DTO
 */
export interface SendMessageDTO {
  toUid?: number;
  groupId?: number;
  content: string;
  msgType?: MessageType;
  mentionedUsers?: number[];
  clientMsgId: string;
}

/**
 * 发送私聊消息
 */
export function sendPrivateMessage(fromUid: number, data: SendMessageDTO): Promise<MessageVO> {
  return request.post('/message/private', data, { params: { fromUid } });
}

/**
 * 发送群聊消息
 */
export function sendGroupMessage(fromUid: number, data: SendMessageDTO): Promise<MessageVO> {
  return request.post('/message/group', data, { params: { fromUid } });
}

/**
 * 获取私聊历史消息
 */
export function getPrivateHistory(userId: number, peerId: number, limit: number = 50): Promise<MessageVO[]> {
  return request.get('/message/private/history', { params: { userId, peerId, limit } });
}

/**
 * 获取群聊历史消息
 */
export function getGroupHistory(groupId: number, limit: number = 50): Promise<MessageVO[]> {
  return request.get('/message/group/history', { params: { groupId, limit } });
}

/**
 * 撤回消息
 */
export function recallMessage(messageId: number, userId: number): Promise<void> {
  return request.post('/message/recall', null, { params: { messageId, userId } });
}
