package com.maisizhe.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 枚举类单元测试
 */
class RoleEnumTest {
    
    @Test
    void testGetByCode_Student() {
        // When
        RoleEnum role = RoleEnum.getByCode(0);
        
        // Then
        assertNotNull(role);
        assertEquals(RoleEnum.STUDENT, role);
        assertEquals("学生", role.getDescription());
    }
    
    @Test
    void testGetByCode_Teacher() {
        // When
        RoleEnum role = RoleEnum.getByCode(1);
        
        // Then
        assertNotNull(role);
        assertEquals(RoleEnum.TEACHER, role);
        assertEquals("教师", role.getDescription());
    }
    
    @Test
    void testGetByCode_Admin() {
        // When
        RoleEnum role = RoleEnum.getByCode(2);
        
        // Then
        assertNotNull(role);
        assertEquals(RoleEnum.ADMIN, role);
        assertEquals("管理员", role.getDescription());
    }
    
    @Test
    void testGetByCode_Invalid() {
        // When
        RoleEnum role = RoleEnum.getByCode(99);
        
        // Then
        assertNull(role);
    }
    
    @Test
    void testGetByCode_Null() {
        // When
        RoleEnum role = RoleEnum.getByCode(null);
        
        // Then
        assertNull(role);
    }
    
    @Test
    void testGetCode() {
        // Then
        assertEquals(0, RoleEnum.STUDENT.getCode());
        assertEquals(1, RoleEnum.TEACHER.getCode());
        assertEquals(2, RoleEnum.ADMIN.getCode());
    }
}
