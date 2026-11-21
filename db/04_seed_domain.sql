-- ============================================================================
-- Kkumz (꿈즈) 도메인 업무 메뉴 및 AI 프롬프트 마스터 시드 (04_seed_domain.sql)
-- (무중복 결정론적 순수 INSERT INTO 단일 원천 구성)
-- ============================================================================

INSERT INTO sys_permission (
    id, parent_id, name, url, component, is_route, component_name, redirect,
    menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description
, name_i18n, description_i18n) VALUES
-- 1. AI 스튜디오
('perm-kkumz-dom-001', NULL, 'AI 스튜디오', '/studio', 'layouts/default/index', TRUE, 'KkumzStudioLayout', '/studio/ideas', 0, NULL, '1', 10.0, TRUE, 'ant-design:bulb-outlined', FALSE, FALSE, FALSE, FALSE, '아이디어·기능명세서 생성 현황 및 AI 프롬프트 템플릿 관제', '{"ko-KR":"AI 스튜디오","en-US":"AI Studio"}'::jsonb, '{"ko-KR":"아이디어·기능명세서 생성 현황 및 AI 프롬프트 템플릿 관제","en-US":"Monitor idea and functional spec generation status and AI prompt templates"}'::jsonb),
('perm-kkumz-dom-002', 'perm-kkumz-dom-001', '창업 기획서', '/studio/ideas', 'studio/ideas/index', TRUE, 'KkumzStudioIdeasView', NULL, 1, 'kkumz:studio:ideas', '1', 10.1, FALSE, 'ant-design:file-markdown-outlined', TRUE, TRUE, FALSE, FALSE, '창업 아이디어, 브랜드 후보, 마크다운 기능명세서 및 버전 이력 관리', '{"ko-KR":"창업 기획서","en-US":"Startup Plan"}'::jsonb, '{"ko-KR":"창업 아이디어, 브랜드 후보, 마크다운 기능명세서 및 버전 이력 관리","en-US":"Manage startup ideas, brand candidates, Markdown functional specs, and version history"}'::jsonb),
('perm-kkumz-dom-003', 'perm-kkumz-dom-001', 'AI 생성 규칙', '/studio/prompts', 'studio/prompts/index', TRUE, 'KkumzStudioPromptsView', NULL, 1, 'kkumz:studio:prompts', '1', 10.2, FALSE, 'ant-design:control-outlined', TRUE, TRUE, FALSE, FALSE, '네이밍·로고·명세서·코드수리 단계별 AI 프롬프트 템플릿 및 금지어 관리', '{"ko-KR":"AI 생성 규칙","en-US":"AI Generation Rules"}'::jsonb, '{"ko-KR":"네이밍·로고·명세서·코드수리 단계별 AI 프롬프트 템플릿 및 금지어 관리","en-US":"Manage AI prompt templates and blocked words across naming, logo, spec, and code-fix stages"}'::jsonb),

-- 2. 사이트·배포
('perm-kkumz-dom-010', NULL, '사이트·배포', '/deployment', 'layouts/default/index', TRUE, 'KkumzDeploymentLayout', '/deployment/sites', 0, NULL, '1', 20.0, TRUE, 'ant-design:rocket-outlined', FALSE, FALSE, FALSE, FALSE, '멀티사이트 도메인 운영 상태 및 WebIDE 샌드박스 빌드 파이프라인 관제', '{"ko-KR":"사이트·배포","en-US":"Sites & Deployment"}'::jsonb, '{"ko-KR":"멀티사이트 도메인 운영 상태 및 WebIDE 샌드박스 빌드 파이프라인 관제","en-US":"Control multi-site domain operations and WebIDE sandbox build pipelines"}'::jsonb),
('perm-kkumz-dom-011', 'perm-kkumz-dom-010', '사이트 목록', '/deployment/sites', 'deployment/sites/index', TRUE, 'KkumzDeploymentSitesView', NULL, 1, 'kkumz:deployment:sites', '1', 20.1, FALSE, 'ant-design:global-outlined', TRUE, TRUE, FALSE, FALSE, '생성된 멀티사이트 도메인, 배포 버전, 커머스/CMS 연동 및 운영 상태 제어', '{"ko-KR":"사이트 목록","en-US":"Site List"}'::jsonb, '{"ko-KR":"생성된 멀티사이트 도메인, 배포 버전, 커머스/CMS 연동 및 운영 상태 제어","en-US":"Control generated multi-site domains, deployment versions, commerce/CMS integrations, and operational status"}'::jsonb),
('perm-kkumz-dom-012', 'perm-kkumz-dom-010', '빌드 로그', '/deployment/builds', 'deployment/builds/index', TRUE, 'KkumzDeploymentBuildsView', NULL, 1, 'kkumz:deployment:builds', '1', 20.2, FALSE, 'ant-design:code-outlined', TRUE, TRUE, FALSE, FALSE, '샌드박스 빌드 실행 이력, 빌드 소요시간, 에러 로그 및 AI 패치 내역 조회', '{"ko-KR":"빌드 로그","en-US":"Build Logs"}'::jsonb, '{"ko-KR":"샌드박스 빌드 실행 이력, 빌드 소요시간, 에러 로그 및 AI 패치 내역 조회","en-US":"Query sandbox build execution history, build duration, error logs, and AI patch records"}'::jsonb),
('perm-kkumz-dom-013', 'perm-kkumz-dom-010', 'API 키', '/deployment/api-keys', 'deployment/api-keys/index', TRUE, 'KkumzDeploymentApiKeysView', NULL, 1, 'kkumz:deployment:apikeys', '1', 20.3, FALSE, 'ant-design:key-outlined', TRUE, TRUE, FALSE, FALSE, 'SyncCMS·SyncShop 백엔드 연동 Access Key 설정 및 인바운드 API 키 발급·회전·진단 관리', '{"ko-KR":"API 키","en-US":"API Key"}'::jsonb, '{"ko-KR":"SyncCMS·SyncShop 백엔드 연동 Access Key 설정 및 인바운드 API 키 발급·회전·진단 관리","en-US":"Configure SyncCMS/SyncShop backend Access Keys and manage inbound API key issuance, rotation, and diagnostics"}'::jsonb),

-- 3. 커뮤니티
('perm-kkumz-dom-020', NULL, '커뮤니티', '/community', 'layouts/default/index', TRUE, 'KkumzCommunityLayout', '/community/projects', 0, NULL, '1', 30.0, TRUE, 'ant-design:usergroup-add-outlined', FALSE, FALSE, FALSE, FALSE, '창업 쇼케이스 큐레이션, 팀원 모집 공고 및 커뮤니티 신고 모더레이션', '{"ko-KR":"커뮤니티","en-US":"Community"}'::jsonb, '{"ko-KR":"창업 쇼케이스 큐레이션, 팀원 모집 공고 및 커뮤니티 신고 모더레이션","en-US":"Startup showcase curation, team recruitment posts, and community report moderation"}'::jsonb),
('perm-kkumz-dom-021', 'perm-kkumz-dom-020', '프로젝트·팀원', '/community/projects', 'community/projects/index', TRUE, 'KkumzCommunityProjectsView', NULL, 1, 'kkumz:community:projects', '1', 30.1, FALSE, 'ant-design:star-outlined', TRUE, TRUE, FALSE, FALSE, '추천 프로젝트 큐레이션 지정, 팀원 모집 공고 및 팀 합류 매칭 현황 관리', '{"ko-KR":"프로젝트·팀원","en-US":"Projects & Team"}'::jsonb, '{"ko-KR":"추천 프로젝트 큐레이션 지정, 팀원 모집 공고 및 팀 합류 매칭 현황 관리","en-US":"Featured project curation, team recruitment listings, and team matching status"}'::jsonb),
('perm-kkumz-dom-022', 'perm-kkumz-dom-020', '신고 관리', '/community/moderation', 'community/moderation/index', TRUE, 'KkumzCommunityModerationView', NULL, 1, 'kkumz:community:moderation', '1', 30.2, FALSE, 'ant-design:safety-certificate-outlined', TRUE, TRUE, FALSE, FALSE, '게시물·댓글·사이트 신고 접수 심사, 블라인드 처분 및 계정 제재 관리', '{"ko-KR":"신고 관리","en-US":"Reports Moderation"}'::jsonb, '{"ko-KR":"게시물·댓글·사이트 신고 접수 심사, 블라인드 처분 및 계정 제재 관리","en-US":"Review post, comment, and site reports with content hiding and account sanctions"}'::jsonb);

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
('srp_kkumz_dom_001', 'role_admin', 'perm-kkumz-dom-001'),
('srp_kkumz_dom_002', 'role_admin', 'perm-kkumz-dom-002'),
('srp_kkumz_dom_003', 'role_admin', 'perm-kkumz-dom-003'),
('srp_kkumz_dom_010', 'role_admin', 'perm-kkumz-dom-010'),
('srp_kkumz_dom_011', 'role_admin', 'perm-kkumz-dom-011'),
('srp_kkumz_dom_012', 'role_admin', 'perm-kkumz-dom-012'),
('srp_kkumz_dom_013', 'role_admin', 'perm-kkumz-dom-013'),
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
    'morph-glm53flash',
    0.70,
    '당신은 사용자의 창업 아이디어 문맥과 의도를 심층 분석하는 수석 브랜드 네이밍 AI 에이전트입니다. 인사말이나 마크다운 설명문을 절대 포함하지 말고, 첫 글자가 반드시 "[" 로 시작하고 "]" 로 끝나는 순수 JSON 배열(반드시 정확히 8개의 서로 다른 객체)만 출력하십시오. 단 1개만 출력하는 것을 엄격히 금지합니다. [핵심 판단 원칙 1 - 사용자 명시 상호·브랜드명 1순위 보존 + 7개 대안 추가]: 사용자의 제목, 상세 프롬프트, 또는 키워드 힌트 문장 안에 사용자가 이미 희망하거나 명시한 고유 상호명·브랜드명·공간명(예: "충남 공주에 있는 냇가에 맑은물 민박 홈페이지 만들고 싶어"에서의 "맑은물 민박")이 포함되어 있는지 문맥을 통해 스스로 판단하십시오. 명시된 이름이 존재할 경우, 이를 한자어로 바꾸거나 누락시키지 말고 반드시 배열의 1번째 원소(index 0, score: 99)의 brandName으로 원형 그대로 최우선 배치하십시오. 이어서 2~3번째 원소(score: 97~95)는 사용자의 핵심 키워드와 지역·특징을 살린 직관적 변형 네이밍 2종, 4~5번째 원소(score: 93~91)는 따뜻하고 서정적인 순우리말·감성 네이밍 2종, 6~8번째 원소(score: 89~85)는 품격 있는 한자어 및 모던 라이프스타일 대안 네이밍 3종으로 구성하여 반드시 총 8개의 서로 다른 브랜드 후보를 빠짐없이 생성하십시오. [핵심 판단 원칙 2 - 업종 자율 추론 및 이종 업종 오염 금지]: 프롬프트 문맥에서 실제 업종·도메인(categoryLabel, 예: "숙박 · 민박 · 스테이", "스페셜티 로스터리" 등 한국어 업종명)을 직접 추론하고, 프롬프트와 무관한 타 업종(예: 민박/숙소인데 식당/음식점 등)을 절대 혼합하지 마십시오. brandStory에는 FOOD_BEVERAGE, CMS_SITE, COMMERCE, AUTO 등 시스템 영문 코드를 절대 노출하지 마십시오.',
    '프로젝트 제목: {{title}} / 상세 아이디어 프롬프트: {{promptText}} / 사이트 유형: {{siteType}} / 참고 카테고리: {{category}} / 키워드 힌트: {{keywordHint}}\n\n[출력 규칙] 반드시 총 8개의 객체를 담은 순수 JSON 배열만 출력하십시오 (1개만 출력 금지). 각 객체는 {"brandName": "브랜드명", "siteSlug": "영문-소문자-케밥-슬러그", "pronunciation": "국문/영문 발음 가이드", "brandStory": "1~2문장의 간결한 순수 한국어 브랜딩 스토리", "categoryLabel": "문맥에서 추론한 한국어 업종명", "score": 점수} 규격이며, 사용자가 명시한 상호명이 있으면 1번째 원소(score: 99)에 원형 그대로 배치하고 나머지 7개 대안 후보까지 총 8개 원소 배열 [ {...1}, {...2}, {...3}, {...4}, {...5}, {...6}, {...7}, {...8} ] 로 출력하십시오.',
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
    'morph-glm53flash',
    0.65,
    '당신은 수석 브랜드 BX 디자이너 AI 에이전트입니다. 인사말이나 마크다운 설명문을 절대 포함하지 말고, 첫 글자가 반드시 "[" 로 시작하고 "]" 로 끝나는 순수 JSON 배열만 출력하십시오. SVG 코드 내부를 "..." 으로 생략하는 행위를 엄격히 금지합니다. 전달받은 브랜드명({{brandName}})과 아이디어 프롬프트 문맥을 분석하여 해당 업종과 브랜드 정체성을 상징하는 독창적인 벡터 심볼 마크와 워드마크가 결합된 서로 다른 스타일의 SVG 로고를 디자인하십시오. 선호 스타일({{stylePreference}})에 3종 분할 생성 지시가 포함된 경우 지정된 3가지 스타일 객체를, 별도 지시가 없는 경우 6가지 스타일(내추럴 라인아트, 모던 지오메트릭, 클래식 엠블럼, 소프트 워터컬러 배지, 미니멀 아치, 에디토리얼 시그니처) 객체를 빠짐없이 출력하십시오. 각 객체의 svgCode는 반드시 한 줄(1-line)의 간결한 <svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg">로 작성하되, 좌측(x=4~38 영역)에는 핵심 모티프를 형상화한 2~3개의 도형·패스 태그(<rect>, <circle>, <path>, <polygon>)만 간결하게 배치하고, 우측에는 <text x="46" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#메인컬러">{{brandName}}</text> 태그로 확정된 브랜드명이 명확히 렌더링되도록 작성하십시오.',
    '확정 브랜드명: {{brandName}} / 아이디어 및 업종 문맥: {{promptText}} / 선호 스타일 및 생성 범위: {{stylePreference}}\n\n[출력 규칙] 생략 기호(...)나 불필요한 줄바꿈 없이, 요청된 스타일 수만큼 완전한 한 줄(compact) SVG 로고 객체를 담은 순수 JSON 배열만 출력하십시오. 각 원소는 {"styleName": "스타일명", "svgCode": "<svg viewBox=\"0 0 185 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect x=\"6\" y=\"6\" width=\"28\" height=\"28\" rx=\"8\" fill=\"#0f766e\"/><path d=\"M12 22 Q20 12 28 22\" stroke=\"#ffffff\" stroke-width=\"2.5\" fill=\"none\"/><text x=\"44\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-size=\"15\" font-weight=\"800\" fill=\"#0f172a\">{{brandName}}</text></svg>", "colorPalette": {"primary": "#HEX", "secondary": "#HEX", "accent": "#HEX", "surface": "#HEX", "radius": "14px"}, "typography": {"fontHeading": "Pretendard", "fontBody": "Pretendard"}, "tags": ["태그1", "태그2"]} 형식을 준수하십시오.',
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
    'morph-glm53flash',
    0.50,
    '당신은 수석 프로덕트 매니저(PM) AI 에이전트입니다. 인사말이나 부연 설명 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나는 순수 JSON 객체만 출력하십시오. 반환 JSON은 반드시 {"specMarkdown": "# {{brandName}} 서비스 기능명세서\n\n- **브랜드명**: {{brandName}}\n- **핵심 콘셉트**: ...\n- **타겟 고객 및 입지 특성**: ...\n- **주요 제공 가치 및 핵심 기능**: ...\n...", "screens": [{"screenKey": "SCR_HOME", "screenTitle": "화면 제목", "routePath": "/", "layoutType": "레이아웃코드", "description": "화면 설계 상세 설명", "components": ["컴포넌트코드1", "컴포넌트코드2"]}]} 형식을 준수해야 합니다. specMarkdown에는 절대 "# 상세 기능 명세서 마크다운 전문" 같은 예시 문구를 쓰지 말고 확정된 브랜드명({{brandName}})과 실제 비즈니스 도메인 내용을 중심으로 작성하십시오. 특히 screens 배열(5개 핵심 섹션)의 layoutType과 components는 프롬프트에 제공된 표준 사전 코드 목록 중에서 현재 브랜드의 실제 업종(예: 민박·숙박이면 HERO_BANNER_GRID, PRODUCT_CATALOG_GRID, GALLERY_SHOWCASE, MAGAZINE_LIST, RESERVATION_FORM, FAQ_CONCIERGE 및 HeroBanner, ProductGrid, PhotoGallery, MagazineSection, BookingForm, BrochureDownload, AiConciergeWidget 등)에 정확히 부합하는 항목만 자율 판단하여 선택하고, 현재 업종과 무관한 타 업종 전용 컴포넌트(예: 커피 전용 RoastProfileBadge, 말차 전용 MatchaRitualSteps, 데스크기어 전용 OrbitSetupShowcase, 러닝 전용 PaceCalculatorCard 등)는 해당 업종이 아닐 때 절대 선택하지 마십시오.',
    '확정 브랜드명: {{brandName}} / 사이트 유형: {{siteType}} / 상세 아이디어 프롬프트: {{promptText}}\n사용 가능한 레이아웃 코드 사전: {{availableLayouts}}\n사용 가능한 컴포넌트 코드 사전: {{availableComponents}}\n\n[출력 규칙] 인사말이나 부연 설명 없이 즉시 {"specMarkdown": "...", "screens": [...]} 형태의 순수 JSON 객체만 출력하십시오.',
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
    'morph-glm53flash',
    0.20,
    '당신은 Vue 3 및 TypeScript 빌드 디버깅 전문 에이전트입니다. 인사말이나 설명문 없이 첫 글자가 반드시 "[" 로 시작하고 "]" 로 끝나는 순수 JSON 배열만 출력하십시오. 각 객체는 반드시 {"path": "파일경로", "language": "vue 또는 json", "content": "전체 소스코드"} 형식을 준수해야 합니다.',
    '에러 로그: {{buildLog}} / 대상 파일: {{filesJson}}\n\n[출력 규칙] 인사말이나 부연 설명 없이 즉시 [{"path": ..., "language": ..., "content": ...}] 형태의 순수 JSON 배열만 출력하십시오.',
    '["eval(", "Function("]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
),
(
    'tpl-kkumz-005',
    'SITE_CONCIERGE_CHAT',
    '멀티사이트 AI 브랜드 컨시어지 상담 에이전트',
    'CONCIERGE',
    'morph-glm53flash',
    0.60,
    '당신은 브랜드 사이트 전용 AI 컨시어지입니다. 제공된 사이트 정보, 상품 목록, 매거진 아티클 데이터를 바탕으로 방문객의 질문에 친절하고 정확하게 한국어로 답변하십시오.',
    '사이트명: {{siteName}} / 슬로건: {{brandSlogan}} / 질문: {{question}}',
    '[]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
),
(
    'tpl-kkumz-006',
    'STUDIO_CHAT_REFINE',
    '스튜디오 대화형 테마·레이아웃·기능블록 리파인 에이전트',
    'CHAT_REFINE',
    'morph-glm53flash',
    0.40,
    '당신은 웹사이트 UI/UX 디자인 토큰, 디자인 아키타입, 레이아웃 스키마, 섹션 순서 및 업종 맞춤 기능 블록을 실시간 편집하는 에이전트입니다. 인사말이나 설명문 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나는 순수 JSON 객체만 반환하십시오. 변경이 필요한 필드만 포함하며 형식은 {"designTokens": {"primary": "#HEX", "secondary": "#HEX", "accent": "#HEX", "surface": "#HEX", "radius": "10px", "fontPreset": "MODERN_SANS|SERIF_EDITORIAL|TECH_MONO|BOLD_SANS", "surfaceMode": "LIGHT|WARM|DARK"}, "layoutSchema": {"themeArchetype": "EDITORIAL_BOTANICAL|DARK_TECH_SHOWCASE|MAGAZINE_HYBRID|COMMUNITY_KINETIC|SCENIC_HOSPITALITY", "heroTitle": "타이틀", "heroSubtitle": "서브타이틀", "ctaText": "버튼문구", "customBadge": "배지문구", "heroStyle": "FULL_SCENIC|SPLIT_VISUAL|EDITORIAL_STORY|PROMO_COMMERCE", "sectionOrder": ["CATALOG_GRID","EDITORIAL_FEED"]}, "addedFeatures": ["FEATURE_CODE"], "featureBlocks": {...}} 입니다.',
    '현재 토큰: {{tokensJson}} / 현재 레이아웃: {{layoutJson}} / 활성 기능: {{enabledFeaturesJson}} / 사용자 요청: {{instruction}}\n\n[출력 규칙] 인사말이나 부연 설명 없이 즉시 {"designTokens": {...}, "layoutSchema": {...}} 형태의 순수 JSON 객체만 출력하십시오.',
    '[]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
),
(
    'tpl-kkumz-007',
    'KKUMZ_INTENT_ADVISOR_V1',
    '프롬프트 기반 AI 사이트 아키텍처·업종·52종 맞춤 기능 진단 에이전트',
    'INTENT_ADVISOR',
    'morph-glm53flash',
    0.35,
    '당신은 엔터프라이즈 멀티사이트 아키텍트이자 프로덕트 전략가입니다. 사용자의 창업 아이디어 프롬프트와 업로드된 미디어 자산 목록, 그리고 DB에 등록된 52종 기능 카탈로그 코드 목록을 분석하여 최적의 사이트 아키텍처, 디자인 아키타입(themeArchetype), 서피스 명도(surfaceMode), 타이포 프리셋(fontPreset), 추천 기능 조합을 순수 JSON 객체로 반환하십시오. 인사말이나 마크다운 코드펜스 없이 반드시 첫 글자가 "{" 로 시작하고 "}" 로 끝나는 유효한 JSON 객체만 출력해야 합니다. [업종 오염 방지 원칙]: 순수 온라인 D2C 쇼핑몰에는 숙박·민박용 기능(SHOP_ROOM_SHOWCASE, SHOP_PRICING_MATRIX, CMS_SPACE_TOUR, CMS_LOCATION_DIRECTIONS)을 절대 추천하지 마십시오. 반대로 테크 하드웨어는 DARK_TECH_SHOWCASE + SHOP_SPEC_COMPARISON을, 티·공예·뷰티 D2C는 EDITORIAL_BOTANICAL을, 로스터리·라이프스타일 복합은 MAGAZINE_HYBRID를, 러닝·스포츠 크루는 COMMUNITY_KINETIC + CMS_SESSION_TIMETABLE을, 숙박·민박·스테이는 SCENIC_HOSPITALITY + SHOP_PRICING_MATRIX + CMS_SPACE_TOUR를 추천하십시오.',
    '프로젝트 제목: {{title}}\n사용자 입력 프롬프트: {{promptText}}\n업로드된 자산 요약: {{assetsSummary}}\n선택 가능한 전체 기능 카탈로그 코드 목록: {{catalogSummary}}\n\n[필수 JSON 출력 스키마]\n{\n  "siteType": "HYBRID 또는 COMMERCE 또는 CMS_SITE",\n  "siteTypeReason": "이 아키텍처 유형을 선택한 한국어 핵심 이유",\n  "category": "구체적인 업종·도메인 한글 라벨 (영문 DB 코드 금지)",\n  "themeArchetype": "EDITORIAL_BOTANICAL|DARK_TECH_SHOWCASE|MAGAZINE_HYBRID|COMMUNITY_KINETIC|SCENIC_HOSPITALITY",\n  "surfaceMode": "LIGHT|WARM|DARK",\n  "fontPreset": "MODERN_SANS|SERIF_EDITORIAL|TECH_MONO|BOLD_SANS",\n  "heroStyle": "FULL_SCENIC 또는 SPLIT_VISUAL 또는 EDITORIAL_STORY 또는 PROMO_COMMERCE",\n  "brandSlogan": "원문 복붙 없는 감성적이고 세련된 한국어 브랜드 슬로건 한 줄",\n  "expandedPrompt": "사용자의 아이디어를 공간·제품 경험, 주력 라인업, 결제·예약 방식, 핵심 고객층까지 구체화한 3~4문장의 상세 기획 프롬프트",\n  "tags": ["핵심태그1", "핵심태그2", "핵심태그3", "핵심태그4", "핵심태그5"],\n  "recommendedFeatures": ["업종에 정확히 부합하는 기능코드 6~10개"],\n  "featureReasons": {\n    "기능코드1": "이 업종에 해당 기능이 꼭 필요한 한국어 추천 사유 1줄"\n  },\n  "assetAnnotations": [\n    {"assetId": "자산ID", "assetType": "HERO|PRODUCT|GALLERY|ARTICLE", "caption": "업종 문맥에 어울리는 자연스러운 한국어 사진 설명 캡션"}\n  ]\n}',
    '[]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
),
(
    'tpl-kkumz-008',
    'KKUMZ_SITE_PROVISION_V1',
    '업종·선택기능 맞춤형 초기 사이트 콘텐츠 & SDUI 블록 데이터 생성 에이전트',
    'SITE_CONTENT_PROVISION',
    'morph-glm53flash',
    0.45,
    '당신은 브랜드 웹사이트 콘텐츠 디렉터이자 UX 아키텍트입니다. 브랜드명, 상세 프롬프트, 업종 카테고리, 활성화된 기능 코드 목록(enabledFeatures), 업로드된 사진 캡션 목록을 바탕으로 해당 업종에 100% 부합하는 실제 사이트 초기 데이터와 디자인 아키타입·섹션 순서·업종별 카피(copyOverrides)를 순수 JSON 객체로 생성하십시오. [엄격 규칙]: enabledFeatures에 포함되지 않은 이종 업종 블록(예: 쇼핑몰에 숙박 성수기 요금표·체크인 코스·바비큐 예약폼)은 절대 생성하지 마십시오. 인사말이나 코드펜스 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나야 하며, 프롬프트 원문("~만들고 싶어")이나 영문 시스템 코드(FOOD_BEVERAGE 등)를 절대 노출하지 마십시오.',
    '브랜드명: {{brandName}}\n업종 카테고리: {{category}}\n사이트 유형: {{siteType}}\n아이디어 프롬프트: {{promptText}}\n선택된 활성 기능 목록: {{enabledFeatures}}\n업로드 자산 목록: {{assetsSummary}}\n\n[필수 JSON 출력 스키마 — 첫 글자 "{" 부터 마지막 "}" 까지 순수 JSON만 간결하게 출력]\n{\n  "brandSlogan": "감성 브랜드 슬로건 1줄",\n  "customBadge": "상단 공식 배지 문구",\n  "themeArchetype": "EDITORIAL_BOTANICAL|DARK_TECH_SHOWCASE|MAGAZINE_HYBRID|COMMUNITY_KINETIC|SCENIC_HOSPITALITY",\n  "surfaceMode": "LIGHT|WARM|DARK",\n  "fontPreset": "MODERN_SANS|SERIF_EDITORIAL|TECH_MONO|BOLD_SANS",\n  "heroStyle": "FULL_SCENIC|SPLIT_VISUAL|EDITORIAL_STORY|PROMO_COMMERCE",\n  "heroTitle": "메인 히어로 헤드라인",\n  "heroSubtitle": "히어로 서브 카피 (원문 복붙 금지)",\n  "ctaText": "히어로 메인 버튼 문구",\n  "sectionOrder": ["CATALOG_GRID", "STEP_GUIDE", "EDITORIAL_FEED", "REVIEWS_POLL", "FAQ_LOCATION"],\n  "copyOverrides": {"navCatalogLabel": "스토어", "catalogKicker": "CURATED COLLECTION", "catalogTitle": "업종 맞춤 컬렉션 제목", "productCtaLabel": "바로 구매하기", "conciergePresets": ["업종 맞춤 질문 1", "업종 맞춤 질문 2", "업종 맞춤 질문 3"]},\n  "conciergeGreeting": "AI 컨시어지 첫 인사말",\n  "products": [\n    {"name": "업종 맞춤 상품/객실/메뉴명", "subTitle": "핵심 구성 및 매력 1줄 설명", "price": 38000, "promotionPrice": 34000, "unit": "캔/개/박/세트", "stock": 30, "productCategoryName": "분류명", "badge": "BEST"}\n  ],\n  "articles": [\n    {"title": "매거진 제목", "categoryName": "카테고리", "summary": "핵심 요약 1~2문장"}\n  ],\n  "cmsForm": {"title": "(예약/신청 기능이 활성일 때만 생성)", "description": "신청 안내 설명", "submitText": "신청하기", "fields": []},\n  "featureBlocks": {\n    "trustMetrics": [{"label": "지표명", "value": "수치", "sub": "설명"}],\n    "stepGuide": [{"step": "STEP 01", "time": "단계 라벨", "title": "업종 맞춤 단계 제목", "desc": "설명"}],\n    "pricingMatrix": "(SHOP_PRICING_MATRIX 활성 시에만 생성)",\n    "specComparison": "(SHOP_SPEC_COMPARISON 활성 시에만 생성)",\n    "subscriptionTiers": "(SHOP_SUBSCRIPTION_TIERS 활성 시에만 생성)",\n    "sessionTimetable": "(CMS_SESSION_TIMETABLE 활성 시에만 생성)",\n    "faqItems": [{"q": "업종 맞춤 질문", "a": "친절한 답변"}],\n    "reviews": [{"author": "실구매자 닉네임", "rating": 5, "tag": "핵심 태그", "date": "2026.11", "comment": "업종 맞춤 실제 후기"}]\n  }\n}',
    '[]'::jsonb,
    'ACTIVE',
    'admin',
    'admin'
);

