package com.esl.dao.repository;

import com.esl.entity.ImageReport;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageReportRepository extends CrudRepository<ImageReport, Long> {
    List<ImageReport> findByDoneFalse();

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ImageReport r set r.done = true where r.phrase = :phrase and r.imageIndex = :imageIndex and r.done = false")
    int markDone(@Param("phrase") String phrase, @Param("imageIndex") int imageIndex);
}
