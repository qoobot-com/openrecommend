package com.qoobot.openrecommend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openrecommend.entity.Video;

import java.util.List;

/**
 * 视频服务接口
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
public interface VideoService {

    /**
     * 上传视频
     * 
     * @param video 视频信息
     * @return 视频ID
     */
    Long upload(Video video);

    /**
     * 更新视频
     * 
     * @param video 视频信息
     * @return 是否成功
     */
    boolean update(Video video);

    /**
     * 删除视频
     * 
     * @param videoId 视频ID
     * @return 是否成功
     */
    boolean delete(Long videoId);

    /**
     * 获取视频详情
     * 
     * @param videoId 视频ID
     * @return 视频详情
     */
    Video getDetail(Long videoId);

    /**
     * 分页查询视频列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频列表
     */
    IPage<Video> list(int pageNum, int pageSize);

    /**
     * 根据分类查询视频
     * 
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频列表
     */
    IPage<Video> listByCategory(Long categoryId, int pageNum, int pageSize);

    /**
     * 根据上传者查询视频
     * 
     * @param uploaderId 上传者ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频列表
     */
    IPage<Video> listByUploader(Long uploaderId, int pageNum, int pageSize);

    /**
     * 搜索视频
     * 
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频列表
     */
    IPage<Video> search(String keyword, int pageNum, int pageSize);

    /**
     * 批量获取视频
     * 
     * @param videoIds 视频ID列表
     * @return 视频列表
     */
    List<Video> getByIds(List<Long> videoIds);
}
