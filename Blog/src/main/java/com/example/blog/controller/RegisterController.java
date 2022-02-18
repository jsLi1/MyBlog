package com.example.blog.controller;

import com.example.blog.service.LoginService;
import com.example.blog.vo.Result;
import com.example.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("register")
public class RegisterController {

    @Autowired
    private LoginService loginService;
    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){
        //sso 单点登录
        return loginService.register(loginParam);
    }
}
