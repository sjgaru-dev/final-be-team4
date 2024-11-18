package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberAudioConcatRepository;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberAudioConcatTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberAudioConcatRepository memberAudioConcatRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberAudioMetaRepository memberAudioMetaRepository;
    @Autowired
    private ConcatProjectRepository concatProjectRepository;

    @Test
    public void 멤버_오디오_컨캣_생성_테스트() {

        // given
            // 멤버 객체 생성후 DB 저장
        Member member = new Member();
        memberRepository.save(member);
        em.flush();
        em.clear();
            // 멤버오디오메타와 컨캣 프로젝트 객체 생성후 DB 저장
        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(member,null, "url", AudioType.CONCAT,null);
        ConcatProject concatProject = ConcatProject.createConcatProject(member, "컨캣프로젝트");
        memberAudioMetaRepository.save(memberAudioMeta);
        concatProjectRepository.save(concatProject);
        em.flush();
        em.clear();

            // 멤버오디오컨캣 객체 생성후 DB 저장
        MemberAudioConcat memberAudioConcat = MemberAudioConcat.createMemberAudioConcat(memberAudioMeta,concatProject);
        memberAudioConcatRepository.save(memberAudioConcat);
        em.flush();
        em.clear();


        // when
            // DB에 저장한 멤버오디오컨캣 조회
        MemberAudioConcat afterMemberAUdioConcat = memberAudioConcatRepository.findById(memberAudioConcat.getId()).orElse(null);

        // then
        assertNotNull(afterMemberAUdioConcat);
        assertEquals(memberAudioConcat.getId(), afterMemberAUdioConcat.getId());

    }

    @Test
    public void 업데이트와_삭제는_없습니다() {
        // 매핑테이블이기 떄문에 객체에 대한 업데이트와 삭제가 일어나지 않습니다.
    }



}