package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcatDetailRepository extends JpaRepository<ConcatDetail, Long> {
    List<ConcatDetail> findByConcatProjectId(Long projectId);
}
