package com.fourformance.tts_vc_web.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Style {

    @Id
    @GeneratedValue
    @Column(name = "style_id")
    private Long id;
    private String language;
    private String voice;
    private String style;
    private String gender;
    private Integer age;
    private Boolean isVisible;
}
