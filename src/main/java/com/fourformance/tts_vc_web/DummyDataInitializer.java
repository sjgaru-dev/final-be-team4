package com.fourformance.tts_vc_web;

import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.Project;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
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
    private final VoiceStyleRepository voiceStyleRepository;

    @Bean
    public ApplicationRunner initializeDummyData() {
        return args -> {
            createDummyMembers();
            createDummyProjects();
            createDummyTTSDetails();
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
                    TTSProject.createTTSProject(members.get(0), "TTS Project 3", voiceStyles.get(0), "Hello Java.",
                            1.0f, 0.0f, 0.0f)
            );
            projectRepository.saveAll(projects);
        }
    }

    private void createDummyTTSDetails() {
        if (ttsDetailRepository.count() == 0) {
            List<TTSProject> ttsProjects = ttsProjectRepository.findAll();
            List<VoiceStyle> voiceStyles = voiceStyleRepository.findAll();

            List<TTSDetail> ttsDetails = List.of(
                    TTSDetail.createTTSDetail(ttsProjects.get(0), "Unit script 1", 1),
                    TTSDetail.createTTSDetail(ttsProjects.get(0), "Unit script 2", 2),
                    TTSDetail.createTTSDetail(ttsProjects.get(1), "Unit script 3", 1)
            );

            // Assign voice styles to TTS details
            ttsDetails.get(0).updateTTSDetail(voiceStyles.get(0), "Unit script 1", 1.0f, 0.0f, 0.0f, 1, false);
            ttsDetails.get(1).updateTTSDetail(voiceStyles.get(1), "Unit script 2", 1.1f, 0.1f, 0.1f, 2, false);
            ttsDetails.get(2).updateTTSDetail(voiceStyles.get(2), "Unit script 3", 0.9f, -0.1f, -0.1f, 3, false);

            ttsDetailRepository.saveAll(ttsDetails);
        }
    }
}