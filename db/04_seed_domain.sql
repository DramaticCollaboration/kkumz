-- ============================================================================
-- Kkumz (꿈즈) 도메인 업무 메뉴 및 AI 프롬프트 마스터 시드 (04_seed_domain.sql)
-- (무중복 결정론적 순수 INSERT INTO 단일 원천 구성)
-- ============================================================================

INSERT INTO sys_permission (
    id, parent_id, name, url, component, is_route, component_name, redirect,
    menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description
) VALUES
-- 1. AI 스튜디오
('perm-kkumz-dom-001', NULL, 'AI 스튜디오', '/studio', 'layouts/default/index', TRUE, 'KkumzStudioLayout', '/studio/ideas', 0, NULL, '1', 10.0, TRUE, 'ant-design:bulb-outlined', FALSE, FALSE, FALSE, FALSE, '아이디어·기능명세서 생성 현황 및 AI 프롬프트 템플릿 관제'),
('perm-kkumz-dom-002', 'perm-kkumz-dom-001', '아이디어·기획서', '/studio/ideas', 'studio/ideas/index', TRUE, 'KkumzStudioIdeasView', NULL, 1, 'kkumz:studio:ideas', '1', 10.1, FALSE, 'ant-design:file-markdown-outlined', TRUE, TRUE, FALSE, FALSE, '창업 아이디어, 브랜드 후보, 마크다운 기능명세서 및 버전 이력 관리'),
('perm-kkumz-dom-003', 'perm-kkumz-dom-001', '프롬프트·금지어', '/studio/prompts', 'studio/prompts/index', TRUE, 'KkumzStudioPromptsView', NULL, 1, 'kkumz:studio:prompts', '1', 10.2, FALSE, 'ant-design:control-outlined', TRUE, TRUE, FALSE, FALSE, '네이밍·로고·명세서·코드수리 단계별 AI 프롬프트 템플릿 및 금지어 관리'),

-- 2. 사이트·배포
('perm-kkumz-dom-010', NULL, '사이트·배포', '/deployment', 'layouts/default/index', TRUE, 'KkumzDeploymentLayout', '/deployment/sites', 0, NULL, '1', 20.0, TRUE, 'ant-design:rocket-outlined', FALSE, FALSE, FALSE, FALSE, '멀티사이트 도메인 운영 상태 및 WebIDE 샌드박스 빌드 파이프라인 관제'),
('perm-kkumz-dom-011', 'perm-kkumz-dom-010', '생성 사이트 목록', '/deployment/sites', 'deployment/sites/index', TRUE, 'KkumzDeploymentSitesView', NULL, 1, 'kkumz:deployment:sites', '1', 20.1, FALSE, 'ant-design:global-outlined', TRUE, TRUE, FALSE, FALSE, '생성된 멀티사이트 도메인, 배포 버전, 커머스/CMS 연동 및 운영 상태 제어'),
('perm-kkumz-dom-012', 'perm-kkumz-dom-010', '빌드·배포 로그', '/deployment/builds', 'deployment/builds/index', TRUE, 'KkumzDeploymentBuildsView', NULL, 1, 'kkumz:deployment:builds', '1', 20.2, FALSE, 'ant-design:code-outlined', TRUE, TRUE, FALSE, FALSE, '샌드박스 빌드 실행 이력, 빌드 소요시간, 에러 로그 및 AI 패치 내역 조회'),

-- 3. 커뮤니티
('perm-kkumz-dom-020', NULL, '커뮤니티', '/community', 'layouts/default/index', TRUE, 'KkumzCommunityLayout', '/community/projects', 0, NULL, '1', 30.0, TRUE, 'ant-design:usergroup-add-outlined', FALSE, FALSE, FALSE, FALSE, '창업 쇼케이스 큐레이션, 팀원 모집 공고 및 커뮤니티 신고 모더레이션'),
('perm-kkumz-dom-021', 'perm-kkumz-dom-020', '쇼케이스·팀빌딩', '/community/projects', 'community/projects/index', TRUE, 'KkumzCommunityProjectsView', NULL, 1, 'kkumz:community:projects', '1', 30.1, FALSE, 'ant-design:star-outlined', TRUE, TRUE, FALSE, FALSE, '추천 프로젝트 큐레이션 지정, 팀원 모집 공고 및 팀 합류 매칭 현황 관리'),
('perm-kkumz-dom-022', 'perm-kkumz-dom-020', '신고·제재 관리', '/community/moderation', 'community/moderation/index', TRUE, 'KkumzCommunityModerationView', NULL, 1, 'kkumz:community:moderation', '1', 30.2, FALSE, 'ant-design:safety-certificate-outlined', TRUE, TRUE, FALSE, FALSE, '게시물·댓글·사이트 신고 접수 심사, 블라인드 처분 및 계정 제재 관리');

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
('srp_kkumz_dom_001', 'role_admin', 'perm-kkumz-dom-001'),
('srp_kkumz_dom_002', 'role_admin', 'perm-kkumz-dom-002'),
('srp_kkumz_dom_003', 'role_admin', 'perm-kkumz-dom-003'),
('srp_kkumz_dom_010', 'role_admin', 'perm-kkumz-dom-010'),
('srp_kkumz_dom_011', 'role_admin', 'perm-kkumz-dom-011'),
('srp_kkumz_dom_012', 'role_admin', 'perm-kkumz-dom-012'),
('srp_kkumz_dom_020', 'role_admin', 'perm-kkumz-dom-020'),
('srp_kkumz_dom_021', 'role_admin', 'perm-kkumz-dom-021'),
('srp_kkumz_dom_022', 'role_admin', 'perm-kkumz-dom-022'),
('srp_kkumz_mod_020', 'role_moderator', 'perm-kkumz-dom-020'),
('srp_kkumz_mod_021', 'role_moderator', 'perm-kkumz-dom-021'),
('srp_kkumz_mod_022', 'role_moderator', 'perm-kkumz-dom-022');

