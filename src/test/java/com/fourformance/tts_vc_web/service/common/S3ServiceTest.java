package com.fourformance.tts_vc_web.service.common;

import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(false)
class S3ServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TTSProjectRepository ttsProjectRepository;
    @Autowired
    private TTSDetailRepository ttsDetailRepository;
    @Autowired
    private ConcatProjectRepository concatProjectRepository;

    @Test
    public void 테스트용_TTS_디테일_생성() {
        Member member = Member.createMember("abc@abc.com", "abc123", "철수",0,now(), "010123123");
        TTSProject ttsProject = TTSProject.createTTSProject(member, "테스트 tts 프로젝트");
        TTSDetail ttsDetail = TTSDetail.createTTSDetail(ttsProject, "테스트 tts 스크립트", 1);

        memberRepository.save(member);
        ttsProjectRepository.save(ttsProject);
        ttsDetailRepository.save(ttsDetail);
    }

    @Test
    public void 테스트용_TTS_더미_날리기() {
        ttsDetailRepository.deleteAll();
        ttsProjectRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    public void 테스트용_컨캣_프로젝트_생성() {
        Member member = Member.createMember("abc@abc.com", "abc123", "짱구",0,now(), "010123123");
        ConcatProject concatProject = ConcatProject.createConcatProject(member, "테스트 컨캣 프로젝트");

        memberRepository.save(member);
        concatProjectRepository.save(concatProject);
    }

    @Test
    public void 테스트용_컨캣_더미_날리기() {
        ttsDetailRepository.deleteAll();
        ttsProjectRepository.deleteAll();
        memberRepository.deleteAll();
    }





}