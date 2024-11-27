package com.fourformance.tts_vc_web.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ExportListDto {

    Long outputAudioMetaId;
    String projectType;
    String projectName;
    String fileName;
    String script;
    String unitStatus;
    String downloadLink; // generatedPresignedUrL로 만들어전달
    LocalDateTime updateAt;

}
