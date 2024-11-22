package com.fourformance.tts_vc_web.dto.concat;

import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConcatDetailDto {

    private Long id;
    private String localFileName;
    private Integer audioSeq;
    private Boolean isChecked;
    private String unitScript;
    private Float endSilence;

//    public static ConcatDetailDto createFromEntity(ConcatDetail concatDetail) {
//        return new ConcatDetailDto(
//                concatDetail.getId(),
//                concatDetail.getAudioSeq(),
//                concatDetail.isChecked(),
//                concatDetail.getUnitScript(),
//                concatDetail.getEndSilence()
//        );
//    }
}

