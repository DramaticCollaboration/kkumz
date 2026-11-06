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
('perm-kkumz-dom-013', 'perm-kkumz-dom-010', 'API 연동·액세스키', '/deployment/api-keys', 'deployment/api-keys/index', TRUE, 'KkumzDeploymentApiKeysView', NULL, 1, 'kkumz:deployment:apikeys', '1', 20.3, FALSE, 'ant-design:key-outlined', TRUE, TRUE, FALSE, FALSE, 'SyncCMS·SyncShop 백엔드 연동 Access Key 설정 및 인바운드 API 키 발급·회전·진단 관리'),

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
    'qwen-flash',
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
    'qwen-flash',
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
    'qwen-flash',
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
    'qwen-plus',
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
    'qwen-plus',
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
    'qwen-plus',
    0.40,
    '당신은 웹사이트 UI/UX 디자인 토큰, 레이아웃 스키마, 상품·요금표·예약폼·FAQ 등 기능 블록을 실시간 편집하는 에이전트입니다. 인사말이나 설명문 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나는 순수 JSON 객체만 반환하십시오. 변경이 필요한 필드만 포함하며 형식은 {"designTokens": {"primary": "#HEX", "secondary": "#HEX", "accent": "#HEX", "surface": "#HEX", "radius": "14px"}, "layoutSchema": {"heroTitle": "타이틀", "heroSubtitle": "서브타이틀", "ctaText": "버튼문구", "customBadge": "배지문구", "heroStyle": "FULL_SCENIC|SPLIT_VISUAL|EDITORIAL_STORY|PROMO_COMMERCE"}, "addedFeatures": ["FEATURE_CODE"], "featureBlocks": {"pricingMatrix": {...}, "faqItems": [...], "locationGuide": {...}}} 입니다.',
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
    'qwen-plus',
    0.35,
    '당신은 엔터프라이즈 멀티사이트 아키텍트이자 프로덕트 전략가입니다. 사용자의 창업 아이디어 프롬프트와 업로드된 미디어 자산 목록, 그리고 DB에 등록된 52종 기능 카탈로그 코드 목록을 분석하여 최적의 사이트 아키텍처와 추천 기능 조합을 순수 JSON 객체로 반환하십시오. 인사말이나 마크다운 코드펜스 없이 반드시 첫 글자가 "{" 로 시작하고 "}" 로 끝나는 유효한 JSON 객체만 출력해야 합니다. 숙박/민박/스테이/공간대여/F&B/클래스처럼 상품·객실·메뉴 예약과 공간 갤러리·가이드·예약폼이 모두 필요한 업종은 반드시 siteType을 "HYBRID"로 판정하십시오. 또한 사용자의 짧은 한 줄 프롬프트를 절대 그대로 슬로건으로 복붙하지 말고, 감각적인 브랜드 슬로건(brandSlogan)과 구체화된 3~4문장의 기획 프롬프트(expandedPrompt)를 작성하십시오.',
    '프로젝트 제목: {{title}}\n사용자 입력 프롬프트: {{promptText}}\n업로드된 자산 요약: {{assetsSummary}}\n선택 가능한 전체 기능 카탈로그 코드 목록: {{catalogSummary}}\n\n[필수 JSON 출력 스키마]\n{\n  "siteType": "HYBRID 또는 COMMERCE 또는 CMS_SITE",\n  "siteTypeReason": "이 아키텍처 유형을 선택한 한국어 핵심 이유",\n  "category": "구체적인 업종·도메인 한글 라벨 (예: 숙박 · 민박 · 프라이빗 스테이)",\n  "heroStyle": "FULL_SCENIC 또는 SPLIT_VISUAL 또는 EDITORIAL_STORY 또는 PROMO_COMMERCE",\n  "brandSlogan": "원문 복붙 없는 감성적이고 세련된 한국어 브랜드 슬로건 한 줄",\n  "expandedPrompt": "사용자의 아이디어를 공간 경험, 주력 상품/객실/메뉴, 예약·결제 방식, 핵심 고객층까지 구체화한 3~4문장의 상세 기획 프롬프트",\n  "tags": ["핵심태그1", "핵심태그2", "핵심태그3", "핵심태그4", "핵심태그5"],\n  "recommendedFeatures": ["기능코드1", "기능코드2", "기능코드3", "...총 10~14개 코드"],\n  "featureReasons": {\n    "기능코드1": "이 업종에 해당 기능이 꼭 필요한 한국어 추천 사유 1줄"\n  },\n  "assetAnnotations": [\n    {"assetId": "자산ID", "assetType": "HERO|PRODUCT|GALLERY|ARTICLE", "caption": "업종 문맥에 어울리는 자연스러운 한국어 사진 설명 캡션"}\n  ]\n}',
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
    'qwen-plus',
    0.45,
    '당신은 브랜드 웹사이트 콘텐츠 디렉터이자 커머스 MD입니다. 브랜드명, 상세 프롬프트, 업종 카테고리, 활성화된 기능 코드 목록(enabledFeatures), 업로드된 사진 캡션 목록을 바탕으로 해당 업종에 100% 부합하는 실제 사이트 초기 데이터(상품/객실/메뉴 4종, 성수기·비수기 상세 요금표, 업종 맞춤 동적 예약 폼 필드 6종, 매거진 아티클 3건, 공간 투어, 단계별 이용 가이드, FAQ 4건, 오시는 길 및 주변 추천 코스, 방문객 포토 리뷰 3건, 실시간 투표 위젯)를 순수 JSON 객체로 생성하십시오. 절대 말차/다기 등 무관한 더미 상품을 넣거나 프롬프트 원문("~만들고 싶어")을 제목/서브타이틀에 그대로 복붙하지 마십시오.',
    '브랜드명: {{brandName}}\n업종 카테고리: {{category}}\n사이트 유형: {{siteType}}\n아이디어 프롬프트: {{promptText}}\n선택된 활성 기능 목록: {{enabledFeatures}}\n업로드 자산 목록: {{assetsSummary}}\n\n[필수 JSON 출력 스키마 — 첫 글자 "{" 부터 마지막 "}" 까지 순수 JSON만 출력]\n{\n  "brandSlogan": "감성 브랜드 슬로건",\n  "customBadge": "상단 공식 배지 문구",\n  "heroTitle": "메인 히어로 헤드라인",\n  "heroSubtitle": "히어로 서브 카피 (원문 복붙 금지)",\n  "ctaText": "히어로 메인 버튼 문구",\n  "heroStyle": "FULL_SCENIC|SPLIT_VISUAL|EDITORIAL_STORY|PROMO_COMMERCE",\n  "conciergeGreeting": "AI 컨시어지 첫 인사말",\n  "products": [\n    {"name": "상품/객실/메뉴명", "subTitle": "핵심 구성 및 매력 설명", "price": 120000, "promotionPrice": 99000, "unit": "박/세트/건", "stock": 15, "productCategoryName": "분류명", "badge": "BEST/시그니처", "options": ["기본 옵션", "추가 옵션"]}\n  ],\n  "articles": [\n    {"title": "매거진 제목", "categoryName": "카테고리", "summary": "요약 2문장", "bodyHtml": "<p>상세 본문</p>"}\n  ],\n  "cmsForm": {\n    "title": "맞춤 예약·신청 폼 제목",\n    "description": "예약 안내 설명",\n    "submitText": "예약·신청 접수하기",\n    "fields": [\n      {"name": "applicantName", "label": "예약자 성함", "type": "text", "required": true},\n      {"name": "phone", "label": "연락처", "type": "tel", "required": true},\n      {"name": "visitDate", "label": "희망 체크인/방문 일자", "type": "date", "required": true},\n      {"name": "optionValue", "label": "희망 객실·프로그램 선택", "type": "select", "required": true, "options": ["옵션1", "옵션2", "옵션3"]},\n      {"name": "guestCount", "label": "방문 인원수", "type": "number", "required": true},\n      {"name": "addonOptions", "label": "추가 선택 옵션", "type": "checkbox", "required": false, "options": ["부가옵션1", "부가옵션2"]},\n      {"name": "message", "label": "추가 요청사항", "type": "textarea", "required": false}\n    ]\n  },\n  "featureBlocks": {\n    "pricingMatrix": {"title": "요금표 제목", "subtitle": "안내 부제", "columns": ["구분", "주중", "주말", "성수기", "포함혜택"], "rows": [["타입1", "100,000원", "130,000원", "150,000원", "기본제공"]], "notice": "안내 문구"},\n    "spaceTour": [{"zoneName": "구역명", "title": "공간 타이틀", "description": "설명", "highlights": ["특징1", "특징2"]}],\n    "stepGuide": [{"stepNo": "STEP 01", "title": "단계 제목", "timeLabel": "시간/순서", "description": "설명"}],\n    "faqItems": [{"category": "분류", "q": "질문", "a": "답변"}],\n    "locationGuide": {"address": "주소", "highlight": "위치 핵심 요약", "carRoute": "자가용 안내", "transitRoute": "대중교통 안내", "parkingInfo": "주차 안내", "nearbyCourses": [{"name": "주변 명소", "distance": "소요시간", "desc": "설명"}]},\n    "policyRules": {"checkInOut": "이용 시간 안내", "usageRules": ["수칙1", "수칙2"], "refundRules": ["환불규정1", "환불규정2"]},\n    "reviews": [{"author": "작성자", "rating": 5, "stayType": "이용상품", "date": "2026.10", "content": "후기 내용", "tags": ["태그1", "태그2"]}],\n    "pollWidget": {"question": "투표 질문", "subtitle": "투표 안내", "options": [{"id": "opt1", "label": "선택지1", "votes": 48}, {"id": "opt2", "label": "선택지2", "votes": 35}]},\n    "trustMetrics": [{"label": "지표명", "value": "수치", "sub": "보조설명"}]\n  }\n}',
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