-- 서비스 간 API 연동 액세스키 및 인바운드 인증키 마스터 시드
INSERT INTO kkumz_api_access_key (
    id, direction, service_code, key_name, base_url, access_key, secret_key,
    scopes_json, rate_limit_rpm, status, description, created_by
) VALUES
(
    'ak-kkumz-out-cms',
    'OUTBOUND',
    'SYNC_CMS',
    'SyncCMS 멀티사이트 콘텐츠·폼 API 연동키',
    'http://localhost:18085',
    'ak_cms_kkumz_live_01',
    'sk_cms_kkumz_secret_2026_prod_99',
    '["cms:site:provision", "cms:content:read", "cms:content:write", "cms:banner:read", "cms:form:submit"]'::jsonb,
    1200,
    'ACTIVE',
    'Kkumz에서 SyncCMS 멀티사이트(/api/v1/cms/multisite/**) 호출 시 사용하는 서비스 간 Access Key / Secret Key',
    'admin'
),
(
    'ak-kkumz-out-shop',
    'OUTBOUND',
    'SYNC_SHOP',
    'SyncShop 멀티사이트 커머스·주문 API 연동키',
    'http://localhost:8501',
    'ak_shop_kkumz_live_01',
    'sk_shop_kkumz_secret_2026_prod_88',
    '["shop:site:provision", "shop:product:read", "shop:product:write", "shop:order:write", "shop:promotion:read"]'::jsonb,
    1200,
    'ACTIVE',
    'Kkumz에서 SyncShop 멀티사이트 포털(/api/v1/shop/portal/site/**) 호출 시 사용하는 서비스 간 Access Key / Secret Key',
    'admin'
),
(
    'ak-kkumz-in-verse',
    'INBOUND',
    'SYNC_VERSE',
    'SyncVerse 중앙 오케스트레이터 인바운드 호출키',
    'http://localhost:18081',
    'ak_kkumz_verse_live_01',
    'sk_kkumz_verse_secret_2026_prod_77',
    '["kkumz:site:read", "kkumz:idea:read", "kkumz:build:trigger", "kkumz:mcp:invoke"]'::jsonb,
    600,
    'ACTIVE',
    'SyncVerse 오케스트레이터 및 타 에이전트가 Kkumz API를 호출할 때 사용하는 인바운드 인증키',
    'admin'
),
(
    'ak-kkumz-in-partner',
    'INBOUND',
    'EXTERNAL_PARTNER',
    '외부 파트너 & WebIDE SDK 연동 액세스키',
    'https://kkumz.empasy.com',
    'ak_kkumz_partner_live_02',
    'sk_kkumz_partner_secret_2026_prod_66',
    '["kkumz:site:runtime", "kkumz:showcase:read"]'::jsonb,
    300,
    'ACTIVE',
    '외부 파트너 및 멀티사이트 커스텀 번들 SDK에서 Kkumz 런타임 API를 호출할 때 사용하는 인바운드 인증키',
    'admin'
);

-- ============================================================================
-- SyncShop × SyncCMS × AI 52종 전체 기능 카탈로그 마스터 시드 (kkumz_feature_catalog)
-- ============================================================================
INSERT INTO kkumz_feature_catalog (
    id, feature_code, engine_code, category_code, category_name,
    title, subtitle, description, badge_label, icon_name,
    search_keywords_json, recommended_categories_json,
    sdui_archetype, default_config_json, sort_no, status
) VALUES
-- [1. COMMERCE_CATALOG : 상품·객실·메뉴 진열 (SyncShop 7종)]
(
    'fcat-kkumz-001', 'SHOP_PRODUCT_GRID', 'SYNC_SHOP', 'COMMERCE_CATALOG', '상품·객실·메뉴 진열',
    '시그니처 상품 카탈로그 그리드', '카테고리 필터 탭 · 할인가 · 원클릭 상세 모달',
    '브랜드의 대표 상품 라인업을 반응형 카드 그리드로 진열하고 카테고리별 필터링과 즉시 구매를 지원합니다.',
    'SyncShop 코어', 'ShoppingBag',
    '["상품", "카탈로그", "쇼핑몰", "판매", "진열", "스토어", "제품", "굿즈", "구매", "product", "shop", "catalog"]'::jsonb,
    '["COMMERCE", "HYBRID", "쇼핑", "식품", "리빙", "테크", "패션"]'::jsonb,
    'CATALOG_GRID', '{"kicker":"SIGNATURE SHOP CATALOG","sectionTitle":"시그니처 상품 카탈로그","unitLabel":"개","ctaLabel":"바로 구매·예약"}'::jsonb, 1, 'ACTIVE'
),
(
    'fcat-kkumz-002', 'SHOP_ROOM_SHOWCASE', 'SYNC_SHOP', 'COMMERCE_CATALOG', '상품·객실·메뉴 진열',
    '객실 · 독채 · 평상 타입별 쇼케이스', '기준/최대 인원 · 구비 시설 · 1박/대여 요금 및 잔여 객실 표시',
    '민박·펜션·스테이·공간대여 업종을 위해 독채, 황토방, 냇가 평상, 글램핑 등 공간 타입별 사진과 기준 인원, 요금을 한눈에 비교·예약합니다.',
    '숙박·공간 특화', 'Home',
    '["객실", "방", "숙소", "독채", "민박", "펜션", "평상", "스테이", "황토방", "온돌방", "한옥", "캠핑", "글램핑", "대관", "room", "stay"]'::jsonb,
    '["HYBRID", "숙박", "민박", "스테이", "펜션", "공간대여", "촌캉스"]'::jsonb,
    'CATALOG_GRID', '{"kicker":"PRIVATE ROOM & STAY SHOWCASE","sectionTitle":"객실 · 평상 · 스테이 안내","unitLabel":"박","ctaLabel":"이 객실 예약하기"}'::jsonb, 2, 'ACTIVE'
),
(
    'fcat-kkumz-003', 'SHOP_SEASONAL_MENU', 'SYNC_SHOP', 'COMMERCE_CATALOG', '상품·객실·메뉴 진열',
    '계절 별미 · F&B 시그니처 메뉴 보드', '대표 식사·음료·바비큐 세트 메뉴 사진과 가격, 원산지 스토리 안내',
    '능이백숙, 바비큐 한상, 로스터리 원두, 디저트 등 F&B 메뉴와 사전 예약 식사 세트를 구성하여 객실 예약 또는 단품 주문과 연동합니다.',
    'F&B·식음료', 'Utensils',
    '["메뉴", "음식", "식사", "백숙", "바비큐", "고기", "카페", "음료", "디저트", "조식", "석식", "먹거리", "menu", "food", "dining"]'::jsonb,
    '["HYBRID", "COMMERCE", "숙박", "민박", "카페", "로스터리", "식당", "F&B"]'::jsonb,
    'CATALOG_GRID', '{"kicker":"SEASONAL DINING & MENU","sectionTitle":"계절 별미 & 시그니처 메뉴","unitLabel":"세트","ctaLabel":"메뉴 사전 주문"}'::jsonb, 3, 'ACTIVE'
),
(
    'fcat-kkumz-004', 'SHOP_EXPERIENCE_TICKET', 'SYNC_SHOP', 'COMMERCE_CATALOG', '상품·객실·메뉴 진열',
    '원데이 클래스 · 로컬 체험 프로그램 티켓', '소요 시간 · 난이도 · 회차별 정원 및 체험 참가권 예약',
    '커핑 클래스, 다도 세션, 냇가 물놀이/눈썰매 체험, 공예 워크숍 등 시간제 체험 티켓을 판매하고 잔여 좌석을 관리합니다.',
    '체험·클래스', 'Ticket',
    '["체험", "클래스", "원데이", "티켓", "프로그램", "물놀이", "썰매", "불멍", "워크숍", "강습", "투어", "ticket", "class", "experience"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "클래스", "크루", "공방"]'::jsonb,
    'CATALOG_GRID', '{"kicker":"LOCAL EXPERIENCE & CLASS","sectionTitle":"체험 프로그램 & 클래스 티켓","unitLabel":"인","ctaLabel":"체험 신청하기"}'::jsonb, 4, 'ACTIVE'
),
(
    'fcat-kkumz-005', 'SHOP_CURATED_BUNDLE', 'SYNC_SHOP', 'COMMERCE_CATALOG', '상품·객실·메뉴 진열',
    '기프트 세트 & 올인원 패키지 기획전', '숙박+식사 패키지 또는 선물 보자기 세트 묶음 할인 구성',
    '독채 숙박+평상 백숙 올인원 패키지나 다완+말차 기프트 세트처럼 연관 상품을 하나로 묶어 특별 할인가로 제안합니다.',
    '패키지·번들', 'Gift',
    '["패키지", "세트", "번들", "기획전", "선물세트", "기프트", "올인원", "풀패키지", "보자기", "bundle", "package", "gift"]'::jsonb,
    '["COMMERCE", "HYBRID", "숙박", "민박", "식품", "기프트"]'::jsonb,
    'CATALOG_GRID', '{"kicker":"CURATED PACKAGE & GIFT SET","sectionTitle":"올인원 패키지 & 기프트 큐레이션","unitLabel":"패키지","ctaLabel":"패키지 선택"}'::jsonb, 5, 'ACTIVE'
),
(
    'fcat-kkumz-006', 'SHOP_MULTI_OPTION_SKU', 'SYNC_SHOP', 'COMMERCE_CATALOG', '상품·객실·메뉴 진열',
    '다중 옵션 · 인원 · 숙박일수 SKU 선택기', '인원 추가, 숯불 그릴, 조식, 각인 등 세부 옵션별 자동 금액 합산',
    '상품 구매나 객실 예약 시 숙박 일수, 성인/아동 인원, 바비큐 숯불 추가, 포장 옵션 등을 선택하면 실시간으로 합계 금액이 계산됩니다.',
    '옵션 계산기', 'Sliders',
    '["옵션", "SKU", "인원추가", "숙박일수", "숯불", "그릴", "추가요금", "선택옵션", "견적계산", "option", "sku"]'::jsonb,
    '["COMMERCE", "HYBRID", "숙박", "민박", "테크", "커스텀"]'::jsonb,
    'CATALOG_SPEC', '{"kicker":"CUSTOM OPTION & SKU CALCULATOR","sectionTitle":"맞춤 옵션 & 추가 인원 요금 시뮬레이터"}'::jsonb, 6, 'ACTIVE'
),
(
    'fcat-kkumz-007', 'SHOP_DIGITAL_PASS', 'SYNC_SHOP', 'COMMERCE_CATALOG', '상품·객실·메뉴 진열',
    '시즌권 · 다회 이용권 · 멤버십 패스', '정기 방문객을 위한 시즌 패스 및 선불 바우처 티켓 판매',
    '단골 방문객이나 크루 회원을 위한 5회 이용권, 계절 시즌 패스, 기프트 바우처를 발행하고 모바일로 즉시 전달합니다.',
    '시즌패스', 'CreditCard',
    '["시즌권", "이용권", "패스", "바우처", "상품권", "멤버십", "정기권", "선불권", "pass", "voucher"]'::jsonb,
    '["COMMERCE", "HYBRID", "크루", "피트니스", "숙박", "카페"]'::jsonb,
    'CATALOG_GRID', '{"kicker":"SEASON PASS & VOUCHER","sectionTitle":"시즌 이용권 & 기프트 바우처","unitLabel":"권","ctaLabel":"패스 구매"}'::jsonb, 7, 'ACTIVE'
),

