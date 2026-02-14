package com.qoobot.openrecommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openrecommend.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 视频数据访问层
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 查询热门视频
     * 
     * @param days 最近天数
     * @param limit 限制数量
     * @return 视频列表
     */
    @Select("SELECT * FROM video " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND publish_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY (view_count * 0.3 + like_count * 0.3 + comment_count * 0.2 + share_count * 0.2) DESC " +
            "LIMIT #{limit}")
    List<Video> selectHotVideos(@Param("days") int days, @Param("limit") int limit);

    /**
     * 根据标签查询视频
     * 
     * @param tags 标签集合
     * @param limit 限制数量
     * @return 视频列表
     */
    @Select("<script>" +
            "SELECT * FROM video " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND (" +
            "<foreach collection='tags' item='tag' open='' close='' separator=' OR '>" +
            "JSON_CONTAINS(tags, JSON_QUOTE(#{tag})) " +
            "</foreach>" +
            ") " +
            "ORDER BY quality_score DESC, publish_time DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Video> selectByTags(@Param("tags") Set<String> tags, @Param("limit") int limit);

    /**
     * 根据分类查询视频
     * 
     * @param categoryId 分类ID
     * @param limit 限制数量
     * @return 视频列表
     */
    @Select("SELECT * FROM video " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND category_id = #{categoryId} " +
            "ORDER BY quality_score DESC, publish_time DESC " +
            "LIMIT #{limit}")
    List<Video> selectByCategory(@Param("categoryId") Long categoryId, @Param("limit") int limit);

    /**
     * 根据上传者查询视频
     * 
     * @param uploaderId 上传者ID
     * @param limit 限制数量
     * @return 视频列表
     */
    @Select("SELECT * FROM video " +
            "WHERE status = 1 AND is_deleted = 0 " +
            "AND uploader_id = #{uploaderId} " +
            "ORDER BY publish_time DESC " +
            "LIMIT #{limit}")
    List<Video> selectByUploader(@Param("uploaderId") Long uploaderId, @Param("limit") int limit);
}
