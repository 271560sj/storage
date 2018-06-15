package io.xlauncher.storage.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.xlauncher.storage.dao.GfsVolumeDaoInterface;
import io.xlauncher.storage.entity.GfsVolumePersistence;
import io.xlauncher.storage.entity.VolumeEntity;
import io.xlauncher.storage.exception.PersistenceExceptions;
import io.xlauncher.storage.persistence.GfsVolumeRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-11 下午3:55
 */
@Component
public class GfsVolumeDaoImpl implements GfsVolumeDaoInterface{

    //REST template实例
    private RestTemplate rest = new RestTemplate();

    //记录日志
    private static Logger logger = Logger.getLogger(GfsVolumeDaoImpl.class);

    //操作数据库
    @Autowired
    private GfsVolumeRepository gfsRepository;

    @Override
    public Map<String, Object> createGfsVolume(String s, VolumeEntity entity) throws Exception {

        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<String> responseEntity = rest.postForEntity(s,entity, String.class);
            String url = responseEntity.getHeaders().getLocation().toString();
            int codeValue = responseEntity.getStatusCodeValue();
            result.put("code", codeValue);
            result.put("url", url);
        }catch (HttpServerErrorException e){
            logger.error("GfsVolumeDaoImpl,createGfsVolume, create volume error",e);
            result.put("code", e.getStatusCode().value());
            result.put("url","");
        }finally {
            return result;
        }
    }

    @Override
    public JSONObject getGfsVolumeWhenCreate(String queue) throws Exception {

        JSONObject object = null;
        try {
            ResponseEntity<String> responseEntity = rest.getForEntity(queue,String.class);
            int code = responseEntity.getStatusCodeValue();

            if (code == 200) {
                String body = responseEntity.getBody();
                object = JSON.parseObject(body);
            }
        }catch (HttpServerErrorException e){
            logger.error("GfsVolumeDaoImpl,getGfsVolumeWhenCreate,get volume info error when create volume.", e);
        }finally {
            return object;
        }
    }

    @Override
    public GfsVolumePersistence insertGfsVolumeInfoToDB(GfsVolumePersistence entity) throws Exception {

        GfsVolumePersistence persistence = null;
        try {
            persistence = gfsRepository.save(persistence);
        }catch (Exception e){
            logger.error("GfsVolumeDaoImpl,insertGfsVolumeInfoToDB,add volume info into db error",e);
        }finally {
            return persistence;
        }
    }

    @Override
    public boolean deleteGfsVolume(String s, String volumeId) throws Exception {
        boolean isDelete = false;
        try {
            ResponseEntity<String> responseEntity = rest.exchange(s, HttpMethod.DELETE,null,String.class,
                    volumeId);
            int code = responseEntity.getStatusCodeValue();
            if (code == 202){
                isDelete = true;
            }
        }catch (HttpServerErrorException e){
            logger.error("GfsVolumeDaoImpl,deleteGfsVolume, delete glusterfs volume from server error", e);
        }finally {
            return isDelete;
        }
    }

    @Override
    public boolean deleteGfsVolumeInDB(GfsVolumePersistence persistence) throws Exception {
        gfsRepository.delete(persistence);
        return true;
    }

    @Override
    public GfsVolumePersistence findVolumeInDBById(String id) throws Exception {
        return gfsRepository.findById(Long.parseLong(id)).orElseThrow(() -> new PersistenceExceptions("glusterfs" +
                " volume","id", id));
    }

    @Override
    public Page<GfsVolumePersistence> getGfsVolumesInDBByUser(Integer page, Integer size, String user) throws Exception {
        Pageable pageable = new PageRequest(page,size, Sort.Direction.ASC,"createTime");
        Page<GfsVolumePersistence> result = gfsRepository.findAll(new Specification<GfsVolumePersistence>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GfsVolumePersistence> root, CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> list = new ArrayList<>();
                list.add(criteriaBuilder.equal(root.get("userName").as(String.class),user));
                Predicate[] predicates = new Predicate[list.size()];
                return criteriaBuilder.and(list.toArray(predicates));
            }
        }, pageable);
        return result;
    }
}
