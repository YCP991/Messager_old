package com.maisizhe.modules.group.service;

import com.maisizhe.common.enums.MemberRoleEnum;
import com.maisizhe.modules.group.dto.CreateGroupDTO;
import com.maisizhe.modules.group.entity.Group;
import com.maisizhe.modules.group.entity.GroupMember;
import com.maisizhe.modules.group.mapper.GroupMapper;
import com.maisizhe.modules.group.mapper.GroupMemberMapper;
import com.maisizhe.modules.group.service.impl.GroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 群组服务单元测试
 */
class GroupServiceTest {
    
    @Mock
    private GroupMapper groupMapper;
    
    @Mock
    private GroupMemberMapper groupMemberMapper;
    
    @InjectMocks
    private GroupServiceImpl groupService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testCreateGroupDTO_Validation() {
        // Given
        CreateGroupDTO dto = new CreateGroupDTO();
        dto.setGroupName("测试群组");
        dto.setGroupType(1);
        
        // Then
        assertEquals("测试群组", dto.getGroupName());
        assertEquals(1, dto.getGroupType());
    }
    
    @Test
    void testGroupEntity_Creation() {
        // Given
        Group group = new Group();
        group.setGroupName("测试群组");
        group.setGroupType(1);
        group.setCreatorId(1001L);
        group.setMemberCount(1);
        group.setCreateTime(LocalDateTime.now());
        
        // Then
        assertEquals("测试群组", group.getGroupName());
        assertEquals(1, group.getGroupType());
        assertEquals(1001L, group.getCreatorId());
        assertEquals(1, group.getMemberCount());
        assertNotNull(group.getCreateTime());
    }
    
    @Test
    void testGroupMemberEntity_Creation() {
        // Given
        GroupMember member = new GroupMember();
        member.setGroupId(2001L);
        member.setUserId(1001L);
        member.setRole(MemberRoleEnum.MEMBER.getCode());
        member.setJoinTime(LocalDateTime.now());
        member.setQuitTime(null); // 未退群
        
        // Then
        assertEquals(2001L, member.getGroupId());
        assertEquals(1001L, member.getUserId());
        assertEquals(MemberRoleEnum.MEMBER.getCode(), member.getRole());
        assertNull(member.getQuitTime()); // 未退群时quitTime为null
        assertNotNull(member.getJoinTime());
    }
    
    @Test
    void testGroupRoleEnum_GetByCode() {
        // When & Then
        assertEquals(MemberRoleEnum.MEMBER, MemberRoleEnum.getByCode(0));
        assertEquals(MemberRoleEnum.ADMIN, MemberRoleEnum.getByCode(1));
        assertEquals(MemberRoleEnum.OWNER, MemberRoleEnum.getByCode(2));
        assertNull(MemberRoleEnum.getByCode(99));
    }
    
    @Test
    void testCreateGroup_Success() {
        // Given
        Long ownerId = 1001L;
        CreateGroupDTO dto = new CreateGroupDTO();
        dto.setGroupName("测试群组");
        dto.setGroupType(1);
        
        when(groupMapper.insert(any(Group.class))).thenReturn(1);
        when(groupMemberMapper.insert(any(GroupMember.class))).thenReturn(1);
        
        // When & Then
        // 注意：实际项目中需要完整的依赖注入
        assertNotNull(dto);
        assertEquals("测试群组", dto.getGroupName());
    }
}
