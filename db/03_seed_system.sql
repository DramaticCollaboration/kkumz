-- ============================================================================
-- Kkumz (꿈즈) 시스템 필수 기초 시드 데이터 (03_seed_system.sql)
-- (무중복 결정론적 순수 INSERT INTO 단일 원천 구성)
-- ============================================================================

INSERT INTO sys_user (id, username, realname, password, salt, avatar, email, phone, org_code, status, del_flag, create_by) VALUES
('usr-kkumz-001', 'admin', '김도현 총괄관리자', '$2a$10$m/W2rmwhIRDePfGUXsMs6.hj8bgi5RuH78rR/f3qLHZTk/eDY.FY6', 'kkumz2026', '/icons/favicon-128x128.png', 'admin@kkumz.com', '010-2026-1001', 'KKUMZ-HQ', 1, 0, 'admin'),
('usr-kkumz-002', 'moderator.kim', '김서연 모더레이터', '$2a$10$m/W2rmwhIRDePfGUXsMs6.hj8bgi5RuH78rR/f3qLHZTk/eDY.FY6', 'kkumz2026', '/icons/favicon-128x128.png', 'moderator@kkumz.com', '010-2026-1002', 'KKUMZ-TRUST', 1, 0, 'admin'),
('usr-kkumz-003', 'soyeon.jeju', '한소연 대표', '$2a$10$m/W2rmwhIRDePfGUXsMs6.hj8bgi5RuH78rR/f3qLHZTk/eDY.FY6', 'kkumz2026', '/icons/favicon-128x128.png', 'soyeon@matcha-atelier.kr', '010-3412-8891', 'CREATOR', 1, 0, 'admin'),
('usr-kkumz-004', 'minjun.maker', '박민준 메이커', '$2a$10$m/W2rmwhIRDePfGUXsMs6.hj8bgi5RuH78rR/f3qLHZTk/eDY.FY6', 'kkumz2026', '/icons/favicon-128x128.png', 'minjun@deskorbit.io', '010-5821-3340', 'CREATOR', 1, 0, 'admin'),
('usr-kkumz-005', 'jihoon.run', '정지훈 크루장', '$2a$10$m/W2rmwhIRDePfGUXsMs6.hj8bgi5RuH78rR/f3qLHZTk/eDY.FY6', 'kkumz2026', '/icons/favicon-128x128.png', 'jihoon@seoulrun.club', '010-7719-0922', 'CREATOR', 1, 0, 'admin'),
('usr-kkumz-006', 'hajun.roast', '최하준 헤드로스터', '$2a$10$m/W2rmwhIRDePfGUXsMs6.hj8bgi5RuH78rR/f3qLHZTk/eDY.FY6', 'kkumz2026', '/icons/favicon-128x128.png', 'hajun@beanchronicle.kr', '010-9102-4418', 'CREATOR', 1, 0, 'admin');

INSERT INTO sys_role (id, role_name, role_code, description, create_by) VALUES
('role_admin', '플랫폼 최고관리자', 'ROLE_ADMIN', '꿈즈 AI 스튜디오, 멀티사이트 배포, 커뮤니티 및 시스템 전체 권한', 'admin'),
('role_moderator', '커뮤니티 모더레이터', 'ROLE_MODERATOR', '커뮤니티 게시물/사이트 신고 심사 및 콘텐츠 안전 정책 집행 권한', 'admin'),
('role_creator', '창업가 메이커', 'ROLE_CREATOR', '아이디어 생성, AI 브랜딩, 사이트 배포 및 팀원 모집 권한', 'admin'),
('role_developer', '협업 개발자', 'ROLE_DEVELOPER', 'WebIDE 코드 편집, 빌드 디버깅 및 프로젝트 기술 협업 권한', 'admin');

INSERT INTO sys_user_role (id, user_id, role_id) VALUES
('ur-kkumz-001', 'usr-kkumz-001', 'role_admin'),
('ur-kkumz-002', 'usr-kkumz-002', 'role_moderator'),
('ur-kkumz-003', 'usr-kkumz-003', 'role_creator'),
('ur-kkumz-004', 'usr-kkumz-004', 'role_creator'),
('ur-kkumz-005', 'usr-kkumz-005', 'role_creator'),
('ur-kkumz-006', 'usr-kkumz-006', 'role_creator');

-- 시스템 기본 메뉴 (대시보드 및 시스템 설정)
INSERT INTO sys_permission (
    id, parent_id, name, url, component, is_route, component_name, redirect,
    menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description
, name_i18n, description_i18n) VALUES
('perm-kkumz-sys-001', NULL, '대시보드', '/dashboard/analysis', 'dashboard/analysis/index', TRUE, 'KkumzDashboardAnalysis', NULL, 1, 'dashboard:analysis:view', '1', 1.0, FALSE, 'ant-design:dashboard-outlined', TRUE, TRUE, FALSE, FALSE, '아이디어 생성, AI 응답성, 사이트 배포 및 커뮤니티 핵심 지표', '{"ko-KR":"대시보드","en-US":"Dashboard"}'::jsonb, '{"ko-KR":"아이디어 생성, AI 응답성, 사이트 배포 및 커뮤니티 핵심 지표","en-US":"Core KPIs for idea generation, AI responsiveness, site deployment, and community activity"}'::jsonb),
('perm-kkumz-sys-010', NULL, '환경 설정', '/system', 'layouts/default/index', TRUE, 'SystemLayout', '/system/user', 0, NULL, '1', 50.0, TRUE, 'ant-design:setting-outlined', FALSE, FALSE, FALSE, FALSE, '사용자, 역할, 메뉴 권한 및 공통 사전 관리', '{"ko-KR":"환경 설정","en-US":"Settings"}'::jsonb, '{"ko-KR":"사용자, 역할, 메뉴 권한 및 공통 사전 관리","en-US":"Manage users, roles, menu permissions, and common dictionaries"}'::jsonb),
('perm-kkumz-sys-011', 'perm-kkumz-sys-010', '사용자', '/system/user', 'system/user/index', TRUE, 'KkumzSystemUserView', NULL, 1, 'system:user:view', '1', 50.1, FALSE, 'ant-design:user-outlined', TRUE, TRUE, FALSE, FALSE, '창업가·모더레이터·관리자 계정 관리', '{"ko-KR":"사용자","en-US":"User"}'::jsonb, '{"ko-KR":"창업가·모더레이터·관리자 계정 관리","en-US":"Manage founder, moderator, and administrator accounts"}'::jsonb),
('perm-kkumz-sys-012', 'perm-kkumz-sys-010', '권한', '/system/role', 'system/role/index', TRUE, 'KkumzSystemRoleView', NULL, 1, 'system:role:view', '1', 50.2, FALSE, 'ant-design:team-outlined', TRUE, TRUE, FALSE, FALSE, 'RBAC 역할 및 권한 매핑 관리', '{"ko-KR":"권한","en-US":"Permission"}'::jsonb, '{"ko-KR":"RBAC 역할 및 권한 매핑 관리","en-US":"Manage RBAC roles and permission mappings"}'::jsonb),
('perm-kkumz-sys-013', 'perm-kkumz-sys-010', '메뉴', '/system/menu', 'system/menu/index', TRUE, 'KkumzSystemMenuView', NULL, 1, 'system:menu:view', '1', 50.3, FALSE, 'ant-design:menu-outlined', TRUE, TRUE, FALSE, FALSE, 'DB 단일 원천 동적 메뉴 트리 관리', '{"ko-KR":"메뉴","en-US":"Menu"}'::jsonb, '{"ko-KR":"DB 단일 원천 동적 메뉴 트리 관리","en-US":"Manage dynamic menu tree from a single DB source of truth"}'::jsonb),
('perm-kkumz-sys-014', 'perm-kkumz-sys-010', '공통 코드', '/system/dict', 'system/dict/index', TRUE, 'KkumzSystemDictView', NULL, 1, 'system:dict:view', '1', 50.4, FALSE, 'ant-design:book-outlined', TRUE, TRUE, FALSE, FALSE, '사이트 유형, 직군, 신고 사유 등 공통 코드 관리', '{"ko-KR":"공통 코드","en-US":"Common Codes"}'::jsonb, '{"ko-KR":"사이트 유형, 직군, 신고 사유 등 공통 코드 관리","en-US":"Manage common codes including site types, job roles, and report reasons"}'::jsonb),
('perm-kkumz-sys-015', 'perm-kkumz-sys-010', '프롬프트', '/system/prompt', 'system/prompt/index', TRUE, 'SysPromptView', NULL, 1, 'system:prompt:view', '1', 50.5, FALSE, 'ant-design:code-outlined', TRUE, TRUE, FALSE, FALSE, '전사 AI 시스템 및 유저 프롬프트 템플릿 관리', '{"ko-KR":"프롬프트","en-US":"Prompt"}'::jsonb, '{"ko-KR":"전사 AI 시스템 및 유저 프롬프트 템플릿 관리","en-US":"Manage enterprise AI system and user prompt templates"}'::jsonb),
('perm-kkumz-sys-016', 'perm-kkumz-sys-010', '부서', '/system/depart', 'system/depart/index', TRUE, 'KkumzSystemDepartView', NULL, 1, 'system:depart:view', '1', 50.6, FALSE, 'ant-design:apartment-outlined', TRUE, TRUE, FALSE, FALSE, '조직 및 크리에이터 운영 부서 트리 관리', '{"ko-KR":"부서","en-US":"Department"}'::jsonb, '{"ko-KR":"조직 및 크리에이터 운영 부서 트리 관리","en-US":"Manage organization and creator operations department tree"}'::jsonb),
('perm-kkumz-sys-017', 'perm-kkumz-sys-010', '알림 발송', '/system/message', 'system/message/index', TRUE, 'KkumzSystemMessageView', NULL, 1, 'system:message:view', '1', 50.7, FALSE, 'ant-design:bell-outlined', TRUE, TRUE, FALSE, FALSE, '시스템 알림 메시지 및 발송 템플릿 관리', '{"ko-KR":"알림 발송","en-US":"Send Notification"}'::jsonb, '{"ko-KR":"시스템 알림 메시지 및 발송 템플릿 관리","en-US":"Manage system notification messages and delivery templates"}'::jsonb),
('perm-kkumz-sys-018', 'perm-kkumz-sys-010', '작업 로그', '/monitor/log', 'monitor/log/index', TRUE, 'KkumzMonitorLogView', NULL, 1, 'monitor:log:view', '1', 50.8, FALSE, 'ant-design:file-search-outlined', TRUE, TRUE, FALSE, FALSE, '관리자 및 시스템 작업 감사 로그 조회', '{"ko-KR":"작업 로그","en-US":"Operation Logs"}'::jsonb, '{"ko-KR":"관리자 및 시스템 작업 감사 로그 조회","en-US":"Query administrator and system operation audit logs"}'::jsonb),
('perm-kkumz-sys-019', 'perm-kkumz-sys-010', '도움말 설정', '/system/help-guide', 'system/help/index', TRUE, 'KkumzHelpGuideMaster', NULL, 1, 'system:help:view', '1', 50.9, FALSE, 'ant-design:question-circle-outlined', TRUE, TRUE, FALSE, FALSE, '화면별 인앱 운영 가이드 및 매뉴얼 관리', '{"ko-KR":"도움말 설정","en-US":"Help Settings"}'::jsonb, '{"ko-KR":"화면별 인앱 운영 가이드 및 매뉴얼 관리","en-US":"Manage per-screen in-app operations guides and manuals"}'::jsonb),
('perm-kkumz-sys-020', 'perm-kkumz-sys-010', '사용 설명서', '/help', 'system/help/HelpViewer', TRUE, 'HelpViewer', NULL, 1, 'system:help:viewer', '1', 51.0, FALSE, 'ant-design:read-outlined', TRUE, TRUE, FALSE, FALSE, '시스템 개요 및 전체화면 운영 매뉴얼 탐색기', '{"ko-KR":"사용 설명서","en-US":"User Manual"}'::jsonb, '{"ko-KR":"시스템 개요 및 전체화면 운영 매뉴얼 탐색기","en-US":"System overview and fullscreen operations manual explorer"}'::jsonb),
('perm-kkumz-sys-config', 'perm-kkumz-sys-010', '시스템 환경설정', '/system/config', 'system/config/index', TRUE, 'KkumzSystemConfigView', NULL, 1, 'system:config:view', '1', 50.55, FALSE, 'ant-design:control-outlined', TRUE, TRUE, FALSE, FALSE, 'AI 엔진, 외부 서비스 연동, 보안, 스토리지 및 비즈니스 정책 동적 설정', '{"ko-KR":"시스템 환경설정","en-US":"System Configuration"}'::jsonb, '{"ko-KR":"AI 엔진, 외부 서비스 연동, 보안, 스토리지 및 비즈니스 정책 동적 설정","en-US":"Dynamic configuration for AI engines, external integrations, security, storage, and business policies"}'::jsonb);

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
('srp_kkumz_sys_001', 'role_admin', 'perm-kkumz-sys-001'),
('srp_kkumz_sys_010', 'role_admin', 'perm-kkumz-sys-010'),
('srp_kkumz_sys_011', 'role_admin', 'perm-kkumz-sys-011'),
('srp_kkumz_sys_012', 'role_admin', 'perm-kkumz-sys-012'),
('srp_kkumz_sys_013', 'role_admin', 'perm-kkumz-sys-013'),
('srp_kkumz_sys_014', 'role_admin', 'perm-kkumz-sys-014'),
('srp_kkumz_sys_015', 'role_admin', 'perm-kkumz-sys-015'),
('srp_kkumz_sys_016', 'role_admin', 'perm-kkumz-sys-016'),
('srp_kkumz_sys_017', 'role_admin', 'perm-kkumz-sys-017'),
('srp_kkumz_sys_018', 'role_admin', 'perm-kkumz-sys-018'),
('srp_kkumz_sys_019', 'role_admin', 'perm-kkumz-sys-019'),
('srp_kkumz_sys_020', 'role_admin', 'perm-kkumz-sys-020'),
('srp_kkumz_sys_config', 'role_admin', 'perm-kkumz-sys-config'),
('srp_kkumz_mod_001', 'role_moderator', 'perm-kkumz-sys-001'),
('srp_kkumz_mod_002', 'role_moderator', 'perm-kkumz-sys-020');

