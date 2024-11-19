package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    //    @Query(value = "SELECT p " +
//            "FROM Project p " +
//            "WHERE p.member.id = :memberId " +
//            "ORDER BY p.createdAt DESC")
    List<Project> findTop5ByMemberIdOrderByUpdatedAtDesc(@Param("memberId") Long memberId);
}