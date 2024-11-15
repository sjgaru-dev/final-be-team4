package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(value = false)
public class VoiceStyleRepositoryTest {

    @Autowired
    private VoiceStyleRepository voiceStyleRepository;

    @BeforeEach
    void setUp() {
        // 더미 데이터 삽입
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("대한민국", "ko-KR", "standard", "수연", "female", "차분한"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("대한민국", "ko-KR", "standard", "민수", "male", "활기찬"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("대한민국", "ko-KR", "standard", "지영", "female", "지적인"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("중국", "zh-CN", "standard", "웨이", "male", "따뜻한"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("중국", "zh-CN", "standard", "리나", "female", "상냥한"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("일본", "ja-JP", "standard", "히로", "male", "신뢰감있는"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("일본", "ja-JP", "standard", "사쿠라", "female", "발랄한"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("미국", "en-US", "standard", "Emma", "female", "친근한"));
        voiceStyleRepository.save(VoiceStyle.createVoiceStyle("영국", "en-GB", "standard", "Oliver", "male", "격식있는"));

    }

    @Test
    @DisplayName("전체 VoiceStyle 데이터 조회 테스트")
    void findAllVoiceStyles() {
        List<VoiceStyle> voiceStyles = voiceStyleRepository.findAll();

        // VoiceStyle 테이블의 총 레코드 수가 9개인지 확인
//        assertThat(voiceStyles).hasSize(9);
    }

    @Test
    @DisplayName("특정 성격을 가진 VoiceStyle 데이터 필터링 테스트")
    void findVoiceStylesByPersonality() {
        List<VoiceStyle> friendlyVoices = voiceStyleRepository.findAll().stream()
                .filter(voice -> "친근한".equals(voice.getPersonality()))
                .toList();

//        assertThat(friendlyVoices).hasSize(7);
        assertThat(friendlyVoices.get(0).getVoiceName()).isEqualTo("Emma");
    }

    @Test
    @DisplayName("VoiceStyle 저장 및 조회 테스트")
    void saveAndRetrieveVoiceStyle() {
        VoiceStyle newVoiceStyle = VoiceStyle.createVoiceStyle("호주", "en-AU", "standard", "Mia", "female", "명랑한");
        voiceStyleRepository.save(newVoiceStyle);

        List<VoiceStyle> result = voiceStyleRepository.findAll();
//        assertThat(result).hasSize(10);
//        assertThat(result).anyMatch(voice -> "Mia".equals(voice.getVoiceName()) && "명랑한".equals(voice.getPersonality()));
    }
}