-- 공통 코드 사전
INSERT INTO sys_dict (id, dict_name, dict_code, description, create_by, dict_name_i18n) VALUES
('dict-kkumz-001', '사이트 구축 유형', 'kkumz_site_type', '꿈즈 멀티사이트 구축 아키텍처 유형', 'admin', '{"ko-KR":"사이트 구축 유형","en-US":"Site Build Type"}'::jsonb),
('dict-kkumz-002', '배포 엔진 모드', 'kkumz_deploy_mode', '실시간 디자인 스키마 배포 vs WebIDE 커스텀 번들 배포', 'admin', '{"ko-KR":"배포 엔진 모드","en-US":"Deployment Engine Mode"}'::jsonb),
('dict-kkumz-003', '팀빌딩 모집 직군', 'kkumz_recruit_role', '창업 프로젝트 합류 모집 포지션', 'admin', '{"ko-KR":"팀빌딩 모집 직군","en-US":"Team Recruitment Role"}'::jsonb),
('dict-kkumz-004', '커뮤니티 신고 사유', 'kkumz_report_reason', '콘텐츠 모더레이션 신고 분류', 'admin', '{"ko-KR":"커뮤니티 신고 사유","en-US":"Community Report Reason"}'::jsonb),
('dict-kkumz-005', '화면 레이아웃 유형', 'kkumz_layout_type', 'AI 기획서 및 스튜디오 화면 레이아웃 코드', 'admin', '{"ko-KR":"화면 레이아웃 유형","en-US":"Screen Layout Type"}'::jsonb),
('dict-kkumz-006', '화면 컴포넌트 유형', 'kkumz_component_type', 'AI 기획서 및 스튜디오 UI 컴포넌트 코드', 'admin', '{"ko-KR":"화면 컴포넌트 유형","en-US":"UI Component Type"}'::jsonb),
('dict-kkumz-007', '스튜디오 스타터 프리셋', 'kkumz_starter_preset', '아이디어 스튜디오 및 쇼케이스 기본 템플릿 프리셋', 'admin', '{"ko-KR":"스튜디오 스타터 프리셋","en-US":"Studio Starter Preset"}'::jsonb),
('dict-kkumz-008', '업로드 자료 용도 분류', 'kkumz_asset_type', '스튜디오 사진·로고·문서 업로드 자산 용도 코드', 'admin', '{"ko-KR":"업로드 자료 용도 분류","en-US":"Upload Asset Purpose"}'::jsonb),
('dict-kkumz-009', '기능 카탈로그 분류', 'kkumz_feature_category', 'SyncShop·SyncCMS·AI 52종 전체 기능 카탈로그의 8대 도메인 분류', 'admin', '{"ko-KR":"기능 카탈로그 분류","en-US":"Feature Catalog Category"}'::jsonb),
('dict-kkumz-010', '히어로 배너 레이아웃 무드', 'kkumz_hero_style', '라이브 사이트 최상단 히어로 섹션 레이아웃 아키타입', 'admin', '{"ko-KR":"히어로 배너 레이아웃 무드","en-US":"Hero Banner Mood"}'::jsonb);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, item_text_i18n) VALUES
('ditem-kkumz-001', 'dict-kkumz-001', '쇼핑몰 스토어', 'COMMERCE', '상품 카탈로그·장바구니·주문서 결제가 포함된 브랜드 스토어', 1, 1, '{"ko-KR":"쇼핑몰 스토어","en-US":"Online Store"}'::jsonb),
('ditem-kkumz-002', 'dict-kkumz-001', '브랜드·커뮤니티 웹', 'CMS_SITE', '매거진 아티클·공지·온라인 참가 신청 폼 중심 웹사이트', 2, 1, '{"ko-KR":"브랜드·커뮤니티 웹","en-US":"Brand & Community Web"}'::jsonb),
('ditem-kkumz-003', 'dict-kkumz-001', '매거진+스토어 복합 사이트', 'HYBRID', '브랜드 저널 콘텐츠와 커머스 스토어가 결합된 복합 사이트', 3, 1, '{"ko-KR":"매거진+스토어 복합 사이트","en-US":"Magazine + Store Hybrid Site"}'::jsonb),
('ditem-kkumz-004', 'dict-kkumz-002', '즉시 디자인 반영 배포', 'DYNAMIC_SDUI', '테마 토큰 및 레이아웃 구성 즉시 반영 배포', 1, 1, '{"ko-KR":"즉시 디자인 반영 배포","en-US":"Instant Design Deploy"}'::jsonb),
('ditem-kkumz-005', 'dict-kkumz-002', '코드 커스텀 번들 배포', 'CUSTOM_BUNDLE', 'WebIDE에서 편집한 Vue/Quasar 소스 코드 번들 빌드 배포', 2, 1, '{"ko-KR":"코드 커스텀 번들 배포","en-US":"Custom Code Bundle Deploy"}'::jsonb),
('ditem-kkumz-006', 'dict-kkumz-003', '프론트엔드 개발', 'FRONTEND', '웹/모바일 UI 및 인터랙션 개발', 1, 1, '{"ko-KR":"프론트엔드 개발","en-US":"Frontend Development"}'::jsonb),
('ditem-kkumz-007', 'dict-kkumz-003', '브랜드 디자이너', 'DESIGNER', 'BX/UI 디자인 및 패키지 그래픽', 2, 1, '{"ko-KR":"브랜드 디자이너","en-US":"Brand Designer"}'::jsonb),
('ditem-kkumz-008', 'dict-kkumz-003', '그로스 마케터', 'MARKETER', '콘텐츠 마케팅 및 퍼포먼스 캠페인', 3, 1, '{"ko-KR":"그로스 마케터","en-US":"Growth Marketer"}'::jsonb),
('ditem-kkumz-009', 'dict-kkumz-003', '커머스 MD', 'MD', '상품 기획 및 소싱·프로모션 운영', 4, 1, '{"ko-KR":"커머스 MD","en-US":"Commerce MD"}'::jsonb),
('ditem-kkumz-010', 'dict-kkumz-003', '서비스 기획', 'PLANNER', '프로덕트 기획 및 커뮤니티 운영', 5, 1, '{"ko-KR":"서비스 기획","en-US":"Service Planning"}'::jsonb),
('ditem-kkumz-011', 'dict-kkumz-003', '공동창업자', 'CO_FOUNDER', '비즈니스 리드 및 핵심 코파운더', 6, 1, '{"ko-KR":"공동창업자","en-US":"Co-Founder"}'::jsonb),
('ditem-kkumz-012', 'dict-kkumz-004', '스팸 및 불법 홍보', 'SPAM_PROMOTION', '무관한 광고 링크 및 도배 게시물', 1, 1, '{"ko-KR":"스팸 및 불법 홍보","en-US":"Spam & Illegal Promotion"}'::jsonb),
('ditem-kkumz-013', 'dict-kkumz-004', '저작권·상표권 침해', 'COPYRIGHT_TRADEMARK', '타 브랜드 로고·콘텐츠 무단 도용', 2, 1, '{"ko-KR":"저작권·상표권 침해","en-US":"Copyright / Trademark Infringement"}'::jsonb),
('ditem-kkumz-014', 'dict-kkumz-004', '허위 채용·모집 정보', 'INFO_VERIFICATION', '사실과 다른 팀원 모집 조건 기재', 3, 1, '{"ko-KR":"허위 채용·모집 정보","en-US":"Fraudulent Recruitment Info"}'::jsonb),
('ditem-kkumz-021', 'dict-kkumz-005', '매거진+스토어 복합 쇼케이스', 'HYBRID_EDITORIAL_COMMERCE', '에디토리얼 저널과 커머스 결합 레이아웃', 1, 1, '{"ko-KR":"매거진+스토어 복합 쇼케이스","en-US":"Magazine + Store Showcase"}'::jsonb),
('ditem-kkumz-022', 'dict-kkumz-005', '듀얼 스트림 그리드', 'DUAL_STREAM_GRID', '콘텐츠와 상품을 2열 스트림으로 배치', 2, 1, '{"ko-KR":"듀얼 스트림 그리드","en-US":"Dual Stream Grid"}'::jsonb),
('ditem-kkumz-023', 'dict-kkumz-005', '동적 신청·예약 폼', 'DYNAMIC_CMS_FORM', '온라인 참가 신청 및 예약 폼 레이아웃', 3, 1, '{"ko-KR":"동적 신청·예약 폼","en-US":"Dynamic Booking & Inquiry Form"}'::jsonb),
('ditem-kkumz-024', 'dict-kkumz-005', '보태니컬 에디토리얼', 'EDITORIAL_BOTANICAL', '브랜드 스토리텔링 중심 히어로 레이아웃', 4, 1, '{"ko-KR":"보태니컬 에디토리얼","en-US":"Botanical Editorial"}'::jsonb),
('ditem-kkumz-025', 'dict-kkumz-005', '3열 필터형 상품 그리드', 'FILTERABLE_3COL_GRID', '카테고리 탭 및 3열 상품 진열 그리드', 5, 1, '{"ko-KR":"3열 필터형 상품 그리드","en-US":"3-Column Filterable Product Grid"}'::jsonb),
('ditem-kkumz-026', 'dict-kkumz-005', '원페이지 간편 결제', 'ONE_PAGE_CHECKOUT', '단일 화면 장바구니 및 배송지 결제 폼', 6, 1, '{"ko-KR":"원페이지 간편 결제","en-US":"One-Page Express Checkout"}'::jsonb),
('ditem-kkumz-027', 'dict-kkumz-005', '다크 테크 쇼케이스 그리드', 'DARK_TECH_GRID', '하드웨어·데스크 기어 사양 중심 그리드', 7, 1, '{"ko-KR":"다크 테크 쇼케이스 그리드","en-US":"Dark Tech Showcase Grid"}'::jsonb),
('ditem-kkumz-028', 'dict-kkumz-005', '스펙 비교형 카탈로그', 'SPEC_COMPARISON_GRID', '제품 호환성 및 상세 스펙 비교 그리드', 8, 1, '{"ko-KR":"스펙 비교형 카탈로그","en-US":"Spec Comparison Catalog"}'::jsonb),
('ditem-kkumz-029', 'dict-kkumz-005', '매거진 풀배너 히어로', 'MAGAZINE_HERO', '커뮤니티 및 미디어 메인 풀배너', 9, 1, '{"ko-KR":"매거진 풀배너 히어로","en-US":"Magazine Full-Banner Hero"}'::jsonb),
('ditem-kkumz-030', 'dict-kkumz-005', '에디토리얼 카드 리스트', 'EDITORIAL_CARDS', '매거진 아티클 카드 피드 레이아웃', 10, 1, '{"ko-KR":"에디토리얼 카드 리스트","en-US":"Editorial Card List"}'::jsonb),
('ditem-kkumz-031', 'dict-kkumz-005', '에디토리얼 매거진 레이아웃', 'EDITORIAL_MAGAZINE_LAYOUT', '저널 열람 및 이벤트 신청 복합 레이아웃', 11, 1, '{"ko-KR":"에디토리얼 매거진 레이아웃","en-US":"Editorial Magazine Layout"}'::jsonb),
('ditem-kkumz-032', 'dict-kkumz-005', '스탠다드 히어로 배너', 'STANDARD_HERO', '기본 브랜드 소개 히어로 섹션', 12, 1, '{"ko-KR":"스탠다드 히어로 배너","en-US":"Standard Hero Banner"}'::jsonb),
('ditem-kkumz-033', 'dict-kkumz-005', '표준 반응형 그리드', 'STANDARD_GRID', '표준 반응형 콘텐츠 그리드', 13, 1, '{"ko-KR":"표준 반응형 그리드","en-US":"Standard Responsive Grid"}'::jsonb),
('ditem-kkumz-034', 'dict-kkumz-005', '온라인 접수 폼 뷰', 'FORM_VIEW', '표준 입력 폼 화면', 14, 1, '{"ko-KR":"온라인 접수 폼 뷰","en-US":"Online Submission Form View"}'::jsonb),
('ditem-kkumz-035', 'dict-kkumz-005', '메인 히어로 배너 & 시그니처 그리드', 'HERO_BANNER_GRID', '대표 비주얼 배너와 핵심 시그니처 소개 결합 레이아웃', 15, 1, '{"ko-KR":"메인 히어로 배너 & 시그니처 그리드","en-US":"Main Hero Banner & Signature Grid"}'::jsonb),
('ditem-kkumz-036', 'dict-kkumz-005', '상품 · 객실 · 메뉴 카탈로그 그리드', 'PRODUCT_CATALOG_GRID', '객실·상품·서비스 카탈로그 카드 진열 그리드', 16, 1, '{"ko-KR":"상품 · 객실 · 메뉴 카탈로그 그리드","en-US":"Product, Room & Menu Catalog Grid"}'::jsonb),
('ditem-kkumz-037', 'dict-kkumz-005', '현장 포토 갤러리 & 공간 아카이브', 'GALLERY_SHOWCASE', '공간 전경 및 현장 포토 갤러리 쇼케이스 레이아웃', 17, 1, '{"ko-KR":"현장 포토 갤러리 & 공간 아카이브","en-US":"On-Site Photo Gallery & Space Archive"}'::jsonb),
('ditem-kkumz-038', 'dict-kkumz-005', '브랜드 매거진 & 스토리 저널', 'MAGAZINE_LIST', '브랜드 이야기 및 소식 아티클 목록 레이아웃', 18, 1, '{"ko-KR":"브랜드 매거진 & 스토리 저널","en-US":"Brand Magazine & Story Journal"}'::jsonb),
('ditem-kkumz-039', 'dict-kkumz-005', '온라인 예약 · 문의 · 신청 폼', 'RESERVATION_FORM', '실시간 예약 및 고객 문의 접수 폼 레이아웃', 19, 1, '{"ko-KR":"온라인 예약 · 문의 · 신청 폼","en-US":"Online Booking, Inquiry & Application Form"}'::jsonb),
('ditem-kkumz-040', 'dict-kkumz-005', 'AI 브랜드 컨시어지 & 오시는 길 안내', 'FAQ_CONCIERGE', 'AI 컨시어지 상담 및 이용·위치 안내 레이아웃', 20, 1, '{"ko-KR":"AI 브랜드 컨시어지 & 오시는 길 안내","en-US":"AI Brand Concierge & Directions"}'::jsonb),
('ditem-kkumz-041', 'dict-kkumz-006', '메인 스토리 히어로', 'FeaturedStoryHero', '대표 스토리텔링 배너 컴포넌트', 1, 1, '{"ko-KR":"메인 스토리 히어로","en-US":"Main Story Hero"}'::jsonb),
('ditem-kkumz-042', 'dict-kkumz-006', '로스팅 프로파일 배지', 'RoastProfileBadge', '원두 로스팅 등급 및 노트 표시 배지', 2, 1, '{"ko-KR":"로스팅 프로파일 배지","en-US":"Roasting Profile Badge"}'::jsonb),
('ditem-kkumz-043', 'dict-kkumz-006', '스토어 상품 카탈로그', 'CommerceCatalogSection', '연동된 쇼핑몰 상품 목록 그리드', 3, 1, '{"ko-KR":"스토어 상품 카탈로그","en-US":"Store Product Catalog"}'::jsonb),
('ditem-kkumz-044', 'dict-kkumz-006', '브랜드 저널 아티클', 'EditorialJournalSection', '연동된 CMS 매거진 아티클 목록', 4, 1, '{"ko-KR":"브랜드 저널 아티클","en-US":"Brand Journal Articles"}'::jsonb),
('ditem-kkumz-045', 'dict-kkumz-006', '클래스 예약 폼', 'ClassBookingForm', '오프라인 클래스 및 세션 예약 입력 폼', 5, 1, '{"ko-KR":"클래스 예약 폼","en-US":"Class Booking Form"}'::jsonb),
('ditem-kkumz-046', 'dict-kkumz-006', 'AI 브루잉 컨시어지', 'AiBrewingConcierge', 'AI 기반 상품·추출 가이드 상담 위젯', 6, 1, '{"ko-KR":"AI 브루잉 컨시어지","en-US":"AI Brewing Concierge"}'::jsonb),
('ditem-kkumz-047', 'dict-kkumz-006', '브랜드 스토리 배너', 'BrandStoryBanner', '브랜드 철학 및 산지 소개 배너', 7, 1, '{"ko-KR":"브랜드 스토리 배너","en-US":"Brand Story Banner"}'::jsonb),
('ditem-kkumz-048', 'dict-kkumz-006', '다도 리추얼 가이드', 'MatchaRitualSteps', '단계별 음용 방법 안내 섹션', 8, 1, '{"ko-KR":"다도 리추얼 가이드","en-US":"Tea Ceremony Ritual Guide"}'::jsonb),
('ditem-kkumz-049', 'dict-kkumz-006', '추천 상품 진열장', 'FeaturedProductGrid', '핵심 주력 상품 큐레이션 그리드', 9, 1, '{"ko-KR":"추천 상품 진열장","en-US":"Featured Products Showcase"}'::jsonb),
('ditem-kkumz-050', 'dict-kkumz-006', '카테고리 필터 탭', 'CategoryTabs', '상품 분류별 필터링 탭 바', 10, 1, '{"ko-KR":"카테고리 필터 탭","en-US":"Category Filter Tabs"}'::jsonb),
('ditem-kkumz-051', 'dict-kkumz-006', '테이스팅 노트 배지', 'TasteNoteBadge', '풍미 및 주요 특징 요약 태그', 11, 1, '{"ko-KR":"테이스팅 노트 배지","en-US":"Tasting Notes Badge"}'::jsonb),
('ditem-kkumz-052', 'dict-kkumz-006', '빠른 장바구니 서랍', 'QuickCartDrawer', '화면 전환 없는 슬라이드 장바구니', 12, 1, '{"ko-KR":"빠른 장바구니 서랍","en-US":"Quick Cart Drawer"}'::jsonb),
('ditem-kkumz-053', 'dict-kkumz-006', '선물 포장 선택기', 'GiftWrapSelector', '기프트 패키징 옵션 선택 컴포넌트', 13, 1, '{"ko-KR":"선물 포장 선택기","en-US":"Gift Wrap Selector"}'::jsonb),
('ditem-kkumz-054', 'dict-kkumz-006', '주문서 입력 폼', 'OrderForm', '수령인 및 배송지 입력 결제 폼', 14, 1, '{"ko-KR":"주문서 입력 폼","en-US":"Order Entry Form"}'::jsonb),
('ditem-kkumz-055', 'dict-kkumz-006', 'AI 티 소믈리에 위젯', 'AiTeaSommelierWidget', '취향 맞춤형 티 추천 대화창', 15, 1, '{"ko-KR":"AI 티 소믈리에 위젯","en-US":"AI Tea Sommelier Widget"}'::jsonb),
('ditem-kkumz-056', 'dict-kkumz-006', '데스크 셋업 쇼케이스', 'OrbitSetupShowcase', '워크스테이션 배치 예시 프리뷰', 16, 1, '{"ko-KR":"데스크 셋업 쇼케이스","en-US":"Desk Setup Showcase"}'::jsonb),
('ditem-kkumz-057', 'dict-kkumz-006', '핵심 스펙 요약 바', 'SpecHighlightStrip', '소재·규격·출력 등 핵심 사양 요약', 17, 1, '{"ko-KR":"핵심 스펙 요약 바","en-US":"Key Specs Summary Bar"}'::jsonb),
('ditem-kkumz-058', 'dict-kkumz-006', '베스트 기어 그리드', 'BestGearGrid', '인기 데스크 기어 상품 그리드', 18, 1, '{"ko-KR":"베스트 기어 그리드","en-US":"Best Gear Grid"}'::jsonb),
('ditem-kkumz-059', 'dict-kkumz-006', '기기 호환성 필터', 'CompatibilityFilter', '연동 기기 및 규격별 필터', 19, 1, '{"ko-KR":"기기 호환성 필터","en-US":"Device Compatibility Filter"}'::jsonb),
('ditem-kkumz-060', 'dict-kkumz-006', '상품 카드', 'ProductCard', '개별 상품 썸네일 및 가격 카드', 20, 1, '{"ko-KR":"상품 카드","en-US":"Product Cards"}'::jsonb),
('ditem-kkumz-061', 'dict-kkumz-006', '장바구니 사이드바', 'CartSidebar', '우측 고정형 장바구니 요약 패널', 21, 1, '{"ko-KR":"장바구니 사이드바","en-US":"Cart Sidebar"}'::jsonb),
('ditem-kkumz-062', 'dict-kkumz-006', '배송지 입력 폼', 'ShippingAddressForm', '우편번호 및 상세 주소 입력란', 22, 1, '{"ko-KR":"배송지 입력 폼","en-US":"Shipping Address Form"}'::jsonb),
('ditem-kkumz-063', 'dict-kkumz-006', '쿠폰 적용기', 'CouponApplier', '프로모션 코드 및 할인 쿠폰 적용', 23, 1, '{"ko-KR":"쿠폰 적용기","en-US":"Coupon Applier"}'::jsonb),
('ditem-kkumz-064', 'dict-kkumz-006', '즉시 결제 버튼', 'InstantOrderButton', '원클릭 주문 확정 액션 버튼', 24, 1, '{"ko-KR":"즉시 결제 버튼","en-US":"Instant Checkout Button"}'::jsonb),
('ditem-kkumz-065', 'dict-kkumz-006', '주간 세션 공지 배너', 'WeeklySessionBanner', '정규 오프라인 모임 일정 배너', 25, 1, '{"ko-KR":"주간 세션 공지 배너","en-US":"Weekly Session Notice Banner"}'::jsonb),
('ditem-kkumz-066', 'dict-kkumz-006', '크루 활동 지표 바', 'CrewMetricsBar', '누적 참가자 및 활동 거리 지표', 26, 1, '{"ko-KR":"크루 활동 지표 바","en-US":"Crew Activity Metrics Bar"}'::jsonb),
('ditem-kkumz-067', 'dict-kkumz-006', '코스 가이드 목록', 'CourseArticleList', '주요 활동 코스 및 난이도 아티클', 27, 1, '{"ko-KR":"코스 가이드 목록","en-US":"Course Guide List"}'::jsonb),
('ditem-kkumz-068', 'dict-kkumz-006', '페이스 계산기 카드', 'PaceCalculatorCard', '목표 기록 대비 구간 페이스 계산', 28, 1, '{"ko-KR":"페이스 계산기 카드","en-US":"Pace Calculator Card"}'::jsonb),
('ditem-kkumz-069', 'dict-kkumz-006', '세션 일정 선택기', 'SessionSelectField', '참가 희망 일자 선택 드롭다운', 29, 1, '{"ko-KR":"세션 일정 선택기","en-US":"Session Schedule Picker"}'::jsonb),
('ditem-kkumz-070', 'dict-kkumz-006', '페이스 그룹 선택', 'PaceRadioGroup', '수준별 그룹 선택 라디오 버튼', 30, 1, '{"ko-KR":"페이스 그룹 선택","en-US":"Pace Group Selector"}'::jsonb),
('ditem-kkumz-071', 'dict-kkumz-006', '참가 신청 제출 버튼', 'SubmitApplicationButton', '온라인 참가 신청서 최종 제출', 31, 1, '{"ko-KR":"참가 신청 제출 버튼","en-US":"Submit Registration Button"}'::jsonb),
('ditem-kkumz-072', 'dict-kkumz-006', '현장 포토 갤러리', 'PhotoGallerySection', '업로드된 현장·매장·상품 사진 갤러리 그리드', 32, 1, '{"ko-KR":"현장 포토 갤러리","en-US":"On-Site Photo Gallery"}'::jsonb),
('ditem-kkumz-073', 'dict-kkumz-006', '안내자료 다운로드 바', 'ReferenceDocDownloadBar', '업로드된 요금표·브로셔·안내문 다운로드 섹션', 33, 1, '{"ko-KR":"안내자료 다운로드 바","en-US":"Brochure Download Bar"}'::jsonb),
('ditem-kkumz-074', 'dict-kkumz-006', '히어로 배너', 'HeroBanner', '브랜드 대표 비주얼 및 슬로건 히어로 배너', 34, 1, '{"ko-KR":"히어로 배너","en-US":"Hero Banner"}'::jsonb),
('ditem-kkumz-075', 'dict-kkumz-006', '상품·객실 진열 그리드', 'ProductGrid', '객실·상품·서비스 대표 라인업 진열 그리드', 35, 1, '{"ko-KR":"상품·객실 진열 그리드","en-US":"Product & Room Display Grid"}'::jsonb),
('ditem-kkumz-076', 'dict-kkumz-006', '포토 갤러리', 'PhotoGallery', '공간 내외부 및 현장 사진 아카이브 갤러리', 36, 1, '{"ko-KR":"포토 갤러리","en-US":"Photo Gallery"}'::jsonb),
('ditem-kkumz-077', 'dict-kkumz-006', '매거진 저널', 'MagazineSection', '브랜드 스토리 및 계절 소식 매거진 섹션', 37, 1, '{"ko-KR":"매거진 저널","en-US":"Magazine Journal"}'::jsonb),
('ditem-kkumz-078', 'dict-kkumz-006', '온라인 예약·신청폼', 'BookingForm', '일정·인원·연락처 기반 온라인 예약 및 문의 신청 폼', 38, 1, '{"ko-KR":"온라인 예약·신청폼","en-US":"Online Booking & Inquiry Forms"}'::jsonb),
('ditem-kkumz-079', 'dict-kkumz-006', '안내문·요금표 자료실', 'BrochureDownload', '이용 안내문 및 요금표 문서 다운로드 모듈', 39, 1, '{"ko-KR":"안내문·요금표 자료실","en-US":"Guides & Rate Sheet Library"}'::jsonb),
('ditem-kkumz-080', 'dict-kkumz-006', 'AI 컨시어지 챗봇', 'AiConciergeWidget', '24시간 실시간 고객 문의 응대 AI 컨시어지 위젯', 40, 1, '{"ko-KR":"AI 컨시어지 챗봇","en-US":"AI Concierge Chatbot"}'::jsonb),
('ditem-kkumz-085', 'dict-kkumz-006', '웰컴 혜택 쿠폰바', 'WelcomeCouponBar', '방문객 및 첫 예약 고객 전용 프로모션 혜택 배너', 41, 1, '{"ko-KR":"웰컴 혜택 쿠폰바","en-US":"Welcome Coupon Bar"}'::jsonb),
('ditem-kkumz-081', 'dict-kkumz-007', '스페셜티 커피 로스터리', 'COFFEE_ROASTERY', '{"badge":"매거진+스토어","siteType":"HYBRID","category":"LIFESTYLE_HYBRID","promptText":"콜롬비아·에티오피아 산지 농장 직교역 인터뷰 매거진과 로스팅 프로파일 아티클을 발행하면서, 매월 로스터가 선별한 싱글오리진 원두와 드립백을 바로 주문하고 시음 클래스도 예약할 수 있는 매거진+스토어 복합 사이트를 만들고 싶습니다.","tags":["스페셜티커피","로스터리매거진","싱글오리진","커핑클래스"]}', 1, 1, '{"ko-KR":"스페셜티 커피 로스터리","en-US":"Specialty Coffee Roastery"}'::jsonb),
('ditem-kkumz-082', 'dict-kkumz-007', '제주 유기농 말차 & 다기', 'JEJU_MATCHA', '{"badge":"쇼핑몰 스토어","siteType":"COMMERCE","category":"FOOD_BEVERAGE","promptText":"제주 서귀포 유기농 다원에서 재배한 첫물 세레모니얼 말차와 이천 도예가의 수제 백자 다완, 100본립 차선을 큐레이션하여 판매하는 프리미엄 다도 D2C 쇼핑몰을 만들고 싶어요.","tags":["제주유기농말차","홈다도","수제다완","보자기패키징"]}', 2, 1, '{"ko-KR":"제주 유기농 말차 & 다기","en-US":"Jeju Organic Matcha & Teaware"}'::jsonb),
('ditem-kkumz-083', 'dict-kkumz-007', '커스텀 키보드 & 데스크 기어', 'DESK_ORBIT', '{"badge":"쇼핑몰 스토어","siteType":"COMMERCE","category":"TECH_HARDWARE","promptText":"개발자와 디자이너의 책상 위 케이블을 정리해 주는 CNC 통절삭 알루미늄 Qi2 충전 스테이션, 북미산 월넛 마그네틱 데스크 쉘프, 비대칭 모니터 조명을 판매하는 테크 브랜드 스토어를 만들고 싶습니다.","tags":["데스크테리어","CNC알루미늄","매그세이프독","모듈형데스크"]}', 3, 1, '{"ko-KR":"커스텀 키보드 & 데스크 기어","en-US":"Custom Keyboards & Desk Gear"}'::jsonb),
('ditem-kkumz-084', 'dict-kkumz-007', '한강 나이트 러닝 크루', 'SEOUL_RUN_CLUB', '{"badge":"브랜드·커뮤니티 웹","siteType":"CMS_SITE","category":"COMMUNITY_MEDIA","promptText":"매주 화요일·목요일 저녁 반포·성수·여의도 한강공원에서 함께 달리는 2030 러닝 크루 공식 브랜드 사이트이자, 러닝 코스 아카이브와 정규 세션 참가 신청을 받는 커뮤니티 웹사이트를 만들고 싶어요.","tags":["러닝크루","한강나이트런","마라톤트레이닝","시티러너"]}', 4, 1, '{"ko-KR":"한강 나이트 러닝 크루","en-US":"Han River Night Running Crew"}'::jsonb),
('ditem-kkumz-086', 'dict-kkumz-007', '공주 냇가 독채 민박 & 평상 스테이', 'RURAL_STAY_MINBAK', '{"badge":"숙박·예약+콘텐츠 복합","siteType":"HYBRID","category":"숙박 · 민박 · 프라이빗 스테이","promptText":"충남 공주 시골 맑은 냇가 바로 옆에 위치한 정겨운 민박집 사이트를 만들고 싶어요. 황토 온돌 독채 객실과 냇가 평상 대여, 능이 토종닭 백숙 세트를 예약받고, 성수기/비수기 요금표와 사계절 현장 사진 갤러리, 체크인 날짜·인원·바비큐 옵션을 선택하는 예약 신청 폼, 공주 오시는 길과 주변 관광 코스를 함께 안내하고 싶습니다.","tags":["공주민박","냇가평상","독채스테이","능이백숙","가족촌캉스"]}', 5, 1, '{"ko-KR":"공주 냇가 독채 민박 & 평상 스테이","en-US":"Gongju Creek Private Stay & Deck"}'::jsonb),
('ditem-kkumz-091', 'dict-kkumz-008', 'AI 자동 분류', 'AUTO', '확장자와 파일명을 분석하여 용도를 자동 분류합니다.', 1, 1, '{"ko-KR":"AI 자동 분류","en-US":"AI Auto Classification"}'::jsonb),
('ditem-kkumz-092', 'dict-kkumz-008', '메인 대표 배너', 'HERO', '사이트 최상단 히어로 섹션의 대표 배경/비주얼 사진으로 배치됩니다.', 2, 1, '{"ko-KR":"메인 대표 배너","en-US":"Main Hero Banner"}'::jsonb),
('ditem-kkumz-093', 'dict-kkumz-008', '상품·객실 사진', 'PRODUCT', '쇼핑몰 스토어 및 객실/메뉴 카탈로그 카드 썸네일로 배치됩니다.', 3, 1, '{"ko-KR":"상품·객실 사진","en-US":"Product & Room Photos"}'::jsonb),
('ditem-kkumz-094', 'dict-kkumz-008', '매거진·스토리 사진', 'ARTICLE', '브랜드 저널 및 커뮤니티 아티클 커버 이미지로 배치됩니다.', 4, 1, '{"ko-KR":"매거진·스토리 사진","en-US":"Magazine & Story Photos"}'::jsonb),
('ditem-kkumz-095', 'dict-kkumz-008', '현장 포토 갤러리', 'GALLERY', '라이브 사이트의 브랜드 포토 갤러리 섹션에 노출됩니다.', 5, 1, '{"ko-KR":"현장 포토 갤러리","en-US":"On-Site Photo Gallery"}'::jsonb),
('ditem-kkumz-096', 'dict-kkumz-008', '보유 브랜드 로고', 'LOGO', '기존에 보유한 로고(SVG/PNG/JPG)를 사이트 헤더 로고로 등록합니다.', 6, 1, '{"ko-KR":"보유 브랜드 로고","en-US":"Brand Logo Assets"}'::jsonb),
('ditem-kkumz-097', 'dict-kkumz-008', '안내문·요금표 문서', 'DOC', '요금표·이용안내·브로셔 문서에서 텍스트를 추출해 AI 기획서와 컨시어지에 반영합니다.', 7, 1, '{"ko-KR":"안내문·요금표 문서","en-US":"Guides & Rate Sheet Documents"}'::jsonb),
('ditem-kkumz-901', 'dict-kkumz-009', '상품·객실·메뉴 진열', 'COMMERCE_CATALOG', 'SyncShop 기반 상품·객실·메뉴·클래스 카탈로그 및 다중 옵션 모듈', 1, 1, '{"ko-KR":"상품·객실·메뉴 진열","en-US":"Product, Room & Menu Showcase"}'::jsonb),
('ditem-kkumz-902', 'dict-kkumz-009', '요금표·예약결제·할인', 'ORDER_PRICING', '시즌별 상세 요금표, 스펙 비교표, 정기구독, 장바구니 및 즉시 예약 결제', 2, 1, '{"ko-KR":"요금표·예약결제·할인","en-US":"Rate Sheet, Booking & Discounts"}'::jsonb),
('ditem-kkumz-903', 'dict-kkumz-009', '쿠폰·타임특가·리뷰', 'PROMO_MARKETING', '웰컴 쿠폰팩, 타임특가 플래시딜, 프리오더 게이지 및 실구매 포토 리뷰', 3, 1, '{"ko-KR":"쿠폰·타임특가·리뷰","en-US":"Coupons, Flash Sales & Reviews"}'::jsonb),
('ditem-kkumz-904', 'dict-kkumz-009', '배너·포토·공간투어', 'VISUAL_MEDIA', '시네마틱 히어로 배너, 현장 포토 갤러리, 구역별 스페이스 투어 및 자료실', 4, 1, '{"ko-KR":"배너·포토·공간투어","en-US":"Banners, Photos & Space Tour"}'::jsonb),
('ditem-kkumz-905', 'dict-kkumz-009', '매거진·연혁·이용가이드', 'CONTENT_EDITORIAL', '브랜드 매거진 저널, 호스트 인터뷰, 브랜드 연혁 및 단계별 이용 가이드', 5, 1, '{"ko-KR":"매거진·연혁·이용가이드","en-US":"Magazine, History & Guides"}'::jsonb),
('ditem-kkumz-906', 'dict-kkumz-009', '맞춤예약폼·일정·설문', 'FORM_SCHEDULE', '업종 맞춤 동적 예약·신청 폼, 정규 세션 시간표, 단체 대관 및 오픈 알림', 6, 1, '{"ko-KR":"맞춤예약폼·일정·설문","en-US":"Booking Forms, Schedules & Surveys"}'::jsonb),
('ditem-kkumz-907', 'dict-kkumz-009', '방명록·투표·팀원모집', 'COMMUNITY_SOCIAL', '실시간 선호도 투표, 방문객 방명록/Q&A, 로컬 콜라보 및 팀원 모집', 7, 1, '{"ko-KR":"방명록·투표·팀원모집","en-US":"Guestbook, Polls & Team Recruitment"}'::jsonb),
('ditem-kkumz-908', 'dict-kkumz-009', 'AI컨시어지·FAQ·오시는길', 'TRUST_AI_GUIDE', '24시간 AI 컨시어지, AI 취향 진단 큐레이터, FAQ 아코디언, 오시는 길 및 환불 규정', 8, 1, '{"ko-KR":"AI컨시어지·FAQ·오시는길","en-US":"AI Concierge, FAQ & Directions"}'::jsonb),
('ditem-kkumz-951', 'dict-kkumz-010', '풀스크린 시네마틱 배너', 'FULL_SCENIC', '공간·풍경 사진이 전면 배경으로 펼쳐지는 몰입형 히어로 레이아웃', 1, 1, '{"ko-KR":"풀스크린 시네마틱 배너","en-US":"Fullscreen Cinematic Banner"}'::jsonb),
('ditem-kkumz-952', 'dict-kkumz-010', '좌우 스플릿 비주얼 배너', 'SPLIT_VISUAL', '좌측 브랜드 카피·CTA와 우측 대표 사진 프레임이 분할 배치되는 레이아웃', 2, 1, '{"ko-KR":"좌우 스플릿 비주얼 배너","en-US":"Split Visual Hero Banner"}'::jsonb),
('ditem-kkumz-953', 'dict-kkumz-010', '에디토리얼 매거진 커버', 'EDITORIAL_STORY', '매거진 표지 감성의 타이포그래피와 호스트 철학 인용구가 강조되는 레이아웃', 3, 1, '{"ko-KR":"에디토리얼 매거진 커버","en-US":"Editorial Magazine Cover"}'::jsonb),
('ditem-kkumz-954', 'dict-kkumz-010', '프로모션 커머스 쇼케이스', 'PROMO_COMMERCE', '핵심 혜택 배지·웰컴 쿠폰·대표 상품 하이라이트가 결합된 스토어형 레이아웃', 4, 1, '{"ko-KR":"프로모션 커머스 쇼케이스","en-US":"Promotional Commerce Showcase"}'::jsonb);

