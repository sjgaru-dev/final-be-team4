package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
//    Member findByEmail(String email);

    Optional<Member> findByEmail(String email);
    void deleteByEmail(String email);

}
