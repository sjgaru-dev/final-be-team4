package com.fourformance.tts_vc_web.dto.workspace;

import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RecentExportDto {
    private Long MetaId; // 이거 outputAudioMeta id
    private String projectName; // 프로젝트이름
    private String script;// script
    private String fileName;
    private String url;
    private APIUnitStatusConst unitStatus;
    private LocalDateTime updatedAt;
}