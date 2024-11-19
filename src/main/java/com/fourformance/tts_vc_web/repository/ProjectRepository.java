package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // (워크스페이스 - 홈 화면) 멤버 id에 해당하는 최근 프로젝트 5개 조회하는 메서드 - 이의준, 조소정
    List<Project> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);
}