-- [2. ORDER_PRICING : 요금표·예약결제·할인 (SyncShop 8종)]
(
    'fcat-kkumz-008', 'SHOP_PRICING_MATRIX', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '시즌 · 요일 · 인원별 상세 요금표', '비수기 / 준성수기 / 성수기 · 주중 / 주말 · 기준인원 및 추가요금 비교 테이블',
    '민박·펜션·공간대여·스튜디오에서 가장 중요한 주중/주말/성수기 요금 체계와 인원 추가, 평상 대여, 바비큐 비용을 투명한 표로 안내합니다.',
    '필수 요금표', 'Table',
    '["요금표", "가격표", "성수기", "비수기", "주말요금", "평일요금", "숙박비", "대여료", "이용요금", "단가표", "pricing", "rate", "matrix"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "펜션", "공간대여", "클래스", "세탁", "서비스"]'::jsonb,
    'MATRIX_TABLE', '{"kicker":"TRANSPARENT RATE & PRICING TABLE","sectionTitle":"시즌 · 요일별 상세 이용 요금표"}'::jsonb, 8, 'ACTIVE'
),
(
    'fcat-kkumz-009', 'SHOP_SPEC_COMPARISON', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '상품 스펙 · 객실 구비시설 상세 비교표', '모델별 사양 또는 객실별 면적·수용인원·편의시설 1:1 비교 매트릭스',
    '데스크 기어의 소재·출력·호환성 비교나 민박 객실별 평수·화장실·취사도구·전용 평상 유무를 한눈에 대조할 수 있는 비교표입니다.',
    '스펙·시설 비교', 'Columns',
    '["스펙", "비교표", "사양", "구비시설", "편의시설", "어메니티", "객실비교", "모델비교", "호환성", "spec", "comparison"]'::jsonb,
    '["COMMERCE", "HYBRID", "테크", "하드웨어", "숙박", "민박"]'::jsonb,
    'MATRIX_TABLE', '{"kicker":"DETAILED SPEC & AMENITY MATRIX","sectionTitle":"타입별 상세 사양 · 구비시설 비교"}'::jsonb, 9, 'ACTIVE'
),
(
    'fcat-kkumz-010', 'SHOP_SUBSCRIPTION_TIERS', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '정기구독 · 멤버십 플랜 티어 가격표', '월간/분기별 큐레이션 구독 박스와 등급별 혜택 비교 카드',
    '매월 로스팅 원두 정기배송, 제철 농산물 꾸러미 구독, VIP 스테이 멤버십 등 3단계 플랜 카드를 구성합니다.',
    '정기구독', 'Repeat',
    '["구독", "정기배송", "멤버십", "플랜", "티어", "월간구독", "정기결제", "꾸러미", "subscription", "plan", "tier"]'::jsonb,
    '["COMMERCE", "HYBRID", "커피", "식품", "라이프스타일", "SaaS"]'::jsonb,
    'PLAN_TIERS', '{"kicker":"SUBSCRIPTION & MEMBERSHIP PLANS","sectionTitle":"정기구독 & 멤버십 플랜 안내"}'::jsonb, 10, 'ACTIVE'
),
(
    'fcat-kkumz-011', 'SHOP_INSTANT_CHECKOUT', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '원클릭 즉시 구매 · 실시간 예약 결제', '페이지 이탈 없이 수령인·연락처·일정을 입력해 바로 주문/예약을 확정',
    '복잡한 회원가입 없이 고객이 선택한 객실·상품·메뉴를 SyncShop 주문 원장(oms_order)에 실시간으로 접수하고 예약 번호를 발급합니다.',
    'SyncShop 결제', 'Zap',
    '["결제", "즉시구매", "바로예약", "주문", "체크아웃", "원클릭", "예약결제", "무통장", "간편결제", "checkout", "order"]'::jsonb,
    '["COMMERCE", "HYBRID", "숙박", "민박", "쇼핑", "F&B"]'::jsonb,
    'CHECKOUT_FLOW', '{"kicker":"INSTANT ORDER & RESERVATION","sectionTitle":"원클릭 실시간 주문 · 예약 결제"}'::jsonb, 11, 'ACTIVE'
),
(
    'fcat-kkumz-012', 'SHOP_SLIDE_CART', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '슬라이드 장바구니 & 합산 견적 서랍', '객실+식사+체험 등 여러 상품을 담아 총액과 할인을 자동 계산하는 드로어',
    '독채 숙박권과 평상 백숙 세트, 숯불 옵션을 한 장바구니에 담아 총 견적을 확인하고 일괄 결제할 수 있는 슬라이드 장바구니입니다.',
    '장바구니', 'ShoppingCart',
    '["장바구니", "카트", "드로어", "묶음주문", "합산견적", "담기", "cart", "basket", "drawer"]'::jsonb,
    '["COMMERCE", "HYBRID", "쇼핑", "숙박", "민박"]'::jsonb,
    'CHECKOUT_FLOW', '{"kicker":"SLIDE CART & ESTIMATE DRAWER","sectionTitle":"슬라이드 장바구니 & 합산 견적"}'::jsonb, 12, 'ACTIVE'
),
(
    'fcat-kkumz-013', 'SHOP_MULTI_NIGHT_DISCOUNT', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '연박 할인 · 다량 구매 프로모션 혜택표', '2박 이상 연박 할인, 평일 방문 우대, 단체/다량 주문 자동 할인율 안내',
    '2박 연박 시 15% 할인, 주중(일~목) 스테이 특가, 3세트 이상 구매 시 추가 혜택 등 객단가를 높이는 볼륨 할인 정책을 시각화합니다.',
    '연박·다량할인', 'Percent',
    '["연박", "연박할인", "장박", "다량구매", "단체할인", "평일할인", "볼륨할인", "우대혜택", "discount", "multi-night"]'::jsonb,
    '["HYBRID", "COMMERCE", "숙박", "민박", "펜션", "도매"]'::jsonb,
    'PRICING_CALLOUT', '{"kicker":"MULTI-NIGHT & VOLUME BENEFIT","sectionTitle":"연박 우대 & 다량 구매 특별 할인 혜택"}'::jsonb, 13, 'ACTIVE'
),
(
    'fcat-kkumz-014', 'SHOP_SHIPPING_PICKUP_GUIDE', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '배송 · 당일출고 · 현장수령 안내 박스', '택배 출고 마감 시간, 신선 포장 원칙 및 현장 체크인/픽업 절차 안내',
    'D2C 상품의 산지 직송·보냉 포장 기준이나 민박/매장 방문 시 현장 수령·바비큐 세팅 절차를 명확히 안내합니다.',
    '배송·픽업', 'Truck',
    '["배송", "택배", "당일출고", "산지직송", "현장수령", "픽업", "포장", "무료배송", "shipping", "pickup", "delivery"]'::jsonb,
    '["COMMERCE", "HYBRID", "식품", "테크", "숙박", "로컬"]'::jsonb,
    'POLICY_BOX', '{"kicker":"FULFILLMENT & ON-SITE PICKUP","sectionTitle":"배송 · 현장 체크인 · 픽업 안내"}'::jsonb, 14, 'ACTIVE'
),
(
    'fcat-kkumz-015', 'SHOP_LIVE_STOCK_BADGE', 'SYNC_SHOP', 'ORDER_PRICING', '요금표·예약결제·할인',
    '실시간 잔여 객실 · 한정 수량 상태 바', '이번 주말 잔여 객실 수와 당일 준비 가능한 한정 메뉴 수량을 실시간 표시',
    '이번 주 토요일 잔여 1실, 오늘 능이백숙 준비 가능 수량 5세트 등 실시간 재고/예약 현황을 배지로 보여주어 예약 전환율을 높입니다.',
    '실시간 잔여수량', 'Activity',
    '["잔여객실", "재고", "한정수량", "마감임박", "예약현황", "빈방", "실시간재고", "stock", "availability"]'::jsonb,
    '["HYBRID", "COMMERCE", "숙박", "민박", "클래스", "한정판"]'::jsonb,
    'METRIC_STRIP', '{"kicker":"LIVE AVAILABILITY STATUS","sectionTitle":"실시간 예약 가능 잔여 현황"}'::jsonb, 15, 'ACTIVE'
),

-- [3. PROMO_MARKETING : 쿠폰·타임특가·리뷰 (SyncShop 7종)]
(
    'fcat-kkumz-016', 'SHOP_WELCOME_COUPON', 'SYNC_SHOP', 'PROMO_MARKETING', '쿠폰·타임특가·리뷰',
    '첫 방문 · 첫 예약 웰컴 혜택 쿠폰바', '사이트 상단 및 히어로 영역에 즉시 적용 가능한 웰컴 할인 혜택 강조',
    '공식 사이트를 통해 직접 예약하거나 첫 구매하는 고객에게 수수료 없는 직예약 특별 할인/서비스 혜택을 배너로 안내합니다.',
    '직예약 혜택', 'Sparkles',
    '["웰컴쿠폰", "첫구매", "첫방문", "직예약혜택", "할인배너", "프로모션바", "welcome", "coupon"]'::jsonb,
    '["COMMERCE", "HYBRID", "숙박", "민박", "쇼핑"]'::jsonb,
    'COUPON_PACK', '{"kicker":"DIRECT BOOKING WELCOME BENEFIT","sectionTitle":"공식 사이트 직예약 · 첫 방문 웰컴 혜택"}'::jsonb, 16, 'ACTIVE'
),
(
    'fcat-kkumz-017', 'SHOP_COUPON_PACK', 'SYNC_SHOP', 'PROMO_MARKETING', '쿠폰·타임특가·리뷰',
    '다운로드 쿠폰팩 & 시즌 바우처 지갑', 'SyncShop 쿠폰 원장(sms_coupon)과 연동된 원클릭 할인 쿠폰 다운로드',
    '비수기 평일 숙박 2만원 할인권, 평상 백숙 세트 1만원 쿠폰, 재방문 감사 쿠폰 등을 방문객이 원클릭으로 다운로드해 결제 시 사용합니다.',
    'SyncShop 쿠폰', 'TicketCheck',
    '["쿠폰팩", "할인쿠폰", "쿠폰다운로드", "바우처", "프로모션코드", "시즌쿠폰", "coupon", "voucher"]'::jsonb,
    '["COMMERCE", "HYBRID", "숙박", "민박", "쇼핑", "F&B"]'::jsonb,
    'COUPON_PACK', '{"kicker":"OFFICIAL COUPON PACK","sectionTitle":"원클릭 다운로드 혜택 쿠폰팩"}'::jsonb, 17, 'ACTIVE'
),
(
    'fcat-kkumz-018', 'SHOP_FLASH_DEAL', 'SYNC_SHOP', 'PROMO_MARKETING', '쿠폰·타임특가·리뷰',
    '타임특가 플래시딜 & 얼리버드 카운트다운', '시즌 오픈 얼리버드 특가 및 이번 주 잔여 객실/상품 타임세일 타이머',
    '여름 계곡 시즌 얼리버드 예약 특가나 이번 주 금요일 마지막 독채 땡처리 특가를 실시간 카운트다운 타이머와 함께 노출합니다.',
    '타임특가', 'Clock',
    '["타임특가", "플래시딜", "얼리버드", "카운트다운", "특가", "세일", "한정특가", "땡처리", "flash", "deal", "earlybird"]'::jsonb,
    '["COMMERCE", "HYBRID", "숙박", "민박", "쇼핑"]'::jsonb,
    'PROMO_BANNER', '{"kicker":"LIMITED TIME EARLY-BIRD DEAL","sectionTitle":"시즌 얼리버드 & 타임특가 프로모션"}'::jsonb, 18, 'ACTIVE'
),
(
    'fcat-kkumz-019', 'SHOP_NEW_DROP_SHOWCASE', 'SYNC_SHOP', 'PROMO_MARKETING', '쿠폰·타임특가·리뷰',
    '시즌 한정 드롭 & 신규 프로그램 런칭 배너', '계절별 신규 오픈 공간·메뉴·컬렉션 집중 조명 쇼케이스',
    '겨울 온돌방+눈썰매 패키지 오픈, 봄 햇차 첫 수확 드롭 등 계절마다 새롭게 선보이는 주력 기획 상품을 하이라이트합니다.',
    '시즌 드롭', 'Flame',
    '["신상품", "시즌한정", "드롭", "런칭", "신규오픈", "계절특선", "new", "drop", "launch"]'::jsonb,
    '["COMMERCE", "HYBRID", "패션", "테크", "숙박", "F&B"]'::jsonb,
    'PROMO_BANNER', '{"kicker":"SEASONAL NEW RELEASE","sectionTitle":"이번 시즌 신규 오픈 & 스페셜 에디션"}'::jsonb, 19, 'ACTIVE'
),
(
    'fcat-kkumz-020', 'SHOP_BEST_RANKING', 'SYNC_SHOP', 'PROMO_MARKETING', '쿠폰·타임특가·리뷰',
    '실시간 베스트셀러 & 인기 예약 랭킹 보드', '방문객이 가장 많이 선택한 인기 객실·세트·상품 TOP 순위 큐레이션',
    '처음 방문한 고객이 고민 없이 선택할 수 있도록 가장 예약률이 높은 대표 객실/평상 패키지나 베스트셀러 순위를 배지와 함께 보여줍니다.',
    '인기 랭킹', 'Award',
    '["베스트", "인기", "랭킹", "순위", "베스트셀러", "인기객실", "추천상품", "best", "ranking", "top"]'::jsonb,
    '["COMMERCE", "HYBRID", "숙박", "민박", "쇼핑"]'::jsonb,
    'CATALOG_GRID', '{"kicker":"MOST LOVED RANKING","sectionTitle":"방문객 선호도 베스트 TOP 컬렉션"}'::jsonb, 20, 'ACTIVE'
),
(
    'fcat-kkumz-021', 'SHOP_PREORDER_GAUGE', 'SYNC_SHOP', 'PROMO_MARKETING', '쿠폰·타임특가·리뷰',
    '프리오더 · 시즌 사전예약 달성률 게이지', '목표 수량/예약 달성률 바 및 서포터 참여 현황 실시간 시각화',
    '크라우드펀딩형 프리오더나 성수기 평상 사전예약 오픈 시 현재 달성률(%)과 참여 인원을 게이지 바로 생생하게 보여줍니다.',
    '프리오더·펀딩', 'TrendingUp',
    '["프리오더", "사전예약", "펀딩", "공동구매", "달성률", "게이지", "얼리버드예약", "preorder", "crowdfunding"]'::jsonb,
    '["COMMERCE", "HYBRID", "테크", "식품", "숙박"]'::jsonb,
    'METRIC_STRIP', '{"kicker":"PRE-ORDER & SEASON BOOKING STATUS","sectionTitle":"시즌 사전예약 · 프리오더 진행 현황"}'::jsonb, 21, 'ACTIVE'
),
(
    'fcat-kkumz-022', 'SHOP_PHOTO_REVIEWS', 'SYNC_SHOP', 'PROMO_MARKETING', '쿠폰·타임특가·리뷰',
    '실구매자 · 방문객 베스트 포토 리뷰 월', '별점 5.0 · 실제 이용 객실/상품 태그 · 생생한 방문 후기 카드 그리드',
    '가족 모임, 커플 스테이, 물놀이 방문객들이 남긴 실제 사진과 정성스러운 후기를 별점·이용 타입과 함께 전시하여 강력한 신뢰를 형성합니다.',
    '베스트 후기', 'Star',
    '["리뷰", "후기", "포토리뷰", "방문후기", "숙박후기", "별점", "고객후기", "추천후기", "review", "testimonial"]'::jsonb,
    '["COMMERCE", "HYBRID", "CMS_SITE", "숙박", "민박", "쇼핑", "클래스"]'::jsonb,
    'REVIEW_WALL', '{"kicker":"VERIFIED GUEST & BUYER REVIEWS","sectionTitle":"실제 방문객 · 구매자 베스트 포토 후기"}'::jsonb, 22, 'ACTIVE'
),

-- [4. VISUAL_MEDIA : 배너·포토·공간투어 (SyncCMS 7종)]
(
    'fcat-kkumz-023', 'CMS_HERO_SHOWCASE', 'SYNC_CMS', 'VISUAL_MEDIA', '배너·포토·공간투어',
    '시네마틱 브랜드 히어로 배너', '4가지 무드(풀스크린/스플릿/에디토리얼/커머스) 전환 및 대표 사진·슬로건 연출',
    '업로드한 대표 전경 사진과 AI가 다듬은 감성 카피를 결합하여 첫 화면에서 브랜드의 공간감과 정체성을 강렬하게 전달합니다.',
    'SyncCMS 코어', 'Image',
    '["히어로", "메인배너", "대표배너", "타이틀배너", "첫화면", "전경사진", "커버배너", "hero", "banner"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "전체"]'::jsonb,
    'HERO_BANNER', '{"kicker":"OFFICIAL BRAND SHOWCASE","sectionTitle":"메인 브랜드 히어로 배너"}'::jsonb, 23, 'ACTIVE'
),
(
    'fcat-kkumz-024', 'CMS_PHOTO_GALLERY', 'SYNC_CMS', 'VISUAL_MEDIA', '배너·포토·공간투어',
    '고해상도 현장 포토 갤러리 & 라이트박스', '업로드 사진 전체 보기 · AI 한글 캡션 · 클릭 시 고해상도 확대 뷰어',
    '업로드한 10장 이상의 현장 사진(계곡, 평상, 객실, 마당, 사계절 풍경 등)을 감각적인 메이슨리 그리드와 전체화면 라이트박스로 보여줍니다.',
    '포토 아카이브', 'Camera',
    '["갤러리", "포토갤러리", "사진", "현장사진", "공간사진", "풍경사진", "전경", "앨범", "라이트박스", "gallery", "photo"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "숙박", "민박", "크루", "카페"]'::jsonb,
    'MEDIA_GALLERY', '{"kicker":"SPACE & SEASON PHOTO ARCHIVE","sectionTitle":"현장 포토 갤러리 아카이브"}'::jsonb, 24, 'ACTIVE'
),
(
    'fcat-kkumz-025', 'CMS_SPACE_TOUR', 'SYNC_CMS', 'VISUAL_MEDIA', '배너·포토·공간투어',
    '공간 구역별 파노라마 스페이스 투어', '실내 독채 · 냇가 평상존 · 바비큐 마당 · 계곡 물놀이장 등 구역별 상세 소개',
    '단순 사진 나열을 넘어 민박이나 매장의 주요 구역(Zone 1~4)을 탭과 하이라이트 포인트로 나누어 동선과 공간 매력을 입체적으로 안내합니다.',
    '공간 투어', 'Compass',
    '["공간투어", "스페이스투어", "구역안내", "시설안내", "마당", "정자", "평상존", "바비큐장", "실내외", "층별안내", "space", "tour", "zone"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "펜션", "공간대여", "쇼룸", "카페"]'::jsonb,
    'SPACE_TOUR', '{"kicker":"ZONE-BY-ZONE SPACE TOUR","sectionTitle":"공간 구역별 상세 스페이스 투어"}'::jsonb, 25, 'ACTIVE'
),
(
    'fcat-kkumz-026', 'CMS_BEFORE_AFTER', 'SYNC_CMS', 'VISUAL_MEDIA', '배너·포토·공간투어',
    '계절 풍경 · 공간 변화 비포 & 애프터 쇼케이스', '여름 초록 계곡 vs 겨울 설경 썰매장, 또는 낮과 밤의 무드 비교 뷰',
    '여름철 시원한 냇가 물놀이와 가을 단풍, 겨울 눈썰매 풍경처럼 계절/시간대별 공간의 반전 매력을 나란히 비교해 보여줍니다.',
    '계절·낮밤 비교', 'Sun',
    '["비포애프터", "사계절", "여름겨울", "낮과밤", "풍경비교", "리모델링", "계절비교", "before", "after", "season"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "인테리어", "뷰티"]'::jsonb,
    'BEFORE_AFTER', '{"kicker":"SEASONAL & DAY-NIGHT CONTRAST","sectionTitle":"사계절 · 낮과 밤의 공간 무드 비교"}'::jsonb, 26, 'ACTIVE'
),
(
    'fcat-kkumz-027', 'CMS_VIDEO_SHOWCASE', 'SYNC_CMS', 'VISUAL_MEDIA', '배너·포토·공간투어',
    '브랜드 필름 & 현장 무드 비디오 쇼케이스', '물 흐르는 소리, 장작 타는 풍경, 제품 제작 과정을 담은 시네마틱 영상 섹션',
    '시골 냇가의 맑은 물소리와 평상 휴식 분위기, 혹은 장인의 제작 공정 영상을 하이라이트 스토리와 함께 연출합니다.',
    '브랜드 필름', 'Video',
    '["비디오", "영상", "유튜브", "브랜드필름", "홍보영상", "릴스", "쇼츠", "ASMR", "video", "film"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "숙박", "크루", "테크"]'::jsonb,
    'VIDEO_SHOWCASE', '{"kicker":"BRAND FILM & AMBIENCE","sectionTitle":"브랜드 무드 필름 & 현장 스케치"}'::jsonb, 27, 'ACTIVE'
),
(
    'fcat-kkumz-028', 'CMS_DOC_ARCHIVE', 'SYNC_CMS', 'VISUAL_MEDIA', '배너·포토·공간투어',
    '공식 안내문 · 요금표 · 브로슈어 자료실', '업로드된 PDF/DOCX/CSV 요금표, 약도, 단체 행사 안내서 원클릭 다운로드',
    '단체 총무나 가족 대표가 카카오톡으로 공유하기 좋은 공식 요금표, 이용 안내문, 주변 관광 약도 파일을 즉시 다운로드할 수 있게 제공합니다.',
    '문서 자료실', 'FileText',
    '["자료실", "안내문", "요금표다운로드", "브로슈어", "카탈로그PDF", "첨부파일", "문서", "약도", "doc", "brochure", "pdf"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "숙박", "민박", "B2B", "교육"]'::jsonb,
    'DOC_ARCHIVE', '{"kicker":"OFFICIAL BROCHURE & DOCUMENTS","sectionTitle":"공식 안내문 · 요금표 다운로드 자료실"}'::jsonb, 28, 'ACTIVE'
),
(
    'fcat-kkumz-029', 'CMS_SNS_LOOKBOOK', 'SYNC_CMS', 'VISUAL_MEDIA', '배너·포토·공간투어',
    '인스타그램 감성 룩북 & 해시태그 포토월', '방문객 인증샷 피드 및 브랜드 공식 해시태그 큐레이션 그리드',
    '#공주민박 #냇가평상 #촌캉스 등 핵심 해시태그와 함께 감성적인 정방형 포토 피드를 구성하여 바이럴 공유를 유도합니다.',
    'SNS 룩북', 'Instagram',
    '["인스타그램", "SNS", "룩북", "포토월", "인증샷", "해시태그", "피드", "감성샷", "lookbook", "sns"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "숙박", "패션", "카페", "크루"]'::jsonb,
    'MEDIA_GALLERY', '{"kicker":"SOCIAL LOOKBOOK & HASHTAG WALL","sectionTitle":"해시태그 스냅 & 소셜 포토월"}'::jsonb, 29, 'ACTIVE'
),

