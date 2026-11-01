# DOC-KZ-11. Kkumz 상용 오픈 및 환경설정 가이드

- **문서 번호**: DOC-KZ-11
- **대상 서비스**: `services/kkumz`
- **관련 설정 파일**:
  - `services/kkumz/docker-compose-local.yml` (IDE 로컬 개발용 인프라 전용)
  - `services/kkumz/docker-compose-dev.yml` (통합 컨테이너 개발 클러스터)
  - `services/kkumz/Server/kkumz-server-admin/src/main/resources/application*.yml`

---

## 1. Spring YAML 4-Tier 프로파일 및 Docker Compose 표준

Kkumz는 SyncSeries 전사 표준에 따라 4단계 Spring YAML 프로파일과 이원화된 Docker Compose 체계를 준수합니다.

### 1.1 Spring Boot 4-Tier 프로파일 구성 (`Server/kkumz-server-admin/src/main/resources/`)
| 설정 파일명 | 프로파일 (`SPRING_PROFILES_ACTIVE`) | 실행 환경 | 주요 데이터소스 및 인프라 연결 정책 |
| :--- | :---: | :--- | :--- |
| `application.yml` | 공통 기본값 (`local` 폴백) | 전 환경 공통 | 서버 포트 `8096`, Jackson 직렬화, 로깅, 기본 프로파일 `${SPRING_PROFILES_ACTIVE:local}` |
| `application-local.yml` | `local` | 개발자 PC (IntelliJ / VS Code) | `docker-compose-local.yml`로 띄운 `localhost:5432/kkumz`, Redis `localhost:6379`, MinIO `http://localhost:9012` 연결 |
| `application-dev.yml` | `dev` | 통합 컨테이너 클러스터 (`docker-compose-dev.yml`) | 도커 브릿지 네트워크 호스트명(`kkumz-db:5432`, `kkumz-redis:6379`, `kkumz-minio:9000`) 연결 |
| `application-prod.yml` | `prod` | 상용 K8s / 클라우드 운영 환경 | 모든 접속 정보 및 시크릿을 환경변수(`${DB_URL}`, `${DB_PASSWORD}` 등)로 주입받으며 미설정 시 즉시 기동 실패(Fail-Fast) |

### 1.2 Docker Compose 이원화 운영 원칙
- **`docker-compose-local.yml` (로컬 인프라 전용)**:
  - 오직 PostgreSQL 18.6 (`pgvector/pgvector:pg18`), Redis, MinIO 인프라 컨테이너만 기동합니다.
  - 컨테이너 최초 기동 시 `./db/00_extensions.sql` ~ `./db/05_sample_dev.sql`이 `/docker-entrypoint-initdb.d/`를 통해 순서대로 자동 실행됩니다.
- **`docker-compose-dev.yml` (All-in-One 통합 개발 클러스터)**:
  - DB + Redis + MinIO + `kkumz-server-admin` (`SPRING_PROFILES_ACTIVE=dev`) + `kkumz-admin` + `kkumz-web` 전체 스택을 단일 명령으로 일괄 기동합니다.

---

## 2. 상용 환경 필수 환경변수 명세 (`prod` 프로파일)

상용 쿠버네티스(K8s) 또는 클라우드 배포 시 반드시 주입해야 하는 환경변수 목록입니다.

| 환경변수명 | 필수 여부 | 설명 | 상용 설정 예시 |
| :--- | :---: | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | 필수 | 실행 프로파일 지정 | `prod` |
| `SERVER_PORT` | 선택 | 백엔드 리스닝 포트 | `8096` |
| `DB_URL` | 필수 | Kkumz 마스터 PostgreSQL 18.6 JDBC URL | `jdbc:postgresql://prod-pg.empasy.internal:5432/kkumz` |
| `DB_USERNAME` / `DB_PASSWORD` | 필수 | DB 접속 계정 및 비밀번호 (Secret 주입) | `kkumz_prod_user` / `(K8s Secret)` |
| `REDIS_HOST` / `REDIS_PORT` | 필수 | Redis 세션·캐시 클러스터 호스트 및 포트 | `prod-redis.empasy.internal` / `6379` |
| `MINIO_ENDPOINT` | 필수 | `CUSTOM_BUNDLE` 정적 번들 저장용 오브젝트 스토리지 | `https://s3.kkumz.io` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | 필수 | MinIO / S3 자격증명 | `(K8s Secret)` |
| `ANTHROPIC_API_KEY` | 필수 | AgentScope Java LLM 호출용 API 키 | `sk-ant-...` |

