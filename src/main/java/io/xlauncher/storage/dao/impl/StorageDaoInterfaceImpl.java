package io.xlauncher.storage.dao.impl;

import io.xlauncher.storage.dao.StorageDaoInterface;
import io.xlauncher.storage.entity.HostEntity;
import io.xlauncher.storage.utils.SshUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:52
 */
@Component
public class StorageDaoInterfaceImpl implements StorageDaoInterface {

    @Autowired
    private SshUtils ssh;
    /**
     * 添加主机到k8s平台
     * @param entity
     * @throws Exception
     */
    @Override
    public void addStorageNode(HostEntity entity, String domain) throws Exception {
        ssh.setHostEntity(entity);
        ssh.execute("curl -ssl -k https://8.16.0.70:30402/joinSlaveNode.sh | sh -s  8.16.0.70:30402 " +
                "54ab26.8b8abe7132019134 8.16.0.70:6443 8.16.0.70:30400 " + domain.toLowerCase());
    }
}
