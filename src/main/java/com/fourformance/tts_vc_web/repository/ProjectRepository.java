package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // sojeong-작성,
    // 사용처 workspace의 최근작업내역 탑10만들기
    // ProjectService에서 메서드 사용
    @Query()

    List<Project> findTop10ByMemberIdOrderByCreatedAtDesc(Long memberId);
}