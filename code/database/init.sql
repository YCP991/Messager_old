-- ============================================
-- 麦思哲(MaiSiZhe)数据库初始化脚本
-- MySQL 8.0+
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
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '用户ID(雪花算法生成)',
  `username` varchar(50) NOT NULL COMMENT '用户名(登录用)',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `student_no` varchar(20) DEFAULT NULL COMMENT '学号/工号',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `class_no` varchar(20) DEFAULT NULL COMMENT '班级号(学生)',
  `department` varchar(50) DEFAULT NULL COMMENT '院系',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色:0-学生,1-教师,2-管理员',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) USING BTREE COMMENT '用户名唯一索引',
  UNIQUE KEY `uk_student_no` (`student_no`) USING BTREE COMMENT '学号唯一索引',
  KEY `idx_class_no` (`class_no`) USING BTREE COMMENT '班级号索引(用于班级群组建)',
  KEY `idx_email` (`email`) USING BTREE COMMENT '邮箱索引',
  KEY `idx_phone` (`phone`) USING BTREE COMMENT '手机号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 好友表
CREATE TABLE `friend` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注名',
  `is_pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶:0-否,1-是',
  `sort_order` double NOT NULL DEFAULT 0 COMMENT '排序权重(用于拖拽排序)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`) USING BTREE COMMENT '好友关系唯一约束',
  KEY `idx_friend_id` (`friend_id`) USING BTREE COMMENT '好友ID索引(反向查询)',
  KEY `idx_user_pinned` (`user_id`, `is_pinned`) USING BTREE COMMENT '置顶查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- ============================================
-- 2. 群组模块
-- ============================================

