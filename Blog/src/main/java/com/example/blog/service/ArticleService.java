package com.example.blog.service;

import com.example.blog.vo.Result;
import com.example.blog.vo.params.ArticleParam;
import com.example.blog.vo.params.PageParams;
import org.springframework.transaction.annotation.Transactional;


public interface ArticleService {

    Result listArticle(PageParams pageParams);

    Result hotArticle(int limit);

    Result newArticles(int limit);
    //文章归档
    Result listArchives();
    //查看文章详情
    Result findArticleById(Long articleId);

    Result publish(ArticleParam articleParam);
}