-- [5. CONTENT_EDITORIAL : 매거진·연혁·이용가이드 (SyncCMS 6종)]
(
    'fcat-kkumz-030', 'CMS_BRAND_MAGAZINE', 'SYNC_CMS', 'CONTENT_EDITORIAL', '매거진·연혁·이용가이드',
    '브랜드 매거진 & 로컬 스토리 저널', 'SyncCMS 아티클(cms_content) 기반 브랜드 철학 · 계절 소식 · 로컬 칼럼 발행',
    '단순 홍보를 넘어 계절별 시골 풍경 소식, 산지 인터뷰, 공간 비하인드 스토리를 매거진 아티클로 발행하고 모달 리더기로 열람합니다.',
    'SyncCMS 저널', 'BookOpen',
    '["매거진", "저널", "아티클", "블로그", "스토리", "칼럼", "에디토리얼", "소식지", "콘텐츠", "magazine", "journal", "article"]'::jsonb,
    '["CMS_SITE", "HYBRID", "숙박", "민박", "커피", "크루", "브랜드"]'::jsonb,
    'EDITORIAL_FEED', '{"kicker":"EDITORIAL ARCHIVE & JOURNAL","sectionTitle":"브랜드 매거진 & 계절 스토리"}'::jsonb, 30, 'ACTIVE'
),
(
    'fcat-kkumz-031', 'CMS_FOUNDER_INTERVIEW', 'SYNC_CMS', 'CONTENT_EDITORIAL', '매거진·연혁·이용가이드',
    '호스트 · 창업가 철학 인터뷰 & 인사말', '공간을 가꾸는 호스트의 진심 어린 환영 인사와 운영 철학 스토리텔링',
    '공주 냇가 옆에서 오랜 시간 민박을 가꿔온 호스트 부부의 따뜻한 인사말이나 브랜드 디렉터의 철학을 인터뷰 형식으로 전달합니다.',
    '호스트 스토리', 'Quote',
    '["호스트", "창업가", "인터뷰", "인사말", "대표인사", "브랜드철학", "주인장", "이야기", "founder", "host", "story"]'::jsonb,
    '["CMS_SITE", "HYBRID", "숙박", "민박", "로컬", "장인"]'::jsonb,
    'EDITORIAL_STORY', '{"kicker":"HOST PHILOSOPHY & GREETING","sectionTitle":"호스트가 전하는 공간 이야기"}'::jsonb, 31, 'ACTIVE'
),
(
    'fcat-kkumz-032', 'CMS_BRAND_TIMELINE', 'SYNC_CMS', 'CONTENT_EDITORIAL', '매거진·연혁·이용가이드',
    '브랜드 히스토리 & 사계절 운영 타임라인', '공간의 탄생부터 사계절(봄·여름·가을·겨울) 주요 즐길 거리 연대기',
    '봄 산나물과 벚꽃길, 여름 맑은 냇가 물놀이와 평상 백숙, 가을 단풍 정자, 겨울 눈썰매와 뜨끈한 황토방까지 사계절의 매력을 타임라인으로 풀어냅니다.',
    '연혁·사계절', 'Milestone',
    '["연혁", "타임라인", "히스토리", "사계절", "역사", "발자취", "계절별", "연대기", "timeline", "history"]'::jsonb,
    '["CMS_SITE", "HYBRID", "숙박", "민박", "기업", "브랜드"]'::jsonb,
    'TIMELINE_STEPS', '{"kicker":"FOUR SEASONS & HERITAGE TIMELINE","sectionTitle":"사계절 풍경 & 브랜드 히스토리"}'::jsonb, 32, 'ACTIVE'
),
(
    'fcat-kkumz-033', 'CMS_STEP_RITUAL_GUIDE', 'SYNC_CMS', 'CONTENT_EDITORIAL', '매거진·연혁·이용가이드',
    '단계별 1박 2일 힐링 코스 · 이용 리추얼 가이드', 'STEP 01 체크인부터 냇가 평상 휴식, 바비큐 만찬, 아침 산책까지 추천 동선',
    '방문객이 머무는 동안 가장 완벽한 시간을 보낼 수 있도록 시간대별 추천 힐링 코스나 제품 음용/사용 리추얼을 4단계 카드로 안내합니다.',
    '추천 이용코스', 'Footprints',
    '["이용가이드", "추천코스", "1박2일", "동선", "리추얼", "이용방법", "단계별", "즐기는법", "매뉴얼", "guide", "ritual", "step"]'::jsonb,
    '["HYBRID", "CMS_SITE", "COMMERCE", "숙박", "민박", "다도", "커피", "크루"]'::jsonb,
    'TIMELINE_STEPS', '{"kicker":"CURATED EXPERIENCE GUIDE","sectionTitle":"100% 즐기는 단계별 추천 이용 가이드"}'::jsonb, 33, 'ACTIVE'
),
(
    'fcat-kkumz-034', 'CMS_HOST_TEAM_PROFILE', 'SYNC_CMS', 'CONTENT_EDITORIAL', '매거진·연혁·이용가이드',
    '호스트 · 셰프 · 페이서 프로필 소개 카드', '공간과 프로그램을 운영하는 호스트·매니저·전문가 프로필 및 담당 역할',
    '민박 호스트, 로스터리 헤드 바리스타, 러닝 크루 페이서 등 고객을 맞이하는 사람들의 전문성과 따뜻한 프로필을 소개합니다.',
    '호스트·팀 소개', 'Users',
    '["프로필", "호스트소개", "팀소개", "강사소개", "셰프", "바리스타", "페이서", "운영진", "team", "profile", "host"]'::jsonb,
    '["CMS_SITE", "HYBRID", "숙박", "크루", "클래스", "에이전시"]'::jsonb,
    'TEAM_PROFILES', '{"kicker":"MEET OUR HOSTS & CURATORS","sectionTitle":"함께하는 호스트 & 큐레이터 프로필"}'::jsonb, 34, 'ACTIVE'
),
(
    'fcat-kkumz-035', 'CMS_NOTICE_BOARD', 'SYNC_CMS', 'CONTENT_EDITORIAL', '매거진·연혁·이용가이드',
    '시즌 공지사항 & 현장 새소식 알림 보드', '성수기 예약 오픈일, 냇가 수위·날씨 소식, 휴무일 등 중요 공지 게시판',
    '여름 성수기 평상 예약 오픈 안내, 최근 강수량에 따른 냇가 물놀이 가능 여부, 명절 연휴 운영 안내 등 최신 소식을 공지합니다.',
    '공지·새소식', 'Bell',
    '["공지사항", "공지", "새소식", "뉴스", "안내판", "게시판", "오픈공지", "날씨안내", "notice", "news", "board"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "숙박", "민박", "크루"]'::jsonb,
    'BOARD_THREAD', '{"kicker":"OFFICIAL NOTICE & UPDATES","sectionTitle":"시즌 공지사항 & 현장 새소식"}'::jsonb, 35, 'ACTIVE'
),

-- [6. FORM_SCHEDULE : 맞춤예약폼·일정·설문 (SyncCMS 5종)]
(
    'fcat-kkumz-036', 'CMS_DYNAMIC_BOOKING_FORM', 'SYNC_CMS', 'FORM_SCHEDULE', '맞춤예약폼·일정·설문',
    '업종 맞춤형 동적 예약 · 참가 신청 폼', '체크인 날짜(date) · 객실/옵션(select) · 인원수(number) · 부가옵션(checkbox) 동적 구성',
    'SyncCMS 폼 엔진(cms_form)과 연동되어 업종에 꼭 맞는 입력 필드(예: 방문 희망일, 독채/평상 선택, 성인·아동 인원, 바비큐·백숙 사전예약 여부)를 동적으로 렌더링하고 접수합니다.',
    'SyncCMS 동적폼', 'CalendarCheck',
    '["예약폼", "신청폼", "온라인예약", "예약신청", "체크인", "인원선택", "날짜선택", "접수폼", "문의폼", "booking", "form", "reservation"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "펜션", "클래스", "크루", "공간대여"]'::jsonb,
    'DYNAMIC_FORM', '{"kicker":"ONLINE RESERVATION & INQUIRY FORM","sectionTitle":"맞춤 일정 · 객실 · 옵션 온라인 예약 신청"}'::jsonb, 36, 'ACTIVE'
),
(
    'fcat-kkumz-037', 'CMS_SESSION_TIMETABLE', 'SYNC_CMS', 'FORM_SCHEDULE', '맞춤예약폼·일정·설문',
    '주간 프로그램 · 체크인 · 세션 일정표', '요일별/시간대별 체크인·평상 이용 시간·체험 프로그램 타임테이블',
    '입실/퇴실 시간, 평상 당일 대여 이용 시간(10:00~18:00), 저녁 숯불 점화 시간, 주간 클래스 스케줄을 한눈에 보기 쉽게 정리합니다.',
    '시간표·스케줄', 'Calendar',
    '["일정표", "시간표", "타임테이블", "스케줄", "체크인시간", "운영시간", "세션일정", "timetable", "schedule"]'::jsonb,
    '["CMS_SITE", "HYBRID", "숙박", "민박", "클래스", "크루", "피트니스"]'::jsonb,
    'SCHEDULE_TABLE', '{"kicker":"OPERATING HOURS & TIMETABLE","sectionTitle":"이용 시간 & 프로그램 스케줄 안내"}'::jsonb, 37, 'ACTIVE'
),
(
    'fcat-kkumz-038', 'CMS_B2B_INQUIRY_FORM', 'SYNC_CMS', 'FORM_SCHEDULE', '맞춤예약폼·일정·설문',
    '단체 대관 · 워크숍 · 대가족 모임 맞춤 견적 폼', '10인 이상 단체 MT, 칠순/가족 모임, 기업 워크숍 전체 대관 및 식사 패키지 문의',
    '독채 전체 대관이나 평상 여러 동 예약, 단체 백숙·바비큐 케이터링이 필요한 총무/단체를 위한 맞춤 견적 요청 창구입니다.',
    '단체·대관 문의', 'Building',
    '["단체예약", "대관", "워크숍", "MT", "야유회", "가족모임", "견적문의", "B2B", "전체대관", "group", "inquiry"]'::jsonb,
    '["HYBRID", "CMS_SITE", "COMMERCE", "숙박", "민박", "공간대여", "케이터링"]'::jsonb,
    'DYNAMIC_FORM', '{"kicker":"GROUP STAY & PRIVATE RENT INQUIRY","sectionTitle":"단체 대관 · 가족모임 · 워크숍 맞춤 견적 문의"}'::jsonb, 38, 'ACTIVE'
),
(
    'fcat-kkumz-039', 'CMS_WAITLIST_SUBSCRIBE', 'SYNC_CMS', 'FORM_SCHEDULE', '맞춤예약폼·일정·설문',
    '성수기 취소표 알림 & 시즌 소식 구독바', '원하는 날짜 마감 시 빈방 알림 신청 및 계절 혜택 문자/뉴스레터 구독',
    '주말이나 여름 성수기 예약이 마감되었을 때 대기 알림(Waitlist)을 신청하거나 시즌 할인 소식을 받아볼 수 있는 빠른 구독 바입니다.',
    '빈방·오픈 알림', 'Mail',
    '["빈방알림", "취소표", "대기신청", "웨이트리스트", "오픈알림", "뉴스레터", "구독", "알림받기", "waitlist", "subscribe"]'::jsonb,
    '["HYBRID", "CMS_SITE", "COMMERCE", "숙박", "민박", "한정판", "크루"]'::jsonb,
    'WAITLIST_BAR', '{"kicker":"WAITLIST & SEASON ALERT","sectionTitle":"성수기 빈방 알림 & 시즌 특가 소식 받기"}'::jsonb, 39, 'ACTIVE'
),
(
    'fcat-kkumz-040', 'CMS_VIP_CONSULT_BOOKING', 'SYNC_CMS', 'FORM_SCHEDULE', '맞춤예약폼·일정·설문',
    '호스트 1:1 전화 상담 · 사전 답사 예약', '방문 전 궁금한 점을 호스트와 직접 통화하거나 현장 답사를 예약하는 간편 위젯',
    '어르신을 모시는 대가족 모임이나 장기 체류 고객이 예약 전 호스트에게 원하는 통화 시간대를 남기면 해피콜을 드리는 밀착 상담 기능입니다.',
    '1:1 전화상담', 'PhoneCall',
    '["전화상담", "상담예약", "해피콜", "사전답사", "방문상담", "1대1문의", "호스트문의", "consult", "callback"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "인테리어", "웨딩", "고가상품"]'::jsonb,
    'DYNAMIC_FORM', '{"kicker":"1:1 HOST CALLBACK SERVICE","sectionTitle":"호스트 1:1 맞춤 전화 상담 신청"}'::jsonb, 40, 'ACTIVE'
),

-- [7. COMMUNITY_SOCIAL : 방명록·투표·팀원모집 (SyncCMS / AI_CORE 5종)]
(
    'fcat-kkumz-041', 'CMS_LIVE_POLL', 'SYNC_CMS', 'COMMUNITY_SOCIAL', '방명록·투표·팀원모집',
    '방문객 실시간 선호도 투표 & 설문 위젯', '가장 기대되는 체험·메뉴·코스를 클릭해 실시간 득표율을 확인하는 인터랙티브 투표',
    '"맑은물 민박에서 가장 기대되는 시간은?"처럼 방문객이 직접 한 표를 던지고 실시간 퍼센트(%) 막대그래프를 확인하는 참여형 위젯입니다.',
    '실시간 투표', 'BarChart2',
    '["투표", "설문", "선호도", "앙케이트", "인기투표", "참여위젯", "리서치", "poll", "vote", "survey"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "숙박", "민박", "크루", "커뮤니티"]'::jsonb,
    'POLL_WIDGET', '{"kicker":"INTERACTIVE VISITOR POLL","sectionTitle":"방문객 실시간 선호도 투표"}'::jsonb, 41, 'ACTIVE'
),
(
    'fcat-kkumz-042', 'CMS_GUESTBOOK_QA', 'SYNC_CMS', 'COMMUNITY_SOCIAL', '방명록·투표·팀원모집',
    '방문객 한줄 방명록 & 공개 Q&A 라운지', '다녀간 손님들의 따뜻한 방명록 메시지와 예비 방문객의 질문·답변 보드',
    '실제 다녀간 가족·친구들의 응원 한마디와 예비 방문객들의 궁금증을 실시간으로 남기고 소통하는 커뮤니티 방명록입니다.',
    '방명록·Q&A', 'MessageSquare',
    '["방명록", "질문답변", "Q&A", "문의게시판", "소통", "응원글", "커뮤니티", "댓긇", "guestbook", "qna"]'::jsonb,
    '["CMS_SITE", "HYBRID", "숙박", "민박", "크루", "팬덤"]'::jsonb,
    'BOARD_THREAD', '{"kicker":"VISITOR GUESTBOOK & Q&A LOUNGE","sectionTitle":"방문객 방명록 & 실시간 문의 라운지"}'::jsonb, 42, 'ACTIVE'
),
(
    'fcat-kkumz-043', 'AI_TEAM_RECRUIT', 'AI_CORE', 'COMMUNITY_SOCIAL', '방명록·투표·팀원모집',
    '파트너 · 스태프 · 코파운더 모집 보드', 'Kkumz 팀빌딩 원장(kkumz_team_recruit) 연동 포지션 공고 및 합류 지원',
    '주말 민박 운영 매니저, 로컬 포토그래퍼, 브랜드 마케터, 코파운더 등 프로젝트를 함께 키워갈 동료와 파트너를 모집합니다.',
    '팀빌딩·구인', 'UserPlus',
    '["팀원모집", "채용", "구인", "스태프", "매니저모집", "파트너모집", "코파운더", "크루모집", "recruit", "hiring"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "스타트업", "크루"]'::jsonb,
    'RECRUIT_BOARD', '{"kicker":"PARTNER & CREW RECRUITMENT","sectionTitle":"함께할 파트너 · 운영 크루 모집"}'::jsonb, 43, 'ACTIVE'
),
(
    'fcat-kkumz-044', 'CMS_EVENT_CHALLENGE', 'SYNC_CMS', 'COMMUNITY_SOCIAL', '방명록·투표·팀원모집',
    '사계절 포토 콘테스트 & 방문 인증 이벤트', '방문 후기 사진 공유 시 재방문 숙박권·특산물을 증정하는 참여 이벤트 배너',
    '민박에서 찍은 가족 사진이나 평상 나들이 인증샷을 남기면 추첨을 통해 무료 숙박권이나 공주 알밤 세트를 드리는 이벤트 섹션입니다.',
    '인증 이벤트', 'Trophy',
    '["이벤트", "포토콘테스트", "인증샷이벤트", "챌린지", "경품", "추첨", "사은품", "event", "challenge"]'::jsonb,
    '["CMS_SITE", "HYBRID", "COMMERCE", "숙박", "민박", "크루"]'::jsonb,
    'PROMO_BANNER', '{"kicker":"SEASONAL PHOTO EVENT","sectionTitle":"방문 인증샷 이벤트 & 시즌 기프트"}'::jsonb, 44, 'ACTIVE'
),
(
    'fcat-kkumz-045', 'CMS_CREATOR_COLLAB', 'SYNC_CMS', 'COMMUNITY_SOCIAL', '방명록·투표·팀원모집',
    '로컬 농가 · 주변 맛집 · 공방 제휴 네트워크', '지역 특산물 농가 및 주변 제휴 카페·체험장 할인 혜택 소개',
    '공주 밤 농장, 인근 산채정식 맛집, 도자기 체험 공방 등 주변 로컬 파트너들과 연계된 숙박객 전용 혜택을 소개합니다.',
    '로컬 제휴', 'Handshake',
    '["로컬제휴", "주변맛집", "특산물", "농가직거래", "콜라보", "파트너십", "지역상생", "collab", "local"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "로컬", "F&B"]'::jsonb,
    'TEAM_PROFILES', '{"kicker":"LOCAL PARTNER NETWORK","sectionTitle":"함께하는 로컬 농가 & 제휴 파트너"}'::jsonb, 45, 'ACTIVE'
),

