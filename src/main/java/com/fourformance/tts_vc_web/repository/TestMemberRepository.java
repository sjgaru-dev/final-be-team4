package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestMemberRepository extends JpaRepository<Member, Long> {

    // 이메일을 통해 회원을 찾는 쿼리 메서드 - 이의준
    Optional<Member> findByEmail(String email);

    // 이름으로 회원을 검색하는 메서드 - 남유람
    Optional<Member> findByName(String name);

}
