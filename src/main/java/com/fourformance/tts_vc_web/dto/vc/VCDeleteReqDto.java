package com.fourformance.tts_vc_web.dto.vc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VCDeleteReqDto {
    private Long projectId;
    private List<Long> detailIds;
    private List<Long> audioIds;
}
