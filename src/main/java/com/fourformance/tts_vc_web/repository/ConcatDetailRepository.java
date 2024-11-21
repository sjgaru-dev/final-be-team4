package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcatDetailRepository extends JpaRepository<ConcatDetail, Long> {

    // Concat 프로젝트의 id로 Concat 디테일 리스트 조회 - 의준
    List<ConcatDetail> findByConcatProject_Id(Long projectId);

    // Concat Detail Id가 담긴 List로 ConcatDetail 객체 반환 받기 - 의준
    @Query("SELECT t FROM ConcatDetail c WHERE c.id IN :concatDetailIdList")
    List<ConcatDetail> findByConcatDetailIds(@Param("concatDetailIdList") List<Long> concatDetailIdList);

}
