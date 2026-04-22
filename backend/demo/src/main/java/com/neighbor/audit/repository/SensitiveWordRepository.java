package com.neighbor.audit.repository;

import com.neighbor.audit.entity.SensitiveWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    List<SensitiveWord> findByStatus(Integer status);

    @Query("SELECT sw.word FROM SensitiveWord sw WHERE sw.status = 1")
    List<String> findAllEnabledWords();

    Page<SensitiveWord> findByWordContaining(String word, Pageable pageable);

    Page<SensitiveWord> findByCategory(String category, Pageable pageable);

    boolean existsByWord(String word);
}
