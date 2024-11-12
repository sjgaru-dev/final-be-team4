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
        voiceStyleRepository.save(new VoiceStyle("대한민국", "ko-KR", "표준", "수연", "여성", "차분한"));
        voiceStyleRepository.save(new VoiceStyle("대한민국", "ko-KR", "표준", "민수", "남성", "활기찬"));
        voiceStyleRepository.save(new VoiceStyle("대한민국", "ko-KR", "표준", "지영", "여성", "지적인"));
        voiceStyleRepository.save(new VoiceStyle("중국", "zh-CN", "표준", "웨이", "남성", "따뜻한"));
        voiceStyleRepository.save(new VoiceStyle("중국", "zh-CN", "표준", "리나", "여성", "상냥한"));
        voiceStyleRepository.save(new VoiceStyle("일본", "ja-JP", "표준", "히로", "남성", "신뢰감있는"));
        voiceStyleRepository.save(new VoiceStyle("일본", "ja-JP", "표준", "사쿠라", "여성", "발랄한"));
        voiceStyleRepository.save(new VoiceStyle("미국", "en-US", "표준", "Emma", "여성", "친근한"));
        voiceStyleRepository.save(new VoiceStyle("영국", "en-GB", "표준", "Oliver", "남성", "격식있는"));
    }

    @Test
    @DisplayName("전체 VoiceStyle 데이터 조회 테스트")
    void findAllVoiceStyles() {
        List<VoiceStyle> voiceStyles = voiceStyleRepository.findAll();

        // VoiceStyle 테이블의 총 레코드 수가 9개인지 확인
        assertThat(voiceStyles).hasSize(9);
    }

    @Test
    @DisplayName("특정 성격을 가진 VoiceStyle 데이터 필터링 테스트")
    void findVoiceStylesByPersonality() {
        List<VoiceStyle> friendlyVoices = voiceStyleRepository.findAll().stream()
                .filter(voice -> "친근한".equals(voice.getPersonality()))
                .toList();

        assertThat(friendlyVoices).hasSize(1);
        assertThat(friendlyVoices.get(0).getVoiceName()).isEqualTo("Emma");
    }

    @Test
    @DisplayName("VoiceStyle 저장 및 조회 테스트")
    void saveAndRetrieveVoiceStyle() {
        VoiceStyle newVoiceStyle = new VoiceStyle("호주", "en-AU", "표준", "Mia", "여성", "명랑한");
        voiceStyleRepository.save(newVoiceStyle);

        List<VoiceStyle> result = voiceStyleRepository.findAll();
        assertThat(result).hasSize(10);
        assertThat(result).anyMatch(voice -> "Mia".equals(voice.getVoiceName()) && "명랑한".equals(voice.getPersonality()));
    }
}