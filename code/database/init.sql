-- ============================================
-- 麦思哲(MaiSiZhe)数据库初始化脚本
-- MySQL 8.0+
-- 版本: v3.0 (优化版)
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `maisizhe` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `maisizhe`;

-- ============================================
-- 1. 用户模块
-- ============================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '用户ID(雪花算法生成)',
  `username` varchar(50) NOT NULL COMMENT '用户名(登录用)',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `student_no` varchar(20) DEFAULT NULL COMMENT '学号/工号',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT '/avatar/default.png' COMMENT '头像URL',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `class_no` varchar(20) DEFAULT NULL COMMENT '班级号(学生)',
  `department` varchar(50) DEFAULT NULL COMMENT '院系',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色:0-学生,1-教师,2-管理员',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
  `is_online` tinyint NOT NULL DEFAULT 0 COMMENT '在线状态:0-离线,1-在线',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) USING BTREE COMMENT '用户名唯一索引',
  UNIQUE KEY `uk_student_no` (`student_no`) USING BTREE COMMENT '学号唯一索引',
  KEY `idx_class_no` (`class_no`) USING BTREE COMMENT '班级号索引(用于班级群组建)',
  KEY `idx_email` (`email`) USING BTREE COMMENT '邮箱索引',
  KEY `idx_phone` (`phone`) USING BTREE COMMENT '手机号索引',
  KEY `idx_deleted` (`deleted`) USING BTREE COMMENT '逻辑删除索引',
  KEY `idx_status` (`status`) USING BTREE COMMENT '状态索引',
  KEY `idx_is_online` (`is_online`) USING BTREE COMMENT '在线状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 好友表
DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注名',
  `is_pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶:0-否,1-是',
  `is_muted` tinyint NOT NULL DEFAULT 0 COMMENT '是否免打扰:0-否,1-是',
  `sort_order` double NOT NULL DEFAULT 0 COMMENT '排序权重(用于拖拽排序)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`, `deleted`) USING BTREE COMMENT '好友关系唯一约束(含删除状态)',
  KEY `idx_friend_id` (`friend_id`) USING BTREE COMMENT '好友ID索引(反向查询)',
  KEY `idx_user_pinned` (`user_id`, `is_pinned`) USING BTREE COMMENT '置顶查询索引',
  KEY `idx_user_muted` (`user_id`, `is_muted`) USING BTREE COMMENT '免打扰查询索引',
  KEY `idx_deleted` (`deleted`) USING BTREE COMMENT '逻辑删除索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 好友请求表
DROP TABLE IF EXISTS `friend_request`;
CREATE TABLE `friend_request` (
  `id` bigint NOT NULL COMMENT '请求ID(雪花算法)',
  `from_user_id` bigint NOT NULL COMMENT '请求发起者ID',
  `to_user_id` bigint NOT NULL COMMENT '请求接收者ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态:0-待处理,1-已同意,2-已拒绝,3-已过期',
  `remark` varchar(255) DEFAULT NULL COMMENT '请求备注/留言',
  `expires_time` datetime DEFAULT NULL COMMENT '过期时间(默认为发送后7天)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_from_to` (`from_user_id`, `to_user_id`, `deleted`) USING BTREE COMMENT '同一用户对只能有一个待处理请求',
  KEY `idx_from_user_id` (`from_user_id`) USING BTREE COMMENT '发起者索引',
  KEY `idx_to_user_id` (`to_user_id`) USING BTREE COMMENT '接收者索引',
  KEY `idx_status` (`status`) USING BTREE COMMENT '状态索引',
  KEY `idx_expires_time` (`expires_time`) USING BTREE COMMENT '过期时间索引',
  KEY `idx_deleted` (`deleted`) USING BTREE COMMENT '逻辑删除索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友请求表';

-- ============================================
-- 2. 群组模块
-- ============================================

-- 群组表
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group` (
  `id` bigint NOT NULL COMMENT '群组ID(雪花算法)',
  `name` varchar(100) NOT NULL COMMENT '群名称',
  `type` tinyint NOT NULL COMMENT '群类型:0-普通群,1-班级群,2-课程群',
  `owner_id` bigint NOT NULL COMMENT '群主ID',
  `avatar` varchar(255) DEFAULT '/avatar/group.png' COMMENT '群头像URL',
  `announcement` text COMMENT '群公告',
  `description` text COMMENT '群描述',
  `course_id` bigint DEFAULT NULL COMMENT '课程ID(仅课程群使用)',
  `class_no` varchar(20) DEFAULT NULL COMMENT '班级号(仅班级群使用)',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '成员数量(冗余字段,便于统计)',
  `max_members` int NOT NULL DEFAULT 500 COMMENT '最大成员数',
  `is_disbanded` tinyint NOT NULL DEFAULT 0 COMMENT '是否已解散:0-否,1-是',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  `disband_time` datetime DEFAULT NULL COMMENT '解散时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`) USING BTREE COMMENT '群主ID索引',
  KEY `idx_type` (`type`) USING BTREE COMMENT '群类型索引',
  KEY `idx_class_no` (`class_no`) USING BTREE COMMENT '班级号索引',
  KEY `idx_course_id` (`course_id`) USING BTREE COMMENT '课程ID索引',
  KEY `idx_is_disbanded` (`is_disbanded`) USING BTREE COMMENT '解散状态索引',
  KEY `idx_deleted` (`deleted`) USING BTREE COMMENT '逻辑删除索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

