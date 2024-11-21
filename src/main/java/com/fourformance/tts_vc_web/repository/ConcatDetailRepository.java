package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcatDetailRepository extends JpaRepository<ConcatDetail, Long> {

    // 프로젝트 ID로 Concat 상세 값들을 찾아 리스트로 반환 - 승민
    List<ConcatDetail> findByConcatProject_Id(Long projectId);
}
