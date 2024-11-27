package com.fourformance.tts_vc_web.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteReqDto {
    private Long projectId;
    private List<Long> detailIds;
    private List<Long> audioIds;
}