-- AI 스튜디오 프롬프트 템플릿 및 필터 규칙 마스터
INSERT INTO kkumz_ai_prompt_template (
    id, template_code, template_name, stage_type, model_name, temperature,
    system_prompt, user_prompt_template, forbidden_words_json, status, created_by, updated_by
) VALUES
(
    'tpl-kkumz-001',
    'BRAND_NAMING_8X',
    'AI 브랜드 네이밍 8종 & 서브도메인 슬러그 생성기',
    'NAMING',
    'qwen-plus',
    0.75,
    '당신은 스타트업 브랜드 네이밍 전문가입니다. 사용자의 아이디어 프롬프트와 카테고리를 분석하여 발음하기 쉽고 기억에 남는 브랜드명 8종, 영문/국문 발음 가이드, 영문 케밥케이스 슬러그(site_slug), 그리고 2문장 이내의 브랜딩 스토리를 JSON 배열로 생성하십시오.',
    '아이디어: {{promptText}} / 카테고리: {{category}} / 키워드 힌트: {{keywordHint}}',
    '["admin", "root", "system", "kkumz-official", "test", "null", "illegal", "casino"]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
),
(
    'tpl-kkumz-002',
    'VECTOR_SVG_LOGO_6X',
    'AI 벡터 SVG 로고 6종 & 브랜드 컬러 팔레트 생성기',
    'LOGO_SVG',
    'qwen-plus',
    0.70,
    '당신은 브랜드 BX 디자이너입니다. 선택된 브랜드명과 아이디어 컨셉을 바탕으로 즉시 브라우저에서 렌더링 가능한 순수 SVG 코드(viewBox="0 0 185 40") 6종과 primary, secondary, accent, surface 컬러 팔레트 토큰을 생성하십시오.',
    '브랜드명: {{brandName}} / 선호 스타일: {{stylePreference}}',
    '["script", "onload", "onerror", "javascript:"]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
),
(
    'tpl-kkumz-003',
    'FRS_SPEC_MARKDOWN',
    '상세 기능 명세서(FRS) 마크다운 & 화면 설계 생성기',
    'SPEC_FRS',
    'qwen-plus',
    0.50,
    '당신은 수석 프로덕트 매니저(PM)입니다. 아이디어 프롬프트와 확정된 브랜드 아이덴티티를 바탕으로 1) 브랜드 비전 및 타겟 고객, 2) 핵심 기능 구성, 3) 화면 흐름 및 주요 컴포넌트 목록(screens_json), 4) 데이터 및 연동 규격을 포함한 마크다운 기능 명세서를 작성하십시오.',
    '브랜드명: {{brandName}} / 사이트 유형: {{siteType}} / 상세 프롬프트: {{promptText}}',
    '["TODO", "TBD", "추후작성"]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
),
(
    'tpl-kkumz-004',
    'WEBIDE_AI_BUILD_FIXER',
    'WebIDE 빌드 오류 진단 및 자동 코드 패치 에이전트',
    'BUILD_FIX',
    'qwen-plus',
    0.20,
    '당신은 Vue 3 및 TypeScript 빌드 디버깅 전문 에이전트입니다. 빌드 로그의 컴파일/타입 오류 원인을 정확히 진단하고, 수정 전/후 Diff 요약과 함께 즉시 빌드 가능한 정상 소스 코드를 생성하십시오.',
    '에러 로그: {{buildLog}} / 대상 파일: {{filesJson}}',
    '["eval(", "Function("]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
);
