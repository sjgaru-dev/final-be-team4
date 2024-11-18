package com.fourformance.tts_vc_web;

import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.Project;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
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
    private final VCProjectRepository vcProjectRepository;
    private final TTSDetailRepository ttsDetailRepository;
    private final VCDetailRepository vcDetailRepository;
    private final VoiceStyleRepository voiceStyleRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final ConcatProjectRepository concatProjectRepository;

    @Bean
    public ApplicationRunner initializeDummyData() {
        return args -> {
            // 생성 순서: Member -> Project -> TTSDetail, VCDetail, ConcatProject
            createDummyMembers();
            createDummyProjects();
            createDummyTTSDetails();
            createDummyVCDetails();
            createDummyConcatProjects();
        };
    }

    private void createDummyMembers() {
        if (memberRepository.count() == 0) {
            List<Member> members = List.of(
                    Member.createMember("user1@example.com", "password123", "User One", 0,
                            LocalDateTime.of(1990, 1, 1, 0, 0), "123-456-7890"),
                    Member.createMember("user2@example.com", "password456", "User Two", 0,
                            LocalDateTime.of(1995, 5, 15, 0, 0), "234-567-8901"),
                    Member.createMember("user3@example.com", "password789", "User Three", 0,
                            LocalDateTime.of(1985, 3, 20, 0, 0), "345-678-9012")
            );
            memberRepository.saveAll(members);
        }
    }

    private void createDummyProjects() {
        if (projectRepository.count() == 0) {
            List<Member> members = memberRepository.findAll();
            List<VoiceStyle> voiceStyles = voiceStyleRepository.findAll();
            List<Project> projects = List.of(
                    TTSProject.createTTSProject(members.get(0), "TTS Project 1", voiceStyles.get(0), "Hello world.",
                            1.0f, 0.0f, 0.0f),
                    TTSProject.createTTSProject(members.get(1), "TTS Project 2", voiceStyles.get(1), "This is a test.",
                            1.2f, 0.2f, -0.1f),
                    VCProject.createVCProject(members.get(2), "VC Project 1"),
                    VCProject.createVCProject(members.get(0), "VC Project 2"),
                    ConcatProject.createConcatProject(members.get(0), "Concat Project 1"),
                    ConcatProject.createConcatProject(members.get(1), "Concat Project 2")
            );
            projectRepository.saveAll(projects);
        }
    }

    private void createDummyTTSDetails() {
        if (ttsDetailRepository.count() == 0) {
            List<TTSProject> ttsProjects = ttsProjectRepository.findAll();
            List<TTSDetail> ttsDetails = List.of(
                    TTSDetail.createTTSDetail(ttsProjects.get(0), "Unit script 1", 1),
                    TTSDetail.createTTSDetail(ttsProjects.get(0), "Unit script 2", 2),
                    TTSDetail.createTTSDetail(ttsProjects.get(1), "Unit script 3", 1)
            );
            ttsDetailRepository.saveAll(ttsDetails);
        }
    }

    private void createDummyVCDetails() {
        if (vcDetailRepository.count() == 0) {
            List<VCProject> vcProjects = vcProjectRepository.findAll();
            List<VCDetail> vcDetails = List.of(
                    VCDetail.createVCDetail(vcProjects.get(0), null),
                    VCDetail.createVCDetail(vcProjects.get(1), null)
            );
            vcDetailRepository.saveAll(vcDetails);
        }
    }

    private void createDummyConcatProjects() {
        if (concatProjectRepository.count() == 0) {
            List<ConcatProject> concatProjects = concatProjectRepository.findAll();
            List<ConcatProject> concatDetails = List.of(
                    ConcatProject.createConcatProject(concatProjects.get(0).getMember(), "Concat Detail Project 1"),
                    ConcatProject.createConcatProject(concatProjects.get(1).getMember(), "Concat Detail Project")
            );
        }
    }
}
