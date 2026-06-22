USE maisizhe;

-- ==================== 添加更多学生用户 ====================
INSERT INTO user (id, username, password, student_no, real_name, class_no, department, role, status) VALUES
(1005, 'sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022005', '孙七', '22计科1班', '计算机学院', 0, 1),
(1006, 'zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022006', '周八', '22计科1班', '计算机学院', 0, 1),
(1007, 'wujiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022007', '吴九', '22计科2班', '计算机学院', 0, 1),
(1008, 'zhengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022008', '郑十', '22计科2班', '计算机学院', 0, 1),
(1009, 'liuyiyi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022009', '刘一一', '22软件1班', '软件学院', 0, 1),
(1010, 'chenshier', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '2022010', '陈十二', '22软件1班', '软件学院', 0, 1),
(2003, 'zhanglaoshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', NULL, '张老师', NULL, '计算机学院', 1, 1),
(2004, 'chenlaoshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', NULL, '陈老师', NULL, '软件学院', 1, 1);

-- ==================== 添加更多好友关系 ====================
-- 张三(1001) 的好友
INSERT INTO friend (user_id, friend_id, remark, is_pinned, is_muted, sort_order) VALUES
(1001, 1005, '室友', 1, 0, 100),
(1005, 1001, '', 0, 0, 0),
(1001, 1006, '班长', 0, 0, 0),
(1006, 1001, '', 0, 0, 0);

-- 李四(1002) 的好友
INSERT INTO friend (user_id, friend_id, remark, is_pinned, is_muted, sort_order) VALUES
(1002, 1007, '学习委员', 1, 0, 100),
(1007, 1002, '', 0, 0, 0),
(1002, 1008, '', 0, 0, 0),
(1008, 1002, '', 0, 0, 0);

-- 王五(1003) 的好友
INSERT INTO friend (user_id, friend_id, remark, is_pinned, is_muted, sort_order) VALUES
(1003, 1009, '', 0, 0, 0),
(1009, 1003, '', 0, 0, 0),
(1003, 1010, '', 0, 0, 0),
(1010, 1003, '', 0, 0, 0);

-- 赵六(1004) 的好友
INSERT INTO friend (user_id, friend_id, remark, is_pinned, is_muted, sort_order) VALUES
(1004, 1005, '', 0, 0, 0),
(1005, 1004, '', 0, 0, 0),
(1004, 1007, '', 0, 0, 0),
(1007, 1004, '', 0, 0, 0);

-- ==================== 添加测试群组 ====================
INSERT INTO im_group (id, name, type, owner_id, class_no, member_count, max_members) VALUES
(5001, '22计科1班群', 0, 2001, '22计科1班', 5, 50),
(5002, '22计科2班群', 0, 2001, '22计科2班', 4, 50),
(5003, '软件1班学习群', 0, 2004, '22软件1班', 3, 50),
(5004, '项目开发小组', 1, 1001, NULL, 4, 20),
(5005, '足球爱好者协会', 2, 1003, NULL, 5, 100);

-- ==================== 添加群成员 ====================
-- 5001: 22计科1班群
INSERT INTO group_member (group_id, user_id, inviter_id, role) VALUES
(5001, 1001, 2001, 1),
(5001, 1002, 2001, 0),
(5001, 1005, 2001, 0),
(5001, 1006, 2001, 0),
(5001, 2001, NULL, 2);

-- 5002: 22计科2班群
INSERT INTO group_member (group_id, user_id, inviter_id, role) VALUES
(5002, 1003, 2001, 0),
(5002, 1004, 2001, 0),
(5002, 1007, 2001, 0),
(5002, 1008, 2001, 0),
(5002, 2001, NULL, 2);

-- 5003: 软件1班学习群
INSERT INTO group_member (group_id, user_id, inviter_id, role) VALUES
(5003, 1009, 2004, 0),
(5003, 1010, 2004, 0),
(5003, 2004, NULL, 2);

-- 5004: 项目开发小组
INSERT INTO group_member (group_id, user_id, inviter_id, role) VALUES
(5004, 1001, NULL, 2),
(5004, 1002, 1001, 0),
(5004, 1003, 1001, 0),
(5004, 1005, 1001, 0);

-- 5005: 足球爱好者协会
INSERT INTO group_member (group_id, user_id, inviter_id, role) VALUES
(5005, 1003, NULL, 2),
(5005, 1001, 1003, 0),
(5005, 1004, 1003, 0),
(5005, 1007, 1003, 0),
(5005, 1008, 1003, 0);

-- ==================== 添加测试私聊消息 ====================
INSERT INTO im_message (id, chat_id, seq_id, from_uid, to_uid, content, msg_type, msg_status) VALUES
(10001, 'p_1001_1002', 1, 1001, 1002, '嘿，李四，最近忙什么呢？', 0, 2),
(10002, 'p_1001_1002', 2, 1002, 1001, '在准备期末考试呢，你呢？', 0, 2),
(10003, 'p_1001_1002', 3, 1001, 1002, '我也是，一起加油！', 0, 2),
(10004, 'p_1001_1003', 1, 1001, 1003, '王五，周末有空吗？', 0, 2),
(10005, 'p_1001_1003', 2, 1003, 1001, '有空啊，什么事？', 0, 2),
(10006, 'p_1002_1007', 1, 1002, 1007, '学习委员，作业什么时候交？', 0, 2),
(10007, 'p_1002_1007', 2, 1007, 1002, '下周一之前都可以', 0, 2),
(10008, 'p_1001_1005', 1, 1001, 1005, '室友，帮我带份饭回来', 0, 2),
(10009, 'p_1001_1005', 2, 1005, 1001, '好的，吃什么？', 0, 2),
(10010, 'p_1001_1005', 3, 1001, 1005, '随便，食堂的就行', 0, 2);

-- ==================== 添加测试群聊消息 ====================
INSERT INTO im_message (id, chat_id, seq_id, from_uid, group_id, content, msg_type, msg_status) VALUES
(20001, 'g_5001', 1, 2001, 5001, '同学们，下周要进行期中考试了，大家好好复习！', 0, 2),
(20002, 'g_5001', 2, 1001, 5001, '收到老师，我们会认真复习的！', 0, 2),
(20003, 'g_5001', 3, 1002, 5001, '老师，考试范围是什么呢？', 0, 2),
(20004, 'g_5001', 4, 2001, 5001, '主要是前八章的内容', 0, 2),
(20005, 'g_5004', 1, 1001, 5004, '大家好，我们的项目进度怎么样了？', 0, 2),
(20006, 'g_5004', 2, 1002, 5004, '前端页面基本完成了', 0, 2),
(20007, 'g_5004', 3, 1003, 5004, '后端接口也差不多了', 0, 2),
(20008, 'g_5005', 1, 1003, 5005, '明天下午有没有人一起踢球？', 0, 2),
(20009, 'g_5005', 2, 1001, 5005, '我去！', 0, 2),
(20010, 'g_5005', 3, 1004, 5005, '算我一个', 0, 2);

-- ==================== 添加好友请求 ====================
INSERT INTO friend_request (id, from_user_id, to_user_id, remark, status, expires_time) VALUES
(1, 1009, 1001, '我是刘一一，想加你为好友', 0, DATE_ADD(NOW(), INTERVAL 3 DAY)),
(2, 1010, 1002, '陈十二请求添加好友', 0, DATE_ADD(NOW(), INTERVAL 3 DAY)),
(3, 1007, 2001, '吴九想加老师为好友', 0, DATE_ADD(NOW(), INTERVAL 3 DAY));

SELECT '测试数据插入完成！' AS message;