---

## 3. 멀티사이트 와일드카드 도메인(`*.kkumz.io`) 및 라우팅 설정

Kkumz에서 배포된 멀티사이트는 다음 두 가지 URL 경로를 통해 접근할 수 있습니다.
1. **플랫폼 내부 표준 경로**: `https://app.kkumz.io/s/{siteSlug}` (예: `/s/matcha-atelier`)
2. **브랜드 전용 와일드카드 서브도메인**: `https://{siteSlug}.kkumz.io` (예: `https://matcha-atelier.kkumz.io`)

### 3.1 Ingress / Nginx 리버스 프록시 구성 가이드
- **DNS 레코드**: `*.kkumz.io`에 대한 와일드카드 `A` 또는 `CNAME` 레코드를 Ingress Load Balancer로 지정합니다.
- **TLS 인증서**: Let's Encrypt DNS-01 챌린지를 통해 `*.kkumz.io` 와일드카드 SSL 인증서를 발급·자동 갱신합니다.
- **Host 헤더 기반 서브도메인 라우팅**:
  ```nginx
  server {
      listen 443 ssl http2;
      server_name ~^(?<site_slug>[a-z0-9-]+)\.kkumz\.io$;

      location /api/v1/kkumz/ {
          proxy_pass http://kkumz-server-admin:8096/api/v1/kkumz/;
          proxy_set_header Host $host;
          proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      }

      location / {
          # 서브도메인 진입 시 /s/{site_slug} 멀티사이트 렌더러로 매핑
          rewrite ^/$ /s/$site_slug break;
          proxy_pass http://kkumz-web:3008;
      }
  }
  ```

---

## 4. 지식재산권(라이선스), 보안 및 개인정보보호 준수 사항

### 4.1 AI 생성 에셋(네이밍·SVG 로고·기능명세서·코드) 권리 귀속 정책
- Kkumz 6-Step AI 스튜디오를 통해 창업가가 생성·확정한 브랜드명, 벡터 SVG 로고(`kkumz_logo_suggestion`), 기능명세서(`spec_markdown`), WebIDE 커스텀 코드(`webide_files_json`)의 상업적 이용 권한은 해당 프로젝트를 생성한 창업가(또는 프로젝트 `OWNER`)에게 귀속됩니다.
- 단, 타인의 등록 상표를 고의로 도용하거나 부정경쟁방지법을 위반한 사이트는 운영정책 및 `SOP-03` 모더레이션 절차에 따라 즉시 정지(`SUSPENDED`)될 수 있음을 약관에 명시합니다.

### 4.2 WebIDE 샌드박스 및 SVG 보안 정책
1. **SVG 인젝션 방지**: `kkumz_logo_suggestion.svg_code` 저장 및 렌더링 시 `<script>`, 인라인 이벤트 핸들러(`onload`, `onerror`), 외부 스크립트 참조를 차단합니다.
2. **WebIDE 샌드박스 격리**: `CUSTOM_BUNDLE` 모드에서 사용자가 편집하는 코드는 프론트엔드 표현 계층(`HeroSection.vue`, `CustomStyles.css`, `site.config.json`)으로 제한되며, 서버 사이드 임의 명령 실행(`child_process`, `eval`) 코드는 `WEBIDE_AI_BUILD_FIXER` 금지어 필터 및 정적 검사기에서 거부됩니다.

### 4.3 멀티사이트 주문자 및 리드 폼 개인정보보호 (PIPA)
- 라이브 사이트(`/s/:siteSlug`)에서 수집되는 수령인 정보(`receiverName`, `receiverPhone`, `receiverAddress`) 및 리드 폼 정보(`applicantName`, `contact`)는 전송 구간 TLS 암호화를 적용하며, 해당 사이트의 인가된 프로젝트 멤버(`OWNER`, `MD`, `EDITOR`)와 플랫폼 총괄관리자에게만 접근을 허용합니다.
