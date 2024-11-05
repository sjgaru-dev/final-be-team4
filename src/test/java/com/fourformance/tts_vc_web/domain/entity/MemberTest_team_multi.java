package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest_team_multi {

    @Autowired
    MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void createTest(){
        Member member = Member.createMember("test@email.com", "1234", "홍길동", 0, LocalDateTime.now(), "0101234","y", false );
        memberRepository.save(member);

        em.flush();
        em.clear();

//        Member findMember = memberRepository.findByEmail(member.)
    }

}