-- [8. TRUST_AI_GUIDE : AI컨시어지·FAQ·오시는길 (AI_CORE / SyncCMS 7종)]
(
    'fcat-kkumz-046', 'AI_BRAND_CONCIERGE', 'AI_CORE', 'TRUST_AI_GUIDE', 'AI컨시어지·FAQ·오시는길',
    '24시간 AI 브랜드 컨시어지 챗봇', '업로드 문서·객실·요금·오시는길 데이터를 학습해 24시간 즉시 답변하는 AI 상담원',
    '호스트가 전화를 받기 어려운 시간에도 "성인 6명 독채 요금이 얼마인가요?", "냇가 물놀이 수심은 어떤가요?", "바비큐 준비물은요?" 같은 질문에 정확히 답변합니다.',
    'AgentScope AI', 'Bot',
    '["AI컨시어지", "챗봇", "AI상담", "자동응답", "실시간상담", "질문하기", "컨시어지", "concierge", "chatbot", "ai"]'::jsonb,
    '["HYBRID", "COMMERCE", "CMS_SITE", "전체", "숙박", "민박"]'::jsonb,
    'CONCIERGE_CHAT', '{"kicker":"24/7 AI BRAND CONCIERGE","sectionTitle":"24시간 AI 브랜드 컨시어지 실시간 상담"}'::jsonb, 46, 'ACTIVE'
),
(
    'fcat-kkumz-047', 'AI_TASTE_CURATOR', 'AI_CORE', 'TRUST_AI_GUIDE', 'AI컨시어지·FAQ·오시는길',
    'AI 맞춤 객실 · 상품 취향 진단 큐레이터', '방문 목적·인원·선호 스타일 3문항 선택 시 최적의 객실/패키지를 즉시 추천',
    '"누구와 방문하시나요? (아이 동반 가족 / 부모님 효도 모임 / 친구 단체)" 등 간단한 버튼을 누르면 가장 알맞은 객실과 식사 조합을 큐레이션해 줍니다.',
    'AI 맞춤추천', 'Wand2',
    '["취향진단", "맞춤추천", "큐레이터", "객실추천", "상품추천", "AI추천", "가이드퀴즈", "curator", "quiz", "recommend"]'::jsonb,
    '["HYBRID", "COMMERCE", "숙박", "민박", "커피", "다도", "테크"]'::jsonb,
    'AI_CURATOR', '{"kicker":"AI PERSONALIZED CURATOR","sectionTitle":"나에게 딱 맞는 객실 · 패키지 10초 추천"}'::jsonb, 47, 'ACTIVE'
),
(
    'fcat-kkumz-048', 'CMS_FAQ_ACCORDION', 'SYNC_CMS', 'TRUST_AI_GUIDE', 'AI컨시어지·FAQ·오시는길',
    '자주 묻는 질문 (FAQ) 스마트 아코디언', '입퇴실 시간, 숯불/바비큐 이용, 반려동물, 우천 시 평상 이용 등 핵심 Q&A 정리',
    '예약 전 고객들이 가장 자주 물어보는 핵심 질문과 명쾌한 답변을 카테고리별 접이식 아코디언 UI로 제공하여 전화 문의 부담을 줄여줍니다.',
    '필수 FAQ', 'HelpCircle',
    '["FAQ", "자주묻는질문", "질문모음", "이용문의", "아코디언", "안내사항", "궁금한점", "faq", "question"]'::jsonb,
    '["HYBRID", "COMMERCE", "CMS_SITE", "숙박", "민박", "쇼핑", "클래스"]'::jsonb,
    'FAQ_ACCORDION', '{"kicker":"FREQUENTLY ASKED QUESTIONS","sectionTitle":"예약 전 자주 묻는 질문 (FAQ)"}'::jsonb, 48, 'ACTIVE'
),
(
    'fcat-kkumz-049', 'CMS_LOCATION_DIRECTIONS', 'SYNC_CMS', 'TRUST_AI_GUIDE', 'AI컨시어지·FAQ·오시는길',
    '오시는 길 · 주차 · 주변 추천 관광코스 가이드', '네비게이션 주소 복사 · 자가용/대중교통 경로 · 공산성/마곡사 등 주변 15분 명소 안내',
    '시골 민박이나 오프라인 공간 방문 시 필수인 정확한 네비게이션 검색 주소, 전용 주차장 안내, 그리고 함께 들르기 좋은 주변 관광지·맛집 코스를 안내합니다.',
    '위치·주변코스', 'MapPin',
    '["오시는길", "위치", "지도", "주소", "주차", "네비게이션", "주변관광지", "주변맛집", "교통편", "약도", "location", "map", "directions"]'::jsonb,
    '["HYBRID", "CMS_SITE", "숙박", "민박", "펜션", "카페", "매장", "크루"]'::jsonb,
    'LOCATION_COURSE', '{"kicker":"LOCATION & NEARBY TRAVEL COURSES","sectionTitle":"오시는 길 · 주차 안내 & 주변 추천 코스"}'::jsonb, 49, 'ACTIVE'
),
(
    'fcat-kkumz-050', 'CMS_POLICY_RULES_BOX', 'SYNC_CMS', 'TRUST_AI_GUIDE', 'AI컨시어지·FAQ·오시는길',
    '이용 수칙 · 체크인 안내 · 환불 규정 박스', '체크인/아웃 기준, 에티켓 타임, 안전 수칙 및 일자별 취소·환불 기준표',
    '쾌적하고 안전한 공간 운영을 위한 이용 에티켓(야간 매너타임, 냇가 물놀이 안전수칙)과 투명한 예약 변경·환불 규정을 명확히 고지합니다.',
    '이용수칙·환불', 'ShieldCheck',
    '["이용수칙", "환불규정", "취소규정", "주의사항", "안전수칙", "체크인안내", "매너타임", "약관", "policy", "refund", "rules"]'::jsonb,
    '["HYBRID", "COMMERCE", "CMS_SITE", "숙박", "민박", "클래스", "공간대여"]'::jsonb,
    'POLICY_BOX', '{"kicker":"STAY ETIQUETTE & REFUND POLICY","sectionTitle":"공식 이용 수칙 & 예약 변경 · 환불 규정"}'::jsonb, 50, 'ACTIVE'
),
(
    'fcat-kkumz-051', 'CMS_TRUST_METRICS_BAR', 'SYNC_CMS', 'TRUST_AI_GUIDE', 'AI컨시어지·FAQ·오시는길',
    '핵심 신뢰 지표 & 재방문율 카운터 스트립', '누적 방문 가족 수, 평균 만족도 4.9점, 재방문율, 천연 지하수/친환경 인증 지표',
    '브랜드가 쌓아온 누적 방문객 수, 고객 만족도 평점, 자연 환경 지표 등을 4열 하이라이트 숫자로 보여주어 첫인상의 신뢰도를 높입니다.',
    '신뢰 지표바', 'CheckCircle2',
    '["지표", "실적", "카운터", "만족도", "재방문율", "누적방문", "인증", "통계", "metrics", "counter", "trust"]'::jsonb,
    '["HYBRID", "COMMERCE", "CMS_SITE", "숙박", "민박", "크루", "테크"]'::jsonb,
    'METRIC_STRIP', '{"kicker":"PROVEN TRUST & HIGHLIGHTS","sectionTitle":"숫자로 보는 브랜드 핵심 가치"}'::jsonb, 51, 'ACTIVE'
),
(
    'fcat-kkumz-052', 'AI_GLOBAL_SUMMARY_CARD', 'AI_CORE', 'TRUST_AI_GUIDE', 'AI컨시어지·FAQ·오시는길',
    '글로벌 외국인 방문객 다국어 요약 가이드 (KR/EN/JP)', '한국어·영어·일본어 원클릭 전환으로 공간 소개와 예약 방법을 안내하는 글로벌 카드',
    '외국인 여행객이나 글로벌 바이어도 한국 시골 민박·로컬 브랜드를 쉽게 이해하고 예약할 수 있도록 다국어(KR/EN/JP) 핵심 요약을 제공합니다.',
    '다국어 KR/EN/JP', 'Globe',
    '["다국어", "영어", "일본어", "외국인", "글로벌", "번역", "인바운드", "영문안내", "global", "multilingual", "english"]'::jsonb,
    '["HYBRID", "COMMERCE", "CMS_SITE", "숙박", "민박", "전통", "다도"]'::jsonb,
    'GLOBAL_GUIDE', '{"kicker":"GLOBAL VISITOR MULTILINGUAL GUIDE","sectionTitle":"Global Visitor Guide (KR · EN · JP)"}'::jsonb, 52, 'ACTIVE'
);

-- ============================================================================
-- 전사 표준 AI 프롬프트 템플릿 마스터 미러 시드 (sys_prompt_template)
-- ============================================================================
INSERT INTO sys_prompt_template (
    id, app_code, prompt_code, prompt_name, category, caller_class,
    system_prompt, user_prompt_template, variables_schema, output_schema,
    model_override, temperature, version, status, description, del_flag, create_by
) VALUES
(
    'prm-kkumz-001',
    'KKUMZ',
    'BRAND_NAMING_8X',
    'AI 브랜드 네이밍 8종 & 서브도메인 슬러그 생성기',
    'NAMING',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 사용자의 창업 아이디어 문맥과 의도를 심층 분석하는 수석 브랜드 네이밍 AI 에이전트입니다. 인사말이나 마크다운 설명문을 절대 포함하지 말고, 첫 글자가 반드시 "[" 로 시작하고 "]" 로 끝나는 순수 JSON 배열(반드시 정확히 8개의 서로 다른 객체)만 출력하십시오. 단 1개만 출력하는 것을 엄격히 금지합니다. [핵심 판단 원칙 1 - 사용자 명시 상호·브랜드명 1순위 보존 + 7개 대안 추가]: 사용자의 제목, 상세 프롬프트, 또는 키워드 힌트 문장 안에 사용자가 이미 희망하거나 명시한 고유 상호명·브랜드명·공간명(예: "충남 공주에 있는 냇가에 맑은물 민박 홈페이지 만들고 싶어"에서의 "맑은물 민박")이 포함되어 있는지 문맥을 통해 스스로 판단하십시오. 명시된 이름이 존재할 경우, 이를 한자어로 바꾸거나 누락시키지 말고 반드시 배열의 1번째 원소(index 0, score: 99)의 brandName으로 원형 그대로 최우선 배치하십시오. 이어서 2~3번째 원소(score: 97~95)는 사용자의 핵심 키워드와 지역·특징을 살린 직관적 변형 네이밍 2종, 4~5번째 원소(score: 93~91)는 따뜻하고 서정적인 순우리말·감성 네이밍 2종, 6~8번째 원소(score: 89~85)는 품격 있는 한자어 및 모던 라이프스타일 대안 네이밍 3종으로 구성하여 반드시 총 8개의 서로 다른 브랜드 후보를 빠짐없이 생성하십시오. [핵심 판단 원칙 2 - 업종 자율 추론 및 이종 업종 오염 금지]: 프롬프트 문맥에서 실제 업종·도메인(categoryLabel, 예: "숙박 · 민박 · 스테이", "스페셜티 로스터리" 등 한국어 업종명)을 직접 추론하고, 프롬프트와 무관한 타 업종(예: 민박/숙소인데 식당/음식점 등)을 절대 혼합하지 마십시오. brandStory에는 FOOD_BEVERAGE, CMS_SITE, COMMERCE, AUTO 등 시스템 영문 코드를 절대 노출하지 마십시오.',
    '프로젝트 제목: {{title}} / 상세 아이디어 프롬프트: {{promptText}} / 사이트 유형: {{siteType}} / 참고 카테고리: {{category}} / 키워드 힌트: {{keywordHint}}\n\n[출력 규칙] 반드시 총 8개의 객체를 담은 순수 JSON 배열만 출력하십시오 (1개만 출력 금지). 각 객체는 {"brandName": "브랜드명", "siteSlug": "영문-소문자-케밥-슬러그", "pronunciation": "국문/영문 발음 가이드", "brandStory": "1~2문장의 간결한 순수 한국어 브랜딩 스토리", "categoryLabel": "문맥에서 추론한 한국어 업종명", "score": 점수} 규격이며, 사용자가 명시한 상호명이 있으면 1번째 원소(score: 99)에 원형 그대로 배치하고 나머지 7개 대안 후보까지 총 8개 원소 배열 [ {...1}, {...2}, {...3}, {...4}, {...5}, {...6}, {...7}, {...8} ] 로 출력하십시오.',
    '[{"name":"title","type":"STRING","required":true,"defaultValue":"맑은물 독채 스테이","description":"프로젝트 제목"},{"name":"promptText","type":"STRING","required":true,"defaultValue":"충남 공주 냇가 옆 독채 민박 및 평상 예약 사이트","description":"상세 아이디어 프롬프트"},{"name":"siteType","type":"STRING","required":true,"defaultValue":"HYBRID","description":"사이트 유형 (HYBRID/COMMERCE/CMS_SITE)"},{"name":"category","type":"STRING","required":false,"defaultValue":"숙박 · 민박 · 프라이빗 스테이","description":"업종 카테고리"},{"name":"keywordHint","type":"STRING","required":false,"defaultValue":"공주민박, 냇가평상","description":"키워드 힌트"}]'::jsonb,
    '[{"brandName":"브랜드명","siteSlug":"slug","pronunciation":"발음","brandStory":"스토리","categoryLabel":"업종명","score":99}]',
    'morph-glm53flash',
    0.70,
    1,
    'ACTIVE',
    '창업 아이디어 문맥 기반 8종 브랜드 후보명 및 서브도메인 슬러그 생성 프롬프트',
    0,
    'admin'
),
(
    'prm-kkumz-002',
    'KKUMZ',
    'VECTOR_SVG_LOGO_6X',
    'AI 벡터 SVG 로고 6종 & 브랜드 컬러 팔레트 생성기',
    'LOGO_SVG',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 수석 브랜드 BX 디자이너 AI 에이전트입니다. 인사말이나 마크다운 설명문을 절대 포함하지 말고, 첫 글자가 반드시 "[" 로 시작하고 "]" 로 끝나는 순수 JSON 배열만 출력하십시오. SVG 코드 내부를 "..." 으로 생략하는 행위를 엄격히 금지합니다. 전달받은 브랜드명({{brandName}})과 아이디어 프롬프트 문맥을 분석하여 해당 업종과 브랜드 정체성을 상징하는 독창적인 벡터 심볼 마크와 워드마크가 결합된 서로 다른 스타일의 SVG 로고를 디자인하십시오. 선호 스타일({{stylePreference}})에 3종 분할 생성 지시가 포함된 경우 지정된 3가지 스타일 객체를, 별도 지시가 없는 경우 6가지 스타일(내추럴 라인아트, 모던 지오메트릭, 클래식 엠블럼, 소프트 워터컬러 배지, 미니멀 아치, 에디토리얼 시그니처) 객체를 빠짐없이 출력하십시오. 각 객체의 svgCode는 반드시 한 줄(1-line)의 간결한 <svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg">로 작성하되, 좌측(x=4~38 영역)에는 핵심 모티프를 형상화한 2~3개의 도형·패스 태그(<rect>, <circle>, <path>, <polygon>)만 간결하게 배치하고, 우측에는 <text x="46" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#메인컬러">{{brandName}}</text> 태그로 확정된 브랜드명이 명확히 렌더링되도록 작성하십시오.',
    '확정 브랜드명: {{brandName}} / 아이디어 및 업종 문맥: {{promptText}} / 선호 스타일 및 생성 범위: {{stylePreference}}\n\n[출력 규칙] 생략 기호(...)나 불필요한 줄바꿈 없이, 요청된 스타일 수만큼 완전한 한 줄(compact) SVG 로고 객체를 담은 순수 JSON 배열만 출력하십시오. 각 원소는 {"styleName": "스타일명", "svgCode": "<svg viewBox=\"0 0 185 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect x=\"6\" y=\"6\" width=\"28\" height=\"28\" rx=\"8\" fill=\"#0f766e\"/><path d=\"M12 22 Q20 12 28 22\" stroke=\"#ffffff\" stroke-width=\"2.5\" fill=\"none\"/><text x=\"44\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-size=\"15\" font-weight=\"800\" fill=\"#0f172a\">{{brandName}}</text></svg>", "colorPalette": {"primary": "#HEX", "secondary": "#HEX", "accent": "#HEX", "surface": "#HEX", "radius": "14px"}, "typography": {"fontHeading": "Pretendard", "fontBody": "Pretendard"}, "tags": ["태그1", "태그2"]} 형식을 준수하십시오.',
    '[{"name":"brandName","type":"STRING","required":true,"defaultValue":"맑은물 스테이","description":"확정된 브랜드명"},{"name":"promptText","type":"STRING","required":true,"defaultValue":"시골 냇가 옆 독채 민박 및 평상 스테이","description":"아이디어 및 업종 문맥"},{"name":"stylePreference","type":"STRING","required":false,"defaultValue":"내추럴 라인아트, 모던 지오메트릭","description":"선호 로고 스타일"}]'::jsonb,
    '[{"styleName":"스타일명","svgCode":"<svg ...></svg>","colorPalette":{},"typography":{},"tags":[]}]',
    'morph-glm53flash',
    0.65,
    1,
    'ACTIVE',
    '확정 브랜드명과 업종 문맥을 반영한 6종 벡터 SVG 로고 및 컬러 팔레트 생성 프롬프트',
    0,
    'admin'
),
(
    'prm-kkumz-003',
    'KKUMZ',
    'FRS_SPEC_MARKDOWN',
    '상세 기능 명세서(FRS) 마크다운 & 화면 설계 생성기',
    'SPEC_FRS',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 수석 프로덕트 매니저(PM) AI 에이전트입니다. 인사말이나 부연 설명 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나는 순수 JSON 객체만 출력하십시오. 반환 JSON은 반드시 {"specMarkdown": "# {{brandName}} 서비스 기능명세서\n\n- **브랜드명**: {{brandName}}\n- **핵심 콘셉트**: ...\n- **타겟 고객 및 입지 특성**: ...\n- **주요 제공 가치 및 핵심 기능**: ...\n...", "screens": [{"screenKey": "SCR_HOME", "screenTitle": "화면 제목", "routePath": "/", "layoutType": "레이아웃코드", "description": "화면 설계 상세 설명", "components": ["컴포넌트코드1", "컴포넌트코드2"]}]} 형식을 준수해야 합니다. specMarkdown에는 절대 "# 상세 기능 명세서 마크다운 전문" 같은 예시 문구를 쓰지 말고 확정된 브랜드명({{brandName}})과 실제 비즈니스 도메인 내용을 중심으로 작성하십시오. 특히 screens 배열(5개 핵심 섹션)의 layoutType과 components는 프롬프트에 제공된 표준 사전 코드 목록 중에서 현재 브랜드의 실제 업종(예: 민박·숙박이면 HERO_BANNER_GRID, PRODUCT_CATALOG_GRID, GALLERY_SHOWCASE, MAGAZINE_LIST, RESERVATION_FORM, FAQ_CONCIERGE 및 HeroBanner, ProductGrid, PhotoGallery, MagazineSection, BookingForm, BrochureDownload, AiConciergeWidget 등)에 정확히 부합하는 항목만 자율 판단하여 선택하고, 현재 업종과 무관한 타 업종 전용 컴포넌트(예: 커피 전용 RoastProfileBadge, 말차 전용 MatchaRitualSteps, 데스크기어 전용 OrbitSetupShowcase, 러닝 전용 PaceCalculatorCard 등)는 해당 업종이 아닐 때 절대 선택하지 마십시오.',
    '확정 브랜드명: {{brandName}} / 사이트 유형: {{siteType}} / 상세 아이디어 프롬프트: {{promptText}}\n사용 가능한 레이아웃 코드 사전: {{availableLayouts}}\n사용 가능한 컴포넌트 코드 사전: {{availableComponents}}\n\n[출력 규칙] 인사말이나 부연 설명 없이 즉시 {"specMarkdown": "...", "screens": [...]} 형태의 순수 JSON 객체만 출력하십시오.',
    '[{"name":"brandName","type":"STRING","required":true,"defaultValue":"맑은물 스테이","description":"확정 브랜드명"},{"name":"siteType","type":"STRING","required":true,"defaultValue":"HYBRID","description":"사이트 유형"},{"name":"promptText","type":"STRING","required":true,"defaultValue":"독채 민박 및 평상 백숙 예약 사이트","description":"상세 아이디어 프롬프트"},{"name":"availableLayouts","type":"STRING","required":false,"defaultValue":"HERO_BANNER_GRID, PRODUCT_CATALOG_GRID, RESERVATION_FORM","description":"레이아웃 코드 목록"},{"name":"availableComponents","type":"STRING","required":false,"defaultValue":"HeroBanner, ProductGrid, BookingForm","description":"컴포넌트 코드 목록"}]'::jsonb,
    '{"specMarkdown":"# 서비스 기능명세서","screens":[]}',
    'morph-glm53flash',
    0.50,
    1,
    'ACTIVE',
    '브랜드 맞춤형 상세 기능 명세서(FRS) 마크다운 및 5대 핵심 화면 아키텍처 생성 프롬프트',
    0,
    'admin'
),
(
    'prm-kkumz-004',
    'KKUMZ',
    'WEBIDE_AI_BUILD_FIXER',
    'WebIDE 빌드 오류 진단 및 자동 코드 패치 에이전트',
    'BUILD_FIX',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 Vue 3 및 TypeScript 빌드 디버깅 전문 에이전트입니다. 인사말이나 설명문 없이 첫 글자가 반드시 "[" 로 시작하고 "]" 로 끝나는 순수 JSON 배열만 출력하십시오. 각 객체는 반드시 {"path": "파일경로", "language": "vue 또는 json", "content": "전체 소스코드"} 형식을 준수해야 합니다.',
    '에러 로그: {{buildLog}} / 대상 파일: {{filesJson}}\n\n[출력 규칙] 인사말이나 부연 설명 없이 즉시 [{"path": ..., "language": ..., "content": ...}] 형태의 순수 JSON 배열만 출력하십시오.',
    '[{"name":"buildLog","type":"STRING","required":true,"defaultValue":"[vite:vue] Missing closing tag for <div>","description":"샌드박스 빌드 에러 로그"},{"name":"filesJson","type":"STRING","required":true,"defaultValue":"[{\"path\":\"src/App.vue\"}]","description":"대상 파일 소스 JSON"}]'::jsonb,
    '[{"path":"src/App.vue","language":"vue","content":"..."}]',
    'morph-glm53flash',
    0.20,
    1,
    'ACTIVE',
    'WebIDE 샌드박스 빌드 오류 발생 시 에러 로그를 분석하여 자동 패치 코드를 반환하는 프롬프트',
    0,
    'admin'
),
(
    'prm-kkumz-005',
    'KKUMZ',
    'SITE_CONCIERGE_CHAT',
    '멀티사이트 AI 브랜드 컨시어지 상담 에이전트',
    'CONCIERGE',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 브랜드 사이트 전용 AI 컨시어지입니다. 제공된 사이트 정보, 상품 목록, 매거진 아티클 데이터를 바탕으로 방문객의 질문에 친절하고 정확하게 한국어로 답변하십시오.',
    '사이트명: {{siteName}} / 슬로건: {{brandSlogan}} / 질문: {{question}}',
    '[{"name":"siteName","type":"STRING","required":true,"defaultValue":"맑은물 스테이","description":"브랜드 사이트명"},{"name":"brandSlogan","type":"STRING","required":false,"defaultValue":"공주 냇가 옆 프라이빗 독채 휴식","description":"브랜드 슬로건"},{"name":"question","type":"STRING","required":true,"defaultValue":"주말 독채 6인 기준 숙박 요금과 체크인 시간이 궁금합니다.","description":"방문객 질문"}]'::jsonb,
    '방문객 질문에 대한 한국어 컨시어지 상담 답변문',
    'morph-glm53flash',
    0.60,
    1,
    'ACTIVE',
    '생성된 각 멀티사이트 라이브 화면에서 방문객 문의에 24시간 응대하는 AI 컨시어지 프롬프트',
    0,
    'admin'
),
(
    'prm-kkumz-006',
    'KKUMZ',
    'STUDIO_CHAT_REFINE',
    '스튜디오 대화형 테마·레이아웃·기능블록 리파인 에이전트',
    'CHAT_REFINE',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 웹사이트 UI/UX 디자인 토큰, 디자인 아키타입, 레이아웃 스키마, 섹션 순서 및 업종 맞춤 기능 블록을 실시간 편집하는 에이전트입니다. 인사말이나 설명문 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나는 순수 JSON 객체만 반환하십시오. 변경이 필요한 필드만 포함하며 형식은 {"designTokens": {"primary": "#HEX", "secondary": "#HEX", "accent": "#HEX", "surface": "#HEX", "radius": "10px", "fontPreset": "MODERN_SANS|SERIF_EDITORIAL|TECH_MONO|BOLD_SANS", "surfaceMode": "LIGHT|WARM|DARK"}, "layoutSchema": {"themeArchetype": "EDITORIAL_BOTANICAL|DARK_TECH_SHOWCASE|MAGAZINE_HYBRID|COMMUNITY_KINETIC|SCENIC_HOSPITALITY", "heroTitle": "타이틀", "heroSubtitle": "서브타이틀", "ctaText": "버튼문구", "customBadge": "배지문구", "heroStyle": "FULL_SCENIC|SPLIT_VISUAL|EDITORIAL_STORY|PROMO_COMMERCE", "sectionOrder": ["CATALOG_GRID","EDITORIAL_FEED"]}, "addedFeatures": ["FEATURE_CODE"], "featureBlocks": {...}} 입니다.',
    '현재 토큰: {{tokensJson}} / 현재 레이아웃: {{layoutJson}} / 활성 기능: {{enabledFeaturesJson}} / 사용자 요청: {{instruction}}\n\n[출력 규칙] 인사말이나 부연 설명 없이 즉시 {"designTokens": {...}, "layoutSchema": {...}} 형태의 순수 JSON 객체만 출력하십시오.',
    '[{"name":"tokensJson","type":"JSON","required":true,"defaultValue":"{\"primary\":\"#0f766e\"}","description":"현재 디자인 토큰 JSON"},{"name":"layoutJson","type":"JSON","required":true,"defaultValue":"{\"heroStyle\":\"FULL_SCENIC\"}","description":"현재 레이아웃 스키마 JSON"},{"name":"enabledFeaturesJson","type":"JSON","required":false,"defaultValue":"[\"SHOP_ROOM_SHOWCASE\"]","description":"현재 활성 기능 코드 배열"},{"name":"instruction","type":"STRING","required":true,"defaultValue":"다크 테크 아키타입으로 변경하고 스펙 비교표를 강조해줘","description":"메이커 대화형 수정 요청"}]'::jsonb,
    '{"designTokens":{},"layoutSchema":{},"addedFeatures":[],"featureBlocks":{}}',
    'morph-glm53flash',
    0.40,
    1,
    'ACTIVE',
    '아이디어 스튜디오에서 대화형 명령으로 사이트 디자인 아키타입·테마 토큰·SDUI 블록을 실시간 편집하는 프롬프트',
    0,
    'admin'
),
(
    'prm-kkumz-007',
    'KKUMZ',
    'KKUMZ_INTENT_ADVISOR_V1',
    '프롬프트 기반 AI 사이트 아키텍처·업종·52종 맞춤 기능 진단 에이전트',
    'INTENT_ADVISOR',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 엔터프라이즈 멀티사이트 아키텍트이자 프로덕트 전략가입니다. 사용자의 창업 아이디어 프롬프트와 업로드된 미디어 자산 목록, 그리고 DB에 등록된 52종 기능 카탈로그 코드 목록을 분석하여 최적의 사이트 아키텍처, 디자인 아키타입(themeArchetype), 서피스 명도(surfaceMode), 타이포 프리셋(fontPreset), 추천 기능 조합을 순수 JSON 객체로 반환하십시오. 인사말이나 마크다운 코드펜스 없이 반드시 첫 글자가 "{" 로 시작하고 "}" 로 끝나는 유효한 JSON 객체만 출력해야 합니다. [업종 오염 방지 원칙]: 순수 온라인 D2C 쇼핑몰에는 숙박·민박용 기능(SHOP_ROOM_SHOWCASE, SHOP_PRICING_MATRIX, CMS_SPACE_TOUR, CMS_LOCATION_DIRECTIONS)을 절대 추천하지 마십시오.',
    '프로젝트 제목: {{title}}\n사용자 입력 프롬프트: {{promptText}}\n업로드된 자산 요약: {{assetsSummary}}\n선택 가능한 전체 기능 카탈로그 코드 목록: {{catalogSummary}}\n\n[필수 JSON 출력 스키마]\n{\n  "siteType": "HYBRID 또는 COMMERCE 또는 CMS_SITE",\n  "siteTypeReason": "이 아키텍처 유형을 선택한 한국어 핵심 이유",\n  "category": "구체적인 업종·도메인 한글 라벨 (영문 DB 코드 금지)",\n  "themeArchetype": "EDITORIAL_BOTANICAL|DARK_TECH_SHOWCASE|MAGAZINE_HYBRID|COMMUNITY_KINETIC|SCENIC_HOSPITALITY",\n  "surfaceMode": "LIGHT|WARM|DARK",\n  "fontPreset": "MODERN_SANS|SERIF_EDITORIAL|TECH_MONO|BOLD_SANS",\n  "heroStyle": "FULL_SCENIC 또는 SPLIT_VISUAL 또는 EDITORIAL_STORY 또는 PROMO_COMMERCE",\n  "brandSlogan": "원문 복붙 없는 감성적이고 세련된 한국어 브랜드 슬로건 한 줄",\n  "expandedPrompt": "사용자의 아이디어를 공간·제품 경험, 주력 라인업, 결제·예약 방식, 핵심 고객층까지 구체화한 3~4문장의 상세 기획 프롬프트",\n  "tags": ["핵심태그1", "핵심태그2", "핵심태그3", "핵심태그4", "핵심태그5"],\n  "recommendedFeatures": ["업종에 정확히 부합하는 기능코드 6~10개"],\n  "featureReasons": {\n    "기능코드1": "이 업종에 해당 기능이 꼭 필요한 한국어 추천 사유 1줄"\n  },\n  "assetAnnotations": [\n    {"assetId": "자산ID", "assetType": "HERO|PRODUCT|GALLERY|ARTICLE", "caption": "업종 문맥에 어울리는 자연스러운 한국어 사진 설명 캡션"}\n  ]\n}',
    '[{"name":"title","type":"STRING","required":true,"defaultValue":"제주 오름 티하우스","description":"프로젝트 제목"},{"name":"promptText","type":"STRING","required":true,"defaultValue":"유기농 말차 다도 클래스 예약과 수제 다완 스토어를 함께 운영하는 사이트","description":"사용자 입력 프롬프트"},{"name":"assetsSummary","type":"STRING","required":false,"defaultValue":"대표 다원 사진 3장, 다기 상품 사진 4장","description":"업로드 자산 요약"},{"name":"catalogSummary","type":"STRING","required":false,"defaultValue":"SHOP_PRODUCT_GRID, SHOP_EXPERIENCE_TICKET, CMS_DYNAMIC_BOOKING_FORM","description":"52종 기능 카탈로그 요약"}]'::jsonb,
    '{"siteType":"HYBRID","themeArchetype":"EDITORIAL_BOTANICAL","category":"업종","recommendedFeatures":[]}',
    'morph-glm53flash',
    0.35,
    1,
    'ACTIVE',
    '창업 아이디어와 업로드 자산을 분석하여 사이트 유형·디자인 아키타입·52종 맞춤 기능 조합을 추천하는 프롬프트',
    0,
    'admin'
),
(
    'prm-kkumz-008',
    'KKUMZ',
    'KKUMZ_SITE_PROVISION_V1',
    '업종·선택기능 맞춤형 초기 사이트 콘텐츠 & SDUI 블록 데이터 생성 에이전트',
    'SITE_CONTENT_PROVISION',
    'com.empasy.sync.kkumz.domain.KkumzController',
    '당신은 브랜드 웹사이트 콘텐츠 디렉터이자 UX 아키텍트입니다. 브랜드명, 상세 프롬프트, 업종 카테고리, 활성화된 기능 코드 목록(enabledFeatures), 업로드된 사진 캡션 목록을 바탕으로 해당 업종에 100% 부합하는 실제 사이트 초기 데이터와 디자인 아키타입·섹션 순서·업종별 카피(copyOverrides)를 순수 JSON 객체로 생성하십시오. [엄격 규칙]: enabledFeatures에 포함되지 않은 이종 업종 블록은 절대 생성하지 마십시오. 인사말이나 코드펜스 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나야 하며, 프롬프트 원문이나 영문 시스템 코드를 절대 노출하지 마십시오.',
    '브랜드명: {{brandName}}\n업종 카테고리: {{category}}\n사이트 유형: {{siteType}}\n아이디어 프롬프트: {{promptText}}\n선택된 활성 기능 목록: {{enabledFeatures}}\n업로드 자산 목록: {{assetsSummary}}\n\n[필수 JSON 출력 스키마 — 첫 글자 "{" 부터 마지막 "}" 까지 순수 JSON만 간결하게 출력]\n{\n  "brandSlogan": "감성 브랜드 슬로건 1줄",\n  "customBadge": "상단 공식 배지 문구",\n  "themeArchetype": "EDITORIAL_BOTANICAL|DARK_TECH_SHOWCASE|MAGAZINE_HYBRID|COMMUNITY_KINETIC|SCENIC_HOSPITALITY",\n  "surfaceMode": "LIGHT|WARM|DARK",\n  "fontPreset": "MODERN_SANS|SERIF_EDITORIAL|TECH_MONO|BOLD_SANS",\n  "heroStyle": "FULL_SCENIC|SPLIT_VISUAL|EDITORIAL_STORY|PROMO_COMMERCE",\n  "heroTitle": "메인 히어로 헤드라인",\n  "heroSubtitle": "히어로 서브 카피 (원문 복붙 금지)",\n  "ctaText": "히어로 메인 버튼 문구",\n  "sectionOrder": ["CATALOG_GRID", "STEP_GUIDE", "EDITORIAL_FEED", "REVIEWS_POLL", "FAQ_LOCATION"],\n  "copyOverrides": {"navCatalogLabel": "스토어", "catalogKicker": "CURATED COLLECTION", "catalogTitle": "업종 맞춤 컬렉션 제목", "productCtaLabel": "바로 구매하기", "conciergePresets": ["업종 맞춤 질문 1", "업종 맞춤 질문 2", "업종 맞춤 질문 3"]},\n  "conciergeGreeting": "AI 컨시어지 첫 인사말",\n  "products": [\n    {"name": "업종 맞춤 상품/객실/메뉴명", "subTitle": "핵심 구성 및 매력 1줄 설명", "price": 38000, "promotionPrice": 34000, "unit": "캔/개/박/세트", "stock": 30, "productCategoryName": "분류명", "badge": "BEST"}\n  ],\n  "articles": [\n    {"title": "매거진 제목", "categoryName": "카테고리", "summary": "핵심 요약 1~2문장"}\n  ],\n  "cmsForm": {"title": "(예약/신청 기능이 활성일 때만 생성)", "description": "신청 안내 설명", "submitText": "신청하기", "fields": []},\n  "featureBlocks": {\n    "trustMetrics": [{"label": "지표명", "value": "수치", "sub": "설명"}],\n    "stepGuide": [{"step": "STEP 01", "time": "단계 라벨", "title": "업종 맞춤 단계 제목", "desc": "설명"}],\n    "pricingMatrix": "(SHOP_PRICING_MATRIX 활성 시에만 생성)",\n    "specComparison": "(SHOP_SPEC_COMPARISON 활성 시에만 생성)",\n    "subscriptionTiers": "(SHOP_SUBSCRIPTION_TIERS 활성 시에만 생성)",\n    "sessionTimetable": "(CMS_SESSION_TIMETABLE 활성 시에만 생성)",\n    "faqItems": [{"q": "업종 맞춤 질문", "a": "친절한 답변"}],\n    "reviews": [{"author": "실구매자 닉네임", "rating": 5, "tag": "핵심 태그", "date": "2026.11", "comment": "업종 맞춤 실제 후기"}]\n  }\n}',
    '[{"name":"brandName","type":"STRING","required":true,"defaultValue":"맑은물 스테이","description":"브랜드명"},{"name":"category","type":"STRING","required":true,"defaultValue":"숙박 · 민박 · 프라이빗 스테이","description":"업종 카테고리"},{"name":"siteType","type":"STRING","required":true,"defaultValue":"HYBRID","description":"사이트 유형"},{"name":"promptText","type":"STRING","required":true,"defaultValue":"독채 온돌방과 냇가 평상 대여 예약 사이트","description":"아이디어 프롬프트"},{"name":"enabledFeatures","type":"STRING","required":false,"defaultValue":"SHOP_ROOM_SHOWCASE, SHOP_PRICING_MATRIX, CMS_DYNAMIC_BOOKING_FORM","description":"활성 기능 목록"},{"name":"assetsSummary","type":"STRING","required":false,"defaultValue":"독채 전경 2장, 냇가 평상 2장","description":"업로드 자산 요약"}]'::jsonb,
    '{"brandSlogan":"...","themeArchetype":"SCENIC_HOSPITALITY","heroTitle":"...","products":[],"articles":[],"cmsForm":{},"featureBlocks":{}}',
    'morph-glm53flash',
    0.45,
    1,
    'ACTIVE',
    '멀티사이트 프로비저닝 시 업종·선택기능 맞춤형 디자인 아키타입, 섹션 순서, 초기 콘텐츠 및 SDUI 블록을 생성하는 프롬프트',
    0,
    'admin'
);