-- 群组表
CREATE TABLE `im_group` (
  `id` bigint NOT NULL COMMENT '群组ID(雪花算法)',
  `name` varchar(100) NOT NULL COMMENT '群名称',
  `type` tinyint NOT NULL COMMENT '群类型:0-普通群,1-班级群,2-课程群',
  `owner_id` bigint NOT NULL COMMENT '群主ID',
  `avatar` varchar(255) DEFAULT NULL COMMENT '群头像URL',
  `announcement` text COMMENT '群公告',
  `course_id` bigint DEFAULT NULL COMMENT '课程ID(仅课程群使用)',
  `class_no` varchar(20) DEFAULT NULL COMMENT '班级号(仅班级群使用)',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '成员数量(冗余字段,便于统计)',
  `max_members` int NOT NULL DEFAULT 500 COMMENT '最大成员数',
  `is_disbanded` tinyint NOT NULL DEFAULT 0 COMMENT '是否已解散:0-否,1-是',
  `disband_time` datetime DEFAULT NULL COMMENT '解散时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`) USING BTREE COMMENT '群主ID索引',
  KEY `idx_type` (`type`) USING BTREE COMMENT '群类型索引',
  KEY `idx_class_no` (`class_no`) USING BTREE COMMENT '班级号索引',
  KEY `idx_course_id` (`course_id`) USING BTREE COMMENT '课程ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

-- 群成员表
CREATE TABLE `group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色:0-普通成员,1-管理员,2-群主',
  `group_nickname` varchar(50) DEFAULT NULL COMMENT '群昵称(覆盖全局昵称)',
  `is_pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶该群:0-否,1-是',
  `sort_order` double NOT NULL DEFAULT 0 COMMENT '排序权重',
  `mute_until` datetime DEFAULT NULL COMMENT '禁言截止时间(NULL表示不禁言)',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
  `quit_time` datetime DEFAULT NULL COMMENT '退群时间(NULL表示在群)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`) USING BTREE COMMENT '群成员唯一约束',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引(查询用户加入的群)',
  KEY `idx_group_role` (`group_id`, `role`) USING BTREE COMMENT '群内角色查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员表';

-- 班级课程映射表
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
CREATE TABLE `im_message` (
  `id` bigint NOT NULL COMMENT '消息ID(雪花算法)',
  `chat_id` varchar(64) NOT NULL COMMENT '会话ID(私聊:p_uid1_uid2, 群聊:g_gid)',
  `seq_id` bigint NOT NULL COMMENT '会话内序列号(连续递增,用于排序和补盲)',
  `from_uid` bigint NOT NULL COMMENT '发送者ID(0表示AI机器人)',
  `to_uid` bigint DEFAULT NULL COMMENT '接收者ID(私聊时有值)',
  `group_id` bigint DEFAULT NULL COMMENT '群组ID(群聊时有值)',
  `content` text NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NOT NULL DEFAULT 0 COMMENT '消息类型:0-文本,1-图片,2-文件,3-AI摘要,99-AI思考中',
  `mentioned_users` json DEFAULT NULL COMMENT '@的用户ID列表(JSON数组)',
  `is_recalled` tinyint NOT NULL DEFAULT 0 COMMENT '是否撤回:0-否,1-是',
  `recall_time` datetime DEFAULT NULL COMMENT '撤回时间',
  `extra_data` json DEFAULT NULL COMMENT '扩展数据(存储消息元信息)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_seq` (`chat_id`, `seq_id`) USING BTREE COMMENT '会话序列号唯一约束',
  KEY `idx_from_uid` (`from_uid`) USING BTREE COMMENT '发送者索引',
  KEY `idx_group_id` (`group_id`) USING BTREE COMMENT '群组ID索引',
  KEY `idx_to_uid` (`to_uid`) USING BTREE COMMENT '接收者索引(私聊)',
  KEY `idx_create_time` (`create_time`) USING BTREE COMMENT '时间索引(用于清理旧消息)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 私聊已读游标表
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
CREATE TABLE `group_read_cursor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `last_read_seq_id` bigint NOT NULL DEFAULT 0 COMMENT '最后已读消息的seq_id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_group` (`user_id`, `group_id`) USING BTREE COMMENT '用户群唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊已读游标表';

-- ============================================
-- 4. AI模块
-- ============================================

-- AI会话表
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

-- 插入AI机器人(user_id=0)
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `avatar`, `role`, `status`) 
VALUES (0, 'AI助手', '', 'AI000', '小智', '/avatar/ai.png', 0, 1);

-- 插入测试学生(密码: 123456, BCrypt加密后的值)
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `class_no`, `role`, `status`) 
VALUES 
(1001, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022001', '张三', '22计科1班', 0, 1),
(1002, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022002', '李四', '22计科1班', 0, 1),
(1003, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022003', '王五', '22计科2班', 0, 1);

-- 插入测试教师
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `role`, `status`) 
VALUES 
(2001, 'wanglaoshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'T2022001', '王老师', 1, 1);

-- 插入测试管理员
INSERT INTO `user` (`id`, `username`, `password`, `student_no`, `real_name`, `role`, `status`) 
VALUES 
(3001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN001', '管理员', 2, 1);

-- 插入测试好友关系
INSERT INTO `friend` (`user_id`, `friend_id`, `remark`) 
VALUES 
(1001, 1002, '小李'),
(1002, 1001, '小张'),
(1001, 1003, '小王');

-- 插入测试群组
INSERT INTO `im_group` (`id`, `name`, `type`, `owner_id`, `member_count`) 
VALUES 
(5001, '22计科1班交流群', 1, 1001, 3),
(5002, '数据库原理课程群', 2, 2001, 50);

-- 插入测试群成员(包含AI机器人)
INSERT INTO `group_member` (`group_id`, `user_id`, `role`) 
VALUES 
(5001, 1001, 2),  -- 张三为群主
(5001, 1002, 0),  -- 李四为成员
(5001, 1003, 0),  -- 王五为成员
(5001, 0, 0),     -- AI助手机器人
(5002, 2001, 2);  -- 王老师为群主

-- ============================================
-- 完成
-- ============================================

SELECT '数据库初始化完成!' AS message;

