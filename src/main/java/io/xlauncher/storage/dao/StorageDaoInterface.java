package io.xlauncher.storage.dao;

import io.xlauncher.storage.entity.HostEntity; /**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:51
 */
public interface StorageDaoInterface {
    /**
     * 添加主机到k8s平台
     * @param entity
     * @param domain
     * @throws Exception
     */
    void addStorageNode(HostEntity entity, String domain) throws Exception;
}