-- 群成员表
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `inviter_id` bigint DEFAULT NULL COMMENT '邀请人ID(NULL表示自己加入)',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色:0-普通成员,1-管理员,2-群主',
  `group_nickname` varchar(50) DEFAULT NULL COMMENT '群昵称(覆盖全局昵称)',
  `is_pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶该群:0-否,1-是',
  `is_muted` tinyint NOT NULL DEFAULT 0 COMMENT '是否免打扰:0-否,1-是',
  `sort_order` double NOT NULL DEFAULT 0 COMMENT '排序权重',
  `mute_until` datetime DEFAULT NULL COMMENT '禁言截止时间(NULL表示不禁言)',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
  `quit_time` datetime DEFAULT NULL COMMENT '退群时间(NULL表示在群)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`, `deleted`) USING BTREE COMMENT '群成员唯一约束(含删除状态)',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引(查询用户加入的群)',
  KEY `idx_group_role` (`group_id`, `role`) USING BTREE COMMENT '群内角色查询索引',
  KEY `idx_inviter_id` (`inviter_id`) USING BTREE COMMENT '邀请人索引',
  KEY `idx_mute_until` (`mute_until`) USING BTREE COMMENT '禁言时间索引',
  KEY `idx_user_muted` (`user_id`, `is_muted`) USING BTREE COMMENT '用户免打扰索引',
  KEY `idx_deleted` (`deleted`) USING BTREE COMMENT '逻辑删除索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员表';

-- 班级课程映射表
DROP TABLE IF EXISTS `class_mapping`;
CREATE TABLE `class_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `class_no` varchar(20) NOT NULL COMMENT '班级号',
  `teacher_id` bigint NOT NULL COMMENT '授课教师ID',
  `semester` varchar(20) NOT NULL COMMENT '学期(如2024-2025-1)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_class` (`course_id`, `class_no`, `semester`) USING BTREE COMMENT '课程班级唯一约束',
  KEY `idx_class_no` (`class_no`) USING BTREE COMMENT '班级号索引',
  KEY `idx_teacher_id` (`teacher_id`) USING BTREE COMMENT '教师ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级课程映射表';

-- ============================================
-- 3. 消息模块
-- ============================================

-- 消息表(重要:单表设计,不分表)
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message` (
  `id` bigint NOT NULL COMMENT '消息ID(雪花算法)',
  `chat_id` varchar(64) NOT NULL COMMENT '会话ID(私聊:p_uid1_uid2, 群聊:g_gid)',
  `seq_id` bigint NOT NULL COMMENT '会话内序列号(连续递增,用于排序和补盲)',
  `from_uid` bigint NOT NULL COMMENT '发送者ID(0表示AI机器人)',
  `to_uid` bigint DEFAULT NULL COMMENT '接收者ID(私聊时有值)',
  `group_id` bigint DEFAULT NULL COMMENT '群组ID(群聊时有值)',
  `content` text NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NOT NULL DEFAULT 0 COMMENT '消息类型:0-文本,1-图片,2-文件,3-AI摘要,99-AI思考中',
  `msg_status` tinyint NOT NULL DEFAULT 0 COMMENT '消息状态:0-发送中,1-已发送,2-已送达,3-已读,4-发送失败',
  `mentioned_users` json DEFAULT NULL COMMENT '@的用户ID列表(JSON数组)',
  `is_recalled` tinyint NOT NULL DEFAULT 0 COMMENT '是否撤回:0-否,1-是',
  `recall_time` datetime DEFAULT NULL COMMENT '撤回时间',
  `extra_data` json DEFAULT NULL COMMENT '扩展数据(存储消息元信息)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_seq` (`chat_id`, `seq_id`) USING BTREE COMMENT '会话序列号唯一约束',
  KEY `idx_chat_id` (`chat_id`) USING BTREE COMMENT '会话ID索引(用于消息历史查询)',
  KEY `idx_from_uid` (`from_uid`) USING BTREE COMMENT '发送者索引',
  KEY `idx_group_id` (`group_id`) USING BTREE COMMENT '群组ID索引',
  KEY `idx_to_uid` (`to_uid`) USING BTREE COMMENT '接收者索引(私聊)',
  KEY `idx_msg_status` (`msg_status`) USING BTREE COMMENT '消息状态索引',
  KEY `idx_create_time` (`create_time`) USING BTREE COMMENT '时间索引(用于清理旧消息)',
  KEY `idx_deleted` (`deleted`) USING BTREE COMMENT '逻辑删除索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 私聊已读游标表