-- ============================================================================
-- 시스템 환경설정 카테고리 및 도메인 운영 공통 사전 확장 시드
-- ============================================================================
INSERT INTO sys_dict (id, dict_name, dict_code, description, create_by, dict_name_i18n) VALUES
('dict-kkumz-011', '환경설정 카테고리', 'sys_config_category', '시스템 동적 환경설정 탭 분류 코드', 'admin', '{"ko-KR":"환경설정 카테고리","en-US":"Config Category"}'::jsonb),
('dict-kkumz-012', '캠페인 운영 상태', 'kkumz_campaign_status', '브랜드 캠페인 및 프로모션 운영 상태', 'admin', '{"ko-KR":"캠페인 운영 상태","en-US":"Campaign Status"}'::jsonb),
('dict-kkumz-013', '콘텐츠 도메인 분류', 'kkumz_content_category', '아이디어 및 브랜드 사이트 콘텐츠 도메인 분류', 'admin', '{"ko-KR":"콘텐츠 도메인 분류","en-US":"Content Domain Category"}'::jsonb),
('dict-kkumz-014', '리워드 혜택 유형', 'kkumz_reward_type', '크리에이터 및 방문객 리워드 혜택 유형', 'admin', '{"ko-KR":"리워드 혜택 유형","en-US":"Reward Benefit Type"}'::jsonb),
('dict-kkumz-015', '신고 심사 처리 상태', 'kkumz_moderation_status', '커뮤니티 신고 접수 및 심사 처분 상태', 'admin', '{"ko-KR":"신고 심사 처리 상태","en-US":"Report Review Status"}'::jsonb),
('dict-kkumz-016', 'API 연동 방향', 'kkumz_api_direction', '서비스 간 API 인증키 발신/수신 방향', 'admin', '{"ko-KR":"API 연동 방향","en-US":"API Integration Direction"}'::jsonb),
('dict-kkumz-017', '연동 대상 서비스 코드', 'kkumz_api_service_code', '외부 및 내부 연동 대상 서비스 식별 코드', 'admin', '{"ko-KR":"연동 대상 서비스 코드","en-US":"Target Service Code"}'::jsonb),
('dict-kkumz-018', 'API 인증키 상태', 'kkumz_api_key_status', '서비스 연동 API 키 활성/폐기 상태', 'admin', '{"ko-KR":"API 인증키 상태","en-US":"API Key Status"}'::jsonb),
('dict-kkumz-019', '샌드박스 빌드 상태', 'kkumz_build_status', 'WebIDE 및 멀티사이트 빌드 실행 상태', 'admin', '{"ko-KR":"샌드박스 빌드 상태","en-US":"Sandbox Build Status"}'::jsonb),
('dict-kkumz-020', '사이트 운영 상태', 'kkumz_site_status', '멀티사이트 라이브 가동 및 정지 상태', 'admin', '{"ko-KR":"사이트 운영 상태","en-US":"Site Operational Status"}'::jsonb),
('dict-kkumz-021', 'AI 스튜디오 생성 단계', 'kkumz_stage_type', 'AI 스튜디오 파이프라인 단계 코드', 'admin', '{"ko-KR":"AI 스튜디오 생성 단계","en-US":"AI Studio Generation Stage"}'::jsonb),
('dict-kkumz-022', '공통 활성 상태', 'kkumz_active_status', '템플릿 및 정책 활성/비활성 상태', 'admin', '{"ko-KR":"공통 활성 상태","en-US":"Common Active Status"}'::jsonb),
('dict-kkumz-023', '멀티사이트 디자인 아키타입', 'kkumz_theme_archetype', '업종 및 브랜드 무드별 멀티사이트 비주얼·레이아웃 아키타입', 'admin', '{"ko-KR":"멀티사이트 디자인 아키타입","en-US":"Multi-Site Design Archetype"}'::jsonb),
('dict-kkumz-024', '멀티사이트 타이포그래피 프리셋', 'kkumz_font_preset', '브랜드 사이트 헤딩·본문 웹폰트 페어링 프리셋', 'admin', '{"ko-KR":"멀티사이트 타이포그래피 프리셋","en-US":"Multi-Site Typography Preset"}'::jsonb),
('dict-kkumz-025', '업종별 큐레이션 스튜디오 미디어', 'kkumz_curated_media', '미업로드 신규 사이트의 업종별 대표 스튜디오 이미지 카탈로그', 'admin', '{"ko-KR":"업종별 큐레이션 스튜디오 미디어","en-US":"Industry-Curated Studio Media"}'::jsonb);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, item_text_i18n) VALUES
('ditem-kkumz-1101', 'dict-kkumz-011', 'AI & LLM 엔진', 'AI_LLM', 'AI 모델 및 추론 타임아웃 설정', 1, 1, '{"ko-KR":"AI & LLM 엔진","en-US":"AI & LLM Engine"}'::jsonb),
('ditem-kkumz-1102', 'dict-kkumz-011', '외부 서비스 연동', 'INTEGRATION', 'SyncCMS, SyncShop, SyncVerse 연동 설정', 2, 1, '{"ko-KR":"외부 서비스 연동","en-US":"External Service Integration"}'::jsonb),
('ditem-kkumz-1103', 'dict-kkumz-011', '보안 & 인증', 'SECURITY', 'JWT 토큰 만료 및 인증 보안 정책', 3, 1, '{"ko-KR":"보안 & 인증","en-US":"Security & Auth"}'::jsonb),
('ditem-kkumz-1104', 'dict-kkumz-011', '스토리지 & 파일', 'STORAGE', '업로드 경로 및 정적 자산 서빙 설정', 4, 1, '{"ko-KR":"스토리지 & 파일","en-US":"Storage & Files"}'::jsonb),
('ditem-kkumz-1105', 'dict-kkumz-011', '비즈니스 정책', 'BIZ_POLICY', '멀티사이트 프로비저닝 및 운영 정책', 5, 1, '{"ko-KR":"비즈니스 정책","en-US":"Business Policies"}'::jsonb),
('ditem-kkumz-1201', 'dict-kkumz-012', '기획 준비중', 'DRAFT', '캠페인 오픈 전 준비 상태', 1, 1, '{"ko-KR":"기획 준비중","en-US":"Planning Pending"}'::jsonb),
('ditem-kkumz-1202', 'dict-kkumz-012', '진행중', 'ACTIVE', '라이브 캠페인 진행 상태', 2, 1, '{"ko-KR":"진행중","en-US":"In Progress"}'::jsonb),
('ditem-kkumz-1203', 'dict-kkumz-012', '일시 중지', 'PAUSED', '캠페인 일시 중지 상태', 3, 1, '{"ko-KR":"일시 중지","en-US":"Paused"}'::jsonb),
('ditem-kkumz-1204', 'dict-kkumz-012', '종료됨', 'COMPLETED', '캠페인 기간 만료 및 종료 상태', 4, 1, '{"ko-KR":"종료됨","en-US":"Closed"}'::jsonb),
('ditem-kkumz-1301', 'dict-kkumz-013', '라이프스타일 & 스테이', 'LIFESTYLE_HYBRID', '숙박·스테이·공간 및 라이프스타일 복합', 1, 1, '{"ko-KR":"라이프스타일 & 스테이","en-US":"Lifestyle & Stay"}'::jsonb),
('ditem-kkumz-1302', 'dict-kkumz-013', 'F&B & 로스터리', 'FOOD_BEVERAGE', '스페셜티 커피, 다도, 베이커리 및 식음료', 2, 1, '{"ko-KR":"F&B & 로스터리","en-US":"F&B & Roastery"}'::jsonb),
('ditem-kkumz-1303', 'dict-kkumz-013', '테크 & 데스크기어', 'TECH_HARDWARE', '커스텀 하드웨어, 데스크 셋업 및 디지털 기어', 3, 1, '{"ko-KR":"테크 & 데스크기어","en-US":"Tech & Desk Gear"}'::jsonb),
('ditem-kkumz-1304', 'dict-kkumz-013', '커뮤니티 & 크루', 'COMMUNITY_MEDIA', '러닝 크루, 매거진, 북클럽 및 로컬 커뮤니티', 4, 1, '{"ko-KR":"커뮤니티 & 크루","en-US":"Community & Crew"}'::jsonb),
('ditem-kkumz-1401', 'dict-kkumz-014', '웰컴 할인 쿠폰', 'WELCOME_COUPON', '첫 방문 및 직예약 즉시 할인 쿠폰', 1, 1, '{"ko-KR":"웰컴 할인 쿠폰","en-US":"Welcome Discount Coupon"}'::jsonb),
('ditem-kkumz-1402', 'dict-kkumz-014', '크리에이터 포인트', 'CREATOR_POINT', '창업 쇼케이스 활동 및 피드백 적립 포인트', 2, 1, '{"ko-KR":"크리에이터 포인트","en-US":"Creator Points"}'::jsonb),
('ditem-kkumz-1403', 'dict-kkumz-014', '기프트 바우처', 'GIFT_VOUCHER', '시즌 패스 및 선불 기프트 이용권', 3, 1, '{"ko-KR":"기프트 바우처","en-US":"Gift Voucher"}'::jsonb),
('ditem-kkumz-1501', 'dict-kkumz-015', '심사 대기', 'PENDING', '모더레이터 심사 대기 상태', 1, 1, '{"ko-KR":"심사 대기","en-US":"Pending Review"}'::jsonb),
('ditem-kkumz-1502', 'dict-kkumz-015', '조치 완료', 'RESOLVED', '신고 승인 및 제재 조치 완료', 2, 1, '{"ko-KR":"조치 완료","en-US":"Action Completed"}'::jsonb),
('ditem-kkumz-1503', 'dict-kkumz-015', '반려 / 이상 없음', 'DISMISSED', '신고 사유 불충분 반려 처분', 3, 1, '{"ko-KR":"반려 / 이상 없음","en-US":"Rejected / No Issue"}'::jsonb),
('ditem-kkumz-1601', 'dict-kkumz-016', '외부 서비스 호출 (OUTBOUND)', 'OUTBOUND', 'Kkumz에서 SyncCMS/SyncShop 등 외부 서비스 호출', 1, 1, '{"ko-KR":"외부 서비스 호출 (OUTBOUND)","en-US":"Outbound Service Call"}'::jsonb),
('ditem-kkumz-1602', 'dict-kkumz-016', '외부 연동 수신 (INBOUND)', 'INBOUND', '외부 서비스에서 Kkumz 런타임 API 호출', 2, 1, '{"ko-KR":"외부 연동 수신 (INBOUND)","en-US":"Inbound Integration"}'::jsonb),
('ditem-kkumz-1701', 'dict-kkumz-017', 'SyncCMS (콘텐츠·배너·폼)', 'SYNC_CMS', 'SyncCMS 멀티사이트 콘텐츠 백엔드', 1, 1, '{"ko-KR":"SyncCMS (콘텐츠·배너·폼)","en-US":"SyncCMS (Content, Banners & Forms)"}'::jsonb),
('ditem-kkumz-1702', 'dict-kkumz-017', 'SyncShop (상품·재고·주문)', 'SYNC_SHOP', 'SyncShop 멀티사이트 커머스 백엔드', 2, 1, '{"ko-KR":"SyncShop (상품·재고·주문)","en-US":"SyncShop (Products, Inventory & Orders)"}'::jsonb),
('ditem-kkumz-1703', 'dict-kkumz-017', 'SyncVerse (전사 총괄 에이전트)', 'SYNC_VERSE', 'SyncVerse 중앙 오케스트레이터', 3, 1, '{"ko-KR":"SyncVerse (전사 총괄 에이전트)","en-US":"SyncVerse Master Orchestrator"}'::jsonb),
('ditem-kkumz-1704', 'dict-kkumz-017', 'EXTERNAL_PARTNER (외부 파트너)', 'EXTERNAL_PARTNER', '외부 파트너 및 커스텀 번들 SDK', 4, 1, '{"ko-KR":"EXTERNAL_PARTNER (외부 파트너)","en-US":"External Partner"}'::jsonb),
('ditem-kkumz-1801', 'dict-kkumz-018', '정상 활성 (ACTIVE)', 'ACTIVE', '정상 호출 가능 상태', 1, 1, '{"ko-KR":"정상 활성 (ACTIVE)","en-US":"Active (ACTIVE)"}'::jsonb),
('ditem-kkumz-1802', 'dict-kkumz-018', '폐기/중지 (REVOKED)', 'REVOKED', '호출 차단 및 폐기 상태', 2, 1, '{"ko-KR":"폐기/중지 (REVOKED)","en-US":"Revoked (REVOKED)"}'::jsonb),
('ditem-kkumz-1901', 'dict-kkumz-019', '성공', 'SUCCESS', '샌드박스 번들 빌드 및 검증 성공', 1, 1, '{"ko-KR":"성공","en-US":"Success"}'::jsonb),
('ditem-kkumz-1902', 'dict-kkumz-019', '실패', 'FAILED', '빌드 컴파일 또는 패키징 오류', 2, 1, '{"ko-KR":"실패","en-US":"Failed"}'::jsonb),
('ditem-kkumz-2001', 'dict-kkumz-020', '정상 가동', 'ACTIVE', '멀티사이트 공개 가동 상태', 1, 1, '{"ko-KR":"정상 가동","en-US":"Operational"}'::jsonb),
('ditem-kkumz-2002', 'dict-kkumz-020', '일시 정지', 'SUSPENDED', '운영 정책에 따른 일시 정지 상태', 2, 1, '{"ko-KR":"일시 정지","en-US":"Paused"}'::jsonb),
('ditem-kkumz-2101', 'dict-kkumz-021', '브랜드 네이밍', 'NAMING', 'AI 브랜드 네이밍 8종 및 슬러그 생성', 1, 1, '{"ko-KR":"브랜드 네이밍","en-US":"Brand Naming"}'::jsonb),
('ditem-kkumz-2102', 'dict-kkumz-021', '벡터 SVG 로고', 'LOGO_SVG', 'AI 벡터 SVG 로고 6종 및 컬러 팔레트 생성', 2, 1, '{"ko-KR":"벡터 SVG 로고","en-US":"Vector SVG Logo"}'::jsonb),
('ditem-kkumz-2103', 'dict-kkumz-021', '마크다운 기능명세서', 'SPEC_FRS', '상세 기능명세서(FRS) 및 화면 설계 생성', 3, 1, '{"ko-KR":"마크다운 기능명세서","en-US":"Markdown Functional Spec"}'::jsonb),
('ditem-kkumz-2104', 'dict-kkumz-021', '대화형 프리뷰 수정', 'CHAT_REFINE', '스튜디오 대화형 테마·레이아웃 리파인', 4, 1, '{"ko-KR":"대화형 프리뷰 수정","en-US":"Interactive Preview Editing"}'::jsonb),
('ditem-kkumz-2105', 'dict-kkumz-021', '사이트 컨시어지 상담', 'CONCIERGE', '멀티사이트 방문객 AI 컨시어지 응대', 5, 1, '{"ko-KR":"사이트 컨시어지 상담","en-US":"Site Concierge Consultation"}'::jsonb),
('ditem-kkumz-2106', 'dict-kkumz-021', 'WebIDE 빌드 패치', 'BUILD_FIX', '샌드박스 빌드 오류 자동 코드 패치', 6, 1, '{"ko-KR":"WebIDE 빌드 패치","en-US":"WebIDE Build Patch"}'::jsonb),
('ditem-kkumz-2107', 'dict-kkumz-021', '아키텍처·기능 진단', 'INTENT_ADVISOR', '프롬프트 기반 아키텍처 및 52종 기능 추천', 7, 1, '{"ko-KR":"아키텍처·기능 진단","en-US":"Architecture & Feature Audit"}'::jsonb),
('ditem-kkumz-2108', 'dict-kkumz-021', '초기 사이트 콘텐츠 생성', 'SITE_CONTENT_PROVISION', '업종 맞춤 초기 상품·요금표·예약폼 생성', 8, 1, '{"ko-KR":"초기 사이트 콘텐츠 생성","en-US":"Initial Site Content Generation"}'::jsonb),
('ditem-kkumz-2201', 'dict-kkumz-022', '활성', 'ACTIVE', '활성 상태', 1, 1, '{"ko-KR":"활성","en-US":"Active"}'::jsonb),
('ditem-kkumz-2202', 'dict-kkumz-022', '비활성', 'INACTIVE', '비활성 상태', 2, 1, '{"ko-KR":"비활성","en-US":"Inactive"}'::jsonb),
('ditem-kkumz-2301', 'dict-kkumz-023', '에디토리얼 보태니컬', 'EDITORIAL_BOTANICAL', '{"surfaceMode":"WARM","fontPreset":"SERIF_EDITORIAL","heroStyle":"EDITORIAL_STORY","radius":"6px","desc":"웜 아이보리 페이퍼 배경과 세리프 타이포, 넓은 여백이 돋보이는 공예·티·뷰티 D2C 아키타입"}', 1, 1, '{"ko-KR":"에디토리얼 보태니컬","en-US":"Editorial Botanical"}'::jsonb),
('ditem-kkumz-2302', 'dict-kkumz-023', '다크 테크 쇼케이스', 'DARK_TECH_SHOWCASE', '{"surfaceMode":"DARK","fontPreset":"TECH_MONO","heroStyle":"PROMO_COMMERCE","radius":"10px","desc":"옵시디언 다크 캔버스와 모노스페이스 스펙 표기, 벤토 그리드 중심의 하드웨어·테크 아키타입"}', 2, 1, '{"ko-KR":"다크 테크 쇼케이스","en-US":"Dark Tech Showcase"}'::jsonb),
('ditem-kkumz-2303', 'dict-kkumz-023', '매거진 하이브리드', 'MAGAZINE_HYBRID', '{"surfaceMode":"WARM","fontPreset":"SERIF_EDITORIAL","heroStyle":"SPLIT_VISUAL","radius":"8px","desc":"저널 아티클과 커머스 셀렉션, 구독 플랜이 자연스럽게 교차하는 로스터리·라이프스타일 복합 아키타입"}', 3, 1, '{"ko-KR":"매거진 하이브리드","en-US":"Magazine Hybrid"}'::jsonb),
('ditem-kkumz-2304', 'dict-kkumz-023', '커뮤니티 키네틱', 'COMMUNITY_KINETIC', '{"surfaceMode":"LIGHT","fontPreset":"BOLD_SANS","heroStyle":"FULL_SCENIC","radius":"12px","desc":"하이콘트라스트 볼드 타이포, 주간 세션 일정표, 참가 신청 폼 중심의 크루·커뮤니티 아키타입"}', 4, 1, '{"ko-KR":"커뮤니티 키네틱","en-US":"Community Kinetic"}'::jsonb),
('ditem-kkumz-2305', 'dict-kkumz-023', '시닉 호스피탈리티', 'SCENIC_HOSPITALITY', '{"surfaceMode":"WARM","fontPreset":"SERIF_EDITORIAL","heroStyle":"FULL_SCENIC","radius":"16px","desc":"풀블리드 자연 풍경 히어로, 공간 투어, 시즌 요금표, 맞춤 예약 폼 특화 스테이·민박 아키타입"}', 5, 1, '{"ko-KR":"시닉 호스피탈리티","en-US":"Scenic Hospitality"}'::jsonb),
('ditem-kkumz-2401', 'dict-kkumz-024', '모던 산스 (Pretendard)', 'MODERN_SANS', 'Pretendard, -apple-system, BlinkMacSystemFont, sans-serif', 1, 1, '{"ko-KR":"모던 산스 (Pretendard)","en-US":"Modern Sans (Pretendard)"}'::jsonb),
('ditem-kkumz-2402', 'dict-kkumz-024', '에디토리얼 세리프 (Noto Serif KR)', 'SERIF_EDITORIAL', '"Noto Serif KR", "Cormorant Garamond", Georgia, serif', 2, 1, '{"ko-KR":"에디토리얼 세리프 (Noto Serif KR)","en-US":"Editorial Serif (Noto Serif KR)"}'::jsonb),
('ditem-kkumz-2403', 'dict-kkumz-024', '테크 모노 (JetBrains Mono)', 'TECH_MONO', '"JetBrains Mono", "Pretendard", monospace', 3, 1, '{"ko-KR":"테크 모노 (JetBrains Mono)","en-US":"Tech Mono (JetBrains Mono)"}'::jsonb),
('ditem-kkumz-2404', 'dict-kkumz-024', '키네틱 볼드 (Pretendard Heavy)', 'BOLD_SANS', 'Pretendard, -apple-system, BlinkMacSystemFont, sans-serif', 4, 1, '{"ko-KR":"키네틱 볼드 (Pretendard Heavy)","en-US":"Kinetic Bold (Pretendard Heavy)"}'::jsonb),
('ditem-kkumz-2501', 'dict-kkumz-025', 'F&B·티·식품 큐레이션', 'FOOD_BEVERAGE', '{"hero":"/images/studio/banner-matcha-jeju.jpg","items":["/images/studio/prod-matcha-tin.jpg","/images/studio/prod-matcha-chawan.jpg","/images/studio/prod-matcha-latte.jpg","/images/studio/prod-matcha-chocolate.jpg"]}', 1, 1, '{"ko-KR":"F&B·티·식품 큐레이션","en-US":"F&B, Tea & Food Curation"}'::jsonb),
('ditem-kkumz-2502', 'dict-kkumz-025', '테크·하드웨어 큐레이션', 'TECH_HARDWARE', '{"hero":"/images/studio/banner-ai-workstation.jpg","items":["/images/studio/prod-qi2-station.jpg","/images/studio/prod-walnut-shelf.jpg","/images/studio/prod-monitor-lightbar.jpg","/images/studio/prod-macropad.jpg"]}', 2, 1, '{"ko-KR":"테크·하드웨어 큐레이션","en-US":"Tech & Hardware Curation"}'::jsonb),
('ditem-kkumz-2503', 'dict-kkumz-025', '커뮤니티·스포츠 큐레이션', 'COMMUNITY_MEDIA', '{"hero":"/images/studio/banner-seoul-night-run.jpg","items":["/images/studio/run-course-banpo.jpg","/images/studio/run-course-seongsu.jpg","/images/studio/run-training-track.jpg"]}', 3, 1, '{"ko-KR":"커뮤니티·스포츠 큐레이션","en-US":"Community & Sports Curation"}'::jsonb),
('ditem-kkumz-2504', 'dict-kkumz-025', '로스터리·라이프스타일 복합', 'LIFESTYLE_HYBRID', '{"hero":"/images/studio/banner-geisha-coffee.jpg","items":["/images/studio/prod-geisha-beans.jpg","/images/studio/prod-dripbag-gift.jpg","/images/studio/prod-pourover-kettle.jpg","/images/studio/prod-coffee-sampler.jpg"]}', 4, 1, '{"ko-KR":"로스터리·라이프스타일 복합","en-US":"Roastery & Lifestyle Complex"}'::jsonb),
('ditem-kkumz-2505', 'dict-kkumz-025', '숙박·스테이·공간대여', 'HOSPITALITY_STAY', '{"hero":"/images/studio/banner-minbak-creek.jpg","items":["/images/studio/minbak-room-ondol.jpg","/images/studio/minbak-pyeongsang.jpg","/images/studio/minbak-dining-bbq.jpg","/images/studio/minbak-autumn-yard.jpg","/images/studio/minbak-winter-snow.jpg"]}', 5, 1, '{"ko-KR":"숙박·스테이·공간대여","en-US":"Stay, Lodging & Space Rental"}'::jsonb);

