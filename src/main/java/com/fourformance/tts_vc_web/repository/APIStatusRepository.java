package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.domain.entity.APIStatus;
import com.fourformance.tts_vc_web.domain.entity.Style;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface APIStatusRepository extends JpaRepository<APIStatus, Long> {
}