DROP TABLE IF EXISTS `private_read_cursor`;
CREATE TABLE `private_read_cursor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `peer_id` bigint NOT NULL COMMENT '对方用户ID',
  `last_read_seq_id` bigint NOT NULL DEFAULT 0 COMMENT '最后已读消息的seq_id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_peer` (`user_id`, `peer_id`) USING BTREE COMMENT '用户对唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊已读游标表';

-- 群聊已读游标表
DROP TABLE IF EXISTS `group_read_cursor`;
CREATE TABLE `group_read_cursor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `last_read_seq_id` bigint NOT NULL DEFAULT 0 COMMENT '最后已读消息的seq_id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_group` (`user_id`, `group_id`) USING BTREE COMMENT '用户群唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊已读游标表';

-- 会话表
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `chat_id` varchar(64) NOT NULL COMMENT '会话ID(私聊:p_uid1_uid2, 群聊:g_gid)',
  `chat_type` tinyint NOT NULL COMMENT '会话类型:0-私聊,1-群聊,2-AI',
  `target_id` bigint NOT NULL COMMENT '目标ID(好友ID或群ID)',
  `unread_count` int NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `last_message_id` bigint DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message` text COMMENT '最后一条消息内容(预览)',
  `is_pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶:0-否,1-是',
  `is_muted` tinyint NOT NULL DEFAULT 0 COMMENT '是否免打扰:0-否,1-是',
  `sort_order` double NOT NULL DEFAULT 0 COMMENT '排序权重',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_chat` (`user_id`, `chat_id`) USING BTREE COMMENT '用户会话唯一约束',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
  KEY `idx_chat_id` (`chat_id`) USING BTREE COMMENT '会话ID索引',
  KEY `idx_user_pinned` (`user_id`, `is_pinned`) USING BTREE COMMENT '用户置顶索引',
  KEY `idx_user_muted` (`user_id`, `is_muted`) USING BTREE COMMENT '用户免打扰索引',
  KEY `idx_deleted` (`deleted`) USING BTREE COMMENT '逻辑删除索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- ============================================
-- 4. AI模块
-- ============================================

-- AI会话表
DROP TABLE IF EXISTS `ai_session`;
CREATE TABLE `ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) DEFAULT NULL COMMENT '会话标题(自动生成或用户设置)',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `message_count` int NOT NULL DEFAULT 0 COMMENT '消息数量',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除:0-否,1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
  KEY `idx_last_message_time` (`last_message_time`) USING BTREE COMMENT '最后消息时间索引(用于排序)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';

