package com.neighbor.repository;

import com.neighbor.entity.SkillExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillExchangeRepository extends JpaRepository<SkillExchange, Long> {
    
    List<SkillExchange> findByCommunityIdAndStatus(Long communityId, String status);
    
    List<SkillExchange> findByStatus(String status);
}
