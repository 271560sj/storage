package io.xlauncher.storage.controller;

import io.xlauncher.storage.entity.HostEntity;
import io.xlauncher.storage.entity.ResponseEntity;
import io.xlauncher.storage.entity.VolumeEntity;
import io.xlauncher.storage.service.StorageServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:49
 */
@RestController
//@RequestMapping(value = "/")
public class StorageController {

    @Autowired
    private StorageServiceInterface storageService;

    /**
     * 获取存储卷列表
     * @param user
     * @param domain
     * @return
     */
    @RequestMapping(value = "/volumes", method = RequestMethod.GET)
    @ResponseBody
    public List<VolumeEntity> getStorageVolumes(String user, String domain) throws Exception{
        List<VolumeEntity> volumes = storageService.getVolumes(user,domain);
        return volumes;
    }

    /**
     * 初始化存储集群
     * @param entities
     * @param type
     */
    @RequestMapping(value = "/init/{domain}/{type}",method = RequestMethod.POST)
    @ResponseBody
    public void initStorageNode(@RequestBody List<HostEntity> entities, @PathVariable(value = "domain") String domain,
                                @PathVariable(value = "type") String type) throws Exception{
        //判断传递的参数是否为空
        if (entities == null){
            return;
        }

        //初始化存储节点
        storageService.initStorageNode(entities, domain, type);
    }

    /**
     * 检查当前租户是否已经部署了存储服务系统
     * @param domain
     * @return
     * @throws
     */
    @RequestMapping(value = "/check/{domain}")
    @ResponseBody
    public ResponseEntity checkStorageSystem(@PathVariable(value = "domain") String domain) throws Exception{
        //检查存储服务是否已经部署
        ResponseEntity entity = storageService.checkInitStorage(domain);
        return entity;
    }
}
