package com.maisizhe.modules.group;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maisizhe.common.enums.GroupTypeEnum;
import com.maisizhe.common.enums.MemberRoleEnum;
import com.maisizhe.modules.group.dto.CreateGroupDTO;
import com.maisizhe.modules.group.entity.Group;
import com.maisizhe.modules.group.entity.GroupMember;
import com.maisizhe.modules.group.mapper.GroupMapper;
import com.maisizhe.modules.group.mapper.GroupMemberMapper;
import com.maisizhe.modules.group.service.impl.GroupServiceImpl;
import com.maisizhe.modules.group.vo.GroupVO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import com.maisizhe.websocket.session.UserSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 群组服务单元测试
 * 
 * @author MaiSiZhe Team
 */
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSessionManager sessionManager;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Group testGroup;
    private GroupMember testMember;
    private User testUser;
    private CreateGroupDTO createGroupDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试群组
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setGroupName("测试群组");
        testGroup.setGroupType(0); // 普通群
        testGroup.setCreatorId(1L);
        testGroup.setMemberCount(1);
        testGroup.setMaxMembers(100);
        testGroup.setCreateTime(LocalDateTime.now());
        testGroup.setUpdateTime(LocalDateTime.now());

        // 初始化测试成员
        testMember = new GroupMember();
        testMember.setId(1L);
        testMember.setGroupId(1L);
        testMember.setUserId(1L);
        testMember.setRole(MemberRoleEnum.OWNER.getCode());
        testMember.setJoinTime(LocalDateTime.now());

        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setAvatar("avatar.jpg");

        // 初始化创建群组DTO
        createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setGroupName("新群组");
        createGroupDTO.setGroupType(0);
        createGroupDTO.setMaxMembers(100);
    }

    @Test
    @DisplayName("创建群组成功测试")
    void testCreateGroupSuccess() {
        // Mock行为
        when(groupMapper.insert(any(Group.class))).thenReturn(1);
        when(groupMemberMapper.insert(any(GroupMember.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        GroupVO result = groupService.createGroup(1L, createGroupDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("新群组", result.getGroupName());

        // 验证方法调用
        verify(groupMapper).insert(any(Group.class));
        verify(groupMemberMapper).insert(any(GroupMember.class));
    }

    @Test
    @DisplayName("创建班级群成功测试")
    void testCreateClassGroupSuccess() {
        // 设置班级群参数
        createGroupDTO.setGroupType(1); // 班级群
        createGroupDTO.setClassNo("2311101");

        // Mock行为
        when(groupMapper.insert(any(Group.class))).thenReturn(1);
        when(groupMemberMapper.insert(any(GroupMember.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        GroupVO result = groupService.createGroup(1L, createGroupDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("新群组", result.getGroupName());
    }

    @Test
    @DisplayName("创建班级群失败-缺少班级号")
    void testCreateClassGroupFailNoClassNo() {
        // 设置班级群参数但不提供班级号
        createGroupDTO.setGroupType(1); // 班级群
        createGroupDTO.setClassNo(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            groupService.createGroup(1L, createGroupDTO);
        });

        // 验证未执行插入
        verify(groupMapper, never()).insert(any(Group.class));
    }

    @Test
    @DisplayName("获取群组详情成功")
    void testGetGroupByIdSuccess() {
        // Mock行为
        when(groupMapper.selectById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMember);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        GroupVO result = groupService.getGroupById(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("测试群组", result.getGroupName());
        assertEquals(MemberRoleEnum.OWNER.getCode(), result.getMyRole());

        // 验证方法调用
        verify(groupMapper).selectById(1L);
        verify(groupMemberMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取群组详情失败-群组不存在")
    void testGetGroupByIdFailNotFound() {
        // Mock行为
        when(groupMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            groupService.getGroupById(999L, 1L);
        });

        // 验证方法调用
        verify(groupMapper).selectById(999L);
    }

    @Test
    @DisplayName("获取用户群组列表成功")
    void testGetUserGroupsSuccess() {
        // Mock行为
        when(groupMemberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testMember));
        when(groupMapper.selectBatchIds(any())).thenReturn(Arrays.asList(testGroup));
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMember);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        List<GroupVO> result = groupService.getUserGroups(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试群组", result.get(0).getGroupName());

        // 验证方法调用
        verify(groupMemberMapper).selectList(any(LambdaQueryWrapper.class));
        verify(groupMapper).selectBatchIds(any());
    }

    @Test
    @DisplayName("获取用户群组列表-空列表")
    void testGetUserGroupsEmpty() {
        // Mock行为
        when(groupMemberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // 执行测试
        List<GroupVO> result = groupService.getUserGroups(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());

        // 验证方法调用
        verify(groupMemberMapper).selectList(any(LambdaQueryWrapper.class));
        verify(groupMapper, never()).selectBatchIds(any());
    }

    @Test
    @DisplayName("加入群组成功")
    void testJoinGroupSuccess() {
        // Mock行为
        when(groupMapper.selectById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(groupMemberMapper.insert(any(GroupMember.class))).thenReturn(1);
        when(groupMapper.updateById(any(Group.class))).thenReturn(1);

        // 执行测试
        groupService.joinGroup(1L, 2L);

        // 验证方法调用
        verify(groupMapper).selectById(1L);
        verify(groupMemberMapper).insert(any(GroupMember.class));
        verify(groupMapper).updateById(any(Group.class));
    }

    @Test
    @DisplayName("加入群组失败-群组不存在")
    void testJoinGroupFailNotFound() {
        // Mock行为
        when(groupMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            groupService.joinGroup(999L, 1L);
        });

        // 验证方法调用
        verify(groupMapper).selectById(999L);
        verify(groupMemberMapper, never()).insert(any(GroupMember.class));
    }

    @Test
    @DisplayName("退出群组成功")
    void testQuitGroupSuccess() {
        // 创建普通成员
        GroupMember normalMember = new GroupMember();
        normalMember.setId(2L);
        normalMember.setGroupId(1L);
        normalMember.setUserId(2L);
        normalMember.setRole(MemberRoleEnum.MEMBER.getCode());
        normalMember.setJoinTime(LocalDateTime.now());

        // Mock行为
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(normalMember);
        when(groupMemberMapper.updateById(any(GroupMember.class))).thenReturn(1);
        when(groupMapper.selectById(1L)).thenReturn(testGroup);
        when(groupMapper.updateById(any(Group.class))).thenReturn(1);

        // 执行测试
        groupService.quitGroup(1L, 2L);

        // 验证方法调用
        verify(groupMemberMapper).updateById(any(GroupMember.class));
        verify(groupMapper).updateById(any(Group.class));
    }

    @Test
    @DisplayName("退出群组失败-群主不能退群")
    void testQuitGroupFailOwnerCannotQuit() {
        // Mock行为
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMember);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            groupService.quitGroup(1L, 1L);
        });

        // 验证未执行更新
        verify(groupMemberMapper, never()).updateById(any(GroupMember.class));
    }

    @Test
    @DisplayName("踢出群成员成功")
    void testKickMemberSuccess() {
        // 创建普通成员
        GroupMember targetMember = new GroupMember();
        targetMember.setId(2L);
        targetMember.setGroupId(1L);
        targetMember.setUserId(2L);
        targetMember.setRole(MemberRoleEnum.MEMBER.getCode());
        targetMember.setJoinTime(LocalDateTime.now());

        // Mock行为 - 操作者是群主
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(testMember) // 操作者
                .thenReturn(targetMember); // 被踢成员
        when(groupMemberMapper.updateById(any(GroupMember.class))).thenReturn(1);
        when(groupMapper.selectById(1L)).thenReturn(testGroup);
        when(groupMapper.updateById(any(Group.class))).thenReturn(1);

        // 执行测试
        groupService.kickMember(1L, 2L, 1L);

        // 验证方法调用
        verify(groupMemberMapper, times(2)).selectOne(any(LambdaQueryWrapper.class));
        verify(groupMemberMapper).updateById(any(GroupMember.class));
    }

    @Test
    @DisplayName("踢出群成员失败-不能踢群主")
    void testKickMemberFailCannotKickOwner() {
        // Mock行为 - 被踢的是群主
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(testMember) // 操作者
                .thenReturn(testMember); // 被踢成员(群主)

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            groupService.kickMember(1L, 1L, 2L);
        });

        // 验证未执行更新
        verify(groupMemberMapper, never()).updateById(any(GroupMember.class));
    }
}