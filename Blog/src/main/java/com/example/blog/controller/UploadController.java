package com.example.blog.controller;

import com.example.blog.vo.utils.QiniuUtils;
import com.example.blog.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private QiniuUtils qiniuUtils;

    @PostMapping
    public Result upload(@RequestParam("image")MultipartFile file){
        String originalFilename=file.getOriginalFilename();
        String fileName= UUID.randomUUID().toString()+"."+ StringUtils.substringAfterLast(originalFilename,".");
        //s上传文件  七牛云服务器
        var upload = qiniuUtils.upload(file, fileName);
        if(upload){
            return Result.success(QiniuUtils.url+"/"+fileName);
        }
        return Result.fail(20001,"上传失败");
    }
}
