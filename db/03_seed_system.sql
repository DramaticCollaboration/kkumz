-- ============================================================================
-- Kkumz (꿈즈) 시스템 필수 기초 시드 데이터 (03_seed_system.sql)
-- (무중복 결정론적 순수 INSERT INTO 단일 원천 구성)
-- ============================================================================

INSERT INTO sys_user (id, username, realname, password, salt, avatar, email, phone, org_code, status, del_flag, create_by) VALUES
('usr-kkumz-001', 'admin', '김도현 총괄관리자', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'kkumz2026', '/icons/favicon-128x128.png', 'admin@kkumz.com', '010-2026-1001', 'KKUMZ-HQ', 1, 0, 'admin'),
('usr-kkumz-002', 'moderator.kim', '김서연 모더레이터', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'kkumz2026', '/icons/favicon-128x128.png', 'moderator@kkumz.com', '010-2026-1002', 'KKUMZ-TRUST', 1, 0, 'admin'),
('usr-kkumz-003', 'soyeon.jeju', '한소연 대표', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'kkumz2026', '/icons/favicon-128x128.png', 'soyeon@matcha-atelier.kr', '010-3412-8891', 'CREATOR', 1, 0, 'admin'),
('usr-kkumz-004', 'minjun.maker', '박민준 메이커', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'kkumz2026', '/icons/favicon-128x128.png', 'minjun@deskorbit.io', '010-5821-3340', 'CREATOR', 1, 0, 'admin'),
('usr-kkumz-005', 'jihoon.run', '정지훈 크루장', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'kkumz2026', '/icons/favicon-128x128.png', 'jihoon@seoulrun.club', '010-7719-0922', 'CREATOR', 1, 0, 'admin'),
('usr-kkumz-006', 'hajun.roast', '최하준 헤드로스터', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'kkumz2026', '/icons/favicon-128x128.png', 'hajun@beanchronicle.kr', '010-9102-4418', 'CREATOR', 1, 0, 'admin');

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
) VALUES
('perm-kkumz-sys-001', NULL, '대시보드', '/dashboard', 'layouts/default/index', TRUE, 'DashboardLayout', '/dashboard/analysis', 0, NULL, '1', 1.0, TRUE, 'ant-design:dashboard-outlined', FALSE, FALSE, FALSE, FALSE, '꿈즈 운영 종합 대시보드'),
('perm-kkumz-sys-002', 'perm-kkumz-sys-001', '대시보드', '/dashboard/analysis', 'dashboard/analysis/index', TRUE, 'KkumzDashboardAnalysis', NULL, 1, 'dashboard:analysis:view', '1', 1.1, FALSE, 'ant-design:bar-chart-outlined', TRUE, TRUE, FALSE, FALSE, '아이디어 생성, AI 응답성, 사이트 배포 및 커뮤니티 핵심 지표'),
('perm-kkumz-sys-010', NULL, '시스템 설정', '/system', 'layouts/default/index', TRUE, 'SystemLayout', '/system/user', 0, NULL, '1', 50.0, TRUE, 'ant-design:setting-outlined', FALSE, FALSE, FALSE, FALSE, '사용자, 역할, 메뉴 권한 및 공통 사전 관리'),
('perm-kkumz-sys-011', 'perm-kkumz-sys-010', '계정 관리', '/system/user', 'system/user/index', TRUE, 'KkumzSystemUserView', NULL, 1, 'system:user:view', '1', 50.1, FALSE, 'ant-design:user-outlined', TRUE, TRUE, FALSE, FALSE, '창업가·모더레이터·관리자 계정 관리'),
('perm-kkumz-sys-012', 'perm-kkumz-sys-010', '권한 관리', '/system/role', 'system/role/index', TRUE, 'KkumzSystemRoleView', NULL, 1, 'system:role:view', '1', 50.2, FALSE, 'ant-design:team-outlined', TRUE, TRUE, FALSE, FALSE, 'RBAC 역할 및 권한 매핑 관리'),
('perm-kkumz-sys-013', 'perm-kkumz-sys-010', '메뉴 관리', '/system/menu', 'system/menu/index', TRUE, 'KkumzSystemMenuView', NULL, 1, 'system:menu:view', '1', 50.3, FALSE, 'ant-design:menu-outlined', TRUE, TRUE, FALSE, FALSE, 'DB 단일 원천 동적 메뉴 트리 관리'),
('perm-kkumz-sys-014', 'perm-kkumz-sys-010', '공통 코드', '/system/dict', 'system/dict/index', TRUE, 'KkumzSystemDictView', NULL, 1, 'system:dict:view', '1', 50.4, FALSE, 'ant-design:book-outlined', TRUE, TRUE, FALSE, FALSE, '사이트 유형, 직군, 신고 사유 등 공통 코드 관리');

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
('srp_kkumz_sys_001', 'role_admin', 'perm-kkumz-sys-001'),
('srp_kkumz_sys_002', 'role_admin', 'perm-kkumz-sys-002'),
('srp_kkumz_sys_010', 'role_admin', 'perm-kkumz-sys-010'),
('srp_kkumz_sys_011', 'role_admin', 'perm-kkumz-sys-011'),
('srp_kkumz_sys_012', 'role_admin', 'perm-kkumz-sys-012'),
('srp_kkumz_sys_013', 'role_admin', 'perm-kkumz-sys-013'),
('srp_kkumz_sys_014', 'role_admin', 'perm-kkumz-sys-014'),
('srp_kkumz_mod_001', 'role_moderator', 'perm-kkumz-sys-001'),
('srp_kkumz_mod_002', 'role_moderator', 'perm-kkumz-sys-002');

