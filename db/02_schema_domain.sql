-- ============================================================================
-- Kkumz (꿈즈) 도메인 전용 비즈니스 스키마 (02_schema_domain.sql)
-- ============================================================================

DROP TABLE IF EXISTS kkumz_notification CASCADE;
DROP TABLE IF EXISTS kkumz_ai_prompt_template CASCADE;
DROP TABLE IF EXISTS kkumz_moderation_report CASCADE;
DROP TABLE IF EXISTS kkumz_feed_like CASCADE;
DROP TABLE IF EXISTS kkumz_feed_comment CASCADE;
DROP TABLE IF EXISTS kkumz_team_member CASCADE;
DROP TABLE IF EXISTS kkumz_team_recruit CASCADE;
DROP TABLE IF EXISTS kkumz_build_job CASCADE;
DROP TABLE IF EXISTS kkumz_site_release CASCADE;
DROP TABLE IF EXISTS kkumz_site CASCADE;
DROP TABLE IF EXISTS kkumz_screen_spec CASCADE;
DROP TABLE IF EXISTS kkumz_logo_suggestion CASCADE;
DROP TABLE IF EXISTS kkumz_name_suggestion CASCADE;
DROP TABLE IF EXISTS kkumz_idea_version CASCADE;
DROP TABLE IF EXISTS kkumz_idea CASCADE;

