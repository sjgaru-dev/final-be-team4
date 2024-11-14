package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VCService_team_multi {
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;


}
