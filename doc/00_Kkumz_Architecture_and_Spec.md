# 꿈즈(Kkumz) 아키텍처 및 기능 명세서 (v2.6.5)

## 1. 서비스 비전 및 목표
**꿈즈(Kkumz)**는 비기술 창업자, 학생, 기획자, 개발자가 한 줄의 아이디어 프롬프트로 **서비스 이름, 벡터 SVG 로고, 상세 기능 명세서(FRS), 화면 설계 목록, 멀티사이트 런타임 및 커스텀 코드 번들**을 자동 생성하고, 창업 커뮤니티에서 피드백을 나누며 팀원을 모집하는 플랫폼입니다.

## 2. 시스템 아키텍처 (`services/kkumz`)
- **GroupId / ArtifactId**: `com.empasy.sync.kkumz:kkumz-parent`
- **API Prefix**: `/api/v1/kkumz/...`
- **사용자 웹 (`web/` — `@empasy/kkumz-web`)**:
  - **커뮤니티 탐색 모드**: 1단 심플 GNB + 쇼케이스 피드 + 팀빌딩 라운지 + 내 워크스페이스 대시보드
  - **집중형 AI 스튜디오 모드**: 좌측 수직 6-Step Stepper 사이드바 + 우측 집중형 캔버스 (1. 아이디어 정의 ➔ 2. 브랜드 네이밍 & 로고 ➔ 3. 기능명세서 & 화면설계 ➔ 4. 실시간 프리뷰 편집 ➔ 5. WebIDE 코드 편집 ➔ 6. 빌드 & 배포)
  - **멀티사이트 라이브 렌더러 (`/s/:siteSlug`)**: 쇼핑몰, 브랜드/커뮤니티 사이트, 복합 사이트 실시간 실행 및 주문/신청 연동
- **관리자 백오피스 (`admin/` — `@empasy/kkumz-admin`)**:
  - Vben Admin 표준 (`PermissionModeEnum.BACK`, `/api/v1/kkumz/sys/permission/getUserPermissionByToken` DB 단일 원천 동적 라우팅)
  - 5대 관리 도메인: 운영 종합 대시보드, AI 스튜디오 관제(아이디어/명세서, AI 프롬프트/필터), 빌드 & 사이트 관제(멀티사이트 도메인, 빌드 파이프라인 로그), 커뮤니티 & 팀빌딩 관리(프로젝트 큐레이션, 신고 모더레이션), 시스템 관리(사용자, 역할, 메뉴, 공통코드)
- **데이터베이스 (`db/`)**:
  - PostgreSQL 18.6 (`pgvector`, `uuid-ossp`, `pg_trgm`)
  - 6단계 표준 파일: `00_extensions.sql` ~ `05_sample_dev.sql` (`sys_*` 시스템 테이블 + `kkumz_*` 도메인 테이블 15종)
