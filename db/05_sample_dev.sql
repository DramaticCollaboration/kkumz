-- ============================================================================
-- Kkumz (꿈즈) — 05_sample_dev.sql
-- [DEV/TEST ONLY] 개발 및 시연 전용 샘플 픽스처 데이터
-- 무중복 결정론적 순수 INSERT INTO 구문만 사용
-- ============================================================================

INSERT INTO kkumz_idea (
    id, author_username, author_display_name, title, prompt_text, site_type, category,
    tags_json, visibility, current_step, selected_brand_name, selected_site_slug, selected_logo_svg,
    design_tokens_json, spec_markdown, screens_json, status, created_at, updated_at
) VALUES
(
    'idea-kkumz-001',
    'soyeon.jeju',
    '한소연 대표',
    '제주 서귀포 첫물 유기농 차광 말차 & 도예가 수제 다기 브랜드 스토어',
    '제주 서귀포 유기농 다원에서 재배한 첫물 세레모니얼 말차와 이천 도예가의 수제 백자 다완, 100본립 차선을 큐레이션하여 판매하는 프리미엄 다도 D2C 쇼핑몰을 만들고 싶어요.',
    'COMMERCE',
    'FOOD_BEVERAGE',
    '["제주유기농말차", "홈다도", "수제다완", "프리미엄기프트", "D2C브랜드"]'::jsonb,
    'PUBLIC',
    6,
    '말차 아뜰리에 제주',
    'matcha-atelier',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="20" cy="20" r="11" fill="#86efac" fill-opacity="0.25"/><path d="M13 24C13 17 20 12 27 15C26 22 20 27 13 24Z" fill="#4ade80"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#14532d","secondary":"#16a34a","accent":"#ca8a04","surface":"#f7fee7","radius":"14px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 브랜드 비전 및 핵심 가치
- **브랜드명**: 말차 아뜰리에 제주
- **핵심 슬로건**: 제주 화산암반수와 해풍이 길러낸 첫물 유기농 차광 말차 & 수제 다기 아뜰리에
- **타겟 고객**: 바쁜 일상 속 5분의 고요한 다도 리추얼을 추구하는 25~45세 프리미엄 라이프스타일 소비자

## 2. 핵심 스토어 기능 구성
1. **다도 큐레이션 히어로**: 첫물 세레모니얼 말차 틴캔 및 백자 다완 스타터 세트 스토리텔링 배너
2. **등급별 말차 카탈로그**: 세레모니얼 말차, 수제 다기 & 차선, 말차 디저트 기프트 카테고리 필터
3. **AI 티 소믈리에 컨시어지**: 취향(고소한 감칠맛 vs 산뜻한 청량감, 라떼용 vs 격불용) 기반 맞춤형 말차 추천
4. **원클릭 장바구니 및 선물 포장 결제**: 보자기 기프트 패키징 옵션 및 실시간 주문·재고 연동',
    '[
      {"screenKey":"HOME_HERO","title":"보태니컬 다도 히어로 섹션","layout":"EDITORIAL_BOTANICAL","components":["BrandStoryBanner","MatchaRitualSteps","FeaturedProductGrid"]},
      {"screenKey":"CATALOG","title":"말차 등급 및 다구 카탈로그","layout":"FILTERABLE_3COL_GRID","components":["CategoryTabs","TasteNoteBadge","QuickCartDrawer"]},
      {"screenKey":"CHECKOUT","title":"기프트 패키징 및 즉시 결제","layout":"ONE_PAGE_CHECKOUT","components":["GiftWrapSelector","OrderForm","AiTeaSommelierWidget"]}
    ]'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'idea-kkumz-002',
    'minjun.maker',
    '박민준 메이커',
    '몰입의 궤도를 설계하는 CNC 알루미늄 데스크테리어 & 매그세이프 기어 샵',
    '개발자와 디자이너의 책상 위 케이블을 깔끔하게 정리해 주는 CNC 통절삭 알루미늄 Qi2 충전 스테이션, 북미산 월넛 마그네틱 데스크 쉘프, 비대칭 모니터 조명을 판매하는 테크 브랜드 스토어를 만들고 싶습니다.',
    'COMMERCE',
    'TECH_HARDWARE',
    '["데스크테리어", "매그세이프충전독", "모듈형데스크", "개발자셋업", "CNC알루미늄"]'::jsonb,
    'PUBLIC',
    6,
    '데스크 오빗',
    'desk-orbit',
    '<svg viewBox="0 0 150 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><ellipse cx="20" cy="20" rx="13" ry="6" transform="rotate(-25 20 20)" stroke="#818cf8" stroke-width="2.5" fill="none"/><circle cx="20" cy="20" r="4" fill="#38bdf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#1e1b4b">DESK ORBIT</text></svg>',
    '{"primary":"#1e1b4b","secondary":"#4f46e5","accent":"#06b6d4","surface":"#f8fafc","radius":"10px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 브랜드 비전 및 핵심 가치
- **브랜드명**: 데스크 오빗
- **핵심 슬로건**: 몰입의 궤도를 설계하는 모듈형 알루미늄 데스크테리어 & 매그세이프 테크 기어
- **타겟 고객**: 재택근무 및 하이엔드 워크스테이션 셋업에 진심인 개발자, 프로덕트 디자이너, 크리에이터

## 2. 핵심 스토어 기능 구성
1. **다크 테크 셋업 쇼케이스**: 실제 듀얼 모니터 & 맥북 워크스테이션 구성별 모듈 호환 가이드
2. **모듈형 카탈로그 & 번들 할인**: 충전 독, 월넛 데스크 쉘프, 비대칭 스크린바, QMK 매크로 패드 라인업
3. **AI 데스크 셋업 어드바이저**: 책상 가로 폭(1200/1400/1600mm)과 보유 기기에 맞는 최적 배치 추천',
    '[
      {"screenKey":"HOME_HERO","title":"다크 테크 워크스테이션 히어로","layout":"DARK_TECH_GRID","components":["OrbitSetupShowcase","SpecHighlightStrip","BestGearGrid"]},
      {"screenKey":"CATALOG","title":"모듈형 기어 카탈로그","layout":"SPEC_COMPARISON_GRID","components":["CompatibilityFilter","ProductCard","CartSidebar"]},
      {"screenKey":"CHECKOUT","title":"익스프레스 주문서","layout":"ONE_PAGE_CHECKOUT","components":["ShippingAddressForm","CouponApplier","InstantOrderButton"]}
    ]'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'idea-kkumz-003',
    'jihoon.run',
    '정지훈 크루장',
    '한강 나이트 러닝 크루 커뮤니티 & 마라톤 트레이닝 매거진 웹사이트',
    '매주 화요일·목요일 저녁 반포·성수·여의도 한강공원에서 함께 달리는 2030 러닝 크루 공식 브랜드 사이트이자, 러닝 코스 아카이브와 정규 세션 참가 신청을 받는 커뮤니티 웹사이트를 만들고 싶어요.',
    'CMS_SITE',
    'COMMUNITY_MEDIA',
    '["러닝크루", "한강나이트런", "마라톤트레이닝", "커뮤니티웹", "시티러너"]'::jsonb,
    'PUBLIC',
    6,
    '서울 나이트 런 클럽',
    'seoul-run-club',
    '<svg viewBox="0 0 170 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><path d="M9 26L18 14L23 21L31 12" stroke="#f97316" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="31" cy="12" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#0f172a">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#ea580c","secondary":"#0f172a","accent":"#f59e0b","surface":"#fff7ed","radius":"12px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 커뮤니티 비전 및 핵심 가치
- **사이트명**: 서울 나이트 런 클럽
- **핵심 슬로건**: 서울의 밤을 가르는 페이스, 함께 달릴 때 더 멀리 가는 시티 러닝 커뮤니티
- **사이트 유형**: 브랜드·커뮤니티 웹 (매거진 아티클·배너·온라인 참가 신청 통합)

## 2. 핵심 콘텐츠 및 커뮤니티 기능
1. **이번 주 나이트 런 세션 공지**: 반포 잠수교 7K 템포런, 성수 서울숲 5K 리커버리런 일정 안내
2. **서울 시티 러닝 코스 매거진**: 급수대, 짐보관소, 노면 상태가 정리된 코스 가이드 아티클
3. **신규 크루원 & 게스트 런 온라인 신청 폼**: 희망 페이스(530/600/630) 및 세션 참가 신청 실시간 접수',
    '[
      {"screenKey":"CMS_HERO","title":"시티 나이트 런 비주얼 배너","layout":"MAGAZINE_HERO","components":["WeeklySessionBanner","CrewMetricsBar"]},
      {"screenKey":"ARTICLE_FEED","title":"러닝 코스 가이드 & 훈련 칼럼","layout":"EDITORIAL_CARDS","components":["CourseArticleList","PaceCalculatorCard"]},
      {"screenKey":"APPLY_FORM","title":"게스트 런 & 크루 합류 신청 폼","layout":"DYNAMIC_CMS_FORM","components":["SessionSelectField","PaceRadioGroup","SubmitApplicationButton"]}
    ]'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),