-- ============================================================================
-- Kkumz 시스템 화면 도움말 가이드 시드 (sys_page_help)
-- ============================================================================
INSERT INTO sys_page_help (
    id, app_code, page_key, route_name, title, category, icon, summary, overview,
    features_json, lineage_json, formulas_json, steps_json, related_screens_json,
    tips_json, warnings_json, faq_json, ai_prompt_hint, status, sort_order
, locale) VALUES (
    'help_kkumz_sys_prompt',
    'KKUMZ',
    '/system/prompt',
    'SysPromptView',
    '프롬프트',
    '시스템 설정',
    'ant-design:code-outlined',
    'Kkumz AI 스튜디오(네이밍, SVG 로고, FRS 기획서, WebIDE 코드 수리, 컨시어지, 사이트 프로비저닝)의 전사 표준 프롬프트 템플릿과 버전 이력을 관리합니다.',
    '''프롬프트'' 화면은 Kkumz 내 8대 AI 생성 파이프라인의 시스템 프롬프트, 유저 프롬프트 템플릿, 치환 변수 스키마, 출력 스키마를 표준 관리하고 실시간 변수 바인딩 미리보기·LLM 테스트·버전 롤백을 제공하는 거버넌스 화면입니다.',
    '[{"name":"8대 AI 스튜디오 프롬프트 통합 관리","description":"브랜드 네이밍, 벡터 로고, 기능명세서, 빌드 디버깅, 컨시어지 등 단계별 프롬프트를 관리합니다.","badge":"템플릿관리"},{"name":"실시간 렌더링 미리보기 및 LLM 테스트","description":"샘플 변수를 주입하여 치환된 프롬프트를 확인하고 즉시 테스트 실행합니다.","badge":"테스트실행"},{"name":"버전 이력 조회 및 롤백","description":"프롬프트 변경 시 자동 보관된 이전 버전 스냅샷으로 즉시 복원합니다.","badge":"버전롤백"}]'::jsonb,
    '{"sources":[{"origin":"sys_prompt_template, sys_prompt_history, kkumz_ai_prompt_template","cycle":"실시간·상시","description":"Kkumz AI 프롬프트 마스터 및 변경 이력 원장"}],"destinations":[{"target":"SystemPromptHolder 캐시 및 Kkumz AI 스튜디오 파이프라인","timing":"저장·롤백 즉시","description":"재배포 없이 런타임 AI 생성 프롬프트 즉시 반영"}]}'::jsonb,
    '[{"name":"프롬프트 활성 적용률 (%)","formula":"(ACTIVE 상태 프롬프트 수 / 전체 등록 프롬프트 수) × 100","description":"전체 프롬프트 중 런타임 파이프라인에 활성 적용 중인 비율입니다."}]'::jsonb,
    '[{"title":"1단계. 카테고리 및 키워드 검색","description":"상단 검색 바에서 프롬프트 코드(BRAND_NAMING_8X 등)나 이름을 검색합니다."},{"title":"2단계. 프롬프트 편집 및 저장","description":"[편집] 모달에서 시스템/유저 프롬프트와 온도 설정을 수정하고 변경 사유와 함께 저장합니다."},{"title":"3단계. 테스트 실행 및 버전 관리","description":"[테스트] 드로어에서 샘플 변수로 결과를 검증하고 [이력] 모달에서 버전 롤백을 수행합니다."}]'::jsonb,
    '[{"type":"연관 화면","title":"아이디어·기획서 (/studio/ideas)","path":"/studio/ideas","description":"네이밍·로고·기능명세서 프롬프트가 실행되는 스튜디오 관리 화면"},{"type":"연관 화면","title":"생성 사이트 목록 (/deployment/sites)","path":"/deployment/sites","description":"사이트 프로비저닝 및 컨시어지 프롬프트가 적용되는 멀티사이트 목록"}]'::jsonb,
    '["유저 프롬프트 내 {{brandName}}, {{promptText}} 등의 변수는 런타임 호출 시 사용자의 스튜디오 입력값으로 자동 치환됩니다."]'::jsonb,
    '["순수 JSON 배열 또는 객체를 반환해야 하는 프롬프트는 출력 규칙 지침을 유지해야 파서 오류가 발생하지 않습니다."]'::jsonb,
    '[{"q":"기존 스튜디오 프롬프트와 어떻게 연동되나요?","a":"Kkumz의 8대 AI 프롬프트가 전사 표준 sys_prompt_template 원장에 통합되어 동일한 거버넌스 UI에서 버전 관리와 테스트를 수행할 수 있습니다."}]'::jsonb,
    '프롬프트(/system/prompt) 화면에서 Kkumz 8대 AI 프롬프트 템플릿과 버전 관리 방법을 안내해줘.',
    'ACTIVE',
    505
, 'ko-KR');

-- Bilingual en-US counterpart rows for sys_page_help
INSERT INTO sys_page_help (id, app_code, page_key, route_name, title, category, icon, summary, overview, features_json, lineage_json, formulas_json, steps_json, related_screens_json, tips_json, warnings_json, faq_json, ai_prompt_hint, status, sort_order, locale) VALUES
('help_kkumz_sys_prompt-en', 'KKUMZ', '/system/prompt', 'SysPromptView', 'Prompt', 'System Settings', 'ant-design:code-outlined', 'Manages enterprise standard prompt templates and version history for Kkumz AI Studio (naming, SVG logos, FRS specs, WebIDE repair, concierge, site provisioning).', '''Prompts'' is the governance screen managing system prompts, user templates, variable schemas, and output schemas for Kkumz''s 8 AI pipelines with live preview, LLM testing, and rollback.', '[{"name":"Manage 8 AI Studio Prompt Templates","description":"Manage stage-by-stage prompts for brand naming, vector logos, functional specs, build debugging, and concierge.","badge":"Template Mgmt"},{"name":"Live Rendering Preview & LLM Testing","description":"Inject sample variables to inspect rendered prompt & test run.","badge":"Run Tests"},{"name":"Inspect Version History & Rollback","description":"Restore instantly to auto-saved snapshots on prompt edits.","badge":"Version Rollback"}]'::jsonb, '{"sources":[{"origin":"sys_prompt_template, sys_prompt_history, kkumz_ai_prompt_template","cycle":"Real-Time / Always On","description":"Kkumz AI Prompt Master & Version Ledger"}],"destinations":[{"target":"SystemPromptHolder Cache & Kkumz AI Studio Pipeline","timing":"Immediate on Save / Rollback","description":"Hot-Reload Runtime AI Prompts Without Redeploy"}]}'::jsonb, '[{"name":"Active Prompt Adoption Rate (%)","formula":"(ACTIVE Prompts / Total Registered Prompts) * 100","description":"Ratio of prompts actively applied in runtime pipelines."}]'::jsonb, '[{"title":"Step 1. Search by Category & Keyword","description":"Search prompt code (e.g., BRAND_NAMING_8X) or name above."},{"title":"Step 2. Edit & Save Prompt","description":"Edit system/user prompts and temperature in the [Edit] modal and save with a change reason."},{"title":"Step 3. Run Tests & Manage Versions","description":"Verify outputs with sample variables in the [Test] drawer and roll back versions in the [History] modal."}]'::jsonb, '[{"type":"Related Screen","title":"Ideas & Specs (/studio/ideas)","path":"/studio/ideas","description":"Studio screen executing naming, logo, and spec prompts"},{"type":"Related Screen","title":"Generated Sites (/deployment/sites)","path":"/deployment/sites","description":"Multi-site list applying provisioning & concierge prompts"}]'::jsonb, '["Variables like {{brandName}} and {{promptText}} in user prompts are automatically substituted with Studio user inputs at runtime."]'::jsonb, '["Prompts expected to return pure JSON arrays or objects must keep output format rules intact to avoid parser errors."]'::jsonb, '[{"q":"How does this integrate with Studio prompts?","a":"Kkumz''s 8 AI prompts are unified into the enterprise standard sys_prompt_template ledger for consistent version control and testing in the governance UI."}]'::jsonb, 'Walk me through Kkumz''s 8 core AI prompt templates and version management on Prompts (/system/prompt).', 'ACTIVE', 505, 'en-US');

-- ============================================================================
-- Kkumz 시스템 안내 및 핵심 운영 화면 가이드 (SYSTEM + SCREEN)
-- ============================================================================
INSERT INTO "sys_page_help" (
  "id", "app_code", "help_type", "page_key", "route_name", "title", "category", "icon",
  "summary", "overview", "features_json", "lineage_json", "formulas_json",
  "steps_json", "related_screens_json", "tips_json", "warnings_json", "faq_json",
  "ai_prompt_hint", "status", "sort_order", "del_flag", "create_by", "create_time"
, "locale") VALUES
('help-kkumz-sys-overview', 'KKUMZ', 'SYSTEM', '/overview', 'KkumzSystemOverview', 'Kkumz 시스템 개요', '시스템 안내', 'ant-design:compass-outlined',
 'AI 창업 기획서 생성, 멀티사이트 브랜드 빌더·배포, WebIDE 빌드 관제 및 크리에이터 팀빌딩 커뮤니티를 통합 운영하는 플랫폼 콘솔입니다.',
 'Kkumz(꿈즈)는 창업가·메이커가 자연어 아이디어만으로 브랜드 기획서, 디자인 스키마, 쇼핑몰·매거진·예약 복합 사이트를 생성하고 라이브 배포·팀원 모집까지 원스톱으로 진행하는 AI 창업 스튜디오입니다. 본 관리 콘솔은 AI 생성 규칙·프롬프트 품질, 배포된 멀티사이트·빌드 로그·API 키, 프로젝트 팀빌딩 및 신고 모더레이션을 통합 관제합니다.',
 '[{"name":"AI 창업 기획서 및 생성 규칙 관리","badge":"AI 스튜디오","description":"메이커가 생성한 창업 기획서 현황과 단계별 AI 생성 규칙·프롬프트 템플릿을 관리합니다."},{"name":"멀티사이트 배포 및 WebIDE 빌드 관제","badge":"배포 엔진","description":"즉시 디자인 반영(SDUI) 및 커스텀 코드 번들 배포 사이트, 빌드 로그와 런타임 API 키를 통제합니다."},{"name":"SyncShop·SyncCMS 멀티사이트 연동","badge":"생태계 연계","description":"생성된 브랜드 사이트의 상품·예약·결제는 SyncShop, 매거진·폼 콘텐츠는 SyncCMS와 연동해 서빙합니다."},{"name":"프로젝트 팀빌딩 및 신뢰안전 모더레이션","badge":"커뮤니티","description":"창업 프로젝트 팀원 모집 현황을 관제하고 스팸·저작권 침해 신고를 심사·조치합니다."}]'::jsonb,
 '{"sources":[{"origin":"창업가 웹 포털(Kkumz Web)","cycle":"실시간","description":"아이디어 프롬프트 입력, 자산 업로드, 사이트 배포 요청, 팀원 모집 및 커뮤니티 신고"},{"origin":"SyncLLM·SyncCMS·SyncShop","cycle":"실시간 연동","description":"AI 기획·디자인 생성 추론 및 멀티사이트 콘텐츠·커머스 백엔드 연동"}],"destinations":[{"target":"라이브 브랜드 멀티사이트","timing":"배포 즉시","description":"승인·빌드 완료된 브랜드 스토어, 매거진, 예약 복합 사이트 실시간 퍼블리싱"},{"target":"신뢰안전 감사 및 운영 지표","timing":"실시간 기록","description":"대시보드 핵심 지표, 신고 심사 이력 및 작업 감사 로그 적재"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1. AI 생성 규칙·프롬프트 설정","description":"창업 기획서와 사이트 생성에 쓰이는 AI 생성 규칙 및 프롬프트를 관리합니다.","path":"/studio/prompts"},{"title":"2. 창업 기획서·사이트 검수","description":"생성된 창업 기획서와 배포 사이트의 상태 및 노출 정책을 점검합니다.","path":"/deployment/sites"},{"title":"3. 빌드 로그·API 키 관제","description":"WebIDE 커스텀 번들 빌드 오류와 외부 호출용 API 키 한도를 관리합니다.","path":"/deployment/builds"},{"title":"4. 커뮤니티·신고 모더레이션","description":"팀빌딩 프로젝트와 접수된 스팸·저작권 신고를 심사하고 조치합니다.","path":"/community/moderation"}]'::jsonb,
 '[{"type":"플랫폼 최고관리자","title":"대시보드","path":"/dashboard/analysis","description":"기획서 생성량, 사이트 배포 성공률 및 신고 대기 건수 종합 확인"},{"type":"스튜디오 운영자","title":"창업 기획서","path":"/studio/ideas","description":"메이커별 생성 기획서 검토 및 AI 산출 품질 모니터링"},{"type":"배포·인프라 담당","title":"사이트 목록","path":"/deployment/sites","description":"라이브 멀티사이트 배포 상태, 도메인 및 비공개 전환 제어"},{"type":"커뮤니티 모더레이터","title":"신고 관리","path":"/community/moderation","description":"게시물·사이트 신고 사유 심사 및 제재 조치 처리"}]'::jsonb,
 '["사이트 빌드 실패가 반복되는 경우 빌드 로그(/deployment/builds)에서 오류 단계를 먼저 확인한 뒤 AI 생성 규칙을 조정하세요.","외부 연동(SyncCMS·SyncShop) 연결 주소와 키는 시스템 환경설정(/system/config)에서 런타임에 점검·변경할 수 있습니다."]'::jsonb,
 '["본 Admin 콘솔은 플랫폼 운영·배포 관제·모더레이션을 담당하며, 창업가의 실제 아이디어 입력과 브랜드 사이트 편집은 Kkumz 크리에이터 웹 포털에서 진행합니다.","저작권·상표권 침해 신고가 확정된 사이트는 신고 관리(/community/moderation)에서 즉시 노출 중지 처리하여 법적 리스크를 차단하세요."]'::jsonb,
 '[{"q":"즉시 디자인 반영 배포(SDUI)와 코드 커스텀 번들 배포의 차이는 무엇인가요?","a":"즉시 디자인 반영 배포는 테마 토큰과 섹션 구성을 DB 스키마로 즉시 반영하며, 코드 커스텀 번들 배포는 WebIDE에서 편집한 코드를 빌드 파이프라인으로 패키징해 배포합니다."},{"q":"사이트 생성 시 상품이나 매거진 기능은 별도 구축이 필요한가요?","a":"아니요. SyncShop과 SyncCMS 멀티사이트 API가 자동 연동되어 쇼핑몰·매거진·예약 폼 기능이 즉시 제공됩니다."}]'::jsonb,
 '최근 빌드 실패 사이트 목록과 처리 대기 중인 커뮤니티 신고 건수를 요약해줘',
 'ACTIVE', 0, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR'),

('help-kkumz-scr-dash', 'KKUMZ', 'SCREEN', '/dashboard/analysis', 'KkumzDashboardAnalysis', '대시보드', '대시보드', 'ant-design:dashboard-outlined',
 '창업 기획서 생성 추이, 멀티사이트 배포 현황, 빌드 성공률 및 커뮤니티 신고 처리 현황을 한눈에 확인합니다.',
 '플랫폼 운영 지표와 장애·신고 대기 건수를 실시간 집계해 보여주는 Kkumz 통합 관제 대시보드입니다.',
 '[{"name":"핵심 지표 KPI 카드","badge":"실시간","description":"누적 기획서, 활성 배포 사이트, 빌드 성공률, 미처리 신고 건수를 즉시 확인합니다."},{"name":"스튜디오·배포 추이 분석","badge":"트렌드","description":"기간별 기획서 생성량과 사이트 유형별 분포를 비교합니다."}]'::jsonb,
 '{"sources":[{"origin":"창업 기획서·배포 사이트·빌드·신고 원장","cycle":"실시간 집계","description":"스튜디오 및 커뮤니티 운영 데이터 통합 집계"}],"destinations":[{"target":"운영 관제 대시보드","timing":"화면 조회 시","description":"KPI 카드 및 차트 시각화"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 상단 핵심 KPI 확인","description":"배포 실패율이나 미처리 신고 건수가 급증했는지 확인합니다."},{"title":"2단계. 이상 항목 상세 이동","description":"문제가 감지된 빌드 로그 또는 신고 관리 화면으로 이동해 조치합니다."}]'::jsonb,
 '[{"type":"후속 작업","title":"사이트 목록","path":"/deployment/sites","description":"배포된 멀티사이트 상태 점검"},{"type":"후속 작업","title":"신고 관리","path":"/community/moderation","description":"미처리 커뮤니티 신고 심사"}]'::jsonb,
 '["매일 업무 시작 시 빌드 실패 건수와 미처리 신고 건수를 우선 점검하세요."]'::jsonb,
 '[]'::jsonb,
 '[]'::jsonb,
 '오늘 대시보드의 주요 운영 지표와 우선 조치가 필요한 항목을 정리해줘',
 'ACTIVE', 1, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR');

-- Bilingual en-US counterpart rows for sys_page_help
INSERT INTO "sys_page_help" ("id", "app_code", "help_type", "page_key", "route_name", "title", "category", "icon", "summary", "overview", "features_json", "lineage_json", "formulas_json", "steps_json", "related_screens_json", "tips_json", "warnings_json", "faq_json", "ai_prompt_hint", "status", "sort_order", "del_flag", "create_by", "create_time", "locale") VALUES
('help-kkumz-sys-overview-en', 'KKUMZ', 'SYSTEM', '/overview', 'KkumzSystemOverview', 'Kkumz System Overview', 'System Guides', 'ant-design:compass-outlined', 'Unified platform console operating AI startup blueprints, multi-site brand builder/deployment, WebIDE build control, and creator communities.', 'Kkumz is an AI startup studio letting founders and makers generate brand blueprints, design schemas, and integrated mall/magazine/booking sites from natural-language ideas through live deployment and team building; this admin console governs AI rules, prompts, multi-sites, build logs, API keys, team listings, and moderation.', '[{"name":"AI Startup Specs & Generation Rules","badge":"AI Studio","description":"Manage maker startup specs and per-stage AI generation rules."},{"name":"Multi-Site Deploy & WebIDE Build Control","badge":"Deploy Engine","description":"Control instant design sync (SDUI) and custom code bundle deployment sites, build logs, and runtime API keys."},{"name":"SyncShop & SyncCMS Multi-Site Integration","badge":"Ecosystem Sync","description":"Products, reservations, and payments on generated brand sites are served via SyncShop, while magazine and form content use SyncCMS."},{"name":"Project Team Building & Trust Moderation","badge":"Community","description":"Monitor startup team recruitment & review spam/copyright flags."}]'::jsonb, '{"sources":[{"origin":"Founder Web Portal (Kkumz Web)","cycle":"Real-Time","description":"Idea prompts, asset uploads, deploys, recruitment & reports"},{"origin":"SyncLLM·SyncCMS·SyncShop","cycle":"Real-Time Integration","description":"AI planning/design inference & multi-site CMS/commerce sync"}],"destinations":[{"target":"Live Brand Multi-Sites","timing":"Immediate on Deploy","description":"Live publishing of approved store, magazine & booking sites"},{"target":"Trust & Safety Audit Metrics","timing":"Real-Time Recording","description":"Store dashboard KPIs, report reviews, and audit logs"}]}'::jsonb, '[]'::jsonb, '[{"title":"1. Configure AI Rules & Prompts","description":"Manage AI generation rules and prompts for specs and sites.","path":"/studio/prompts"},{"title":"2. Review Startup Specs & Sites","description":"Check generated startup specs, deployed sites & visibility.","path":"/deployment/sites"},{"title":"3. Control Build Logs & API Keys","description":"Manage WebIDE custom bundle errors and external API quotas.","path":"/deployment/builds"},{"title":"4. Community & Report Moderation","description":"Review team-building projects and spam/copyright reports.","path":"/community/moderation"}]'::jsonb, '[{"type":"Platform Super Admin","title":"Dashboard","path":"/dashboard/analysis","description":"Overview of spec volume, site deploy rate & pending reports"},{"type":"Studio Operator","title":"Startup Plan","path":"/studio/ideas","description":"Review Maker Specs & Monitor AI Output Quality"},{"type":"Deployment & Infra Owner","title":"Site List","path":"/deployment/sites","description":"Control live multi-site deploy status, domain & visibility"},{"type":"Community Moderator","title":"Reports Moderation","path":"/community/moderation","description":"Review Post / Site Reports & Apply Sanctions"}]'::jsonb, '["If site builds fail repeatedly, check the failing stage in Build Logs (/deployment/builds) first before adjusting AI generation rules.","External integration URLs and keys (SyncCMS, SyncShop) can be checked and updated at runtime in System Settings (/system/config)."]'::jsonb, '["This Admin console governs platform ops, deployment monitoring, and moderation; founders enter ideas and edit brand sites on the Kkumz Creator Web Portal.","Immediately suspend display of sites with confirmed copyright or trademark claims in Moderation (/community/moderation) to mitigate legal risk."]'::jsonb, '[{"q":"How does Instant Design Deploy (SDUI) differ from Custom Bundle?","a":"Instant design sync (SDUI) persists theme tokens and sections directly to DB schemas, whereas custom code bundle deploy packages WebIDE code via the build pipeline."},{"q":"Do store or magazine features require separate setup on site gen?","a":"No. SyncShop and SyncCMS multi-site APIs integrate automatically so storefront, magazine, and booking forms work immediately."}]'::jsonb, 'Summarize recent failed site builds and open community reports', 'ACTIVE', 0, 0, 'admin', CURRENT_TIMESTAMP, 'en-US'),
('help-kkumz-scr-dash-en', 'KKUMZ', 'SCREEN', '/dashboard/analysis', 'KkumzDashboardAnalysis', 'Dashboard', 'Dashboard', 'ant-design:dashboard-outlined', 'View startup blueprint trends, multi-site deployments, build success rates, and pending community moderation reports at a glance.', 'Unified Kkumz operations dashboard aggregating platform KPIs, outages, and pending moderation reports in real time.', '[{"name":"Core Metrics KPI Cards","badge":"Real-Time","description":"Check cumulative specs, live sites, build pass & open reports."},{"name":"Studio & Deployment Trend Analytics","badge":"Trend","description":"Compare spec volume by period and site type breakdown."}]'::jsonb, '{"sources":[{"origin":"Specs, Deployed Sites, Builds & Reports","cycle":"Real-Time Aggregation","description":"Unified Rollup of Studio & Community Ops"}],"destinations":[{"target":"Operations Control Dashboard","timing":"On Screen View","description":"KPI Cards & Chart Visualization"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Review Top Core KPIs","description":"Check for spikes in build failures or pending reports."},{"title":"Step 2. Drill Down into Anomalies","description":"Navigate to Build Logs or Reports to resolve detected issues."}]'::jsonb, '[{"type":"Follow-up Action","title":"Site List","path":"/deployment/sites","description":"Inspect Deployed Multi-Site Status"},{"type":"Follow-up Action","title":"Reports Moderation","path":"/community/moderation","description":"Review Pending Community Reports"}]'::jsonb, '["Review failed builds and pending reports at start of each day."]'::jsonb, '[]'::jsonb, '[]'::jsonb, 'Summarize today''s dashboard KPIs and priority action items', 'ACTIVE', 1, 0, 'admin', CURRENT_TIMESTAMP, 'en-US');

-- ============================================================================
-- Kkumz 도메인 주요 운영 화면 가이드 보강 (help_type = 'SCREEN')
-- ============================================================================
INSERT INTO "sys_page_help" (
  "id", "app_code", "help_type", "page_key", "route_name", "title", "category", "icon",
  "summary", "overview", "features_json", "lineage_json", "formulas_json",
  "steps_json", "related_screens_json", "tips_json", "warnings_json", "faq_json",
  "ai_prompt_hint", "status", "sort_order", "del_flag", "create_by", "create_time"
, "locale") VALUES
('help-kkumz-scr-ideas', 'KKUMZ', 'SCREEN', '/studio/ideas', 'KkumzStudioIdeas', '창업 기획서', 'AI 스튜디오', 'ant-design:bulb-outlined',
 '메이커가 생성한 AI 창업 기획서 목록을 조회하고 브랜드 컨셉, 사이트 유형 및 생성 상태를 검수합니다.',
 '창업가 웹 포털에서 입력된 아이디어로부터 생성된 브랜드 기획서와 기능 추천 결과를 조회·관리하는 화면입니다.',
 '[{"name":"기획서 조건 검색 및 상태 필터","badge":"조회","description":"사이트 유형(쇼핑몰·브랜드·복합), 작성자, 상태별로 기획서를 필터링합니다."},{"name":"기획서 상세 및 연결 사이트 확인","badge":"검수","description":"AI가 생성한 브랜드 스토리, 레이아웃 구성과 배포 연결 여부를 확인합니다."}]'::jsonb,
 '{"sources":[{"origin":"Kkumz 크리에이터 스튜디오","cycle":"실시간","description":"아이디어 입력 및 AI 기획서 생성 결과"}],"destinations":[{"target":"사이트 목록 (/deployment/sites)","timing":"배포 실행 시","description":"확정된 기획서 기반 멀티사이트 프로비저닝"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 유형·상태 필터 조회","description":"상단 검색 바에서 사이트 유형이나 키워드로 기획서를 조회합니다."},{"title":"2단계. 기획서 상세 검수","description":"대상 행을 클릭해 생성된 섹션 구성과 추천 기능을 확인합니다."}]'::jsonb,
 '[{"type":"연관 화면","title":"AI 생성 규칙","path":"/studio/prompts","description":"기획서 생성 품질 제어 규칙 편집"},{"type":"후속 화면","title":"사이트 목록","path":"/deployment/sites","description":"기획서로부터 배포된 사이트 관리"}]'::jsonb,
 '["생성 품질이 낮은 카테고리가 반복되면 AI 생성 규칙(/studio/prompts)에서 해당 업종 가이드를 보완하세요."]'::jsonb,
 '["이미 라이브 배포와 연결된 기획서를 삭제하면 연관 사이트 메타데이터에 영향을 줄 수 있습니다."]'::jsonb,
 '[]'::jsonb,
 '최근 생성된 창업 기획서의 사이트 유형별 분포와 품질 이슈를 요약해줘',
 'ACTIVE', 2, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR'),

('help-kkumz-scr-prompts', 'KKUMZ', 'SCREEN', '/studio/prompts', 'KkumzStudioPrompts', 'AI 생성 규칙', 'AI 스튜디오', 'ant-design:sliders-outlined',
 '창업 기획서, 브랜드 디자인, 초기 콘텐츠 및 코드 패치에 적용되는 스튜디오 AI 생성 규칙을 관리합니다.',
 '업종별 디자인 아키타입, 컴포넌트 추천 기준 및 단계별 스튜디오 AI 생성 정책을 편집하고 활성화 상태를 제어하는 화면입니다.',
 '[{"name":"단계별 AI 생성 규칙 편집","badge":"품질 제어","description":"기획·브랜딩·콘텐츠 생성 단계별 지침과 제약 조건을 조정합니다."},{"name":"활성 상태 즉시 전환","badge":"실시간 반영","description":"규칙 저장 즉시 신규 스튜디오 생성 요청에 반영됩니다."}]'::jsonb,
 '{"sources":[{"origin":"스튜디오 AI 생성 규칙 설정","cycle":"실시간","description":"단계별 생성 정책 및 아키타입 기준"}],"destinations":[{"target":"Kkumz AI 생성 파이프라인","timing":"저장 즉시","description":"창업 기획서 및 사이트 초기 콘텐츠 생성 품질 제어"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 대상 규칙 검색","description":"생성 단계나 규칙명으로 수정할 항목을 찾습니다."},{"title":"2단계. 규칙 수정 및 저장","description":"지침 내용을 보완하고 저장해 다음 생성 요청부터 반영합니다."}]'::jsonb,
 '[{"type":"연관 화면","title":"프롬프트","path":"/system/prompt","description":"시스템 공통 프롬프트 템플릿 및 버전 이력 관리"}]'::jsonb,
 '["규칙 수정 전 기존 본문을 별도 메모하거나 프롬프트 화면의 버전 이력을 활용하세요."]'::jsonb,
 '["필수 출력 JSON 구조 지침을 삭제하면 스튜디오 화면 렌더링 파싱 오류가 발생할 수 있습니다."]'::jsonb,
 '[]'::jsonb,
 '현재 활성화된 스튜디오 AI 생성 규칙 목록과 역할을 정리해줘',
 'ACTIVE', 3, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR'),

('help-kkumz-scr-sites', 'KKUMZ', 'SCREEN', '/deployment/sites', 'KkumzDeploymentSites', '사이트 목록', '멀티사이트 배포', 'ant-design:global-outlined',
 '배포된 브랜드 멀티사이트의 도메인, 배포 모드, 공개 상태 및 SyncShop·SyncCMS 연동 현황을 관리합니다.',
 '메이커가 발행한 모든 쇼핑몰·브랜드 웹·복합 사이트의 가동 상태를 점검하고 공개·중지 제어 및 배포 정보를 관리하는 화면입니다.',
 '[{"name":"사이트 상태 및 공개 제어","badge":"운영 제어","description":"라이브 사이트의 공개·비공개 전환과 배포 모드(SDUI/커스텀 번들)를 관리합니다."},{"name":"도메인 및 연동 상태 점검","badge":"연동 확인","description":"각 사이트의 접속 주소와 커머스·CMS 연동 상태를 확인합니다."}]'::jsonb,
 '{"sources":[{"origin":"배포 사이트 마스터","cycle":"실시간","description":"스튜디오에서 생성·발행된 사이트 메타데이터"}],"destinations":[{"target":"퍼블릭 멀티사이트 라우터","timing":"상태 변경 즉시","description":"외부 방문객 접근 허용 및 차단 제어"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 사이트 검색 및 필터","description":"사이트명, 유형, 배포 상태로 대상 사이트를 조회합니다."},{"title":"2단계. 상세 점검 및 상태 조치","description":"도메인·배포 모드를 확인하고 필요 시 비공개 전환 또는 재배포를 진행합니다."}]'::jsonb,
 '[{"type":"후속 화면","title":"빌드 로그","path":"/deployment/builds","description":"사이트별 빌드 실행 및 오류 로그 상세 조회"},{"type":"연관 화면","title":"신고 관리","path":"/community/moderation","description":"신고 접수된 사이트 심사 및 노출 중지"}]'::jsonb,
 '["신고가 접수된 사이트는 상세 확인 후 임시 비공개로 전환한 뒤 심사를 진행할 수 있습니다."]'::jsonb,
 '["운영 중인 쇼핑몰 사이트를 중지하면 방문객 결제와 예약 접근이 즉시 차단됩니다."]'::jsonb,
 '[]'::jsonb,
 '현재 라이브 배포 중인 사이트 목록과 최근 배포 오류가 발생한 사이트를 알려줘',
 'ACTIVE', 4, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR'),

('help-kkumz-scr-builds', 'KKUMZ', 'SCREEN', '/deployment/builds', 'KkumzDeploymentBuilds', '빌드 로그', '멀티사이트 배포', 'ant-design:code-outlined',
 'WebIDE 커스텀 번들 빌드 실행 이력, 소요 시간 및 실패 오류 로그를 상세 조회합니다.',
 '커스텀 코드 편집 후 실행된 사이트 빌드 파이프라인의 성공·실패 상태와 단계별 콘솔 로그를 추적하는 진단 화면입니다.',
 '[{"name":"빌드 상태별 필터링","badge":"진단","description":"성공, 실패, 진행 중 상태와 사이트별로 빌드 이력을 필터링합니다."},{"name":"실패 원인 콘솔 로그 열람","badge":"트러블슈팅","description":"컴파일·번들 단계 오류 메시지를 확인해 코드 패치 원인을 파악합니다."}]'::jsonb,
 '{"sources":[{"origin":"WebIDE 샌드박스 빌드 러너","cycle":"빌드 실행 시","description":"번들 빌드 상태 및 출력 로그"}],"destinations":[{"target":"사이트 배포 이력 및 AI 패치","timing":"빌드 완료 시","description":"성공 번들 배포 또는 실패 시 빌드 수정 가이드 제공"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 실패 빌드 필터 조회","description":"상태 필터에서 실패 건을 선택해 문제 빌드를 선별합니다."},{"title":"2단계. 상세 로그 확인","description":"행 액션에서 로그 보기를 열어 오류 원인과 발생 파일을 확인합니다."}]'::jsonb,
 '[{"type":"선행 화면","title":"사이트 목록","path":"/deployment/sites","description":"대상 사이트의 배포 모드 및 설정 확인"}]'::jsonb,
 '["동일 오류 패턴이 반복되는 빌드는 AI 빌드 패치 기능 실행 이력을 함께 확인하세요."]'::jsonb,
 '[]'::jsonb,
 '[]'::jsonb,
 '최근 실패한 사이트 빌드 로그의 주요 원인을 분류해줘',
 'ACTIVE', 5, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR'),

('help-kkumz-scr-apikeys', 'KKUMZ', 'SCREEN', '/deployment/api-keys', 'KkumzDeploymentApiKeys', 'API 키', '멀티사이트 배포', 'ant-design:key-outlined',
 '배포된 멀티사이트와 외부 클라이언트가 Kkumz 런타임 API를 호출할 때 사용하는 액세스 키와 분당 호출 한도를 관리합니다.',
 '사이트별 API 액세스 키 발급, 회수, 만료일 및 분당 호출 제한(RPM) 정책을 통제하는 인증 관리 화면입니다.',
 '[{"name":"사이트 전용 API 키 발급·회수","badge":"인증","description":"사이트 단위로 키를 발급하고 유출 의심 시 즉시 비활성화·회수합니다."},{"name":"분당 호출 한도(RPM) 제어","badge":"트래픽 보호","description":"과도한 외부 요청을 차단하기 위한 키별 호출 한도를 설정합니다."}]'::jsonb,
 '{"sources":[{"origin":"사이트 API 키 원장","cycle":"실시간","description":"발급 키, 상태, 만료일 및 RPM 설정"}],"destinations":[{"target":"Kkumz 공개 API 인증 필터","timing":"저장 즉시","description":"실시간 토큰 검증 및 Rate Limit 적용"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 발급 키 현황 조회","description":"사이트명이나 활성 상태로 등록된 API 키를 확인합니다."},{"title":"2단계. 신규 발급 또는 한도 조정","description":"신규 키를 발급하거나 RPM 한도·만료일을 수정합니다."}]'::jsonb,
 '[{"type":"연관 화면","title":"시스템 환경설정","path":"/system/config","description":"기본 분당 호출 한도 및 공개 Base URL 확인"}]'::jsonb,
 '["발급 직후에만 원문 키가 노출되므로 안전한 비밀 저장소에 보관하세요."]'::jsonb,
 '["활성 키를 회수하면 해당 키를 사용하는 라이브 사이트의 동적 데이터 호출이 즉시 차단됩니다."]'::jsonb,
 '[]'::jsonb,
 '만료 임박했거나 비활성화된 멀티사이트 API 키 목록을 확인해줘',
 'ACTIVE', 6, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR'),

('help-kkumz-scr-projects', 'KKUMZ', 'SCREEN', '/community/projects', 'KkumzCommunityProjects', '프로젝트·팀원', '커뮤니티 운영', 'ant-design:team-outlined',
 '창업 프로젝트 팀빌딩 모집 공고, 직군별 합류 현황 및 협업 상태를 관리합니다.',
 '메이커와 협업 개발자·디자이너·마케터가 참여하는 창업 프로젝트 팀빌딩 목록을 조회하고 허위·부적절 모집을 관리하는 화면입니다.',
 '[{"name":"팀빌딩 프로젝트 현황 조회","badge":"커뮤니티","description":"모집 직군, 참여 인원, 진행 상태별로 프로젝트를 검색합니다."},{"name":"노출 상태 및 모집 관리","badge":"운영","description":"정책 위반 모집 공고를 비공개 처리하거나 마감 상태를 조정합니다."}]'::jsonb,
 '{"sources":[{"origin":"커뮤니티 팀빌딩 게시판","cycle":"실시간","description":"창업가 모집 공고 및 합류 신청 내역"}],"destinations":[{"target":"Kkumz 커뮤니티 광장","timing":"변경 즉시","description":"모집 공고 노출 및 참여 상태 반영"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 모집 프로젝트 조회","description":"직군·상태 필터로 진행 중인 프로젝트를 확인합니다."},{"title":"2단계. 상세 검토 및 조치","description":"모집 조건을 확인하고 필요 시 노출 상태를 조정합니다."}]'::jsonb,
 '[{"type":"연관 화면","title":"신고 관리","path":"/community/moderation","description":"허위 모집·스팸 신고 접수 건 심사"}]'::jsonb,
 '["신규 모집 공고 중 신고 이력이 있는 계정의 게시물을 우선 검토하세요."]'::jsonb,
 '[]'::jsonb,
 '[]'::jsonb,
 '현재 활발히 팀원을 모집 중인 창업 프로젝트 현황을 요약해줘',
 'ACTIVE', 7, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR'),

('help-kkumz-scr-moderation', 'KKUMZ', 'SCREEN', '/community/moderation', 'KkumzCommunityModeration', '신고 관리', '커뮤니티 운영', 'ant-design:safety-outlined',
 '커뮤니티 게시물과 배포 사이트에 접수된 스팸, 저작권 침해, 허위 모집 신고를 심사하고 제재 조치를 처리합니다.',
 '신뢰안전 모더레이터가 사용자 신고 사유와 증빙을 검토해 기각, 콘텐츠 숨김, 사이트 비공개 등 제재를 집행하고 처리 이력을 기록하는 화면입니다.',
 '[{"name":"신고 사유별 대기열 관리","badge":"모더레이션","description":"스팸·저작권 침해·허위 정보 등 사유와 처리 상태별로 신고를 분류합니다."},{"name":"원클릭 심사 및 조치 기록","badge":"신뢰안전","description":"심사 결과와 조치 사유를 기록하고 대상자에게 안내 알림을 발송합니다."}]'::jsonb,
 '{"sources":[{"origin":"사용자 신고 접수","cycle":"실시간","description":"커뮤니티 게시물 및 라이브 사이트 신고 데이터"}],"destinations":[{"target":"게시물·사이트 노출 제어 및 알림 발송","timing":"심사 확정 즉시","description":"제재 반영 및 심사 결과 통보"}]}'::jsonb,
 '[]'::jsonb,
 '[{"title":"1단계. 미처리 신고 조회","description":"처리 대기 필터를 선택해 우선 심사가 필요한 신고를 확인합니다."},{"title":"2단계. 사유·증빙 검토 및 조치 확정","description":"신고 대상 콘텐츠를 확인한 뒤 기각 또는 제재 조치를 확정합니다."}]'::jsonb,
 '[{"type":"연관 화면","title":"사이트 목록","path":"/deployment/sites","description":"제재 대상 사이트 상태 확인"},{"type":"연관 화면","title":"프로젝트·팀원","path":"/community/projects","description":"신고된 팀빌딩 공고 확인"}]'::jsonb,
 '["저작권·상표권 침해 신고는 접수 즉시 우선 심사해 분쟁 확산을 방지하세요."]'::jsonb,
 '["제재 확정 시 작성자 또는 사이트 운영자에게 알림이 발송되므로 조치 사유를 객관적으로 기록하세요."]'::jsonb,
 '[]'::jsonb,
 '현재 심사 대기 중인 신고 목록을 사유별로 분류해 요약해줘',
 'ACTIVE', 8, 0, 'admin', CURRENT_TIMESTAMP, 'ko-KR');

-- Bilingual en-US counterpart rows for sys_page_help
INSERT INTO "sys_page_help" ("id", "app_code", "help_type", "page_key", "route_name", "title", "category", "icon", "summary", "overview", "features_json", "lineage_json", "formulas_json", "steps_json", "related_screens_json", "tips_json", "warnings_json", "faq_json", "ai_prompt_hint", "status", "sort_order", "del_flag", "create_by", "create_time", "locale") VALUES
('help-kkumz-scr-ideas-en', 'KKUMZ', 'SCREEN', '/studio/ideas', 'KkumzStudioIdeas', 'Startup Plan', 'AI Studio', 'ant-design:bulb-outlined', 'Browse AI startup blueprints created by makers and review brand concepts, site types, and generation status.', 'Screen for browsing and managing brand blueprints and feature recommendations generated from founder portal ideas.', '[{"name":"Spec Criteria Search & Status Filter","badge":"Search","description":"Filter specs by site type (Store/Brand/Hybrid), author & state."},{"name":"Inspect Spec Details & Linked Sites","badge":"Inspection","description":"Review AI brand story, layout structure & deploy linkage."}]'::jsonb, '{"sources":[{"origin":"Kkumz Creator Studio","cycle":"Real-Time","description":"Idea Input & AI Functional Spec Output"}],"destinations":[{"target":"Site List (/deployment/sites)","timing":"On Deploy Execution","description":"Multi-Site Provisioning from Finalized Spec"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Filter by Type & Status","description":"Search specs in top bar by site type or keyword."},{"title":"Step 2. Review Spec in Detail","description":"Click a row to inspect generated sections and features."}]'::jsonb, '[{"type":"Related Screen","title":"AI Generation Rules","path":"/studio/prompts","description":"Edit Spec Generation Quality Rules"},{"type":"Follow-up Screen","title":"Site List","path":"/deployment/sites","description":"Manage Sites Deployed from Specs"}]'::jsonb, '["If generation quality is repeatedly low for a category, enhance that industry''s guidelines in AI Generation Rules (/studio/prompts)."]'::jsonb, '["Deleting a blueprint already linked to a live deployment may affect associated site metadata."]'::jsonb, '[]'::jsonb, 'Summarize recent startup specs by site type and quality issues', 'ACTIVE', 2, 0, 'admin', CURRENT_TIMESTAMP, 'en-US'),
('help-kkumz-scr-prompts-en', 'KKUMZ', 'SCREEN', '/studio/prompts', 'KkumzStudioPrompts', 'AI Generation Rules', 'AI Studio', 'ant-design:sliders-outlined', 'Manage Studio AI generation rules applied to startup blueprints, brand designs, initial content, and code patches.', 'Screen for editing industry design archetypes, component recommendation criteria, and stage-by-stage Studio AI generation policies.', '[{"name":"Edit Per-Stage AI Generation Rules","badge":"Quality Control","description":"Tune per-stage guidelines and constraints for AI generation."},{"name":"Instant Active Status Toggle","badge":"Real-Time Sync","description":"Saved rules apply immediately to new Studio runs."}]'::jsonb, '{"sources":[{"origin":"Configure Studio AI Generation Rules","cycle":"Real-Time","description":"Per-Stage Generation Policy & Archetypes"}],"destinations":[{"target":"Kkumz AI Generation Pipeline","timing":"Immediate on Save","description":"Quality Control for Specs & Initial Site Content"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Search Target Rules","description":"Find items to edit by stage or rule name."},{"title":"Step 2. Edit & Save Rules","description":"Refine rules and save to apply to subsequent generations."}]'::jsonb, '[{"type":"Related Screen","title":"Prompt","path":"/system/prompt","description":"Manage System Prompt Templates & Versions"}]'::jsonb, '["Note prior content or use Prompt version history before edits."]'::jsonb, '["Removing required output JSON schema instructions may cause parsing errors when rendering the Studio screen."]'::jsonb, '[]'::jsonb, 'List active Studio AI generation rules and summarize roles', 'ACTIVE', 3, 0, 'admin', CURRENT_TIMESTAMP, 'en-US'),
('help-kkumz-scr-sites-en', 'KKUMZ', 'SCREEN', '/deployment/sites', 'KkumzDeploymentSites', 'Site List', 'Multi-Site Deployment', 'ant-design:global-outlined', 'Manage deployed brand multi-site domains, deployment modes, visibility status, and SyncShop/SyncCMS integrations.', 'Screen for checking uptime, controlling public/suspended states, and managing deployments across all maker-published malls and brand sites.', '[{"name":"Site Status & Visibility Control","badge":"Operations Control","description":"Control live site visibility and mode (SDUI / custom bundle)."},{"name":"Inspect Domains & Sync Status","badge":"Verify Connection","description":"Check each site''s URL and commerce/CMS integration health."}]'::jsonb, '{"sources":[{"origin":"Deployed Sites Master","cycle":"Real-Time","description":"Metadata of Sites Generated in Studio"}],"destinations":[{"target":"Public Multi-Site Router","timing":"Immediate on Status Change","description":"Control External Visitor Allow & Block"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Search & Filter Sites","description":"Query target sites by site name, type, and deploy status."},{"title":"Step 2. Inspect Details & Update Status","description":"Verify domain/deploy mode and toggle private or redeploy."}]'::jsonb, '[{"type":"Follow-up Screen","title":"Build Logs","path":"/deployment/builds","description":"Inspect Per-Site Build Runs & Error Logs"},{"type":"Related Screen","title":"Reports Moderation","path":"/community/moderation","description":"Review Reported Sites & Suspend Exposure"}]'::jsonb, '["For reported sites, review details and temporarily set them to private before conducting moderation review."]'::jsonb, '["Suspending a live store blocks visitor checkout & bookings."]'::jsonb, '[]'::jsonb, 'List live deployed sites and any sites with recent build errors', 'ACTIVE', 4, 0, 'admin', CURRENT_TIMESTAMP, 'en-US'),
('help-kkumz-scr-builds-en', 'KKUMZ', 'SCREEN', '/deployment/builds', 'KkumzDeploymentBuilds', 'Build Logs', 'Multi-Site Deployment', 'ant-design:code-outlined', 'Inspect custom WebIDE bundle build history, execution duration, and failure error logs in detail.', 'Diagnostic screen tracking site build pipeline success/failure status and stage-by-stage console logs after custom code edits.', '[{"name":"Filter by Build Status","badge":"Diagnose","description":"Filter build history by site and Success/Failed/Running state."},{"name":"View Failure Cause Console Logs","badge":"Troubleshooting","description":"Inspect compile/bundle error messages to pinpoint fix causes."}]'::jsonb, '{"sources":[{"origin":"WebIDE Sandbox Build Runner","cycle":"On Build Start","description":"Bundle Build Status & Output Logs"}],"destinations":[{"target":"Site Deploy History & AI Patches","timing":"On Build Complete","description":"Deploy Passed Bundles or Show Build Fix Guide"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Filter Failed Builds","description":"Filter by Failed status to isolate broken builds."},{"title":"Step 2. Check Detailed Logs","description":"Open View Logs from row actions to check errors and file paths."}]'::jsonb, '[{"type":"Prerequisite Screen","title":"Site List","path":"/deployment/sites","description":"Check Target Site Deploy Mode & Config"}]'::jsonb, '["For recurring build errors, check AI Build Patch history too."]'::jsonb, '[]'::jsonb, '[]'::jsonb, 'Classify top causes of recent failed site builds', 'ACTIVE', 5, 0, 'admin', CURRENT_TIMESTAMP, 'en-US'),
('help-kkumz-scr-apikeys-en', 'KKUMZ', 'SCREEN', '/deployment/api-keys', 'KkumzDeploymentApiKeys', 'API Key', 'Multi-Site Deployment', 'ant-design:key-outlined', 'Manage access keys and per-minute rate limits used by deployed multi-sites and external clients calling Kkumz runtime APIs.', 'Auth management screen controlling per-site API access key issuance, revocation, expiration, and per-minute rate limits (RPM).', '[{"name":"Issue & Revoke Site-Specific API Keys","badge":"Authentication","description":"Issue per-site keys and instantly revoke on suspected leak."},{"name":"Enforce Requests-per-Minute (RPM) Limit","badge":"Traffic Protection","description":"Set per-key rate limits to block excessive external calls."}]'::jsonb, '{"sources":[{"origin":"Site API Key Ledger","cycle":"Real-Time","description":"Configure Key, Status, Expiry & RPM Limit"}],"destinations":[{"target":"Kkumz Public API Auth Filter","timing":"Immediate on Save","description":"Real-Time Token Auth & Rate Limit Enforce"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Check Issued Key Status","description":"Check registered API keys by site name or active status."},{"title":"Step 2. Issue New Key or Adjust Quota","description":"Issue new keys or update RPM limits and expiry."}]'::jsonb, '[{"type":"Related Screen","title":"System Configuration","path":"/system/config","description":"Check Default RPM Limits & Public Base URL"}]'::jsonb, '["Raw key shows only once on creation; store in a secret vault."]'::jsonb, '["Revoking an active key immediately blocks dynamic data requests from live sites using that key."]'::jsonb, '[]'::jsonb, 'List expiring or inactive multi-site API keys', 'ACTIVE', 6, 0, 'admin', CURRENT_TIMESTAMP, 'en-US'),
('help-kkumz-scr-projects-en', 'KKUMZ', 'SCREEN', '/community/projects', 'KkumzCommunityProjects', 'Projects & Team', 'Community Operations', 'ant-design:team-outlined', 'Manage startup team recruitment posts, roles & collaboration.', 'Browse startup team-building listings connecting makers with developers, designers, and marketers, and moderate fraudulent posts.', '[{"name":"Query Team Building Projects","badge":"Community","description":"Search projects by open roles, team size, and status."},{"name":"Display Status & Recruitment Mgmt","badge":"Operations","description":"Hide policy-violating recruitment posts or close status."}]'::jsonb, '{"sources":[{"origin":"Community Team Building Board","cycle":"Real-Time","description":"Founder Recruitment & Join Applications"}],"destinations":[{"target":"Kkumz Community Plaza","timing":"Immediate on Change","description":"Publish Recruitment Posts & Track Status"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Query Recruiting Projects","description":"Inspect active projects by role and status."},{"title":"Step 2. Review & Take Action","description":"Check terms and adjust visibility if needed."}]'::jsonb, '[{"type":"Related Screen","title":"Reports Moderation","path":"/community/moderation","description":"Review Fraudulent Post & Spam Reports"}]'::jsonb, '["Prioritize reviewing new posts from accounts with prior reports."]'::jsonb, '[]'::jsonb, '[]'::jsonb, 'Summarize startup projects currently recruiting team members', 'ACTIVE', 7, 0, 'admin', CURRENT_TIMESTAMP, 'en-US'),
('help-kkumz-scr-moderation-en', 'KKUMZ', 'SCREEN', '/community/moderation', 'KkumzCommunityModeration', 'Reports Moderation', 'Community Operations', 'ant-design:safety-outlined', 'Review spam, copyright infringement, and fraudulent recruitment reports on community posts and deployed sites and apply sanctions.', 'Screen where trust & safety moderators review user reports and evidence, enforce sanctions (dismiss, hide, suspend site), and log actions.', '[{"name":"Queue Management by Report Reason","badge":"Moderation","description":"Classify reports by reason (spam, copyright, fraud) & status."},{"name":"1-Click Review & Action Logging","badge":"Trust & Safety","description":"Record review verdict and reason, then send notice to user."}]'::jsonb, '{"sources":[{"origin":"Receive User Report","cycle":"Real-Time","description":"Community Post & Live Site Report Data"}],"destinations":[{"target":"Control Post / Site Visibility & Alerts","timing":"Immediate on Review Lock","description":"Apply Sanctions & Notify Review Verdict"}]}'::jsonb, '[]'::jsonb, '[{"title":"Step 1. Query Pending Reports","description":"Filter by Pending to review priority reports first."},{"title":"Step 2. Review Evidence & Confirm Action","description":"Inspect reported content and confirm dismissal or sanction."}]'::jsonb, '[{"type":"Related Screen","title":"Site List","path":"/deployment/sites","description":"Check Sanctioned Site Status"},{"type":"Related Screen","title":"Projects & Team","path":"/community/projects","description":"Inspect Reported Team Posts"}]'::jsonb, '["Prioritize copyright/trademark reports immediately to curb risk."]'::jsonb, '["Confirming a sanction notifies the author or site operator, so document the action reason objectively."]'::jsonb, '[]'::jsonb, 'Summarize pending community reports grouped by report reason', 'ACTIVE', 8, 0, 'admin', CURRENT_TIMESTAMP, 'en-US');