-- ============================================================================
-- 시스템 통합 동적 환경설정 초기 시드 (sys_config)
-- ============================================================================
INSERT INTO sys_config (
    id, config_category, config_key, config_name, config_value, default_value,
    value_type, is_secret, is_dynamic, status, sort_order, description, create_by
) VALUES
('cfg-kkumz-001', 'INTEGRATION', 'kkumz.integration.cms.base-url', 'SyncCMS 멀티사이트 연동 Base URL', 'http://localhost:18085', 'http://localhost:18085', 'STRING', FALSE, TRUE, 1, 10, 'SyncCMS 멀티사이트 콘텐츠·배너·폼 API 호출 기본 엔드포인트', 'admin'),
('cfg-kkumz-002', 'INTEGRATION', 'kkumz.integration.cms.access-key', 'SyncCMS 연동 Access Key', 'ak_cms_kkumz_live_01', 'ak_cms_kkumz_live_01', 'STRING', FALSE, TRUE, 1, 20, 'SyncCMS 멀티사이트 API 호출용 기본 Access Key', 'admin'),
('cfg-kkumz-003', 'INTEGRATION', 'kkumz.integration.cms.secret-key', 'SyncCMS 연동 Secret Key', 'sk_cms_kkumz_secret_2026_prod_99', 'sk_cms_kkumz_secret_2026_prod_99', 'SECRET', TRUE, TRUE, 1, 30, 'SyncCMS 멀티사이트 API 호출용 기본 Secret Key (AES-256-GCM 암호화 저장)', 'admin'),
('cfg-kkumz-004', 'INTEGRATION', 'kkumz.integration.shop.base-url', 'SyncShop 멀티사이트 연동 Base URL', 'http://localhost:8501', 'http://localhost:8501', 'STRING', FALSE, TRUE, 1, 40, 'SyncShop 멀티사이트 상품·주문 API 호출 기본 엔드포인트', 'admin'),
('cfg-kkumz-005', 'INTEGRATION', 'kkumz.integration.shop.access-key', 'SyncShop 연동 Access Key', 'ak_shop_kkumz_live_01', 'ak_shop_kkumz_live_01', 'STRING', FALSE, TRUE, 1, 50, 'SyncShop 멀티사이트 API 호출용 기본 Access Key', 'admin'),
('cfg-kkumz-006', 'INTEGRATION', 'kkumz.integration.shop.secret-key', 'SyncShop 연동 Secret Key', 'sk_shop_kkumz_secret_2026_prod_88', 'sk_shop_kkumz_secret_2026_prod_88', 'SECRET', TRUE, TRUE, 1, 60, 'SyncShop 멀티사이트 API 호출용 기본 Secret Key (AES-256-GCM 암호화 저장)', 'admin'),
('cfg-kkumz-007', 'INTEGRATION', 'kkumz.integration.verse.base-url', 'SyncVerse 오케스트레이터 Base URL', 'http://localhost:18081', 'http://localhost:18081', 'STRING', FALSE, TRUE, 1, 70, 'SyncVerse 중앙 오케스트레이터 연동 기본 엔드포인트', 'admin'),
('cfg-kkumz-008', 'INTEGRATION', 'kkumz.server.public-base-url', 'Kkumz 공개 API Base URL', 'http://localhost:9580', 'http://localhost:9580', 'STRING', FALSE, TRUE, 1, 80, 'WebIDE 번들 및 외부 사이트에서 Kkumz 런타임 API 호출 시 사용하는 공개 URL', 'admin'),
('cfg-kkumz-009', 'INTEGRATION', 'kkumz.integration.circuit-cooldown-ms', '외부 연동 서킷브레이커 쿨다운 (ms)', '60000', '60000', 'NUMBER', FALSE, TRUE, 1, 90, '원격 서비스 장애 감지 시 로컬 DB 어댑터 폴백 유지 시간(밀리초)', 'admin'),
('cfg-kkumz-010', 'AI_LLM', 'kkumz.ai.default-model', 'AI 스튜디오 기본 LLM 모델명', 'morph-glm53flash', 'morph-glm53flash', 'STRING', FALSE, TRUE, 1, 10, '단계별 프롬프트에 모델이 지정되지 않았을 때 사용할 기본 LLM 모델 식별자', 'admin'),
('cfg-kkumz-011', 'AI_LLM', 'kkumz.ai.timeout-seconds', 'AI 추론 요청 타임아웃 (초)', '60', '60', 'NUMBER', FALSE, TRUE, 1, 20, 'AI 게이트웨이 단일 추론 호출 최대 대기 시간(초)', 'admin'),
('cfg-kkumz-012', 'SECURITY', 'kkumz.security.jwt-expire-ms', 'JWT 인증 세션 유효기간 (ms)', '86400000', '86400000', 'NUMBER', FALSE, TRUE, 1, 10, '로그인 액세스 토큰 기본 만료 시간(밀리초)', 'admin'),
('cfg-kkumz-013', 'STORAGE', 'kkumz.storage.upload-dir', '미디어·문서 업로드 로컬 루트 경로', 'uploads/kkumz', 'uploads/kkumz', 'STRING', FALSE, TRUE, 1, 10, '스튜디오 업로드 자산 및 Git 번들 저장 디렉터리 경로', 'admin'),
('cfg-kkumz-014', 'STORAGE', 'kkumz.storage.public-prefix', '정적 자산 공개 URL Prefix', '/api/v1/kkumz/assets/files', '/api/v1/kkumz/assets/files', 'STRING', FALSE, TRUE, 1, 20, '업로드된 미디어 파일 서빙을 위한 공개 URL 접두사', 'admin'),
('cfg-kkumz-015', 'BIZ_POLICY', 'kkumz.policy.default-rate-limit-rpm', 'API 키 기본 분당 호출 한도 (RPM)', '600', '600', 'NUMBER', FALSE, TRUE, 1, 10, '신규 API 액세스 키 발급 시 기본 분당 호출 허용 횟수', 'admin'),
('cfg-kkumz-016', 'STORAGE', 'kkumz.storage.media-base-url', '공유 미디어 허브 공개 Base URL', '/media', '/media', 'STRING', FALSE, TRUE, 1, 30, 'MinIO/S3 공유 오브젝트 스토리지 프록시 미디어 기본 경로', 'admin');

