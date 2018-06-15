package io.xlauncher.storage.service;

import io.xlauncher.storage.entity.VolumeEntity;

import java.util.Map;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-11 下午3:54
 */
public interface GfsVolumeServiceInterface {

    /**
     * 获取用户创建的存储卷的列表
     * @param page
     * @param size
     * @param user 存储卷所属的用户
     * @return
     * @throws Exception
     */
    Map<String,Object> getGfsVolumes(Integer page, Integer size,String user)throws Exception;

    /**
     * 获取存储卷详细信息
     * @param user 存储卷所属的用户
     * @param id 存储卷的id
     * @return
     * @throws Exception
     */
    Map<String,Object> getGfsVolumeById(String user, String id)throws Exception;

    /**
     * 创建存储卷
     * @param user 创建存储卷的用户
     * @param domain 用户所在的租户组
     * @param entity 创建存储卷的信息
     * @return
     * @throws Exception
     */
    Map<String,Object> createGfsVolume(String user, String domain, VolumeEntity entity)throws Exception;

    /**
     * 删除存储卷
     * @param domain 用户所在的租户组
     * @param user 存储卷所属的用户
     * @param ids 需要删除的存储卷的id列表
     * @return
     * @throws Exception
     */
    Map<String,Object> deleteGfsVolume(String domain, String user, String[] ids)throws Exception;
}
