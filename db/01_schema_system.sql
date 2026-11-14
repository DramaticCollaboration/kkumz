-- ============================================================================
-- Kkumz (꿈즈) 플랫폼 공통 시스템 스키마 (01_schema_system.sql)
-- ============================================================================

DROP TABLE IF EXISTS sys_log CASCADE;
DROP TABLE IF EXISTS sys_dict_item CASCADE;
DROP TABLE IF EXISTS sys_dict CASCADE;
DROP TABLE IF EXISTS sys_role_permission CASCADE;
DROP TABLE IF EXISTS sys_user_role CASCADE;
DROP TABLE IF EXISTS sys_permission CASCADE;
DROP TABLE IF EXISTS sys_role CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;

CREATE TABLE sys_user (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    realname VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    salt VARCHAR(64),
    avatar VARCHAR(500),
    email VARCHAR(128),
    phone VARCHAR(32),
    org_code VARCHAR(64) DEFAULT 'KKUMZ',
    status INTEGER NOT NULL DEFAULT 1,
    del_flag INTEGER NOT NULL DEFAULT 0,
    create_by VARCHAR(64) DEFAULT 'admin',
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT 'admin',
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role (
    id VARCHAR(64) PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(500),
    create_by VARCHAR(64) DEFAULT 'admin',
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT 'admin',
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_permission (
    id VARCHAR(64) PRIMARY KEY,
    parent_id VARCHAR(64),
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    component VARCHAR(255) NOT NULL,
    is_route BOOLEAN NOT NULL DEFAULT TRUE,
    component_name VARCHAR(128),
    redirect VARCHAR(255),
    menu_type INTEGER NOT NULL DEFAULT 1,
    perms VARCHAR(128),
    perms_type VARCHAR(16) DEFAULT '1',
    sort_no NUMERIC(8,2) NOT NULL DEFAULT 1.0,
    always_show BOOLEAN NOT NULL DEFAULT FALSE,
    icon VARCHAR(128),
    is_leaf BOOLEAN NOT NULL DEFAULT TRUE,
    keep_alive BOOLEAN NOT NULL DEFAULT FALSE,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    hide_tab BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    create_by VARCHAR(64) DEFAULT 'admin',
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT 'admin',
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INTEGER NOT NULL DEFAULT 0,
    rule_flag INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    internal_or_external BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE sys_user_role (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    role_id VARCHAR(64) NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT uk_kkumz_sys_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE sys_role_permission (
    id VARCHAR(64) PRIMARY KEY,
    role_id VARCHAR(64) NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    permission_id VARCHAR(64) NOT NULL REFERENCES sys_permission(id) ON DELETE CASCADE,
    data_rule_ids VARCHAR(500),
    operate_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operate_ip VARCHAR(64),
    CONSTRAINT uk_kkumz_sys_role_perm UNIQUE (role_id, permission_id)
);

CREATE TABLE sys_dict (
    id VARCHAR(64) PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    del_flag INTEGER NOT NULL DEFAULT 0,
    type INTEGER NOT NULL DEFAULT 0,
    create_by VARCHAR(64) DEFAULT 'admin',
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT 'admin',
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_dict_item (
    id VARCHAR(64) PRIMARY KEY,
    dict_id VARCHAR(64) NOT NULL REFERENCES sys_dict(id) ON DELETE CASCADE,
    item_text VARCHAR(100) NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 1,
    create_by VARCHAR(64) DEFAULT 'admin',
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT 'admin',
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_log (
    id VARCHAR(64) PRIMARY KEY,
    log_type INTEGER NOT NULL DEFAULT 1,
    log_content VARCHAR(1000) NOT NULL,
    operate_type INTEGER,
    userid VARCHAR(64),
    username VARCHAR(100),
    ip VARCHAR(64),
    method VARCHAR(255),
    request_url VARCHAR(500),
    cost_time BIGINT DEFAULT 0,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kkumz_sys_perm_parent ON sys_permission(parent_id, sort_no);
CREATE INDEX idx_kkumz_sys_dict_item_dict ON sys_dict_item(dict_id, sort_order);

-- ==============================================================================
-- 전사 공통 화면 사용자 가이드 및 데이터 Lineage 마스터 (sys_page_help)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS sys_page_help (
    id                    VARCHAR(64) PRIMARY KEY,
    app_code              VARCHAR(32) NOT NULL,
    page_key              VARCHAR(128) NOT NULL UNIQUE,
    route_name            VARCHAR(64) DEFAULT NULL,
    title                 VARCHAR(100) NOT NULL,
    category              VARCHAR(50) NOT NULL,
    icon                  VARCHAR(50) DEFAULT NULL,
    summary               VARCHAR(500) NOT NULL,
    overview              TEXT NOT NULL,
    features_json         JSONB DEFAULT '[]'::jsonb,
    lineage_json          JSONB DEFAULT '{}'::jsonb,
    formulas_json         JSONB DEFAULT '[]'::jsonb,
    steps_json            JSONB DEFAULT '[]'::jsonb,
    related_screens_json  JSONB DEFAULT '[]'::jsonb,
    tips_json             JSONB DEFAULT '[]'::jsonb,
    warnings_json         JSONB DEFAULT '[]'::jsonb,
    faq_json              JSONB DEFAULT '[]'::jsonb,
    ai_prompt_hint        TEXT DEFAULT NULL,
    status                VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order            INTEGER DEFAULT 0,
    del_flag              INTEGER DEFAULT 0,
    create_by             VARCHAR(32) DEFAULT 'SYSTEM',
    create_time           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by             VARCHAR(32) DEFAULT NULL,
    update_time           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_page_help_app_key ON sys_page_help (app_code, page_key);
CREATE INDEX IF NOT EXISTS idx_sys_page_help_cat ON sys_page_help (category);

-- ==============================================================================
-- 전사 AI 프롬프트 템플릿 및 변경 이력 테이블 (sys_prompt_template, sys_prompt_history)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS sys_prompt_template (
    id                  VARCHAR(64) PRIMARY KEY,
    app_code            VARCHAR(32) NOT NULL DEFAULT 'COMMON',
    prompt_code         VARCHAR(100) NOT NULL UNIQUE,
    prompt_name         VARCHAR(150) NOT NULL,
    category            VARCHAR(50) NOT NULL DEFAULT 'AGENT',
    caller_class        VARCHAR(200),
    system_prompt       TEXT NOT NULL,
    user_prompt_template TEXT NOT NULL,
    variables_schema    JSONB NOT NULL DEFAULT '[]'::jsonb,
    output_schema       TEXT,
    model_override      VARCHAR(100),
    temperature         NUMERIC(3,2) DEFAULT 0.20,
    version             INT NOT NULL DEFAULT 1,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description         VARCHAR(500),
    del_flag            INT NOT NULL DEFAULT 0,
    create_by           VARCHAR(50) NOT NULL DEFAULT 'admin',
    create_time         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by           VARCHAR(50),
    update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_prompt_history (
    id                  VARCHAR(64) PRIMARY KEY,
    prompt_id           VARCHAR(64) NOT NULL,
    app_code            VARCHAR(32) NOT NULL DEFAULT 'COMMON',
    prompt_code         VARCHAR(100) NOT NULL,
    version             INT NOT NULL DEFAULT 1,
    system_prompt       TEXT NOT NULL,
    user_prompt_template TEXT NOT NULL,
    variables_schema    JSONB DEFAULT '[]'::jsonb,
    output_schema       TEXT,
    model_override      VARCHAR(100),
    temperature         NUMERIC(3,2) DEFAULT 0.20,
    change_reason       VARCHAR(300),
    created_by          VARCHAR(50) NOT NULL DEFAULT 'admin',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_prompt_template_app_cat ON sys_prompt_template(app_code, category, del_flag);
CREATE INDEX IF NOT EXISTS idx_sys_prompt_history_prompt_ver ON sys_prompt_history(prompt_id, version DESC);

