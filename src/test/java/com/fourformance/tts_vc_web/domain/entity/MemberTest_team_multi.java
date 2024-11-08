package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest_team_multi {

    @Autowired
    MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    public Member createTestMember(){
        return Member.createMember("test@email.com", "1234", "홍길동", 0, LocalDateTime.now(), "0101234","y", false );
    }

    @Test
    @DisplayName("회원생성")
    public void createTest(){
        Member member = Member.createMember("test@email.com", "1234", "홍길동", 0, LocalDateTime.now(), "0101234","y", false );
        memberRepository.save(member);

        em.flush();
        em.clear();

        Member findMember = memberRepository.findByEmail(member.getEmail()).get();

        assertEquals(member.getEmail(), findMember.getEmail());
    }


    @Test
    @DisplayName("회원업데이트")
    public void updateTest(){
        Member member = Member.createMember("test12@email.com", "1234", "홍길동", 0, LocalDateTime.now(), "0101234","y", false );
        System.out.println("member : "+member.toString());
//        em.persist(member); // 회원 정보 저장
        memberRepository.save(member);
        em.flush();
        em.clear();

        Member managedMember = em.find(Member.class, member.getId()); // 영속성 컨텍스트에서 다시 관리
        managedMember.updatePassword("1111"); // 비밀번호 업데이트
        em.flush(); // 변경 사항 반영
        em.clear();

        Member updatedMember = memberRepository.findByEmail(member.getEmail()).get();
        assertEquals(updatedMember.getPwd(),"1111");
    }

    @Test
    @DisplayName("회원삭제")
    public void deleteMember() {
        Member member = createTestMember();
        memberRepository.save(member);
        String memberEmail = member.getEmail();

        em.flush();
        em.clear();

        memberRepository.deleteByEmail(memberEmail);

        Member deletedMember = memberRepository.findByEmail(memberEmail).orElse(null);
        assertNull(deletedMember, "회원이 삭제되지 않았습니다.");


    }

}