# 꿈즈 (Kkumz) — AI 브랜드·멀티사이트 빌더 & 창업 팀빌딩 커뮤니티

**꿈즈(Kkumz)**는 한 줄의 아이디어만 입력하면 AI가 **브랜드 네이밍(슬러그 가용성 검증), 벡터 SVG 로고, 상세 기능 명세서(FRS), 화면 설계**를 자동 생성하고, 실시간 프리뷰 편집 및 WebIDE 코드 커스터마이징을 거쳐 즉시 멀티사이트로 배포하며 함께할 창업 팀원까지 모집하는 **SyncSeries 버티컬 애플리케이션 서비스(`services/kkumz`)**입니다.

---

## 📁 프로젝트 구조 (`services/kkumz`)

```text
services/kkumz/
├── Server/                         # Spring Boot 3.5 + Java 25 + AgentScope Java 백엔드
│   ├── pom.xml                     # kkumz-server-parent
│   └── kkumz-server-admin/         # kkumz-server-admin (/api/v1/kkumz/**)
├── web/                            # @empasy/kkumz-web (창업가 커뮤니티 & 집중형 AI 스튜디오)
├── admin/                          # @empasy/kkumz-admin (Vben Admin 기반 전용 운영 백오피스)
├── db/                             # PostgreSQL 18.6 6단계 표준 DB 초기화 스크립트
│   ├── 00_extensions.sql
│   ├── 01_schema_system.sql
│   ├── 02_schema_domain.sql
│   ├── 03_seed_system.sql
│   ├── 04_seed_domain.sql
│   └── 05_sample_dev.sql
├── doc/                            # 기능 명세서 및 화면/아키텍처 설계 문서
├── docker-compose-local.yml        # 로컬 IDE 개발용 인프라 격리 (PostgreSQL 18.6, Redis)
└── docker-compose-dev.yml          # 통합 컨테이너 클러스터 (Gateway, Web, Admin, Server, DB, Redis)
```

## 🚀 빠른 실행 가이드

### 1. 로컬 인프라 기동 및 백엔드 실행
```bash
docker compose -f docker-compose-local.yml up -d
mvn clean test
```

### 2. 프론트엔드 빌드 및 실행
```bash
# 사용자 포털 & AI 스튜디오
cd web && pnpm install && pnpm run build

# 관리자 백오피스
cd ../admin && pnpm install && pnpm run build
```