-- 1. 아이디어 마스터 (kkumz_idea)
CREATE TABLE kkumz_idea (
    id VARCHAR(64) PRIMARY KEY,
    author_username VARCHAR(64) NOT NULL DEFAULT 'admin',
    author_display_name VARCHAR(100) NOT NULL DEFAULT '김도현 창업가',
    title VARCHAR(200) NOT NULL,
    prompt_text TEXT NOT NULL,
    site_type VARCHAR(32) NOT NULL DEFAULT 'COMMERCE',
    category VARCHAR(64) NOT NULL DEFAULT 'FOOD_BEVERAGE',
    tags_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    current_step INTEGER NOT NULL DEFAULT 1,
    selected_brand_name VARCHAR(128),
    selected_site_slug VARCHAR(64),
    selected_logo_svg TEXT,
    design_tokens_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    spec_markdown TEXT,
    screens_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. 아이디어 및 기능명세서 버전 히스토리 (kkumz_idea_version)
CREATE TABLE kkumz_idea_version (
    id VARCHAR(64) PRIMARY KEY,
    idea_id VARCHAR(64) NOT NULL REFERENCES kkumz_idea(id) ON DELETE CASCADE,
    version_no INTEGER NOT NULL DEFAULT 1,
    title VARCHAR(200) NOT NULL,
    prompt_text TEXT NOT NULL,
    spec_markdown TEXT NOT NULL,
    screens_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_by VARCHAR(64) NOT NULL DEFAULT 'admin',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_kkumz_idea_ver UNIQUE (idea_id, version_no)
);

-- 3. AI 브랜드 네이밍 추천 후보 (kkumz_name_suggestion)
CREATE TABLE kkumz_name_suggestion (
    id VARCHAR(64) PRIMARY KEY,
    idea_id VARCHAR(64) NOT NULL REFERENCES kkumz_idea(id) ON DELETE CASCADE,
    brand_name VARCHAR(128) NOT NULL,
    site_slug VARCHAR(64) NOT NULL,
    pronunciation VARCHAR(128) NOT NULL,
    brand_story TEXT NOT NULL,
    score INTEGER NOT NULL DEFAULT 95,
    slug_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_selected BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. AI 벡터 SVG 로고 및 컬러 팔레트 후보 (kkumz_logo_suggestion)
CREATE TABLE kkumz_logo_suggestion (
    id VARCHAR(64) PRIMARY KEY,
    idea_id VARCHAR(64) NOT NULL REFERENCES kkumz_idea(id) ON DELETE CASCADE,
    style_name VARCHAR(100) NOT NULL,
    svg_code TEXT NOT NULL,
    color_palette_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    typography_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    tags_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_selected BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. 화면 설계 목록 (kkumz_screen_spec)
CREATE TABLE kkumz_screen_spec (
    id VARCHAR(64) PRIMARY KEY,
    idea_id VARCHAR(64) NOT NULL REFERENCES kkumz_idea(id) ON DELETE CASCADE,
    screen_key VARCHAR(64) NOT NULL,
    screen_title VARCHAR(150) NOT NULL,
    route_path VARCHAR(128) NOT NULL,
    layout_type VARCHAR(64) NOT NULL DEFAULT 'STANDARD_GRID',
    description VARCHAR(500),
    components_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    sort_no INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. 생성 및 배포된 멀티사이트 마스터 (kkumz_site)
CREATE TABLE kkumz_site (
    id VARCHAR(64) PRIMARY KEY,
    idea_id VARCHAR(64) NOT NULL REFERENCES kkumz_idea(id) ON DELETE CASCADE,
    site_slug VARCHAR(64) NOT NULL,
    site_name VARCHAR(128) NOT NULL,
    site_type VARCHAR(32) NOT NULL DEFAULT 'COMMERCE',
    category VARCHAR(64) NOT NULL DEFAULT 'FOOD_BEVERAGE',
    brand_slogan VARCHAR(255) NOT NULL,
    logo_svg TEXT NOT NULL,
    shop_site_id BIGINT,
    cms_site_id BIGINT,
    deploy_mode VARCHAR(32) NOT NULL DEFAULT 'DYNAMIC_SDUI',
    active_version INTEGER NOT NULL DEFAULT 1,
    deployed_url VARCHAR(255) NOT NULL,
    custom_domain VARCHAR(255),
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    owner_username VARCHAR(64) NOT NULL DEFAULT 'admin',
    owner_display_name VARCHAR(100) NOT NULL DEFAULT '김도현 창업가',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_kkumz_site_slug UNIQUE (site_slug)
);

-- 7. 멀티사이트 릴리즈 스냅샷 및 WebIDE 소스 (kkumz_site_release)
CREATE TABLE kkumz_site_release (
    id VARCHAR(64) PRIMARY KEY,
    site_id VARCHAR(64) NOT NULL REFERENCES kkumz_site(id) ON DELETE CASCADE,
    version_no INTEGER NOT NULL DEFAULT 1,
    release_note VARCHAR(500) NOT NULL,
    deploy_mode VARCHAR(32) NOT NULL DEFAULT 'DYNAMIC_SDUI',
    design_tokens_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    layout_schema_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    webide_files_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    minio_bundle_path VARCHAR(500),
    build_status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    build_log TEXT,
    created_by VARCHAR(64) NOT NULL DEFAULT 'admin',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_kkumz_site_rel_ver UNIQUE (site_id, version_no)
);

-- 8. 샌드박스 빌드 잡 & AI 에러 패치 이력 (kkumz_build_job)
CREATE TABLE kkumz_build_job (
    id VARCHAR(64) PRIMARY KEY,
    site_id VARCHAR(64) NOT NULL REFERENCES kkumz_site(id) ON DELETE CASCADE,
    release_id VARCHAR(64) REFERENCES kkumz_site_release(id) ON DELETE SET NULL,
    job_type VARCHAR(32) NOT NULL DEFAULT 'INITIAL_PROVISION',
    deploy_mode VARCHAR(32) NOT NULL DEFAULT 'DYNAMIC_SDUI',
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    duration_ms BIGINT NOT NULL DEFAULT 1200,
    error_summary VARCHAR(500),
    ai_patch_applied BOOLEAN NOT NULL DEFAULT FALSE,
    build_log TEXT NOT NULL,
    triggered_by VARCHAR(64) NOT NULL DEFAULT 'admin',
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. 팀원 모집 공고 (kkumz_team_recruit)
CREATE TABLE kkumz_team_recruit (
    id VARCHAR(64) PRIMARY KEY,
    site_id VARCHAR(64) NOT NULL REFERENCES kkumz_site(id) ON DELETE CASCADE,
    position_role VARCHAR(32) NOT NULL DEFAULT 'FRONTEND',
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    required_skills_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    headcount INTEGER NOT NULL DEFAULT 1,
    recruited_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. 프로젝트 합류 팀원 및 지원/초대 내역 (kkumz_team_member)
CREATE TABLE kkumz_team_member (
    id VARCHAR(64) PRIMARY KEY,
    site_id VARCHAR(64) NOT NULL REFERENCES kkumz_site(id) ON DELETE CASCADE,
    recruit_id VARCHAR(64) REFERENCES kkumz_team_recruit(id) ON DELETE SET NULL,
    username VARCHAR(64) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    member_role VARCHAR(32) NOT NULL DEFAULT 'EDITOR',
    position_role VARCHAR(32) NOT NULL DEFAULT 'FRONTEND',
    join_type VARCHAR(32) NOT NULL DEFAULT 'APPLIED',
    application_message TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. 커뮤니티 피드 댓글 (kkumz_feed_comment)
CREATE TABLE kkumz_feed_comment (
    id VARCHAR(64) PRIMARY KEY,
    site_id VARCHAR(64) NOT NULL REFERENCES kkumz_site(id) ON DELETE CASCADE,
    username VARCHAR(64) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    author_role_badge VARCHAR(64) DEFAULT '메이커',
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. 커뮤니티 피드 좋아요 (kkumz_feed_like)
CREATE TABLE kkumz_feed_like (
    id VARCHAR(64) PRIMARY KEY,
    site_id VARCHAR(64) NOT NULL REFERENCES kkumz_site(id) ON DELETE CASCADE,
    username VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_kkumz_feed_like UNIQUE (site_id, username)
);

-- 13. 콘텐츠 신고 및 모더레이션 심사 원장 (kkumz_moderation_report)
CREATE TABLE kkumz_moderation_report (
    id VARCHAR(64) PRIMARY KEY,
    target_type VARCHAR(32) NOT NULL DEFAULT 'SITE',
    target_id VARCHAR(64) NOT NULL,
    site_id VARCHAR(64) NOT NULL REFERENCES kkumz_site(id) ON DELETE CASCADE,
    reporter_username VARCHAR(64) NOT NULL,
    reason_category VARCHAR(64) NOT NULL,
    reason_detail TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    action_taken VARCHAR(255),
    moderator_username VARCHAR(64),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 14. AI 생성 프롬프트 템플릿 및 도메인 필터 규칙 (kkumz_ai_prompt_template)
CREATE TABLE kkumz_ai_prompt_template (
    id VARCHAR(64) PRIMARY KEY,
    template_code VARCHAR(64) NOT NULL UNIQUE,
    template_name VARCHAR(128) NOT NULL,
    stage_type VARCHAR(32) NOT NULL,
    model_name VARCHAR(64) NOT NULL DEFAULT 'qwen-plus',
    temperature NUMERIC(3,2) NOT NULL DEFAULT 0.70,
    system_prompt TEXT NOT NULL,
    user_prompt_template TEXT NOT NULL DEFAULT '',
    forbidden_words_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(64) NOT NULL DEFAULT 'admin',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'admin',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 15. 사용자 인앱 알림 센터 (kkumz_notification)
CREATE TABLE kkumz_notification (
    id VARCHAR(64) PRIMARY KEY,
    recipient_username VARCHAR(64) NOT NULL,
    notif_type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    link_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kkumz_site_type_status ON kkumz_site (site_type, status, like_count DESC);
CREATE INDEX idx_kkumz_recruit_site_status ON kkumz_team_recruit (site_id, status);
CREATE INDEX idx_kkumz_comment_site_time ON kkumz_feed_comment (site_id, created_at DESC);
CREATE INDEX idx_kkumz_report_status ON kkumz_moderation_report (status, created_at DESC);
CREATE INDEX idx_kkumz_build_site_time ON kkumz_build_job (site_id, started_at DESC);
CREATE INDEX idx_kkumz_noti_user_read ON kkumz_notification (recipient_username, is_read, created_at DESC);
