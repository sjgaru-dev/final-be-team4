package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.fourformance.tts_vc_web.domain.entity.Project.createProject;

@SpringBootTest
class ProjectRepositoryTest_team_multi {
    @Autowired
    ProjectRepository projectRepository;

}