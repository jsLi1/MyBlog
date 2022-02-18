package com.example.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.blog.bean.SysUser;
import com.example.blog.service.LoginService;
import com.example.blog.service.SysUserService;
import com.example.blog.vo.utils.JWTUtils;
import com.example.blog.vo.ErrorCode;
import com.example.blog.vo.Result;
import com.example.blog.vo.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    private static final String slat = "mszlu!@#";
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 检查桉树是否合法
         * 根据用户名和密码去user表中查询 是否存在
         * 如果不存在 登陆失败
         * 如果存在 使用jwt生成token返回给前端
         * token放入redis中，redis token：user信息 设置过期时间（登录认证的时候 先认证token字符串是否合法，去redis认证是否存在）
         */
         String account=loginParam.getAccount();
         String password=loginParam.getPassword();
         if(StringUtils.isBlank(account) || StringUtils.isBlank(password)){
             return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
         }
         password= DigestUtils.md5Hex(password+slat);
        SysUser sysUser= sysUserService.findUser(account,password);
         if(sysUser==null){
             return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
         }
        String token = JWTUtils.createToken(sysUser.getId());
         redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return Result.success(token);
    }

//    @Override
//    public SysUser checkToken(String token) {
//        if(StringUtils.isBlank(token)){
//            return null;
//        }
//        Map<String,Object> stringObjectMap = JWTUtils.checkToken(token);
//        if(stringObjectMap==null){
//            return null;
//        }
//        String userJson= redisTemplate.opsForValue().get("TOKEN_" + token);
//        if(StringUtils.isBlank(userJson)) return null;
//        var sysUser = JSON.parseObject(userJson, SysUser.class);
//        return sysUser;
//    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_"+token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 判断参数是否合法
         * 判断账户是否存在，存在返回账户已经被注册
         * 不存在，注册用户
         * 存入redis 并返回
         * 加上事务，一旦中间出现任何问题，需要回滚
         */

        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
       SysUser sysUser= sysUserService.findUserByAccount(account);
        if(sysUser!=null){
            return Result.fail(ErrorCode.ACCOUNT_EXIT.getCode(), ErrorCode.ACCOUNT_EXIT.getMsg());
        }
        sysUser= new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        this.sysUserService.save(sysUser);
        //token
        String token = JWTUtils.createToken(sysUser.getId());

        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return Result.success(token);
    }
}
