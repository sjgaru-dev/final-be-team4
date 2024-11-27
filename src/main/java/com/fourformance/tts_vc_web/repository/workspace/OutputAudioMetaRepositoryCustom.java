package com.fourformance.tts_vc_web.repository.workspace;

import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;

import java.util.List;

public interface OutputAudioMetaRepositoryCustom {

    List<OutputAudioMeta> findExportHistoryBySearchCriteria(Long memberId,String keyword);
}
