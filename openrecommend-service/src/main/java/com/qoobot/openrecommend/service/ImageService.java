package com.qoobot.openrecommend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openrecommend.entity.Image;

import java.util.List;

/**
 * 图片服务接口
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
public interface ImageService {

    /**
     * 上传图片
     * 
     * @param image 图片信息
     * @return 图片ID
     */
    Long upload(Image image);

    /**
     * 更新图片
     * 
     * @param image 图片信息
     * @return 是否成功
     */
    boolean update(Image image);

    /**
     * 删除图片
     * 
     * @param imageId 图片ID
     * @return 是否成功
     */
    boolean delete(Long imageId);

    /**
     * 获取图片详情
     * 
     * @param imageId 图片ID
     * @return 图片详情
     */
    Image getDetail(Long imageId);

    /**
     * 分页查询图片列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 图片列表
     */
    IPage<Image> list(int pageNum, int pageSize);

    /**
     * 根据分类查询图片
     * 
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 图片列表
     */
    IPage<Image> listByCategory(Long categoryId, int pageNum, int pageSize);

    /**
     * 根据上传者查询图片
     * 
     * @param uploaderId 上传者ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 图片列表
     */
    IPage<Image> listByUploader(Long uploaderId, int pageNum, int pageSize);

    /**
     * 搜索图片
     * 
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 图片列表
     */
    IPage<Image> search(String keyword, int pageNum, int pageSize);

    /**
     * 批量获取图片
     * 
     * @param imageIds 图片ID列表
     * @return 图片列表
     */
    List<Image> getByIds(List<Long> imageIds);
}
