package io.xlauncher.storage.persistence;

import io.xlauncher.storage.entity.GfsVolumePersistence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-14 下午1:35
 */
@Repository
public interface GfsVolumeRepository extends JpaRepository<GfsVolumePersistence,Long>,
        JpaSpecificationExecutor<GfsVolumePersistence>{

    Page<GfsVolumePersistence> findAll(Specification<GfsVolumePersistence> userName, Pageable page);
}
