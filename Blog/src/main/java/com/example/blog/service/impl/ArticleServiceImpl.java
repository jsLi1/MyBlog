package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.bean.*;
import com.example.blog.dao.ArticleBodyMapper;
import com.example.blog.dao.ArticleMapper;
import com.example.blog.dao.ArticleTagMapper;
import com.example.blog.dao.daos.Archives;
import com.example.blog.service.*;
import com.example.blog.vo.utils.UserThreadLocal;
import com.example.blog.vo.ArticleBodyVo;
import com.example.blog.vo.ArticleVo;
import com.example.blog.vo.Result;
import com.example.blog.vo.TagVo;
import com.example.blog.vo.params.ArticleParam;
import com.example.blog.vo.params.PageParams;
import org.apache.commons.collections.map.HashedMap;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {
   @Autowired
    private ArticleMapper articleMapper;
@Autowired
private TagService tagService;
@Autowired
private CategoryService categoryService;

@Autowired
private SysUserService sysUserService;

@Autowired
private ArticleTagMapper articleTagMapper;
    @Override
    public Result listArticle(PageParams pageParams) {
        Page<Article> page=new Page<>(pageParams.getPage(),pageParams.getPagesize());

        var articleIPage = articleMapper.listArticle(page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth());
        var records = articleIPage.getRecords();
        return Result.success(copyList(records,true,true));
    }

//    @Override
//    public Result listArticle(PageParams pageParams) {
//        //分页查询
//        Page<Article> page=new Page<>(pageParams.getPage(),pageParams.getPagesize());
//        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
//        if(pageParams.getCategoryId()!=null){
//            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
//        }
//        List<Long> articleIdList=new ArrayList<>();
//        if(pageParams.getTagId()!=null){
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper=new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            var articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for(ArticleTag articleTag:articleTags){
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if(articleIdList.size()>0){
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//        }
//        //是否置顶排序
//        queryWrapper.orderByDesc(Article::getWeight);
//        //order by create_date desc
//        queryWrapper.orderByDesc(Article::getCreateDate);
//        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
//        List<Article> records = articlePage.getRecords();
//        //不能直接返回
//        List<ArticleVo> articleVoList=copyList(records,true,true);
//
//        return Result.success(articleVoList);
//    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
       List<Archives> archivesList=articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticleById(Long articleId) {
        /**
         * 根据id查看文章详情
         * 根据bodyid和category id做关联查询
         */
         Article article=articleMapper.selectById(articleId);
        var articleVo = copy(article, true, true,true,true);
        //更新完文章后，本应该直接返回数据，这时候做了一个更新操作，更新的时候加写锁，阻塞其他的读操作，性能就会比较低
        //更新增加了此次接口的耗时 如果一旦更新出问题 不能影响 查看文章的操作
        //线程池 可以把更新操作扔到主线程去执行，和主线程不想关
        threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        var sysUser = UserThreadLocal.get();
        /**
         * 发布文章 目的 构建article对象
         * 作者id 当前的登录用户
         * 标签 要将标签加入到关联列表中
         * body 内容存储
         */
        var article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        //插入后，会生成文章id
        articleMapper.insert(article);
        var tags = articleParam.getTags();
        if(tags!=null){
            for(TagVo tagVo:tags){
            var articleId = article.getId();
            var articleTag = new ArticleTag();
            articleTag.setTagId(Long.parseLong(tagVo.getId()));
            articleTag.setArticleId(articleId);
            articleTagMapper.insert(articleTag);
            }
        }
        //body
        var articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);

        Map<String,String> map=new HashedMap();
        map.put("id",article.getId().toString());
        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor) {
        ArrayList<ArticleVo> articleVoList = new ArrayList<>();
        for(Article record:records){
            articleVoList.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
    }
    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory) {
        ArrayList<ArticleVo> articleVoList = new ArrayList<>();
        for(Article record:records){
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }

    private ArticleVo copy(Article record,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory) {
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(record.getId()));
        BeanUtils.copyProperties(record,articleVo);
        articleVo.setCreateDate(new DateTime(record.getCreateDate()).toString("yyyy-MM-dd HH:MM"));
        if(isTag){
            Long articleId = record.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if(isAuthor){
            Long authorId = record.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if(isBody){
            var bodyId = record.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }
        if(isCategory){
            Long categoryId=record.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }
        return articleVo;
    }
     @Autowired
     private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {

        var articleBody = articleBodyMapper.selectById(bodyId);
        var articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
