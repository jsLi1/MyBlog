package com.example.blogadmin.service;

import com.example.blogadmin.bean.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Component
public class SecurityUserService implements UserDetailsService {
    @Autowired
    AdminService adminServie;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //登录的时候会把username传递到这里
        //通过username查询admin表，如果admin存在 将密码告诉springsecurity
        //如果不存在，返回null，认证失败
        var adminByUsername = adminServie.findAdminByUsername(username);
        if(adminByUsername==null){
            return null;
        }
        UserDetails userDetails=new User(username,adminByUsername.getPassword(),new ArrayList<>());
        return userDetails;
    }
}