(
    'idea-kkumz-004',
    'hajun.roast',
    '최하준 헤드로스터',
    '스페셜티 커피 로스터리 아카이브 매거진 & 싱글오리진 원두 스토어 복합 사이트',
    '콜롬비아·에티오피아 산지 농장 직교역(Direct Trade) 인터뷰 매거진과 로스팅 프로파일 아티클을 발행하면서, 매월 로스터가 선별한 싱글오리진 원두와 드립백을 바로 주문하고 시음 클래스도 예약할 수 있는 매거진+스토어 복합 사이트를 만들고 싶습니다.',
    'HYBRID',
    'LIFESTYLE_HYBRID',
    '["스페셜티커피", "로스터리매거진", "원두스토어", "시음클래스예약", "복합사이트"]'::jsonb,
    'PUBLIC',
    6,
    '빈 크로니클 서울',
    'bean-chronicle',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#451a03"/><ellipse cx="20" cy="20" rx="10" ry="13" fill="#d97706"/><path d="M20 8C17 14 23 26 20 32" stroke="#451a03" stroke-width="2.2" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#451a03">BEAN CHRONICLE</text></svg>',
    '{"primary":"#78350f","secondary":"#b45309","accent":"#d97706","surface":"#fffbeb","radius":"12px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 브랜드 비전 및 복합 아키텍처
- **사이트명**: 빈 크로니클 서울
- **핵심 슬로건**: 한 잔의 커피에 담긴 산지의 연대기 — 로스터리 저널 & 스페셜티 셀렉트 샵
- **사이트 유형**: 매거진 + 스토어 복합 사이트 (아티클·클래스 신청 + 상품 카탈로그·주문 통합)

## 2. 핵심 복합 기능 구성
1. **산지 다이렉트 트레이드 매거진**: 농장 인터뷰, 커핑 노트, 브루잉 레시피 저널
2. **이달의 로스터리 셀렉션 & 브루잉 기어 스토어**: 원두·드립 커피머신·그라인더 즉시 구매
3. **프라이빗 커핑 세션 & 원두 납품 문의**: 주말 커핑 클래스 예약 및 카페 B2B 원두 샘플 신청',
    '[
      {"screenKey":"HYBRID_HERO","title":"로스터리 저널 & 이달의 원두 스플릿 히어로","layout":"HYBRID_EDITORIAL_COMMERCE","components":["FeaturedStoryHero","RoastProfileBadge"]},
      {"screenKey":"SHOP_AND_JOURNAL","title":"브루잉 스토어 & 커핑 아카이브 통합 탭","layout":"DUAL_STREAM_GRID","components":["CommerceCatalogSection","EditorialJournalSection"]},
      {"screenKey":"CUPPING_RESERVE","title":"커핑 클래스 예약 및 B2B 샘플 신청","layout":"DYNAMIC_CMS_FORM","components":["ClassBookingForm","AiBrewingConcierge"]}
    ]'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
);

