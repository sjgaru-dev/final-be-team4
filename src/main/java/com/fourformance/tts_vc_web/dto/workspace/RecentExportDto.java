package com.fourformance.tts_vc_web.dto.workspace;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class RecentExportDto {
    private Long MetaId; // 이거 outputAudioMeta id
    private String projectName; // 프로젝트 이름
    private String script;// 파이어프레임의 내용에는 VC/TTSDetail의 script가 들어감.
    private String fileName; // 파일네임이 근데 audiometa의 경로에서 정제해와야함.
    private String url; // 다운로드 받는 url
    private APIUnitStatusConst unitStatus;// 각 Detail의 Apistatus를 처리(양방향 매핑일경우)
    private LocalDateTime updatedAt; // 최신작업했던 내역 5개를 뽑으려고

}
