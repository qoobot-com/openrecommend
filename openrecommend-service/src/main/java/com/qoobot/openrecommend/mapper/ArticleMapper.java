package com.qoobot.openrecommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openrecommend.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 文章数据访问层
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 根据标签查询文章
     * 
     * @param tags 标签集合
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("<script>" +
            "SELECT * FROM article " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND (" +
            "<foreach collection='tags' item='tag' open='' close='' separator=' OR '>" +
            "JSON_CONTAINS(tags, JSON_QUOTE(#{tag})) " +
            "</foreach>" +
            ") " +
            "ORDER BY quality_score DESC, publish_time DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Article> selectByTags(@Param("tags") Set<String> tags, @Param("limit") int limit);

    /**
     * 查询热门文章
     * 
     * @param days 最近天数
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("SELECT * FROM article " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND publish_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY (view_count * 0.3 + like_count * 0.3 + comment_count * 0.2 + share_count * 0.2) DESC " +
            "LIMIT #{limit}")
    List<Article> selectHotArticles(@Param("days") int days, @Param("limit") int limit);

    /**
     * 查询未阅读的文章
     * 
     * @param excludeIds 排除的ID列表
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("<script>" +
            "SELECT * FROM article " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "<if test='excludeIds != null and excludeIds.size() > 0'>" +
            "AND id NOT IN " +
            "<foreach collection='excludeIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY quality_score DESC, publish_time DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Article> selectNotInIds(@Param("excludeIds") List<Long> excludeIds, @Param("limit") int limit);

    /**
     * 根据分类查询文章
     * 
     * @param categoryId 分类ID
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("SELECT * FROM article " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND category_id = #{categoryId} " +
            "ORDER BY quality_score DESC, publish_time DESC " +
            "LIMIT #{limit}")
    List<Article> selectByCategory(@Param("categoryId") Long categoryId, @Param("limit") int limit);

    /**
     * 根据作者查询文章
     * 
     * @param authorId 作者ID
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("SELECT * FROM article " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND author_id = #{authorId} " +
            "ORDER BY publish_time DESC " +
            "LIMIT #{limit}")
    List<Article> selectByAuthor(@Param("authorId") Long authorId, @Param("limit") int limit);
}
