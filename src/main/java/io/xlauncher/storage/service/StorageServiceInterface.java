package io.xlauncher.storage.service;

import io.xlauncher.storage.entity.HostEntity;
import io.xlauncher.storage.entity.ResponseEntity;
import io.xlauncher.storage.entity.VolumeEntity;

import java.util.List;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:49
 */
public interface StorageServiceInterface {

    /**
     * 获取存储卷列表
     * @param user
     * @param domain
     * @return
     * @throws Exception
     */
    List<VolumeEntity> getVolumes(String user, String domain) throws Exception;

    /**
     * 初始化存储节点
     * @param entities
     * @param domain
     * @param type
     * @throws Exception
     */
    void initStorageNode(List<HostEntity> entities, String domain, String type) throws Exception;

    /**
     * 检查当前租户是否已经部署存储服务系统
     * @param domain
     * @return
     * @throws Exception
     */
    ResponseEntity checkInitStorage(String domain) throws Exception;
}
