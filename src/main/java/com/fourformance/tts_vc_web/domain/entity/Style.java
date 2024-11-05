package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Style extends BaseEntity {

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
