package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "style_id")
    private Long id;
    private String language;
    private String voice;
    private String style;
    private String gender;
    private Integer age;
    private Boolean isVisible;

    public static Style createStyle(String language, String voice, String style, String gender, Integer age, Boolean isVisible) {
        Style styled = new Style();
        styled.language = language;
        styled.voice = voice;
        styled.style = style;
        styled.gender = gender;
        styled.age = age;
        styled.isVisible = isVisible;
        return styled;
    }
}
