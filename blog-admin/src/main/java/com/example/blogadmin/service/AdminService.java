package com.example.blogadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogadmin.bean.Admin;
import com.example.blogadmin.bean.Permission;
import com.example.blogadmin.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    public Admin findAdminByUsername(String username){
        LambdaQueryWrapper<Admin> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername,username);
        queryWrapper.last("limit 1");
        return (Admin) adminMapper.selectOne(queryWrapper);
    }

    public List<Permission> findPermissionByAdminId(Long adminId) {

        return adminMapper.findPermissionByAdminId(adminId);
    }
}
