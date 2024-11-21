package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcatDetailRepository extends JpaRepository<ConcatDetail, Long> {
    // 특정 ConcatProject와 연관된 모든 ConcatDetail 조회
    List<ConcatDetail> findByConcatProjectId(Long projectId);

    // 특정 ConcatProject와 연관된 ConcatDetail의 script만 조회
    @Query("SELECT d.unitScript FROM ConcatDetail d WHERE d.concatProject.id = :projectId")
    List<String> findScriptsByConcatProjectId(@Param("projectId") Long projectId);
}
