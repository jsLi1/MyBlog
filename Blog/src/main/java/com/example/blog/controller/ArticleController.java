package com.example.blog.controller;

import com.example.blog.common.aop.LogAnnotation;
import com.example.blog.common.cache.Cache;
import com.example.blog.service.ArticleService;
import com.example.blog.vo.Result;
import com.example.blog.vo.params.ArticleParam;
import com.example.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController//json数据进行交互
@RequestMapping("articles")
public class ArticleController {
    @Autowired
    ArticleService articleService;
    /**
     * 首页文章列表
     * @param pageParams
     * @return
     */
    @PostMapping
    @LogAnnotation(module="文章",operator="获取文章列表")
    @Cache(expire = 5*60*1000,name = "list_article")
    public Result listArticle(@RequestBody PageParams pageParams){

        return articleService.listArticle(pageParams);
    }

    /**
     * 首页最热文章
     * @return
     */
    @PostMapping("hot")
    @Cache(expire = 5*60*1000,name = "hot_article")
    public Result hostArticle(){
        int limit=5;

        return articleService.hotArticle(limit);
    }
    /**
     * 首页最新文章
     * @return
     */
    @PostMapping("new")
    @Cache(expire = 5*60*1000,name = "new_article")
    public Result newArticles(){
        int limit=5;
        return articleService.newArticles(limit);
    }
    @PostMapping("listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId);
    }

    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }

}