-- 关键词提醒表
DROP TABLE IF EXISTS `keyword_alert`;
CREATE TABLE `keyword_alert` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `keyword` varchar(50) NOT NULL COMMENT '关键词',
  `is_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用:0-否,1-是',
  `alert_count` int NOT NULL DEFAULT 0 COMMENT '触发次数(统计用)',
  `last_alert_time` datetime DEFAULT NULL COMMENT '最后触发时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
  KEY `idx_enabled` (`is_enabled`) USING BTREE COMMENT '启用状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词提醒表';

-- ============================================
-- 5. 系统模块
-- ============================================

-- 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '操作用户ID',
  `operation` varchar(50) NOT NULL COMMENT '操作类型(如LOGIN, CREATE_GROUP)',
  `module` varchar(50) NOT NULL COMMENT '模块名称',
  `request_params` json DEFAULT NULL COMMENT '请求参数',
  `response_result` json DEFAULT NULL COMMENT '响应结果',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '浏览器UA',
  `cost_time` int DEFAULT NULL COMMENT '耗时(毫秒)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:0-失败,1-成功',
  `error_msg` text COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
  KEY `idx_operation` (`operation`) USING BTREE COMMENT '操作类型索引',
  KEY `idx_create_time` (`create_time`) USING BTREE COMMENT '时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 6. 插入测试数据
-- ============================================

-- 清空现有测试数据
DELETE FROM `group_member`;
DELETE FROM `im_group`;
DELETE FROM `friend`;
DELETE FROM `friend_request`;
DELETE FROM `user` WHERE id NOT IN (0);

-- 插入AI机器人(user_id=0)
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `avatar`, `role`, `status`, `is_online`, `deleted`) 
VALUES (0, 'AI助手', '', 'AI000', '小智', '/avatar/ai.png', 0, 1, 1, 0)
ON DUPLICATE KEY UPDATE `username`='AI助手', `real_name`='小智', `is_online`=1;

-- 插入测试学生(密码: 123456, BCrypt加密后的值)
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `class_no`, `role`, `status`, `is_online`, `deleted`) 
VALUES 
(1001, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022001', '张三', '22计科1班', 0, 1, 1, 0),
(1002, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022002', '李四', '22计科1班', 0, 1, 1, 0),
(1003, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022003', '王五', '22计科2班', 0, 1, 0, 0),
(1004, 'zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022004', '赵六', '22计科2班', 0, 1, 0, 0);

-- 插入测试教师
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `role`, `status`, `is_online`, `deleted`) 
VALUES 
(2001, 'wanglaoshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'T2022001', '王老师', 1, 1, 1, 0),
(2002, 'lilaoshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'T2022002', '李老师', 1, 1, 0, 0);

-- 插入测试管理员
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `role`, `status`, `is_online`, `deleted`) 
VALUES 
(3001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN001', '管理员', 2, 1, 1, 0);

-- 插入测试好友关系(双向存储)
INSERT INTO `friend` (`user_id`, `friend_id`, `remark`, `is_muted`, `deleted`) 
VALUES 
(1001, 1002, '小李', 0, 0),
(1002, 1001, '小张', 0, 0),
(1001, 1003, '小王', 0, 0),
(1003, 1001, '张三', 0, 0),
(1002, 1003, '王五', 0, 0),
(1003, 1002, '李四', 0, 0);

-- 插入测试群组
INSERT INTO `im_group` (`id`, `name`, `type`, `owner_id`, `member_count`, `deleted`) 
VALUES 
(5001, '22计科1班交流群', 1, 1001, 4, 0),
(5002, '数据库原理课程群', 2, 2001, 4, 0),
(5003, '篮球爱好者俱乐部', 0, 1003, 3, 0);

-- 插入测试群成员(包含AI机器人)
INSERT INTO `group_member` (`group_id`, `user_id`, `inviter_id`, `role`, `is_muted`, `deleted`) 
VALUES 
(5001, 1001, NULL, 2, 0, 0),  -- 张三为群主(自己创建)
(5001, 1002, 1001, 0, 0, 0),  -- 李四由张三邀请
(5001, 1003, 1001, 0, 0, 0),  -- 王五由张三邀请
(5001, 0, 1001, 0, 0, 0),     -- AI助手机器人由张三邀请
(5002, 2001, NULL, 2, 0, 0),  -- 王老师为群主(自己创建)
(5002, 1001, 2001, 0, 0, 0),  -- 张三由王老师邀请
(5002, 1002, 2001, 0, 0, 0),  -- 李四由王老师邀请
(5002, 0, 2001, 0, 0, 0),     -- AI助手机器人由王老师邀请
(5003, 1003, NULL, 2, 0, 0),  -- 王五为群主(自己创建)
(5003, 1004, 1003, 0, 0, 0),  -- 赵六由王五邀请
(5003, 0, 1003, 0, 0, 0);     -- AI助手机器人由王五邀请

-- 插入测试好友请求
INSERT INTO `friend_request` (`id`, `from_user_id`, `to_user_id`, `status`, `remark`, `deleted`) 
VALUES 
(10001, 1004, 1001, 0, '您好，我想加您为好友', 0),
(10002, 1004, 1002, 1, '同学你好', 0);

-- ============================================
-- 完成
-- ============================================

SELECT '数据库初始化完成!' AS message;