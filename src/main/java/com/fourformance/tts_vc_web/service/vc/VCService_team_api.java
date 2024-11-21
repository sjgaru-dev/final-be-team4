package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VCService_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCService_team_api.class.getName());
    private final ElevenLabsClient_team_api elevenLabsClient;
    private final MemberRepository memberRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final VCDetailRepository vcDetailRepository;
    // 로컬 파일 저장 경로 설정 (테스트 환경에서 사용)
    @Value("${user.home}/uploads")
    private String uploadDir;

    /**
     * 사용자가 업로드한 타겟 오디오를 통해 Voice ID를 생성하고 저장합니다.
     *
     * **주요 흐름**:
     * 1. 업로드된 오디오를 로컬 경로에 저장.
     * 2. Eleven Labs API를 호출하여 Voice ID 생성.
     * 3. 생성된 Voice ID와 관련된 정보를 MemberAudioMeta 엔티티에 저장.
     *
     * @param targetAudio 사용자가 업로드한 타겟 오디오 파일.
     * @param memberId    Voice ID를 생성 요청한 사용자 ID.
     * @return 생성된 Voice ID 문자열.
     * @throws IOException 파일 저장 또는 Voice ID 생성 중 오류가 발생할 경우.
     * @throws BusinessException 사용자가 존재하지 않을 경우.
     */
    public String createVoiceId(MultipartFile targetAudio, Long memberId) throws IOException {
        // 1. 파일 저장 디렉토리를 생성 (이미 존재하면 무시)
        Files.createDirectories(Paths.get(uploadDir));

        // 2. 업로드된 파일의 경로 지정
        String targetFilePath = uploadDir + File.separator + targetAudio.getOriginalFilename();
        File targetFile = new File(targetFilePath);

        LOGGER.info("타겟 오디오 파일 저장: " + targetFilePath);
        // 3. 업로드된 파일을 로컬 디스크에 저장
        targetAudio.transferTo(targetFile);

        LOGGER.info("Eleven Labs API를 통해 Voice ID 생성 요청 중...");
        // 4. Eleven Labs API 호출하여 Voice ID 생성
        String voiceId = elevenLabsClient.uploadVoice(targetFilePath);

        LOGGER.info("생성된 Voice ID: " + voiceId);

        // 5. 사용자 ID를 통해 Member 엔티티를 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 6. MemberAudioMeta 엔티티 생성 및 저장
        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                member,
                targetFilePath,  // 파일 경로
                "http://local/file/url",  // 테스트 환경용 임시 URL
                AudioType.VC_TRG,  // AudioType은 VC 타겟
                voiceId  // 생성된 Voice ID
        );

        memberAudioMetaRepository.save(memberAudioMeta);
        LOGGER.info("Voice ID와 오디오 메타 정보 저장 완료: " + voiceId);

        return voiceId;
    }

    /**
     * 사용자가 업로드한 소스 오디오 파일들을 주어진 Voice ID로 변환합니다.
     *
     * **주요 흐름**:
     * 1. 각 소스 오디오 파일을 로컬 디스크에 저장.
     * 2. Eleven Labs API를 호출하여 Voice ID를 이용한 변환 요청.
     * 3. 변환된 파일의 경로를 리스트로 반환.
     *
     * @param sourceAudios 변환할 소스 오디오 파일 배열.
     * @param voiceId      변환에 사용할 타겟 Voice ID.
     * @return 변환된 오디오 파일의 경로 리스트.
     * @throws IOException 파일 저장 또는 변환 중 오류가 발생할 경우.
     */
    public List<String> convertMultipleVoices(MultipartFile[] sourceAudios, String voiceId) throws IOException {
        // 1. 파일 저장 디렉토리 생성
        Files.createDirectories(Paths.get(uploadDir));

        // 2. 변환된 파일 경로를 저장할 리스트
        List<String> convertedFiles = new ArrayList<>();

        // 3. 소스 오디오 파일 하나씩 변환 처리
        for (MultipartFile sourceAudio : sourceAudios) {
            String sourceFilePath = uploadDir + File.separator + sourceAudio.getOriginalFilename();
            File sourceFile = new File(sourceFilePath);

            LOGGER.info("소스 오디오 파일 저장: " + sourceFilePath);
            // 업로드된 파일을 로컬 디스크에 저장
            sourceAudio.transferTo(sourceFile);

            LOGGER.info("소스 오디오 변환 중 (Voice ID: " + voiceId + ")");
            // Eleven Labs API를 통해 변환 요청
            String convertedFile = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFilePath);

            // 변환된 파일 경로를 리스트에 추가
            convertedFiles.add("/uploads/" + convertedFile);
        }

        LOGGER.info("소스 오디오 변환 완료: " + convertedFiles);
        return convertedFiles;
    }

    /**
     * VCDetail의 isChecked 상태를 업데이트합니다.
     *
     * @param vcDetailId VCDetail ID
     * @param isChecked 새로운 isChecked 상태
     */
    public void updateIsChecked(Long vcDetailId, Boolean isChecked) {
        LOGGER.info("VCDetail isChecked 상태 업데이트 요청: vcDetailId=" + vcDetailId + ", isChecked=" + isChecked);
        VCDetail vcDetail = vcDetailRepository.findById(vcDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DETAIL_NOT_FOUND));

        // VCDetail의 isChecked 필드를 업데이트
        vcDetail.updateDetails(isChecked, vcDetail.getUnitScript());
        vcDetailRepository.save(vcDetail);

        LOGGER.info("VCDetail isChecked 상태 업데이트 완료: vcDetailId=" + vcDetailId + ", isChecked=" + vcDetail.getIsChecked());
    }
}
