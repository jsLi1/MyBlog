package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.bean.Tag;
import com.example.blog.dao.TagMapper;
import com.example.blog.service.TagService;
import com.example.blog.vo.Result;
import com.example.blog.vo.TagVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Service
public class TagsServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    public TagVo copy(Tag tag){
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag,tagVo);
        tagVo.setId(String.valueOf(tag.getId()));
        return tagVo;
    }
    public List<TagVo> copyList(List<Tag> tagList){
        List<TagVo> tagVoList = new ArrayList<>();
        for (Tag tag : tagList) {
            tagVoList.add(copy(tag));
        }
        return tagVoList;
    }
    @Override
    public List<TagVo> findTagsByArticleId(Long articleId) {
        //mybatis无法进行多表查询
       List<Tag> tags=tagMapper.findTagsByArticleId(articleId);

        return copyList(tags);
    }

    @Override
    public Result hots(int limit) {
        /**
         * 1.标签所拥有的文章数量最多 最热标签
         * 2.查询根据tag_id 分组 计数 ，从小到大 排列 取前limit个
         */
       List<Long> tagIds= tagMapper.findHotsTagIds(limit);
       if(CollectionUtils.isEmpty(tagIds)){
           return Result.success(Collections.emptyList());
       }
       //需求的是tagId 和 tagName Tag对象
       List<Tag> tagList=tagMapper.findTagsByTagIds(tagIds);
        return Result.success(tagList);
    }

    @Override
    public Result findAll() {
       LambdaQueryWrapper<Tag> queryWrapper =new LambdaQueryWrapper<>();
       queryWrapper.select(Tag::getId,Tag::getTagName);
        var tagList = tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tagList));
    }

    @Override
    public Result findAllDetail() {
        LambdaQueryWrapper<Tag> queryWrapper =new LambdaQueryWrapper<>();
        var tagList = tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tagList));
    }

    @Override
    public Result findDetailById(Long id) {
        var tag = tagMapper.selectById(id);
        return Result.success(copy(tag));
    }
}
