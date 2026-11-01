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

-- 시스템 기본 메뉴 (대시보드 및 시스템 관리)
INSERT INTO sys_permission (
    id, parent_id, name, url, component, is_route, component_name, redirect,
    menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description
) VALUES
('perm-kkumz-sys-001', NULL, '대시보드', '/dashboard', 'layouts/default/index', TRUE, 'DashboardLayout', '/dashboard/analysis', 0, NULL, '1', 1.0, TRUE, 'ant-design:dashboard-outlined', FALSE, FALSE, FALSE, FALSE, '꿈즈 운영 종합 대시보드'),
('perm-kkumz-sys-002', 'perm-kkumz-sys-001', '운영 종합 대시보드', '/dashboard/analysis', 'dashboard/analysis/index', TRUE, 'KkumzDashboardAnalysis', NULL, 1, 'dashboard:analysis:view', '1', 1.1, FALSE, 'ant-design:bar-chart-outlined', TRUE, TRUE, FALSE, FALSE, '아이디어 생성, AI 응답성, 사이트 배포 및 커뮤니티 핵심 지표'),
('perm-kkumz-sys-010', NULL, '시스템 관리', '/system', 'layouts/default/index', TRUE, 'SystemLayout', '/system/user', 0, NULL, '1', 50.0, TRUE, 'ant-design:setting-outlined', FALSE, FALSE, FALSE, FALSE, '사용자, 역할, 메뉴 권한 및 공통 사전 관리'),
('perm-kkumz-sys-011', 'perm-kkumz-sys-010', '사용자 계정 관리', '/system/user', 'system/user/index', TRUE, 'KkumzSystemUserView', NULL, 1, 'system:user:view', '1', 50.1, FALSE, 'ant-design:user-outlined', TRUE, TRUE, FALSE, FALSE, '창업가·모더레이터·관리자 계정 관리'),
('perm-kkumz-sys-012', 'perm-kkumz-sys-010', '역할 및 권한 관리', '/system/role', 'system/role/index', TRUE, 'KkumzSystemRoleView', NULL, 1, 'system:role:view', '1', 50.2, FALSE, 'ant-design:team-outlined', TRUE, TRUE, FALSE, FALSE, 'RBAC 역할 및 권한 매핑 관리'),
('perm-kkumz-sys-013', 'perm-kkumz-sys-010', '메뉴 및 라우트 관리', '/system/menu', 'system/menu/index', TRUE, 'KkumzSystemMenuView', NULL, 1, 'system:menu:view', '1', 50.3, FALSE, 'ant-design:menu-outlined', TRUE, TRUE, FALSE, FALSE, 'DB 단일 원천 동적 메뉴 트리 관리'),
('perm-kkumz-sys-014', 'perm-kkumz-sys-010', '공통 코드(사전) 관리', '/system/dict', 'system/dict/index', TRUE, 'KkumzSystemDictView', NULL, 1, 'system:dict:view', '1', 50.4, FALSE, 'ant-design:book-outlined', TRUE, TRUE, FALSE, FALSE, '사이트 유형, 직군, 신고 사유 등 공통 코드 관리');

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
('dict-kkumz-004', '커뮤니티 신고 사유', 'kkumz_report_reason', '콘텐츠 모더레이션 신고 분류', 'admin');

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
('ditem-kkumz-014', 'dict-kkumz-004', '허위 채용·모집 정보', 'INFO_VERIFICATION', '사실과 다른 팀원 모집 조건 기재', 3, 1);