-- ============================================================================
-- 기본 홈 라우트, 조직 부서 트리, 알림 템플릿 및 초기 감사 로그 시드
-- ============================================================================
INSERT INTO sys_role_index (id, role_code, url, component, is_route, priority, status, create_by) VALUES
('ridx-kkumz-001', 'DEF_INDEX_ALL', '/dashboard/analysis', 'dashboard/analysis/index', TRUE, 1, '1', 'admin');

INSERT INTO sys_depart (
    id, parent_id, depart_name, depart_name_en, depart_order, description,
    org_category, org_type, org_code, mobile, fax, address, memo, status, del_flag, create_by
) VALUES
('dept-kkumz-001', NULL, '꿈즈 플랫폼 본부', 'Kkumz Platform HQ', 1, '꿈즈 창업·멀티사이트 플랫폼 운영 총괄 본부', '1', '1', 'KKUMZ-HQ', NULL, NULL, '서울특별시 강남구 테헤란로', '플랫폼 운영 총괄', '1', 0, 'admin'),
('dept-kkumz-002', 'dept-kkumz-001', '신뢰안전·모더레이션팀', 'Trust and Safety', 2, '커뮤니티 게시물 및 배포 사이트 신고 심사 전담 팀', '2', '2', 'KKUMZ-TRUST', NULL, NULL, '서울특별시 강남구 테헤란로', '커뮤니티 모더레이션 전담', '1', 0, 'admin'),
('dept-kkumz-003', 'dept-kkumz-001', '크리에이터 성장팀', 'Creator Growth', 3, '창업가 메이커 온보딩 및 AI 스튜디오 지원 팀', '2', '2', 'CREATOR', NULL, NULL, '서울특별시 강남구 테헤란로', '메이커 온보딩 지원', '1', 0, 'admin');

