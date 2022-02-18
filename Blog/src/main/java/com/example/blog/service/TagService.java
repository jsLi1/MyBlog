package com.example.blog.service;

import com.example.blog.vo.Result;
import com.example.blog.vo.TagVo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
public interface TagService {
    List<TagVo> findTagsByArticleId(Long articleId);

    Result hots(int limit);

    Result findAll();

    Result findAllDetail();

    Result findDetailById(Long id);
}
