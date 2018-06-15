package io.xlauncher.storage.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.xlauncher.storage.dao.GfsVolumeDaoInterface;
import io.xlauncher.storage.entity.GfsVolumePersistence;
import io.xlauncher.storage.entity.ResultEntity;
import io.xlauncher.storage.entity.VolumeEntity;
import io.xlauncher.storage.exception.PersistenceExceptions;
import io.xlauncher.storage.service.GfsVolumeServiceInterface;
import io.xlauncher.storage.utils.SshUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-11 下午3:54
 */
@Service
public class GfsVolumeServiceImpl implements GfsVolumeServiceInterface{

    //记录日志
    private static Logger logger = Logger.getLogger(GfsVolumeServiceImpl.class);

    //实例化ssh，用于操作om管理面主机
    @Autowired
    private SshUtils ssh;

    @Autowired
    private GfsVolumeDaoInterface volumeDao;

    @Override
    public Map<String, Object> getGfsVolumes(Integer page, Integer size, String user) throws Exception {
        Map<String, Object> result = new HashMap<>();

        Page<GfsVolumePersistence> volumes = volumeDao.getGfsVolumesInDBByUser(page,size,user);

        List<GfsVolumePersistence> persistences = volumes.getContent();

        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setObject(persistences);
        result.put("result", resultEntity);
        return result;
    }

    @Override
    public Map<String, Object> getGfsVolumeById(String user, String id) throws Exception {

        Map<String, Object> result = new HashMap<>();
        ResultEntity resultEntity = new ResultEntity();
        try {
            GfsVolumePersistence persistence = volumeDao.findVolumeInDBById(id);
            resultEntity.setMessage("get glusterfs volume success");
            resultEntity.setCode(200);
            resultEntity.setObject(persistence);
        }catch (PersistenceExceptions e){
            logger.error("GfsVolumeServiceImpl,getGfsVolumeById,not found volume.",e);
            resultEntity.setCode(400);
            resultEntity.setMessage("not found volume");
        }


        result.put("result",resultEntity);
        return result;
    }

    @Override
    public Map<String, Object> createGfsVolume(String user, String domain, VolumeEntity entity) throws Exception {
        //创建服务的IP地址
//        String ip = ssh.getStorageHost("glusterfs", domain);
//
//        Map<String, Object> map = new HashMap<>();
//        ResultEntity resultEntity = new ResultEntity();
//
//        String url = ip + "/volumes";
//
//        Map<String, Object> durability = new HashMap<>();
//        durability.put("type", "replicate");
//        Map<String, Integer> replica = new HashMap<>();
//        replica.put("replica",2);
//        durability.put("replicate", replica);
//
//        entity.setDurability(durability);
//        Map<String, Object> result = volumeDao.createGfsVolume(url,entity);
//
//        boolean flag = false;
//        if (result.get("code").equals(202)) {
//            String queue = ip + result.get("url").toString();
//            JSONObject object = volumeDao.getGfsVolumeWhenCreate(queue);
//            if (object != null){
//                String volumeId = object.get("id").toString().trim();
//                entity.setVolumeId(volumeId);
//                entity.setUser(user);
//            }
//
//            GfsVolumePersistence persistence = changeVolumeEntity(entity);
//            GfsVolumePersistence save = volumeDao.insertGfsVolumeInfoToDB(persistence);
//            if (save == null) {
//                volumeDao.deleteGfsVolume(ip + "/volumes/{id}", entity.getVolumeId());
//            }else {
//                flag = true;
//            }
//        }
//
//        if (flag) {
//            resultEntity.setCode(200);
//            resultEntity.setMessage("create glusterfs volume success!");
//        }else {
//            resultEntity.setCode(400);
//            resultEntity.setMessage("create glusterfs volume failed");
//        }
//
//        map.put("result", resultEntity);
//        return map;
//

        Map<String, Object> result = new HashMap<>();

        return result;
    }

    @Override
    public Map<String, Object> deleteGfsVolume(String domain, String user, String[] ids) throws Exception {

        //记录操作结果
        Map<String,Object> result = new HashMap<>();
        ResultEntity resultEntity = new ResultEntity();

        //存储服务器的IP地址
        String ip = ssh.getStorageHost("glusterfs", domain.toLowerCase());

        if (StringUtils.isBlank(user) || ids.length <= 0){
            resultEntity.setCode(300);
            resultEntity.setMessage("the user info is empty or the ids is empty!");
            result.put("result", resultEntity);
            return result;
        }

        //首先删除存储服务器中的存储卷
        for (String id : ids) {
            GfsVolumePersistence persistence = volumeDao.findVolumeInDBById(id);
            boolean isDelete = volumeDao.deleteGfsVolume(ip + "/volumes/{id}", persistence.getVolumeId());
            //从存储服务器删除成功以后，删除数据库中存储的信息
            if (isDelete) {
                volumeDao.deleteGfsVolumeInDB(persistence);
            }
        }
        return result;
    }

    /**
     * 将接收到的数据转换为实体类的数据格式
     * @param entity
     * @return
     */
    private GfsVolumePersistence changeVolumeEntity(VolumeEntity entity){
        GfsVolumePersistence persistence;

        String json = JSONObject.toJSONString(entity);

        persistence = JSON.parseObject(json, GfsVolumePersistence.class);

        return persistence;
    }

}