INSERT INTO kkumz_idea_version (
    id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at
) VALUES
(
    'iver-kkumz-001-v1',
    'idea-kkumz-001',
    1,
    '제주 유기농 말차 쇼핑몰 초기 기획안 (v1)',
    '제주도 유기농 말차 가루와 차선을 파는 쇼핑몰을 만들고 싶어요.',
    '## 1. 초기 기획 요약\n- 제주산 유기농 말차 파우더 단품 판매 중심의 기본 쇼핑몰 구성\n- 기본 상품 리스트 및 장바구니 기능 포함',
    '[{"screenKey":"HOME_HERO","title":"기본 메인 배너","layout":"STANDARD_HERO","components":["HeroBanner","ProductGrid"]}]'::jsonb,
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'iver-kkumz-001-v2',
    'idea-kkumz-001',
    2,
    '제주 서귀포 첫물 유기농 차광 말차 & 도예가 수제 다기 브랜드 스토어 (v2 확정)',
    '제주 서귀포 유기농 다원에서 재배한 첫물 세레모니얼 말차와 이천 도예가의 수제 백자 다완, 100본립 차선을 큐레이션하여 판매하는 프리미엄 다도 D2C 쇼핑몰을 만들고 싶어요.',
    '## 1. 브랜드 비전 및 핵심 가치\n- **브랜드명**: 말차 아뜰리에 제주\n- **핵심 슬로건**: 제주 화산암반수와 해풍이 길러낸 첫물 유기농 차광 말차 & 수제 다기 아뜰리에\n- **차별화 포인트**: 이천 도예 장인 협업 백자 다완 스타터 세트 및 AI 티 소믈리에 컨시어지 탑재',
    '[{"screenKey":"HOME_HERO","title":"보태니컬 다도 히어로 섹션","layout":"EDITORIAL_BOTANICAL","components":["BrandStoryBanner","MatchaRitualSteps","FeaturedProductGrid"]},{"screenKey":"CATALOG","title":"말차 등급 및 다구 카탈로그","layout":"FILTERABLE_3COL_GRID","components":["CategoryTabs","TasteNoteBadge","QuickCartDrawer"]}]'::jsonb,
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'iver-kkumz-002-v1',
    'idea-kkumz-002',
    1,
    '데스크 오빗 모듈형 데스크테리어 스토어 기획서 (v1)',
    '개발자와 디자이너의 책상 위 케이블을 깔끔하게 정리해 주는 CNC 통절삭 알루미늄 Qi2 충전 스테이션, 북미산 월넛 마그네틱 데스크 쉘프, 비대칭 모니터 조명을 판매하는 테크 브랜드 스토어를 만들고 싶습니다.',
    '## 1. 브랜드 비전\n- **브랜드명**: 데스크 오빗\n- **핵심 기능**: 다크 테크 감성의 3-in-1 Qi2 충전 독 및 마그네틱 데스크 쉘프 전문 D2C 커머스',
    '[{"screenKey":"HOME_HERO","title":"다크 테크 워크스테이션 히어로","layout":"DARK_TECH_GRID","components":["OrbitSetupShowcase","BestGearGrid"]}]'::jsonb,
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'iver-kkumz-003-v1',
    'idea-kkumz-003',
    1,
    '서울 나이트 런 클럽 커뮤니티 사이트 기획서 (v1)',
    '매주 화요일·목요일 저녁 반포·성수·여의도 한강공원에서 함께 달리는 2030 러닝 크루 공식 브랜드 사이트이자, 러닝 코스 아카이브와 정규 세션 참가 신청을 받는 커뮤니티 웹사이트를 만들고 싶어요.',
    '## 1. 커뮤니티 비전\n- **사이트명**: 서울 나이트 런 클럽\n- **핵심 기능**: 주간 세션 일정 공지, 한강 러닝 코스 매거진, 게스트 런 참가 신청 폼',
    '[{"screenKey":"CMS_HERO","title":"시티 나이트 런 비주얼 배너","layout":"MAGAZINE_HERO","components":["WeeklySessionBanner","CourseArticleList","SessionApplyForm"]}]'::jsonb,
    'jihoon.run',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'iver-kkumz-004-v1',
    'idea-kkumz-004',
    1,
    '빈 크로니클 서울 로스터리 매거진 & 스토어 복합 기획서 (v1)',
    '콜롬비아·에티오피아 산지 농장 직교역 인터뷰 매거진과 로스팅 프로파일 아티클을 발행하면서, 싱글오리진 원두를 바로 주문하고 시음 클래스도 예약할 수 있는 복합 사이트를 만들고 싶습니다.',
    '## 1. 복합 사이트 비전\n- **사이트명**: 빈 크로니클 서울\n- **핵심 기능**: 산지 아카이브 저널 + 브루잉 스토어 + 커핑 클래스 예약 폼 통합',
    '[{"screenKey":"HYBRID_HERO","title":"로스터리 저널 & 원두 스토어 복합 히어로","layout":"HYBRID_EDITORIAL_COMMERCE","components":["FeaturedStoryHero","CommerceCatalogSection","EditorialJournalSection"]}]'::jsonb,
    'hajun.roast',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- AI Brand Naming Suggestions
INSERT INTO kkumz_name_suggestion (
    id, idea_id, brand_name, site_slug, pronunciation, brand_story, score, slug_available, is_selected, created_at
) VALUES
('name-kkumz-101', 'idea-kkumz-001', '말차 아뜰리에 제주', 'matcha-atelier', 'Matcha Atelier Jeju / 말차 아뜰리에 제주', '제주 차밭의 장인정신과 현대적인 다도 공방의 감성을 결합한 프리미엄 다이렉트 브랜드명입니다.', 99, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-102', 'idea-kkumz-001', '설록차실 서귀포', 'seogwipo-chasil', 'Seogwipo Chasil / 서귀포 차실', '서귀포 화산암반수 다원의 지역성과 고요한 프라이빗 다실의 공간감을 강조한 네이밍입니다.', 95, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-103', 'idea-kkumz-001', '비취다원', 'jade-tea-garden', 'Bichwi Dawon / 비취다원', '21일 차광 재배로 깊어진 첫물 말차의 에메랄드(비취)빛 수색을 직관적으로 표현합니다.', 93, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-104', 'idea-kkumz-001', '첫물 리추얼', 'first-harvest-ritual', 'Cheotmul Ritual / 첫물 리추얼', '매년 봄 가장 먼저 수확한 어린 찻잎으로 시작하는 5분 명상 다도 루틴을 뜻합니다.', 92, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-105', 'idea-kkumz-001', '다완과 차선', 'chawan-chasen', 'Chawan & Chasen / 다완과 차선', '이천 수제 백자 다완과 대나무 차선으로 완성하는 정통 홈 다구 큐레이션을 상징합니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-106', 'idea-kkumz-001', '우마미 그린', 'umami-green-jeju', 'Umami Green / 우마미 그린', '떫은맛 없이 깊고 풍부한 테아닌 감칠맛(우마미)을 전면에 내세운 모던 감각 네이밍입니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-107', 'idea-kkumz-001', '하영다도', 'hayoung-tea', 'Hayoung Dado / 하영다도', '제주의 너른 자연과 풍요로움을 뜻하는 제주어 어감을 살린 로컬 헤리티지 브랜드명입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-108', 'idea-kkumz-001', '크림말차 랩', 'cream-matcha-lab', 'Cream Matcha Lab / 크림말차 랩', '마이크로 벨벳 거품과 무가당 라떼 베이스 연구에 특화된 젊은 감각의 브랜드명입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-201', 'idea-kkumz-002', '데스크 오빗', 'desk-orbit', 'Desk Orbit / 데스크 오빗', '책상 위 모든 디바이스와 도구가 안정적인 몰입의 궤도로 정렬된다는 의미를 담은 테크 기어 브랜드입니다.', 98, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-202', 'idea-kkumz-002', '알루미나 독 랩', 'alumina-dock-lab', 'Alumina Dock Lab / 알루미나 독 랩', '항공 등급 6061 통절삭 알루미늄 가공 기술과 마그네틱 충전 스테이션의 정밀함을 강조합니다.', 95, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-203', 'idea-kkumz-002', '모듈러 그리드 서울', 'modular-grid-seoul', 'Modular Grid Seoul / 모듈러 그리드', '개발자와 디자이너의 작업 동선에 맞춰 자유롭게 확장되는 모듈형 데스크 셋업 시스템입니다.', 94, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-204', 'idea-kkumz-002', '월넛 앤 아노다이즈', 'walnut-anodize', 'Walnut & Anodize / 월넛 앤 아노다이즈', '따뜻한 북미산 호두나무 원목과 차가운 애노다이징 메탈의 조화로운 대비를 직관적으로 담았습니다.', 92, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-205', 'idea-kkumz-002', '포커스 아키텍처', 'focus-architecture', 'Focus Architecture / 포커스 아키텍처', '케이블 노이즈를 제거하고 순수한 몰입의 공간만을 설계한다는 프로페셔널 워크스페이스 브랜드입니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-206', 'idea-kkumz-002', '제로케이블 스튜디오', 'zerocable-studio', 'ZeroCable Studio / 제로케이블 스튜디오', 'Qi2 무선 충전과 매립형 마그네틱 레일로 선 없는 미니멀 데스크를 구현하는 브랜드 정체성을 표현합니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-207', 'idea-kkumz-002', '스페이스 기어 콜렉티브', 'space-gear-collective', 'Space Gear Collective / 스페이스 기어', '하이엔드 키보드, 모니터 조명, 충전 독 메이커들이 모인 크리에이터 장비 큐레이션 샵입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-208', 'idea-kkumz-002', '노바 데스크웨어', 'nova-deskware', 'Nova Deskware / 노바 데스크웨어', '야간 코딩과 크리에이티브 작업 환경을 밝혀주는 차세대 데스크테리어 하드웨어 브랜드명입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-301', 'idea-kkumz-003', '서울 나이트 런 클럽', 'seoul-run-club', 'Seoul Night Run Club / 서울 나이트 런 클럽', '서울 한강의 야경을 가르며 함께 달리는 역동적인 시티 러닝 크루 아이덴티티를 담았습니다.', 97, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-302', 'idea-kkumz-003', '한강 페이서스 530', 'hangang-pacers', 'Hangang Pacers / 한강 페이서스', '반포·성수·여의도 한강공원에서 서로의 페이스를 끌어주는 2030 러너 커뮤니티를 상징합니다.', 95, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-303', 'idea-kkumz-003', '미드나잇 스트라이드', 'midnight-stride', 'Midnight Stride / 미드나잇 스트라이드', '퇴근 후 도심의 밤공기를 가르며 달리는 시티 러너들의 경쾌한 보폭과 에너지를 표현합니다.', 94, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-304', 'idea-kkumz-003', '브릿지 러너스 아카이브', 'bridge-runners', 'Bridge Runners Archive / 브릿지 러너스', '잠수교부터 성수대교까지 서울의 다리와 강변 코스를 기록하는 러닝 매거진 & 크루 브랜드입니다.', 92, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-305', 'idea-kkumz-003', '펄스 트랙 서울', 'pulse-track-seoul', 'Pulse Track Seoul / 펄스 트랙 서울', '심박수가 뛰는 템포런 세션과 체계적인 마라톤 풀코스 훈련 커뮤니티의 전문성을 담았습니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-306', 'idea-kkumz-003', '어반 마일 콜렉티브', 'urban-mile-collective', 'Urban Mile Collective / 어반 마일', '도심 속 1마일씩 함께 누적하며 성장하는 오픈 게스트 런 & 라이프스타일 크루 네이밍입니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-307', 'idea-kkumz-003', '런 앤 리커버리 클럽', 'run-recovery-club', 'Run & Recovery Club / 런 앤 리커버리', '부상 없는 러닝과 세션 후 스트레칭·교류 문화를 지향하는 친근한 커뮤니티 브랜드명입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-308', 'idea-kkumz-003', '시티 템포 저널', 'city-tempo-journal', 'City Tempo Journal / 시티 템포 저널', '러닝 코스 리뷰, 러닝화 큐레이션, 주간 세션 신청을 아우르는 미디어형 러닝 클럽입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-401', 'idea-kkumz-004', '빈 크로니클 서울', 'bean-chronicle', 'Bean Chronicle Seoul / 빈 크로니클 서울', '산지 농부부터 로스터, 바리스타까지 이어지는 스페셜티 원두의 연대기를 기록하는 복합 문화 브랜드입니다.', 98, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-402', 'idea-kkumz-004', '테루아 로스터리 저널', 'terroir-roastery', 'Terroir Roastery Journal / 테루아 로스터리', '콜롬비아·에티오피아 고산지대의 토양과 기후(떼루아)를 한 잔의 커피와 아티클로 전달합니다.', 96, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-403', 'idea-kkumz-004', '마이크로랏 아뜰리에', 'microlot-atelier', 'Microlot Atelier / 마이크로랏 아뜰리에', '소량 직교역(Direct Trade)으로만 공수한 희귀 게이샤·무산소 발효 원두 전문 큐레이션 스토어입니다.', 94, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-404', 'idea-kkumz-004', '성수 커핑 노트', 'seongsu-cupping-note', 'Seongsu Cupping Note / 성수 커핑 노트', '매월 헤드로스터의 테이스팅 노트 매거진과 주말 프라이빗 시음 클래스가 열리는 복합 공간 브랜드입니다.', 93, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-405', 'idea-kkumz-004', '다이렉트 빈 아카이브', 'direct-bean-archive', 'Direct Bean Archive / 다이렉트 빈 아카이브', '산지 농장 인터뷰 아카이브와 신선한 로스팅 원두 즉시 주문을 하나로 결합한 하이브리드 브랜드명입니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-406', 'idea-kkumz-004', '나인티투 브루잉 랩', 'ninetytwo-brewing', '92 Brewing Lab / 나인티투 브루잉 랩', '스페셜티 추출의 황금 온도인 92℃에서 영감을 얻은 정밀 로스팅 & 브루잉 기어 셀렉트샵입니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-407', 'idea-kkumz-004', '앰버 크래프트 로스터스', 'amber-craft-roasters', 'Amber Craft Roasters / 앰버 크래프트', '호박빛 클린컵 추출액과 장인 로스터의 섬세한 불조절 프로파일을 상징하는 감성 브랜드명입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-408', 'idea-kkumz-004', '오리진 앤 푸어', 'origin-and-pour', 'Origin & Pour / 오리진 앤 푸어', '싱글오리진 원두의 본질과 푸어오버 핸드드립의 여유로운 일상을 잇는 모던 커피 브랜드입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days');

-- AI SVG Logo & Palette Suggestions
INSERT INTO kkumz_logo_suggestion (
    id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
) VALUES
(
    'logo-kkumz-101',
    'idea-kkumz-001',
    '보태니컬 리프 엠블럼',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="20" cy="20" r="11" fill="#86efac" fill-opacity="0.25"/><path d="M13 24C13 17 20 12 27 15C26 22 20 27 13 24Z" fill="#4ade80"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#14532d","secondary":"#16a34a","accent":"#ca8a04","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["보태니컬", "딥포레스트", "세레모니얼"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-102',
    'idea-kkumz-001',
    '미니멀 백자 다완 라인아트',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="20" fill="#ecfccb"/><path d="M10 17H30C30 25 25 29 20 29C15 29 10 25 10 17Z" stroke="#15803d" stroke-width="2.5" fill="none"/><path d="M15 31H25" stroke="#15803d" stroke-width="2.5" stroke-linecap="round"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="700" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#15803d","secondary":"#3f6212","accent":"#a16207","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"700"}'::jsonb,
    '["다완실루엣", "미니멀", "젠스타일"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-103',
    'idea-kkumz-001',
    '맷돌 스톤 모노그램',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="8" fill="#064e3b"/><circle cx="20" cy="20" r="12" stroke="#6ee7b7" stroke-width="2" fill="none"/><circle cx="20" cy="20" r="4" fill="#34d399"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#064e3b">MATCHA ATELIER</text></svg>',
    '{"primary":"#064e3b","secondary":"#059669","accent":"#d97706","surface":"#ecfdf5"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["맷돌모티브", "에메랄드", "크래프트"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-104',
    'idea-kkumz-001',
    '제주 오름 & 차밭 지오메트릭',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#166534"/><path d="M8 28L17 15L24 23L28 18L33 28H8Z" fill="#bbf7d0"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#166534">MATCHA ATELIER</text></svg>',
    '{"primary":"#166534","secondary":"#22c55e","accent":"#eab308","surface":"#f0fdf4"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["제주오름", "내추럴", "가든"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-105',
    'idea-kkumz-001',
    '차선 위스크 시그니처',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#365314"/><path d="M14 12C14 20 17 24 20 28C23 24 26 20 26 12" stroke="#bef264" stroke-width="2.2" fill="none"/><path d="M20 11V29" stroke="#bef264" stroke-width="2.2"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#365314">MATCHA ATELIER</text></svg>',
    '{"primary":"#365314","secondary":"#65a30d","accent":"#ca8a04","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["100본립차선", "아티장", "모던다도"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-106',
    'idea-kkumz-001',
    '모던 씰 스탬프 배지',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect x="2" y="2" width="36" height="36" rx="8" stroke="#14532d" stroke-width="2.5" fill="#dcfce7"/><text x="12" y="26" font-family="Pretendard, sans-serif" font-weight="900" font-size="16" fill="#14532d">茶</text><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#14532d","secondary":"#15803d","accent":"#b45309","surface":"#f0fdf4"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["낙관스탬프", "헤리티지", "티하우스"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-201',
    'idea-kkumz-002',
    '오빗 일립스 테크 마크',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><ellipse cx="20" cy="20" rx="13" ry="6" transform="rotate(-25 20 20)" stroke="#818cf8" stroke-width="2.5" fill="none"/><circle cx="20" cy="20" r="4" fill="#38bdf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#1e1b4b">DESK ORBIT</text></svg>',
    '{"primary":"#1e1b4b","secondary":"#4f46e5","accent":"#06b6d4","surface":"#f8fafc"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["데스크테리어", "인디고테크", "모듈형"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-202',
    'idea-kkumz-002',
    'CNC 헥사곤 정밀 배지',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><polygon points="20,8 30,14 30,26 20,32 10,26 10,14" stroke="#38bdf8" stroke-width="2.2" fill="none"/><circle cx="20" cy="20" r="3.5" fill="#818cf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#0f172a">DESK ORBIT</text></svg>',
    '{"primary":"#0f172a","secondary":"#334155","accent":"#38bdf8","surface":"#f1f5f9"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["CNC알루미늄", "다크슬레이트", "하드웨어"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-203',
    'idea-kkumz-002',
    '월넛 & 티타늄 레이어 심볼',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#27272a"/><rect x="9" y="13" width="22" height="5" rx="2.5" fill="#d97706"/><rect x="9" y="22" width="22" height="5" rx="2.5" fill="#a1a1aa"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#27272a">DESK ORBIT</text></svg>',
    '{"primary":"#27272a","secondary":"#78350f","accent":"#d97706","surface":"#fafafa"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["월넛원목", "티타늄그레이", "데스크쉘프"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-204',
    'idea-kkumz-002',
    '매그세이프 링 마크',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#172554"/><circle cx="20" cy="18" r="9" stroke="#60a5fa" stroke-width="2.5" fill="none"/><line x1="20" y1="29" x2="20" y2="33" stroke="#38bdf8" stroke-width="2.5" stroke-linecap="round"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#172554">DESK ORBIT</text></svg>',
    '{"primary":"#172554","secondary":"#2563eb","accent":"#38bdf8","surface":"#eff6ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["Qi2무선충전", "딥네이비", "스마트독"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-205',
    'idea-kkumz-002',
    '비대칭 라이트바 프리즘',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#18181b"/><line x1="10" y1="12" x2="30" y2="12" stroke="#facc15" stroke-width="3" stroke-linecap="round"/><polygon points="13,15 27,15 32,30 8,30" fill="#fde047" fill-opacity="0.22"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#18181b">DESK ORBIT</text></svg>',
    '{"primary":"#18181b","secondary":"#3f3f46","accent":"#eab308","surface":"#f4f4f5"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["모니터조명", "앰비언트", "블랙골드"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-206',
    'idea-kkumz-002',
    '사이버 매트릭스 모노그램',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#311042"/><rect x="11" y="11" width="7" height="7" rx="2" fill="#e879f9"/><rect x="22" y="11" width="7" height="7" rx="2" fill="#38bdf8"/><rect x="11" y="22" width="18" height="7" rx="2" fill="#c084fc"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#311042">DESK ORBIT</text></svg>',
    '{"primary":"#3b0764","secondary":"#7e22ce","accent":"#06b6d4","surface":"#faf5ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["매크로패드", "네온바이올렛", "크리에이터"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-301',
    'idea-kkumz-003',
    '나이트 펄스 트랙 마크',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><path d="M9 26L18 14L23 21L31 12" stroke="#f97316" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="31" cy="12" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#0f172a">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#ea580c","secondary":"#0f172a","accent":"#f59e0b","surface":"#fff7ed"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["러닝크루", "에너지오렌지", "나이트런"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-302',
    'idea-kkumz-003',
    '한강 브릿지 웨이브 엠블럼',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e293b"/><path d="M8 24C14 14 26 14 32 24" stroke="#38bdf8" stroke-width="2.5" fill="none"/><path d="M8 28H32" stroke="#f97316" stroke-width="2.5" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#1e293b">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#0284c7","secondary":"#0f172a","accent":"#f97316","surface":"#f0f9ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["한강 잠수교", "리버블루", "시티러너"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-303',
    'idea-kkumz-003',
    '네온 볼트 스트라이드',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#111827"/><polygon points="22,8 11,22 20,22 18,32 29,18 20,18" fill="#84cc16"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#111827">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#3f6212","secondary":"#111827","accent":"#84cc16","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["네온라임", "스피드세션", "마라톤"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-304',
    'idea-kkumz-003',
    '트랙 오벌 크루 실드',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#7c2d12"/><rect x="8" y="13" width="24" height="14" rx="7" stroke="#fdba74" stroke-width="2.5" fill="none"/><circle cx="26" cy="20" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#7c2d12">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#c2410c","secondary":"#431407","accent":"#fb923c","surface":"#fff7ed"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["트랙오벌", "테라코타", "인터벌훈련"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-305',
    'idea-kkumz-003',
    '미드나잇 문라이트 러너',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><path d="M24 11A9 9 0 1 0 29 25A7 7 0 1 1 24 11Z" fill="#fde047"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#1e1b4b">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#1e1b4b","secondary":"#312e81","accent":"#eab308","surface":"#eef2ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["미드나잇", "문라이트", "야간러닝"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-306',
    'idea-kkumz-003',
    '하트비트 페이서 크레스트',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#881337"/><path d="M7 20H14L17 12L22 28L25 20H33" stroke="#fda4af" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#881337">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#be123c","secondary":"#4c0519","accent":"#fb7185","surface":"#fff1f2"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["심박수페이스", "크림슨레드", "열정크루"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-401',
    'idea-kkumz-004',
    '로스팅 빈 엠블럼',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#451a03"/><ellipse cx="20" cy="20" rx="10" ry="13" fill="#d97706"/><path d="M20 8C17 14 23 26 20 32" stroke="#451a03" stroke-width="2.2" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#451a03">BEAN CHRONICLE</text></svg>',
    '{"primary":"#78350f","secondary":"#b45309","accent":"#d97706","surface":"#fffbeb"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["스페셜티로스터리", "에스프레소브라운", "매거진스토어"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-402',
    'idea-kkumz-004',
    '푸어오버 드리퍼 라인 마크',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#292524"/><polygon points="10,11 30,11 23,25 17,25" stroke="#f59e0b" stroke-width="2.2" fill="none"/><line x1="14" y1="29" x2="26" y2="29" stroke="#fbbf24" stroke-width="2.5" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#292524">BEAN CHRONICLE</text></svg>',
    '{"primary":"#292524","secondary":"#57534e","accent":"#d97706","surface":"#fafaf9"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["푸어오버", "모던스톤", "핸드드립"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-403',
    'idea-kkumz-004',
    '테루아 마운틴 & 선 배지',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="26" cy="14" r="4" fill="#fbbf24"/><path d="M8 29L17 16L24 24L29 19L33 29H8Z" fill="#86efac"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#14532d">BEAN CHRONICLE</text></svg>',
    '{"primary":"#14532d","secondary":"#166534","accent":"#d97706","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["고산지농장", "다이렉트트레이드", "보태니컬"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-404',
    'idea-kkumz-004',
    '에디토리얼 아카이브 북 심볼',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e293b"/><path d="M11 13H19V28H11V13ZM21 13H29V28H21V13Z" stroke="#fdba74" stroke-width="2" fill="none"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#1e293b">BEAN CHRONICLE</text></svg>',
    '{"primary":"#1e293b","secondary":"#475569","accent":"#ea580c","surface":"#f8fafc"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["매거진아카이브", "슬레이트네이비", "저널리즘"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-405',
    'idea-kkumz-004',
    '스팀 아로마 웨이브 실드',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#7c2d12"/><path d="M15 27C13 22 17 18 15 13M20 27C18 22 22 18 20 13M25 27C23 22 27 18 25 13" stroke="#fed7aa" stroke-width="2.2" stroke-linecap="round" fill="none"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#7c2d12">BEAN CHRONICLE</text></svg>',
    '{"primary":"#7c2d12","secondary":"#9a3412","accent":"#f97316","surface":"#fff7ed"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["로스팅아로마", "웜테라코타", "크래프트"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-406',
    'idea-kkumz-004',
    '게이샤 블라썸 크레스트',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#4c0519"/><circle cx="20" cy="20" r="9" stroke="#fda4af" stroke-width="2.2" fill="none"/><circle cx="20" cy="20" r="3.5" fill="#fb7185"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#4c0519">BEAN CHRONICLE</text></svg>',
    '{"primary":"#881337","secondary":"#be123c","accent":"#f43f5e","surface":"#fff1f2"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["게이샤플로럴", "버건디로즈", "부티크로스터리"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- Screen Blueprint Specifications (kkumz_screen_spec)
INSERT INTO kkumz_screen_spec (
    id, idea_id, screen_key, screen_title, route_path, layout_type, description, components_json, sort_no, created_at
) VALUES
('scr-kkumz-101', 'idea-kkumz-001', 'HOME_HERO', '보태니컬 다도 히어로 섹션', '/', 'EDITORIAL_BOTANICAL', '제주 첫물 세레모니얼 말차와 수제 백자 다완 스토리텔링 메인 배너 및 추천 큐레이션', '["BrandStoryBanner","MatchaRitualSteps","FeaturedProductGrid"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('scr-kkumz-102', 'idea-kkumz-001', 'CATALOG', '말차 등급 및 다구 카탈로그', '/catalog', 'FILTERABLE_3COL_GRID', '세레모니얼 등급별 말차, 이천 도예 다완, 100본립 차선 필터 및 장바구니 담기', '["CategoryTabs","TasteNoteBadge","QuickCartDrawer"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('scr-kkumz-103', 'idea-kkumz-001', 'CHECKOUT', '기프트 패키징 및 즉시 결제', '/checkout', 'ONE_PAGE_CHECKOUT', '보자기 선물 포장 선택 및 AI 티 소믈리에 추천 연동 원페이지 주문서', '["GiftWrapSelector","OrderForm","AiTeaSommelierWidget"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('scr-kkumz-201', 'idea-kkumz-002', 'HOME_HERO', '다크 테크 워크스테이션 히어로', '/', 'DARK_TECH_GRID', '듀얼 모니터 & 맥북 프로 셋업 쇼케이스 및 알루미늄 기어 하이라이트', '["OrbitSetupShowcase","SpecHighlightStrip","BestGearGrid"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('scr-kkumz-202', 'idea-kkumz-002', 'CATALOG', '모듈형 기어 카탈로그', '/catalog', 'SPEC_COMPARISON_GRID', '책상 규격 호환성 필터 및 Qi2 충전 독·데스크 쉘프 비교 카탈로그', '["CompatibilityFilter","ProductCard","CartSidebar"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('scr-kkumz-203', 'idea-kkumz-002', 'CHECKOUT', '데스크 풀셋업 번들 주문서', '/checkout', 'ONE_PAGE_CHECKOUT', '케이블 정리 기어 번들 할인 적용 및 당일 출고 익스프레스 결제', '["ShippingAddressForm","CouponApplier","InstantOrderButton"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('scr-kkumz-301', 'idea-kkumz-003', 'CMS_HERO', '시티 나이트 런 비주얼 배너', '/', 'MAGAZINE_HERO', '주간 나이트 런 일정 안내 및 한강 코스 하이라이트 배너', '["WeeklySessionBanner","CrewMetricsBar"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('scr-kkumz-302', 'idea-kkumz-003', 'ARTICLE_FEED', '한강 러닝 코스 아카이브 매거진', '/articles', 'EDITORIAL_CARDS', '반포·성수·여의도 급수대 및 노면 가이드 아티클 피드', '["CourseArticleList","PaceCalculatorCard"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('scr-kkumz-303', 'idea-kkumz-003', 'APPLY_FORM', '게스트 런 & 크루 합류 신청 폼', '/apply', 'DYNAMIC_CMS_FORM', '희망 러닝 페이스 및 세션 참가 온라인 신청 폼', '["SessionSelectField","PaceRadioGroup","SubmitApplicationButton"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('scr-kkumz-401', 'idea-kkumz-004', 'HYBRID_HERO', '로스터리 저널 & 이달의 원두 히어로', '/', 'HYBRID_EDITORIAL_COMMERCE', '산지 농장 인터뷰 아티클과 이달의 싱글오리진 원두 셀렉션 통합 메인', '["FeaturedStoryHero","RoastProfileBadge","AiBrewingConcierge"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('scr-kkumz-402', 'idea-kkumz-004', 'SHOP_CATALOG', '싱글오리진 원두 & 브루잉 기어 스토어', '/store', 'FILTERABLE_3COL_GRID', '게이샤 홀빈, 질소충전 드립백, 온도조절 드립포트 카탈로그 및 즉시 주문', '["CommerceCatalogSection","RoastLevelFilter","InstantCheckoutModal"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('scr-kkumz-403', 'idea-kkumz-004', 'JOURNAL_AND_CLASS', '산지 직교역 매거진 & 커핑 클래스 예약', '/journal', 'EDITORIAL_MAGAZINE_LAYOUT', '농장 직교역 인터뷰 저널 열람 및 주말 성수동 커핑 세션 온라인 예약 폼', '["EditorialJournalSection","ClassBookingForm"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '2 days');

-- Deployed Kkumz Multi-Sites (kkumz_site)
INSERT INTO kkumz_site (
    id, idea_id, site_slug, site_name, site_type, category, brand_slogan, logo_svg,
    shop_site_id, cms_site_id, deploy_mode, active_version, deployed_url, custom_domain, is_featured, visibility,
    like_count, comment_count, view_count, owner_username, owner_display_name, status, created_at, updated_at
) VALUES
(
    'site-kkumz-001',
    'idea-kkumz-001',
    'matcha-atelier',
    '말차 아뜰리에 제주',
    'COMMERCE',
    'FOOD_BEVERAGE',
    '제주 화산암반수와 해풍이 길러낸 첫물 유기농 차광 말차 & 수제 다기 아뜰리에',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="20" cy="20" r="11" fill="#86efac" fill-opacity="0.25"/><path d="M13 24C13 17 20 12 27 15C26 22 20 27 13 24Z" fill="#4ade80"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    2,
    NULL,
    'CUSTOM_BUNDLE',
    2,
    '/s/matcha-atelier',
    'matcha-atelier.kkumz.io',
    TRUE,
    'PUBLIC',
    148,
    12,
    1890,
    'soyeon.jeju',
    '한소연 대표',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'site-kkumz-002',
    'idea-kkumz-002',
    'desk-orbit',
    '데스크 오빗',
    'COMMERCE',
    'TECH_HARDWARE',
    '몰입의 궤도를 설계하는 모듈형 알루미늄 데스크테리어 & 매그세이프 테크 기어',
    '<svg viewBox="0 0 150 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><ellipse cx="20" cy="20" rx="13" ry="6" transform="rotate(-25 20 20)" stroke="#818cf8" stroke-width="2.5" fill="none"/><circle cx="20" cy="20" r="4" fill="#38bdf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#1e1b4b">DESK ORBIT</text></svg>',
    3,
    NULL,
    'DYNAMIC_SDUI',
    2,
    '/s/desk-orbit',
    'desk-orbit.kkumz.io',
    TRUE,
    'PUBLIC',
    215,
    18,
    2640,
    'minjun.maker',
    '박민준 메이커',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'site-kkumz-003',
    'idea-kkumz-003',
    'seoul-run-club',
    '서울 나이트 런 클럽',
    'CMS_SITE',
    'COMMUNITY_MEDIA',
    '서울의 밤을 가르는 페이스, 함께 달릴 때 더 멀리 가는 시티 러닝 커뮤니티',
    '<svg viewBox="0 0 170 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><path d="M9 26L18 14L23 21L31 12" stroke="#f97316" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="31" cy="12" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#0f172a">SEOUL RUN CLUB</text></svg>',
    NULL,
    101,
    'DYNAMIC_SDUI',
    1,
    '/s/seoul-run-club',
    'seoul-run-club.kkumz.io',
    FALSE,
    'PUBLIC',
    176,
    15,
    2110,
    'jihoon.run',
    '정지훈 크루장',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),
(
    'site-kkumz-004',
    'idea-kkumz-004',
    'bean-chronicle',
    '빈 크로니클 서울',
    'HYBRID',
    'LIFESTYLE_HYBRID',
    '한 잔의 커피에 담긴 산지의 연대기 — 로스터리 저널 & 스페셜티 셀렉트 샵',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#451a03"/><ellipse cx="20" cy="20" rx="10" ry="13" fill="#d97706"/><path d="M20 8C17 14 23 26 20 32" stroke="#451a03" stroke-width="2.2" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#451a03">BEAN CHRONICLE</text></svg>',
    4,
    102,
    'CUSTOM_BUNDLE',
    1,
    '/s/bean-chronicle',
    'bean-chronicle.kkumz.io',
    TRUE,
    'PUBLIC',
    192,
    14,
    2380,
    'hajun.roast',
    '최하준 헤드로스터',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
);

-- 2-Track Releases & WebIDE Snapshots (kkumz_site_release)
INSERT INTO kkumz_site_release (
    id, site_id, version_no, release_note, deploy_mode, design_tokens_json, layout_schema_json,
    webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
) VALUES
(
    'rel-kkumz-001-v1',
    'site-kkumz-001',
    1,
    '초기 멀티사이트 프로비저닝 및 즉시 디자인 스키마 배포',
    'DYNAMIC_SDUI',
    '{"primary":"#14532d","secondary":"#16a34a","accent":"#ca8a04","surface":"#f7fee7","radius":"14px"}'::jsonb,
    '{"heroTitle":"제주 첫물 유기농 차광 말차의 깊은 여운","heroSubtitle":"서귀포 단일 다원에서 수확한 1번 차광 세레모니얼 등급 말차와 수제 백자 다완 컬렉션","ctaText":"햇차 컬렉션 살펴보기","showFlashDeal":true,"showAiConcierge":true}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"matcha-store\">\n    <HeroSection :tokens=\"tokens\" />\n    <CatalogSection :products=\"products\" />\n  </div>\n</template>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#14532d\",\n  \"secondary\": \"#16a34a\",\n  \"accent\": \"#ca8a04\",\n  \"surface\": \"#f7fee7\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/matcha-atelier/v1/schema.json',
    'SUCCESS',
    '[10:12:01] [Instant-Deploy] Validating JSON layout schema and design tokens... OK\n[10:12:02] [Commerce-Link] Bound store catalog (4 products, 3 categories, 2 banners)... OK\n[10:12:02] [Publish] Live route /s/matcha-atelier activated (v1).',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'rel-kkumz-001-v2',
    'site-kkumz-001',
    2,
    'WebIDE 커스텀 히어로 다도 가이드 배너 및 선물 포장 배지 추가 (코드 번들 빌드)',
    'CUSTOM_BUNDLE',
    '{"primary":"#14532d","secondary":"#16a34a","accent":"#ca8a04","surface":"#f7fee7","radius":"14px"}'::jsonb,
    '{"heroTitle":"2026 제주 첫물 세레모니얼 말차 & 백자 다완 아뜰리에","heroSubtitle":"화강암 맷돌 저온 분쇄로 지킨 에메랄드빛 우마미 · 전 상품 보자기 기프트 패키징 지원","ctaText":"첫물 말차 & 다구 세트 구매하기","showFlashDeal":true,"showAiConcierge":true,"customBadge":"2026 햇차 한정 수량"}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"matcha-atelier-shell\" :style=\"{ ''--brand-primary'': tokens.primary }\">\n    <HeroSection :title=\"layout.heroTitle\" :subtitle=\"layout.heroSubtitle\" :badge=\"layout.customBadge\" />\n    <RitualGuideBanner />\n    <CatalogSection :products=\"products\" @add-cart=\"onAddCart\" />\n  </div>\n</template>\n<script setup lang=\"ts\">\nimport HeroSection from ''./sections/HeroSection.vue'';\nimport CatalogSection from ''./sections/CatalogSection.vue'';\n</script>"},
      {"path":"src/sections/HeroSection.vue","language":"vue","content":"<template>\n  <section class=\"hero-botanical\">\n    <span class=\"badge\">{{ badge }}</span>\n    <h1>{{ title }}</h1>\n    <p>{{ subtitle }}</p>\n  </section>\n</template>\n<script setup lang=\"ts\">\ndefineProps<{ title: string; subtitle: string; badge?: string }>();\n</script>"},
      {"path":"src/sections/CatalogSection.vue","language":"vue","content":"<template>\n  <section class=\"catalog-grid\">\n    <article v-for=\"item in products\" :key=\"item.id\" class=\"product-card\">\n      <h3>{{ item.name }}</h3>\n      <p>{{ item.subTitle }}</p>\n    </article>\n  </section>\n</template>\n<script setup lang=\"ts\">\ndefineProps<{ products: any[] }>();\n</script>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#14532d\",\n  \"secondary\": \"#16a34a\",\n  \"accent\": \"#ca8a04\",\n  \"surface\": \"#f7fee7\",\n  \"radius\": \"14px\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/matcha-atelier/v2/dist/index.js',
    'SUCCESS',
    '[14:20:10] [Bundle-Builder] Compiling 4 virtual SFC files (App.vue, HeroSection.vue, CatalogSection.vue, theme.json)...\n[14:20:11] [Vite-SSR-Check] AST & TypeScript props verification passed (0 errors, 0 warnings).\n[14:20:12] [Artifact-Store] Uploaded bundle to minio://kkumz-sites/matcha-atelier/v2/dist/index.js (42.8 KB gzipped).\n[14:20:12] [Publish] Promoted v2 to active release for /s/matcha-atelier.',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'rel-kkumz-002-v1',
    'site-kkumz-002',
    1,
    '데스크 오빗 초기 프로비저닝 및 다크 테크 테마 스키마 배포',
    'DYNAMIC_SDUI',
    '{"primary":"#1e1b4b","secondary":"#4f46e5","accent":"#06b6d4","surface":"#f8fafc","radius":"10px"}'::jsonb,
    '{"heroTitle":"Design Your Focus Orbit","heroSubtitle":"CNC 풀알루미늄 Qi2 충전 독과 북미산 호두나무 마그네틱 데스크 쉘프로 완성하는 무결점 워크스페이스","ctaText":"데스크 기어 컬렉션 보기","showFlashDeal":true,"showAiConcierge":true}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"desk-orbit-app\">\n    <HeroSection />\n    <CatalogSection />\n  </div>\n</template>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#1e1b4b\",\n  \"secondary\": \"#4f46e5\",\n  \"accent\": \"#06b6d4\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/desk-orbit/v1/schema.json',
    'SUCCESS',
    '[11:05:00] [Instant-Deploy] Bound store catalog (4 products, 3 categories)... OK',
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'rel-kkumz-002-v2',
    'site-kkumz-002',
    2,
    '대화형 편집: 듀얼 모니터 셋업 호환성 배지 및 타임특가 섹션 강조 반영',
    'DYNAMIC_SDUI',
    '{"primary":"#1e1b4b","secondary":"#4f46e5","accent":"#06b6d4","surface":"#f8fafc","radius":"10px"}'::jsonb,
    '{"heroTitle":"케이블 없는 몰입의 궤도, 데스크 오빗 2026 컬렉션","heroSubtitle":"항공 등급 6061 알루미늄 Qi2 충전 스테이션 & 모듈형 마그네틱 월넛 데스크 쉘프 풀셋업","ctaText":"워크스테이션 기어 탐색","showFlashDeal":true,"showAiConcierge":true,"customBadge":"Qi2 공식 인증 · 당일 출고"}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"desk-orbit-app\">\n    <HeroSection :badge=\"layout.customBadge\" />\n    <CatalogSection />\n  </div>\n</template>"},
      {"path":"src/sections/HeroSection.vue","language":"vue","content":"<template>\n  <section class=\"orbit-hero\">\n    <span class=\"badge\">{{ badge }}</span>\n    <h1>케이블 없는 몰입의 궤도</h1>\n  </section>\n</template>\n<script setup lang=\"ts\">\ndefineProps<{ badge?: string }>();\n</script>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#1e1b4b\",\n  \"secondary\": \"#4f46e5\",\n  \"accent\": \"#06b6d4\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/desk-orbit/v2/schema.json',
    'SUCCESS',
    '[16:40:12] [Chat-Refine] Updated hero copy and activated flash deal highlight. Live in 45ms.',
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'rel-kkumz-003-v1',
    'site-kkumz-003',
    1,
    '서울 나이트 런 클럽 아티클 및 게스트 런 온라인 신청 폼 통합 배포',
    'DYNAMIC_SDUI',
    '{"primary":"#ea580c","secondary":"#0f172a","accent":"#f59e0b","surface":"#fff7ed","radius":"12px"}'::jsonb,
    '{"heroTitle":"SEOUL NIGHT RUNNERS — 매주 화·목 서울의 밤을 달립니다","heroSubtitle":"반포 잠수교 · 성수 서울숲 · 여의도 한강공원 페이스별 그룹 러닝 및 마라톤 풀코스 훈련 커뮤니티","ctaText":"이번 주 게스트 런 신청하기","showCmsArticles":true,"showCmsForm":true,"customBadge":"매주 화·목 20:00 정규 세션"}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"seoul-run-cms\">\n    <RunClubHero />\n    <CourseMagazineSection />\n    <GuestRunApplyForm />\n  </div>\n</template>"},
      {"path":"src/sections/RunClubHero.vue","language":"vue","content":"<template>\n  <section class=\"night-run-hero\">\n    <h1>SEOUL NIGHT RUNNERS</h1>\n    <p>반포 잠수교 · 성수 서울숲 · 여의도 한강공원 그룹 러닝</p>\n  </section>\n</template>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#ea580c\",\n  \"secondary\": \"#0f172a\",\n  \"surface\": \"#fff7ed\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/seoul-run-club/v1/schema.json',
    'SUCCESS',
    '[09:30:00] [Content-Link] Provisioned 4 course articles and Guest Run registration form... OK',
    'jihoon.run',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'rel-kkumz-004-v1',
    'site-kkumz-004',
    1,
    '빈 크로니클 서울 로스터리 저널 + 브루잉 스토어 복합 사이트 배포',
    'CUSTOM_BUNDLE',
    '{"primary":"#78350f","secondary":"#b45309","accent":"#d97706","surface":"#fffbeb","radius":"12px"}'::jsonb,
    '{"heroTitle":"산지의 떼루아부터 한 잔의 브루잉까지, 빈 크로니클 서울","heroSubtitle":"직교역 커피 농장 아카이브 저널과 이달의 싱글오리진 & 브루잉 머신 셀렉트 스토어","ctaText":"이달의 로스터리 셀렉션 & 저널 보기","showFlashDeal":true,"showCmsArticles":true,"showCmsForm":true,"customBadge":"산지 직교역 로스터리 · 당일 로스팅 발송"}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"bean-chronicle-hybrid\" :style=\"{ ''--brand-primary'': tokens.primary }\">\n    <EditorialSplitHero :title=\"layout.heroTitle\" :subtitle=\"layout.heroSubtitle\" :badge=\"layout.customBadge\" />\n    <RoasteryShopSection :products=\"commerce.products\" />\n    <DirectTradeJournalSection :articles=\"cms.articles\" />\n    <CuppingClassForm :form=\"cms.forms?.[0]\" />\n  </div>\n</template>\n<script setup lang=\"ts\">\nimport EditorialSplitHero from ''./sections/HeroSection.vue'';\nimport RoasteryShopSection from ''./sections/CatalogSection.vue'';\n</script>"},
      {"path":"src/sections/HeroSection.vue","language":"vue","content":"<template>\n  <section class=\"roastery-split-hero\">\n    <span class=\"roast-badge\">{{ badge }}</span>\n    <h1>{{ title }}</h1>\n    <p>{{ subtitle }}</p>\n  </section>\n</template>\n<script setup lang=\"ts\">\ndefineProps<{ title: string; subtitle: string; badge?: string }>();\n</script>"},
      {"path":"src/sections/CatalogSection.vue","language":"vue","content":"<template>\n  <section class=\"roastery-catalog-grid\">\n    <article v-for=\"bean in products\" :key=\"bean.id\" class=\"bean-card\">\n      <h3>{{ bean.name }}</h3>\n      <p>{{ bean.subTitle }}</p>\n      <strong>{{ bean.promotionPrice || bean.price }}원</strong>\n    </article>\n  </section>\n</template>\n<script setup lang=\"ts\">\ndefineProps<{ products: any[] }>();\n</script>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#78350f\",\n  \"secondary\": \"#b45309\",\n  \"accent\": \"#d97706\",\n  \"surface\": \"#fffbeb\",\n  \"radius\": \"12px\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/bean-chronicle/v1/dist/index.js',
    'SUCCESS',
    '[15:10:00] [Hybrid-Deploy] Linked specialty coffee catalog (4 products) + editorial journal (2 articles) + cupping reservation form.\n[15:10:01] [Publish] Custom bundle built and published to /s/bean-chronicle.',
    'hajun.roast',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- Build Jobs History (kkumz_build_job)
INSERT INTO kkumz_build_job (
    id, site_id, release_id, job_type, deploy_mode, status, duration_ms, error_summary, ai_patch_applied, build_log, triggered_by, started_at, finished_at
) VALUES
(
    'job-kkumz-001',
    'site-kkumz-001',
    'rel-kkumz-001-v1',
    'INITIAL_PROVISION',
    'DYNAMIC_SDUI',
    'SUCCESS',
    820,
    NULL,
    FALSE,
    '[10:12:01] [Instant-Deploy] Validating JSON layout schema and design tokens... OK\n[10:12:02] [Publish] Live route /s/matcha-atelier activated (v1).',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '1 second'
),
(
    'job-kkumz-002',
    'site-kkumz-001',
    'rel-kkumz-001-v2',
    'WEBIDE_BUNDLE_BUILD',
    'CUSTOM_BUNDLE',
    'SUCCESS',
    2140,
    NULL,
    TRUE,
    '[14:20:10] [Bundle-Builder] Compiling 4 virtual SFC files...\n[14:20:11] [AI-Self-Heal] Auto-resolved missing closing tag in HeroSection.vue\n[14:20:12] [Publish] Promoted v2 to active release for /s/matcha-atelier.',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '1 hour' + INTERVAL '2 seconds'
),
(
    'job-kkumz-003',
    'site-kkumz-002',
    'rel-kkumz-002-v2',
    'CHAT_HOT_RELOAD',
    'DYNAMIC_SDUI',
    'SUCCESS',
    450,
    NULL,
    FALSE,
    '[16:40:12] [Chat-Refine] Updated hero copy and activated flash deal highlight. Live in 450ms.',
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours' + INTERVAL '1 second'
),
(
    'job-kkumz-004',
    'site-kkumz-004',
    'rel-kkumz-004-v1',
    'WEBIDE_BUNDLE_BUILD',
    'CUSTOM_BUNDLE',
    'SUCCESS',
    1980,
    NULL,
    FALSE,
    '[15:10:00] [Hybrid-Deploy] Linked store catalog + editorial journal. Custom bundle built and published.',
    'hajun.roast',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '2 seconds'
);

-- Team Recruitment Posts (kkumz_team_recruit)
INSERT INTO kkumz_team_recruit (
    id, site_id, position_role, title, description, required_skills_json, headcount, recruited_count, status, created_at
) VALUES
(
    'rec-kkumz-001',
    'site-kkumz-001',
    'MARKETER',
    '제주 유기농 말차 브랜드 숏폼 콘텐츠 & 글로벌 D2C 그로스 마케터 모십니다',
    '서귀포 다원 현지 스토리텔링 릴스/쇼츠 기획과 북미·일본 마켓 초기 진출 캠페인을 함께 리드할 코파운더급 브랜드 마케터를 찾습니다. 초기 지분 스톡옵션 및 매출 인센티브를 함께 나눕니다.',
    '["인스타그램 릴스 기획", "브랜드 카피라이팅", "Meta/Google 퍼포먼스 광고", "글로벌 D2C"]'::jsonb,
    2,
    1,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'rec-kkumz-002',
    'site-kkumz-001',
    'DESIGNER',
    '수제 백자 다완 패키지 & 보자기 기프트 언박싱 경험 디자이너 모집',
    '친환경 지류 패키지와 보자기 선물 포장, 다도 가이드북 편집 디자인을 담당할 브랜드/패키지 디자이너를 모십니다.',
    '["패키지 디자인", "브랜드 아이덴티티", "Figma", "인쇄 감리"]'::jsonb,
    1,
    0,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'rec-kkumz-003',
    'site-kkumz-002',
    'FRONTEND',
    '3D 책상 배치 컨피규레이터(Three.js / Vue 3) 함께 개발할 프론트엔드 엔지니어',
    '데스크 오빗 스토어 내에서 고객이 자신의 책상 크기(1200~1800mm)에 맞춰 월넛 데스크 쉘프와 모니터, 충전 독을 3D로 배치해 볼 수 있는 인터랙티브 컨피규레이터를 개발할 프론트엔드 팀원을 찾습니다.',
    '["Vue 3", "TypeScript", "Three.js / WebGL", "인터랙티브 UI"]'::jsonb,
    1,
    0,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'rec-kkumz-004',
    'site-kkumz-002',
    'MD',
    '커스텀 키보드·데스크 기어 글로벌 소싱 및 프리오더 운영 MD 모집',
    '알루미늄 CNC 가공 파트너사 커뮤니케이션 및 신규 데스크테리어 기어 라인업 기획을 함께할 하드웨어/커머스 MD를 찾습니다.',
    '["하드웨어 소싱", "와디즈/킥스타터 펀딩", "커머스 상품기획"]'::jsonb,
    1,
    1,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'rec-kkumz-005',
    'site-kkumz-003',
    'PLANNER',
    '한강 나이트 런 시즌 마라톤 대회 제휴 & 브랜드 콜라보 커뮤니티 매니저',
    '스포츠 브랜드 팝업 세션 제휴 및 200명 규모 한강 나이트 런 행사 운영을 함께 기획할 커뮤니티 플래너를 모십니다.',
    '["오프라인 이벤트 기획", "브랜드 파트너십", "러닝 크루 운영"]'::jsonb,
    2,
    1,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'rec-kkumz-006',
    'site-kkumz-004',
    'CO_FOUNDER',
    '스페셜티 원두 월간 구독 모델 & 오프라인 쇼룸 비즈니스 리드 코파운더',
    '성수동 로스터리 쇼룸 오프라인 커핑 클래스와 온라인 정기구독 리텐션 모델을 함께 설계할 비즈니스 공동창업자를 찾습니다.',
    '["구독 커머스 전략", "F&B 브랜딩", "B2B 원두 세일즈"]'::jsonb,
    1,
    0,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
);

-- Team Members & Applications (kkumz_team_member)
INSERT INTO kkumz_team_member (
    id, site_id, recruit_id, username, display_name, member_role, position_role, join_type, application_message, status, joined_at
) VALUES
('tmem-kkumz-001', 'site-kkumz-001', NULL, 'soyeon.jeju', '한소연 대표', 'OWNER', 'CO_FOUNDER', 'FOUNDER', '제주 유기농 다원 총괄 및 브랜드 디렉팅', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('tmem-kkumz-002', 'site-kkumz-001', 'rec-kkumz-001', 'yuna.growth', '김유나 마케터', 'EDITOR', 'MARKETER', 'APPLIED', '티 브랜드 인스타그램 릴스 100만 뷰 캠페인 경험이 있습니다. 제주 말차의 매력을 글로벌 MZ세대에게 알리고 싶습니다!', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('tmem-kkumz-003', 'site-kkumz-001', 'rec-kkumz-002', 'daeun.pack', '이다은 패키지디자이너', 'EDITOR', 'DESIGNER', 'APPLIED', '전통 한지와 친환경 펄프 몰드를 활용한 프리미엄 패키지 디자인 포트폴리오를 보유하고 있습니다. 함께하고 싶어요.', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '6 hours'),
('tmem-kkumz-004', 'site-kkumz-002', NULL, 'minjun.maker', '박민준 메이커', 'OWNER', 'CO_FOUNDER', 'FOUNDER', '제품 기구 설계 및 데스크 오빗 총괄 디렉터', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('tmem-kkumz-005', 'site-kkumz-002', 'rec-kkumz-004', 'hyunwoo.md', '장현우 테크MD', 'MD', 'MD', 'INVITED', '커스텀 키보드 및 데스크 기어 전문 MD 합류', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('tmem-kkumz-006', 'site-kkumz-002', 'rec-kkumz-003', 'seojun.fe', '윤서준 프론트엔드', 'DEVELOPER', 'FRONTEND', 'APPLIED', 'Vue 3와 Three.js로 가구 배치 시뮬레이터를 구현해 본 경험이 있습니다. 데스크 오빗 3D 컨피규레이터를 완성하고 싶습니다.', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('tmem-kkumz-007', 'site-kkumz-003', NULL, 'jihoon.run', '정지훈 크루장', 'OWNER', 'PLANNER', 'FOUNDER', '서울 나이트 런 클럽 헤드 코치 및 크루장', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('tmem-kkumz-008', 'site-kkumz-004', NULL, 'hajun.roast', '최하준 헤드로스터', 'OWNER', 'CO_FOUNDER', 'FOUNDER', 'Q-Grader 및 빈 크로니클 수석 로스터', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '2 days');

-- Community Feed Comments (kkumz_feed_comment)
INSERT INTO kkumz_feed_comment (
    id, site_id, username, display_name, author_role_badge, content, created_at
) VALUES
('cmt-kkumz-001', 'site-kkumz-001', 'minjun.maker', '박민준 메이커', '프로덕트 메이커', '보태니컬 딥그린 컬러 팔레트와 백자 다완 스타터 세트 구성이 정말 감각적이네요! 라이브 스토어(/s/matcha-atelier)에서 말차 라떼 베이스 바로 주문했습니다.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('cmt-kkumz-002', 'site-kkumz-001', 'hajun.roast', '최하준 헤드로스터', '로스터리 창업가', '성수동 저희 로스터리 쇼룸에서도 제주 첫물 말차 콜라보 시즌 메뉴를 기획해 보고 싶습니다. 응원합니다!', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('cmt-kkumz-003', 'site-kkumz-002', 'seojun.fe', '윤서준 프론트엔드', '프론트엔드 개발자', 'Qi2 3-in-1 알루미늄 충전 독과 월넛 데스크 쉘프 조합이 개발자 취향 저격입니다. 프론트엔드 포지션에 지원서 남겼습니다!', CURRENT_TIMESTAMP - INTERVAL '18 hours'),
('cmt-kkumz-004', 'site-kkumz-003', 'soyeon.jeju', '한소연 대표', '브랜드 창업가', '반포 잠수교 나이트 런 코스 가이드 아티클이 정말 자세히 정리되어 있네요. 이번 주 목요일 600 페이스 게스트 런 신청 완료했습니다!', CURRENT_TIMESTAMP - INTERVAL '12 hours'),
('cmt-kkumz-005', 'site-kkumz-004', 'jihoon.run', '정지훈 크루장', '커뮤니티 리더', '산지 농장 직교역 매거진 읽다가 브루잉 머신이랑 원두까지 한 화면에서 바로 담을 수 있는 복합 사이트 구성이 무척 매끄럽습니다.', CURRENT_TIMESTAMP - INTERVAL '5 hours');

-- Community Feed Likes (kkumz_feed_like)
INSERT INTO kkumz_feed_like (id, site_id, username, created_at) VALUES
('like-kkumz-001', 'site-kkumz-001', 'admin', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('like-kkumz-002', 'site-kkumz-001', 'minjun.maker', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('like-kkumz-003', 'site-kkumz-002', 'admin', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('like-kkumz-004', 'site-kkumz-002', 'soyeon.jeju', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('like-kkumz-005', 'site-kkumz-003', 'admin', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('like-kkumz-006', 'site-kkumz-004', 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Moderation Reports (kkumz_moderation_report)
INSERT INTO kkumz_moderation_report (
    id, target_type, target_id, site_id, reporter_username, reason_category, reason_detail,
    status, action_taken, moderator_username, resolved_at, created_at
) VALUES
(
    'rep-kkumz-001',
    'COMMENT',
    'cmt-spam-901',
    'site-kkumz-002',
    'minjun.maker',
    'SPAM_PROMOTION',
    '프로젝트 피드 댓글란에 무관한 외부 불법 홍보 링크를 반복 게시하는 스팸 댓글이 감지되어 신고합니다.',
    'PENDING',
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),
(
    'rep-kkumz-002',
    'RECRUIT',
    'rec-kkumz-005',
    'site-kkumz-003',
    'guest.runner',
    'INFO_VERIFICATION',
    '오프라인 나이트 런 세션 참가비 및 보험 가입 여부에 대한 안내 문구 보완이 필요합니다.',
    'PENDING',
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '5 hours'
),
(
    'rep-kkumz-003',
    'SITE',
    'site-kkumz-001',
    'site-kkumz-001',
    'competitor.check',
    'COPYRIGHT_TRADEMARK',
    '유기농 인증 번호 표기 누락 여부 확인 요청 건',
    'DISMISSED',
    '서귀포 유기농 친환경 인증서 원본 및 상세페이지 하단 법적 필수 표기 사항이 정상 확인되어 신고 기각 처리함',
    'admin',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- Notifications (kkumz_notification)
INSERT INTO kkumz_notification (
    id, recipient_username, notif_type, title, message, link_url, is_read, created_at
) VALUES
('noti-kkumz-001', 'soyeon.jeju', 'TEAM_APPLY', '패키지 디자이너 포지션 신규 지원 도착', '이다은 패키지디자이너님이 [말차 아뜰리에 제주] 팀 합류를 신청했습니다.', '/lounge', FALSE, CURRENT_TIMESTAMP - INTERVAL '6 hours'),
('noti-kkumz-002', 'minjun.maker', 'TEAM_APPLY', '프론트엔드 엔지니어 포지션 신규 지원 도착', '윤서준 프론트엔드님이 [데스크 오빗] 팀 합류를 신청했습니다.', '/lounge', FALSE, CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('noti-kkumz-003', 'soyeon.jeju', 'BUILD_DONE', '말차 아뜰리에 제주 v2 배포 완료', '커스텀 번들 빌드가 성공적으로 완료되어 /s/matcha-atelier 에 즉시 반영되었습니다.', '/s/matcha-atelier', TRUE, CURRENT_TIMESTAMP - INTERVAL '1 hour');
