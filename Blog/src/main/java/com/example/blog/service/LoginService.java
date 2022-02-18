package com.example.blog.service;

import com.example.blog.bean.SysUser;
import com.example.blog.vo.Result;
import com.example.blog.vo.params.LoginParam;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LoginService {

    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

//    SysUser checkToken(String token);

    Result logout(String token);

    Result register(LoginParam loginParam);
}
