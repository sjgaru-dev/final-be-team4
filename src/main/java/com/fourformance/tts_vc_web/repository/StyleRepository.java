package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StyleRepository extends JpaRepository<Style, Long> {
}
