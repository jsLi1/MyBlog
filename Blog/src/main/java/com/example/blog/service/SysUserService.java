package com.example.blog.service;

import com.example.blog.bean.SysUser;
import com.example.blog.vo.Result;
import com.example.blog.vo.UserVo;
import org.springframework.transaction.annotation.Transactional;


public interface SysUserService {
    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);
     //根据token查询用户信息
    Result findUserByToken(String token);

    SysUser findUserByAccount(String account);

    void save(SysUser sysUser);

    UserVo findUserVoById(Long id);
}
