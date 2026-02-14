package com.qoobot.openrecommend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openrecommend.entity.Article;

import java.util.List;

/**
 * 文章服务接口
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
public interface ArticleService {

    /**
     * 发布文章
     * 
     * @param article 文章信息
     * @return 文章ID
     */
    Long publish(Article article);

    /**
     * 更新文章
     * 
     * @param article 文章信息
     * @return 是否成功
     */
    boolean update(Article article);

    /**
     * 删除文章
     * 
     * @param articleId 文章ID
     * @return 是否成功
     */
    boolean delete(Long articleId);

    /**
     * 获取文章详情
     * 
     * @param articleId 文章ID
     * @return 文章详情
     */
    Article getDetail(Long articleId);

    /**
     * 分页查询文章列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 文章列表
     */
    IPage<Article> list(int pageNum, int pageSize);

    /**
     * 根据分类查询文章
     * 
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 文章列表
     */
    IPage<Article> listByCategory(Long categoryId, int pageNum, int pageSize);

    /**
     * 根据作者查询文章
     * 
     * @param authorId 作者ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 文章列表
     */
    IPage<Article> listByAuthor(Long authorId, int pageNum, int pageSize);

    /**
     * 搜索文章
     * 
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 文章列表
     */
    IPage<Article> search(String keyword, int pageNum, int pageSize);

    /**
     * 批量获取文章
     * 
     * @param articleIds 文章ID列表
     * @return 文章列表
     */
    List<Article> getByIds(List<Long> articleIds);
}
