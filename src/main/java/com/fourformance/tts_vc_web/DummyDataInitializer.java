package com.fourformance.tts_vc_web;

import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.repository.ConcatDetailRepository;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import com.fourformance.tts_vc_web.repository.VoiceStyleRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DummyDataInitializer {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final TTSProjectRepository ttsProjectRepository;
    private final TTSDetailRepository ttsDetailRepository;
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;
    private final ConcatProjectRepository concatProjectRepository;
    private final ConcatDetailRepository concatDetailRepository;
    private final VoiceStyleRepository voiceStyleRepository;

    @Bean
    public ApplicationRunner initializeDummyData() {
        return args -> initializeData();
    }

    private void initializeData() {
        // VoiceStyle이 미리 데이터베이스에 존재한다고 가정
        List<VoiceStyle> voiceStyles = voiceStyleRepository.findAll();

        if (voiceStyles.isEmpty()) {
            throw new IllegalStateException("VoiceStyle 데이터가 없습니다. data.sql을 확인하세요.");
        }

        // 멤버 생성
        List<Member> members = List.of(
                Member.createMember("user1@example.com", "password123", "User One", 0,
                        LocalDateTime.of(1990, 1, 1, 0, 0), "123-456-7890"),
                Member.createMember("user2@example.com", "password456", "User Two", 0,
                        LocalDateTime.of(1995, 5, 15, 0, 0), "234-567-8901"),
                Member.createMember("user3@example.com", "password789", "User Three", 0,
                        LocalDateTime.of(1985, 3, 20, 0, 0), "345-678-9012")
        );

        memberRepository.saveAll(members);

        Member firstMember = members.get(0);

        // TTS 프로젝트 및 디테일 생성
//        for (int i = 1; i <= 5; i++) {
//            VoiceStyle voiceStyle = voiceStyles.get((i - 1) % voiceStyles.size());
//            TTSProject ttsProject = TTSProject.createTTSProject(
//                    firstMember,
//                    "TTS Project " + i,
//                    voiceStyle,
//                    "This is the full script for project " + i,
//                    1.0f + i * 0.1f,
//                    0.0f,
//                    0.0f
//            );
//
//            ttsProjectRepository.save(ttsProject);
//
//            for (int j = 1; j <= 5; j++) {
//                TTSDetail ttsDetail = TTSDetail.createTTSDetail(
//                        ttsProject,
//                        "Unit script " + j,
//                        j
//                );
//
//                ttsDetail.updateTTSDetail(
//                        voiceStyle,
//                        "Unit script " + j,
//                        1.0f + j * 0.1f,
//                        0.0f,
//                        0.0f,
//                        j,
//                        false
//                );
//
//                ttsDetailRepository.save(ttsDetail);
//            }
//        }
//
//        // VC 프로젝트 및 디테일 생성
//        for (int i = 1; i <= 5; i++) {
//            VCProject vcProject = VCProject.createVCProject(
//                    firstMember,
//                    "VC Project " + i
//            );
//
//            vcProjectRepository.save(vcProject);
//
//            for (int j = 1; j <= 5; j++) {
//                VCDetail vcDetail = VCDetail.createVCDetail(
//                        vcProject,
//                        null // memberAudioMeta는 여기서 null로 시작합니다.
//                );
//
//                vcDetail.updateDetails(false, "Unit script " + j);
//                vcDetailRepository.save(vcDetail);
//            }
//        }
//
//        // Concat 프로젝트 및 디테일 생성
//        for (int i = 1; i <= 5; i++) {
//            ConcatProject concatProject = ConcatProject.createConcatProject(
//                    firstMember,
//                    "Concat Project " + i
//            );
//
//            concatProjectRepository.save(concatProject);
//
//            for (int j = 1; j <= 5; j++) {
//                ConcatDetail concatDetail = ConcatDetail.createConcatDetail(
//                        concatProject,
//                        j,
//                        true,
//                        "Concat unit script " + j,
//                        0.2f * j
//                );
//
//                concatDetailRepository.save(concatDetail);
//            }
//        }
    }
}