-- 공통 코드 사전
INSERT INTO sys_dict (id, dict_name, dict_code, description, create_by) VALUES
('dict-kkumz-001', '사이트 구축 유형', 'kkumz_site_type', '꿈즈 멀티사이트 구축 아키텍처 유형', 'admin'),
('dict-kkumz-002', '배포 엔진 모드', 'kkumz_deploy_mode', '실시간 디자인 스키마 배포 vs WebIDE 커스텀 번들 배포', 'admin'),
('dict-kkumz-003', '팀빌딩 모집 직군', 'kkumz_recruit_role', '창업 프로젝트 합류 모집 포지션', 'admin'),
('dict-kkumz-004', '커뮤니티 신고 사유', 'kkumz_report_reason', '콘텐츠 모더레이션 신고 분류', 'admin'),
('dict-kkumz-005', '화면 레이아웃 유형', 'kkumz_layout_type', 'AI 기획서 및 스튜디오 화면 레이아웃 코드', 'admin'),
('dict-kkumz-006', '화면 컴포넌트 유형', 'kkumz_component_type', 'AI 기획서 및 스튜디오 UI 컴포넌트 코드', 'admin'),
('dict-kkumz-007', '스튜디오 스타터 프리셋', 'kkumz_starter_preset', '아이디어 스튜디오 및 쇼케이스 기본 템플릿 프리셋', 'admin');

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status) VALUES
('ditem-kkumz-001', 'dict-kkumz-001', '쇼핑몰 스토어', 'COMMERCE', '상품 카탈로그·장바구니·주문서 결제가 포함된 브랜드 스토어', 1, 1),
('ditem-kkumz-002', 'dict-kkumz-001', '브랜드·커뮤니티 웹', 'CMS_SITE', '매거진 아티클·공지·온라인 참가 신청 폼 중심 웹사이트', 2, 1),
('ditem-kkumz-003', 'dict-kkumz-001', '매거진+스토어 복합 사이트', 'HYBRID', '브랜드 저널 콘텐츠와 커머스 스토어가 결합된 복합 사이트', 3, 1),
('ditem-kkumz-004', 'dict-kkumz-002', '즉시 디자인 반영 배포', 'DYNAMIC_SDUI', '테마 토큰 및 레이아웃 구성 즉시 반영 배포', 1, 1),
('ditem-kkumz-005', 'dict-kkumz-002', '코드 커스텀 번들 배포', 'CUSTOM_BUNDLE', 'WebIDE에서 편집한 Vue/Quasar 소스 코드 번들 빌드 배포', 2, 1),
('ditem-kkumz-006', 'dict-kkumz-003', '프론트엔드 개발', 'FRONTEND', '웹/모바일 UI 및 인터랙션 개발', 1, 1),
('ditem-kkumz-007', 'dict-kkumz-003', '브랜드 디자이너', 'DESIGNER', 'BX/UI 디자인 및 패키지 그래픽', 2, 1),
('ditem-kkumz-008', 'dict-kkumz-003', '그로스 마케터', 'MARKETER', '콘텐츠 마케팅 및 퍼포먼스 캠페인', 3, 1),
('ditem-kkumz-009', 'dict-kkumz-003', '커머스 MD', 'MD', '상품 기획 및 소싱·프로모션 운영', 4, 1),
('ditem-kkumz-010', 'dict-kkumz-003', '서비스 기획', 'PLANNER', '프로덕트 기획 및 커뮤니티 운영', 5, 1),
('ditem-kkumz-011', 'dict-kkumz-003', '공동창업자', 'CO_FOUNDER', '비즈니스 리드 및 핵심 코파운더', 6, 1),
('ditem-kkumz-012', 'dict-kkumz-004', '스팸 및 불법 홍보', 'SPAM_PROMOTION', '무관한 광고 링크 및 도배 게시물', 1, 1),
('ditem-kkumz-013', 'dict-kkumz-004', '저작권·상표권 침해', 'COPYRIGHT_TRADEMARK', '타 브랜드 로고·콘텐츠 무단 도용', 2, 1),
('ditem-kkumz-014', 'dict-kkumz-004', '허위 채용·모집 정보', 'INFO_VERIFICATION', '사실과 다른 팀원 모집 조건 기재', 3, 1),
('ditem-kkumz-021', 'dict-kkumz-005', '매거진+스토어 복합 쇼케이스', 'HYBRID_EDITORIAL_COMMERCE', '에디토리얼 저널과 커머스 결합 레이아웃', 1, 1),
('ditem-kkumz-022', 'dict-kkumz-005', '듀얼 스트림 그리드', 'DUAL_STREAM_GRID', '콘텐츠와 상품을 2열 스트림으로 배치', 2, 1),
('ditem-kkumz-023', 'dict-kkumz-005', '동적 신청·예약 폼', 'DYNAMIC_CMS_FORM', '온라인 참가 신청 및 예약 폼 레이아웃', 3, 1),
('ditem-kkumz-024', 'dict-kkumz-005', '보태니컬 에디토리얼', 'EDITORIAL_BOTANICAL', '브랜드 스토리텔링 중심 히어로 레이아웃', 4, 1),
('ditem-kkumz-025', 'dict-kkumz-005', '3열 필터형 상품 그리드', 'FILTERABLE_3COL_GRID', '카테고리 탭 및 3열 상품 진열 그리드', 5, 1),
('ditem-kkumz-026', 'dict-kkumz-005', '원페이지 간편 결제', 'ONE_PAGE_CHECKOUT', '단일 화면 장바구니 및 배송지 결제 폼', 6, 1),
('ditem-kkumz-027', 'dict-kkumz-005', '다크 테크 쇼케이스 그리드', 'DARK_TECH_GRID', '하드웨어·데스크 기어 사양 중심 그리드', 7, 1),
('ditem-kkumz-028', 'dict-kkumz-005', '스펙 비교형 카탈로그', 'SPEC_COMPARISON_GRID', '제품 호환성 및 상세 스펙 비교 그리드', 8, 1),
('ditem-kkumz-029', 'dict-kkumz-005', '매거진 풀배너 히어로', 'MAGAZINE_HERO', '커뮤니티 및 미디어 메인 풀배너', 9, 1),
('ditem-kkumz-030', 'dict-kkumz-005', '에디토리얼 카드 리스트', 'EDITORIAL_CARDS', '매거진 아티클 카드 피드 레이아웃', 10, 1),
('ditem-kkumz-031', 'dict-kkumz-005', '에디토리얼 매거진 레이아웃', 'EDITORIAL_MAGAZINE_LAYOUT', '저널 열람 및 이벤트 신청 복합 레이아웃', 11, 1),
('ditem-kkumz-032', 'dict-kkumz-005', '스탠다드 히어로 배너', 'STANDARD_HERO', '기본 브랜드 소개 히어로 섹션', 12, 1),
('ditem-kkumz-033', 'dict-kkumz-005', '표준 반응형 그리드', 'STANDARD_GRID', '표준 반응형 콘텐츠 그리드', 13, 1),
('ditem-kkumz-034', 'dict-kkumz-005', '온라인 접수 폼 뷰', 'FORM_VIEW', '표준 입력 폼 화면', 14, 1),
('ditem-kkumz-041', 'dict-kkumz-006', '메인 스토리 히어로', 'FeaturedStoryHero', '대표 스토리텔링 배너 컴포넌트', 1, 1),
('ditem-kkumz-042', 'dict-kkumz-006', '로스팅 프로파일 배지', 'RoastProfileBadge', '원두 로스팅 등급 및 노트 표시 배지', 2, 1),
('ditem-kkumz-043', 'dict-kkumz-006', '스토어 상품 카탈로그', 'CommerceCatalogSection', '연동된 쇼핑몰 상품 목록 그리드', 3, 1),
('ditem-kkumz-044', 'dict-kkumz-006', '브랜드 저널 아티클', 'EditorialJournalSection', '연동된 CMS 매거진 아티클 목록', 4, 1),
('ditem-kkumz-045', 'dict-kkumz-006', '클래스 예약 폼', 'ClassBookingForm', '오프라인 클래스 및 세션 예약 입력 폼', 5, 1),
('ditem-kkumz-046', 'dict-kkumz-006', 'AI 브루잉 컨시어지', 'AiBrewingConcierge', 'AI 기반 상품·추출 가이드 상담 위젯', 6, 1),
('ditem-kkumz-047', 'dict-kkumz-006', '브랜드 스토리 배너', 'BrandStoryBanner', '브랜드 철학 및 산지 소개 배너', 7, 1),
('ditem-kkumz-048', 'dict-kkumz-006', '다도 리추얼 가이드', 'MatchaRitualSteps', '단계별 음용 방법 안내 섹션', 8, 1),
('ditem-kkumz-049', 'dict-kkumz-006', '추천 상품 진열장', 'FeaturedProductGrid', '핵심 주력 상품 큐레이션 그리드', 9, 1),
('ditem-kkumz-050', 'dict-kkumz-006', '카테고리 필터 탭', 'CategoryTabs', '상품 분류별 필터링 탭 바', 10, 1),
('ditem-kkumz-051', 'dict-kkumz-006', '테이스팅 노트 배지', 'TasteNoteBadge', '풍미 및 주요 특징 요약 태그', 11, 1),
('ditem-kkumz-052', 'dict-kkumz-006', '빠른 장바구니 서랍', 'QuickCartDrawer', '화면 전환 없는 슬라이드 장바구니', 12, 1),
('ditem-kkumz-053', 'dict-kkumz-006', '선물 포장 선택기', 'GiftWrapSelector', '기프트 패키징 옵션 선택 컴포넌트', 13, 1),
('ditem-kkumz-054', 'dict-kkumz-006', '주문서 입력 폼', 'OrderForm', '수령인 및 배송지 입력 결제 폼', 14, 1),
('ditem-kkumz-055', 'dict-kkumz-006', 'AI 티 소믈리에 위젯', 'AiTeaSommelierWidget', '취향 맞춤형 티 추천 대화창', 15, 1),
('ditem-kkumz-056', 'dict-kkumz-006', '데스크 셋업 쇼케이스', 'OrbitSetupShowcase', '워크스테이션 배치 예시 프리뷰', 16, 1),
('ditem-kkumz-057', 'dict-kkumz-006', '핵심 스펙 요약 바', 'SpecHighlightStrip', '소재·규격·출력 등 핵심 사양 요약', 17, 1),
('ditem-kkumz-058', 'dict-kkumz-006', '베스트 기어 그리드', 'BestGearGrid', '인기 데스크 기어 상품 그리드', 18, 1),
('ditem-kkumz-059', 'dict-kkumz-006', '기기 호환성 필터', 'CompatibilityFilter', '연동 기기 및 규격별 필터', 19, 1),
('ditem-kkumz-060', 'dict-kkumz-006', '상품 카드', 'ProductCard', '개별 상품 썸네일 및 가격 카드', 20, 1),
('ditem-kkumz-061', 'dict-kkumz-006', '장바구니 사이드바', 'CartSidebar', '우측 고정형 장바구니 요약 패널', 21, 1),
('ditem-kkumz-062', 'dict-kkumz-006', '배송지 입력 폼', 'ShippingAddressForm', '우편번호 및 상세 주소 입력란', 22, 1),
('ditem-kkumz-063', 'dict-kkumz-006', '쿠폰 적용기', 'CouponApplier', '프로모션 코드 및 할인 쿠폰 적용', 23, 1),
('ditem-kkumz-064', 'dict-kkumz-006', '즉시 결제 버튼', 'InstantOrderButton', '원클릭 주문 확정 액션 버튼', 24, 1),
('ditem-kkumz-065', 'dict-kkumz-006', '주간 세션 공지 배너', 'WeeklySessionBanner', '정규 오프라인 모임 일정 배너', 25, 1),
('ditem-kkumz-066', 'dict-kkumz-006', '크루 활동 지표 바', 'CrewMetricsBar', '누적 참가자 및 활동 거리 지표', 26, 1),
('ditem-kkumz-067', 'dict-kkumz-006', '코스 가이드 목록', 'CourseArticleList', '주요 활동 코스 및 난이도 아티클', 27, 1),
('ditem-kkumz-068', 'dict-kkumz-006', '페이스 계산기 카드', 'PaceCalculatorCard', '목표 기록 대비 구간 페이스 계산', 28, 1),
('ditem-kkumz-069', 'dict-kkumz-006', '세션 일정 선택기', 'SessionSelectField', '참가 희망 일자 선택 드롭다운', 29, 1),
('ditem-kkumz-070', 'dict-kkumz-006', '페이스 그룹 선택', 'PaceRadioGroup', '수준별 그룹 선택 라디오 버튼', 30, 1),
('ditem-kkumz-071', 'dict-kkumz-006', '참가 신청 제출 버튼', 'SubmitApplicationButton', '온라인 참가 신청서 최종 제출', 31, 1),
('ditem-kkumz-081', 'dict-kkumz-007', '스페셜티 커피 로스터리', 'COFFEE_ROASTERY', '{"badge":"매거진+스토어","siteType":"HYBRID","category":"LIFESTYLE_HYBRID","promptText":"콜롬비아·에티오피아 산지 농장 직교역 인터뷰 매거진과 로스팅 프로파일 아티클을 발행하면서, 매월 로스터가 선별한 싱글오리진 원두와 드립백을 바로 주문하고 시음 클래스도 예약할 수 있는 매거진+스토어 복합 사이트를 만들고 싶습니다.","tags":["스페셜티커피","로스터리매거진","싱글오리진","커핑클래스"]}', 1, 1),
('ditem-kkumz-082', 'dict-kkumz-007', '제주 유기농 말차 & 다기', 'JEJU_MATCHA', '{"badge":"쇼핑몰 스토어","siteType":"COMMERCE","category":"FOOD_BEVERAGE","promptText":"제주 서귀포 유기농 다원에서 재배한 첫물 세레모니얼 말차와 이천 도예가의 수제 백자 다완, 100본립 차선을 큐레이션하여 판매하는 프리미엄 다도 D2C 쇼핑몰을 만들고 싶어요.","tags":["제주유기농말차","홈다도","수제다완","보자기패키징"]}', 2, 1),
('ditem-kkumz-083', 'dict-kkumz-007', '커스텀 키보드 & 데스크 기어', 'DESK_ORBIT', '{"badge":"쇼핑몰 스토어","siteType":"COMMERCE","category":"TECH_HARDWARE","promptText":"개발자와 디자이너의 책상 위 케이블을 정리해 주는 CNC 통절삭 알루미늄 Qi2 충전 스테이션, 북미산 월넛 마그네틱 데스크 쉘프, 비대칭 모니터 조명을 판매하는 테크 브랜드 스토어를 만들고 싶습니다.","tags":["데스크테리어","CNC알루미늄","매그세이프독","모듈형데스크"]}', 3, 1),
('ditem-kkumz-084', 'dict-kkumz-007', '한강 나이트 러닝 크루', 'SEOUL_RUN_CLUB', '{"badge":"브랜드·커뮤니티 웹","siteType":"CMS_SITE","category":"COMMUNITY_MEDIA","promptText":"매주 화요일·목요일 저녁 반포·성수·여의도 한강공원에서 함께 달리는 2030 러닝 크루 공식 브랜드 사이트이자, 러닝 코스 아카이브와 정규 세션 참가 신청을 받는 커뮤니티 웹사이트를 만들고 싶어요.","tags":["러닝크루","한강나이트런","마라톤트레이닝","시티러너"]}', 4, 1);
