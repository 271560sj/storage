package io.xlauncher.storage.dao;

import com.alibaba.fastjson.JSONObject;
import io.xlauncher.storage.entity.GfsVolumePersistence;
import io.xlauncher.storage.entity.VolumeEntity;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-11 下午3:55
 */
public interface GfsVolumeDaoInterface {
    /**
     * 创建存储卷
     * @param s
     * @param entity
     * @return
     * @throws Exception
     */
    Map<String, Object> createGfsVolume(String s, VolumeEntity entity)throws Exception;

    /**
     * 当创建glusterfs存储卷成功以后，根据返回的查询URL。，获取存储卷的详细信息，便于保存到数据库
     * @param queue
     * @return
     * @throws Exception
     */
    JSONObject getGfsVolumeWhenCreate(String queue)throws Exception;

    /**
     * 将glusterfs存储卷的信息插入到数据库中，用于维护存储卷与用户之间的关系
     * @param entity
     * @return
     * @throws Exception
     */
    GfsVolumePersistence insertGfsVolumeInfoToDB(GfsVolumePersistence entity)throws Exception;

    /**
     * 从存储服务器删除创建的存储卷
     * @param s
     * @param volumeId
     * @return
     * @throws Exception
     */
    boolean deleteGfsVolume(String s, String volumeId)throws Exception;


    /**
     * 删除存储在数据库中的glusterfs的存储卷的信息
     * @param persistence
     * @return
     * @throws Exception
     */
    boolean deleteGfsVolumeInDB(GfsVolumePersistence persistence)throws Exception;

    /**
     * 从数据库中查询单个glusterfs存储卷的记录
     * @param id
     * @return
     * @throws Exception
     */
    GfsVolumePersistence findVolumeInDBById(String id)throws Exception;

    /**
     * 获取用户的存储卷列表
     * @param page
     * @param size
     * @param user
     * @return
     * @throws Exception
     */
    Page<GfsVolumePersistence> getGfsVolumesInDBByUser(Integer page, Integer size, String user)throws Exception;
}
