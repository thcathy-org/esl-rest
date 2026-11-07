package com.esl.dao.repository;

import com.esl.entity.FlagImage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface FlagImageRepository extends CrudRepository<FlagImage, Long> {
    Optional<FlagImage> findByPhraseAndImageIndex(String phrase, int imageIndex);

    @Transactional
    long deleteByPhraseAndImageIndex(String phrase, int imageIndex);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO flag_image (PHRASE, IMAGE_INDEX, FLAG_COUNT, CREATED_DATE)
            VALUES (:phrase, :imageIndex, 1, :createdDate)
            ON DUPLICATE KEY UPDATE FLAG_COUNT = FLAG_COUNT + 1
            """, nativeQuery = true)
    int addFlag(@Param("phrase") String phrase, @Param("imageIndex") int imageIndex, @Param("createdDate") Date createdDate);
}
