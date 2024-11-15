# TRUNCATE TABLE voice_style;
# ALTER TABLE voice_style AUTO_INCREMENT = 1;

-- TRUNCATE TABLE voice_style;
-- ALTER TABLE voice_style AUTO_INCREMENT = 1;

INSERT INTO voice_style (country, language_code, voice_type, voice_name, gender, personality, is_visible)
VALUES
    ('대한민국', 'ko-KR', 'standard', '수연', 'female', '차분한', 1),
    ('대한민국', 'ko-KR', 'standard', '민수', 'male', '활기찬', 1),
    ('대한민국', 'ko-KR', 'standard', '지영', 'female', '지적인', 1),
    ('중국', 'zh-CN', 'standard', '웨이', 'male', '따뜻한', 1),
    ('중국', 'zh-CN', 'standard', '리나', 'female', '상냥한', 1),
    ('일본', 'ja-JP', 'standard', '히로', 'male', '신뢰감있는', 1),
    ('일본', 'ja-JP', 'standard', '사쿠라', 'female', '발랄한', 1),
    ('미국', 'en-US', 'standard', 'Emma', 'female', '친근한', 1),
    ('영국', 'en-GB', 'standard', 'Oliver', 'male', '침착한', 1);