INSERT INTO sys_message_template (
    id, template_name, template_code, template_type, template_content, template_test_json, use_status, create_by
) VALUES
('msgtpl-kkumz-001', '사이트 배포 완료 알림', 'KKUMZ_SITE_DEPLOYED', '4', '${creatorName}님의 ${siteTitle} 사이트가 정상 배포되었습니다.', '{"creatorName":"메이커","siteTitle":"브랜드 스토어"}', '1', 'admin'),
('msgtpl-kkumz-002', '커뮤니티 신고 심사 안내', 'KKUMZ_MODERATION_RESULT', '4', '접수된 신고(${reportId})의 처리 상태가 ${status}(으)로 업데이트되었습니다.', '{"reportId":"RPT-001","status":"조치 완료"}', '1', 'admin'),
('msgtpl-kkumz-003', '팀빌딩 합류 요청 알림', 'KKUMZ_TEAM_INVITE', '4', '${projectName} 프로젝트의 ${roleName} 포지션 합류 요청이 도착했습니다.', '{"projectName":"꿈즈 프로젝트","roleName":"프론트엔드"}', '1', 'admin');

INSERT INTO sys_log (
    id, log_type, log_content, operate_type, userid, username, ip, method, request_url, request_param, client_type, cost_time
) VALUES
('log-kkumz-001', 1, '관리자 계정 시스템 로그인 성공', 1, 'usr-kkumz-001', 'admin', '127.0.0.1', 'POST', '/api/v1/kkumz/sys/login', '{}', 'WEB', 24),
('log-kkumz-002', 2, '시스템 동적 메뉴 권한 트리 초기화 및 검증', 1, 'usr-kkumz-001', 'admin', '127.0.0.1', 'GET', '/api/v1/kkumz/sys/permission/getUserPermissionByToken', '{}', 'WEB', 15),
('log-kkumz-003', 2, '멀티사이트 운영 환경설정 상태 점검 완료', 1, 'usr-kkumz-001', 'admin', '127.0.0.1', 'GET', '/api/v1/kkumz/sys/config/list', '{}', 'WEB', 18);