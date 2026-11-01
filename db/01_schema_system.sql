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
    status VARCHAR(8) NOT NULL DEFAULT '1',
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
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
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
