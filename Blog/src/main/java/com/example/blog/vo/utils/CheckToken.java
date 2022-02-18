package com.example.blog.vo.utils;

import com.alibaba.fastjson.JSON;
import com.example.blog.bean.SysUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class CheckToken {
    @Autowired
    private  RedisTemplate<String,String> redisTemplate;

    public  SysUser check(String token) {
        if(StringUtils.isBlank(token)){
            return null;
        }
        Map<String,Object> stringObjectMap = JWTUtils.checkToken(token);
        if(stringObjectMap==null){
            return null;
        }
        String userJson= redisTemplate.opsForValue().get("TOKEN_" + token);
        if(StringUtils.isBlank(userJson)) return null;
        var sysUser = JSON.parseObject(userJson, SysUser.class);
        return sysUser;
    }
}
