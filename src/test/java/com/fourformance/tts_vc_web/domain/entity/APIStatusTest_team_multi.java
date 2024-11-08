package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.repository.APIStatusRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback(value = false)
class APIStatusTest_team_multi {
    @PersistenceContext
    EntityManager em;

    @Autowired
    APIStatusRepository apiStatusRepository;

//    public APIStatus createDumyData() {
//        APIStatus apiStatus = createAPIStatus();
//    }


}