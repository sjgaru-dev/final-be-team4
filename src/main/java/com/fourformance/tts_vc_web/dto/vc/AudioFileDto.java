package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AudioFileDto {
    private AudioType audioType; // 오디오 타입 (VC_TRG 또는 VC_SRC)
    private Long s3MemberAudioMetaId; //  S3 메타 정보 (DB에서 조회)
    private String localFileName; // 로컬 업로드 파일의 이름 (MultipartFile 매칭)
    private String unitScript; // 선택 사항: 텍스트 스크립트 (nullable)
    private Boolean isChecked; // 체크 표시 여부

    // 유효성 검사 메서드
    public void validate() {
        // s3MemberAudioMetaId와 localFileName은 둘 중 하나만 존재해야 함
        if ((s3MemberAudioMetaId != null && localFileName != null) ||
                (s3MemberAudioMetaId == null && localFileName == null)) {
            throw new IllegalArgumentException("Either s3MemberAudioMetaId or localFileName must be set, but not both.");
        }
    }
}
