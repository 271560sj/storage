package io.xlauncher.storage.controller;

import io.xlauncher.storage.entity.VolumeEntity;
import io.xlauncher.storage.service.GfsVolumeServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-11 下午3:17
 * 用于操作GFS Volume
 */
@RestController
public class GfsVolumeController {

    @Autowired
    private GfsVolumeServiceInterface volumeService;

    /**
     * 获取镜像列表
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list/gfs/{user}/{page}/{size}", method = RequestMethod.GET)
    public Map<String, Object> getGfsVolumeList(@PathVariable("page")Integer page, @PathVariable("size")Integer size,
                                                @PathVariable(value = "user")String user)throws Exception {
        //获取存储卷列表
        Map<String, Object> volumes = volumeService.getGfsVolumes(page,size,user);
        return volumes;
    }

    /**
     * 获取单个镜像的信息
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list/gfs/{user}/{id}", method = RequestMethod.GET)
    public Map<String, Object> getGfsVolume(@PathVariable(value = "user") String user, @PathVariable(value = "id")
            String id)throws Exception {
        Map<String,Object> info = volumeService.getGfsVolumeById(user,id);
        return info;
    }

    /**
     * 创建镜像
     * @param user
     * @param entity
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/create/gfs/{user}/{domain}", method = RequestMethod.POST)
    public Map<String,Object> createGfsVolume(@PathVariable("user") String user, @PathVariable("domain")String domain,
                                              @RequestBody VolumeEntity entity)throws Exception {
        Map<String, Object> result = volumeService.createGfsVolume(user,domain,entity);
        return result;
    }

    /**
     * 删除glusterfs镜像
     * @param user
     * @param ids
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/delete/gfs/{user}/{domain}", method = RequestMethod.POST)
    public Map<String, Object> deleteGfsVolume(@PathVariable("domain")String domain, @PathVariable("user") String user,
                                               String... ids)throws Exception {
        Map<String, Object> result = volumeService.deleteGfsVolume(domain,user, ids);
        return result;
    }

}
