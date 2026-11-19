import type { StandaloneProductData } from '@/types/homepage.types';

const LOCAL_HOST = import.meta.env?.VITE_LOCAL_HOST || 'localhost';

export const productData: StandaloneProductData = {
  "id": "kkumz",
  "domain": "kkumz",
  "name": "Kkumz",
  "subtitle": "11단계 AI 브랜드·캐릭터·멀티사이트 창작 스튜디오 & 팀빌딩 플랫폼",
  "englishRoleBadge": "11-Step AI Brand, Character & Multi-Site Creation Studio",
  "tierBadge": "Vertical Service · AI Creative Studio",
  "subdomain": "kkumz.empasy.io",
  "repoPath": "services/kkumz",
  "canonicalApiPrefix": "/api/v1/kkumz",
  "docUrl": "https://doc.empasy.com/kkumz/",
  "proposalPdfUrl": "/downloads/proposals/Kkumz_Proposal.pdf",
  "corporateSolutionUrl": "https://empasy.io/ko/solutions/kkumz.html",
  "portalProductUrl": "https://sync.empasy.io/products/kkumz",
  "iconBoxClass": "agent-icon-verse",
  "faIconClass": "fas fa-wand-magic-sparkles",
  "accentColor": "#8b5cf6",
  "techTags": [
    "Spring Boot 4.0",
    "11-Step Creative Pipeline",
    "Live Site Renderer",
    "WebIDE & Auto-Deploy"
  ],
  "summary": "아이디어 한 줄부터 AI 브랜드 네이밍·로고·세계관·기능명세서 생성, 실시간 라이브 사이트 프리뷰 편집, WebIDE 소스 커스터마이징, 무중단 배포 및 창업 팀원 모집까지 이어지는 올인원 AI 창작 스튜디오입니다.",
  "coreCapabilities": (
    "아이디어 키워드 기반 AI 브랜드 네이밍 8종 · 고화질 심볼 로고 · 세계관 스토리 자동 생성\n" +
    "업종별 맞춤 기능 카탈로그 조합 및 화면 블루프린트·DB 스키마 명세서 자동 설계\n" +
    "창작 스튜디오 웹 앱(kkumz-app.empasy.io) 실시간 라이브 프리뷰 편집 및 WebIDE 코드 패치\n" +
    "원클릭 독립 브랜드 사이트 빌드·무중단 퍼블리싱 및 프로젝트 라운지 팀원 모집·피드백\n" +
    "운영 관리 콘솔(kkumz-admin.empasy.io) 크리에이터 프로젝트·템플릿·빌드 파이프라인 통합 관제"
  ).split("\n"),
  "vsLegacyTitle": "기존 외주 제작 또는 파편화된 노코드 홈페이지 빌더",
  "vsLegacyPoints": [
    "브랜드 기획, 로고 디자인, 웹사이트 개발, 배포를 각각 다른 도구와 외주 업체에 맡겨 시간과 비용이 과다 소요됨",
    "단순 템플릿 빌더는 정해진 레이아웃만 바꿀 수 있어 실제 비즈니스 기능이나 소스코드 확장이 불가능함",
    "사이트 오픈 후 함께 사업을 키울 기획자·디자이너·개발자 팀원을 구하려면 별도 커뮤니티를 찾아다녀야 함"
  ],
  "vsAgentTitle": "Kkumz 11단계 AI 멀티모달 창작 & 라이브 배포 스튜디오",
  "vsAgentPoints": [
    "아이디어 입력부터 브랜드 아이덴티티, 기능 명세, 실제 동작하는 라이브 웹사이트까지 연속된 파이프라인으로 완성함",
    "이지 모드(실시간 화면 클릭 편집)와 고급 모드(내장 WebIDE + AI 빌드 오류 자동 패치)를 자유롭게 오가며 제한 없이 확장함",
    "완성된 브랜드 사이트를 즉시 라이브 배포하고 프로젝트 라운지에서 팀원 채용과 커뮤니티 피드백을 한 번에 진행함"
  ],
  "richFeatures": [
    {
      "badgeNo": "01",
      "title": "AI 브랜드 아이덴티티 & 기능 명세 블루프린트 생성",
      "description": "창업 아이템 한 줄만 입력하면 타겟 페르소나, 브랜드 네이밍, 로고 디자인, 화면 구조도와 데이터 명세를 자동 생성합니다."
    },
    {
      "badgeNo": "02",
      "title": "듀얼 모드 스튜디오 (실시간 비주얼 편집 + 내장 WebIDE)",
      "description": "비개발자는 라이브 화면을 보며 문구·사진·요금표를 즉시 수정하고, 개발자는 WebIDE에서 Vue/TS 코드를 직접 편집·패치합니다."
    },
    {
      "badgeNo": "03",
      "title": "원클릭 라이브 사이트 배포 & 창업 팀빌딩 라운지",
      "description": "빌드 검증 후 고유 URL로 라이브 사이트를 즉시 오픈하며, 프로젝트 라운지에서 포지션별 합류 지원자를 모집·승인합니다."
    }
  ],
  "processSteps": [
    {
      "stepNo": 1,
      "title": "기능 카탈로그 선택 및 AI 브랜드·명세 생성",
      "description": "업종별 필수 기능을 고르고 AI가 제안하는 브랜드 이름, 로고, 컬러 팔레트와 서비스 설계도를 확정합니다."
    },
    {
      "stepNo": 2,
      "title": "라이브 프리뷰 스테이지 & WebIDE 커스터마이징",
      "description": "실시간 렌더링 스테이지에서 객실·상품·갤러리·FAQ를 편집하거나 WebIDE에서 AI 코드 어시스턴트와 함께 기능을 고도화합니다."
    },
    {
      "stepNo": 3,
      "title": "무중단 라이브 배포 및 팀빌딩 라운지 오픈",
      "description": "자동 빌드 파이프라인을 통해 실제 웹사이트를 배포하고 피드·라운지에서 초기 고객 피드백과 팀원을 확보합니다."
    }
  ],
  "faqs": [
    {
      "question": "코딩을 전혀 모르는 창업자도 실제 동작하는 사이트를 만들 수 있나요?",
      "answer": "네, 스튜디오의 이지 모드(Easy Mode)를 사용하면 코딩 없이 실시간 프리뷰 화면에서 브랜드 소개, 상품/프로그램, 요금표, 갤러리, 위치 안내를 클릭만으로 편집하고 즉시 오픈할 수 있습니다."
    },
    {
      "question": "생성된 웹사이트의 소스코드를 직접 수정하거나 독립 서버로 이전할 수 있나요?",
      "answer": "네, Kkumz는 표준 Vue 3 + Vite 기반 독립 Git 저장소를 생성하므로 내장 WebIDE에서 직접 코드를 수정할 수 있고 언제든 자체 클라우드로 내보낼 수 있습니다."
    },
    {
      "question": "빌드 중 코드 오류가 발생하면 어떻게 처리되나요?",
      "answer": "SyncLLM 및 SyncVerse 코딩 에이전트와 연동된 AI 자동 패치 엔진이 빌드 로그를 분석하여 타입 오류나 누락된 임포트를 스스로 교정하고 재빌드를 수행합니다."
    }
  ],
  "cliSimulation": {
    "command": "empasy kkumz studio --project \"malgeunmul-minbak\" --stage \"build-and-deploy\"",
    "roleTag": "AI 브랜드·웹 창작 에이전트",
    "roleSummary": "브랜드 네이밍·로고·명세 생성부터 Vue/Vite 프로젝트 빌드 및 라이브 배포를 수행합니다.",
    "outputLines": [
      "[Brand Engine] Generated 8 brand names, 6 SVG/PNG logos & 4-part spec blueprint",
      "[WebIDE Builder] Vite 8 production bundle compiled in 2.4s (0 warnings)",
      "[Live Publisher] Site deployed to https://kkumz-app.empasy.io/sites/malgeunmul-minbak"
    ]
  },
  "mcpTools": [
    "kkumz_GenerateBrandIdentity",
    "kkumz_SynthesizeServiceSpec",
    "kkumz_PatchWebIdeSource",
    "kkumz_DeployLiveBrandSite"
  ],
  "dbTables": [
    "kkumz_idea_project",
    "kkumz_brand_asset",
    "kkumz_site_runtime_spec",
    "kkumz_build_history",
    "kkumz_team_position",
    "kkumz_lounge_comment"
  ],
  "demoAccounts": [
    {
      "roleTitle": "플랫폼 총괄 관리자 (Admin Console)",
      "accountId": "admin",
      "demoAccessCode": "123456",
      "scopeSummary": "kkumz-admin.empasy.io · 전체 창작 프로젝트, 업종 템플릿, 빌드 서버 및 커뮤니티 관제 권한"
    },
    {
      "roleTitle": "크리에이터 스튜디오 체험 계정 (Studio Web App)",
      "accountId": "creator_demo",
      "demoAccessCode": "123456",
      "scopeSummary": "kkumz-app.empasy.io · AI 브랜드·사이트 생성 스튜디오, WebIDE 편집, 라이브 배포 및 팀빌딩"
    }
  ],
  "entryLinks": [
    {
      "id": "kkumz-app",
      "label": "AI 창작 스튜디오 & 라운지 (Launch Web App)",
      "badgeText": "kkumz-app.empasy.io",
      "cloudUrl": "https://kkumz-app.empasy.io",
      "isPrimary": true
    },
    {
      "id": "kkumz-admin",
      "label": "크리에이터 운영 관리 콘솔 (Admin Console)",
      "badgeText": "kkumz-admin.empasy.io",
      "cloudUrl": "https://kkumz-admin.empasy.io",
      "isPrimary": false
    }
  ],
  "downloadAssets": [],
  "resetScheduleNotice": "데모 환경에서 생성·변경한 데이터는 매일 오전 04:00(KST)에 초기 상태로 복원됩니다.",
  "impactValue": "제작 기간 90% 단축",
  "impactTitle": "신규 브랜드 기획 및 라이브 웹사이트 출시 기간 90% 단축",
  "impactSummary": "아이디어 발상부터 브랜드 디자인, 기능 설계, 라이브 웹사이트 배포, 초기 팀빌딩까지 창업과 디지털 프로덕트 제작의 전 과정을 하나로 연결합니다.",
  "standaloneArch": {
    "badge": "Standalone Mode (단독 서비스 도입)",
    "title": "Kkumz Standalone Edition — 창업보육기관·에이전시·기업 사내벤처용 AI 빌더",
    "summary": "창작 스튜디오 웹 앱(Vue 3.5 + Vite 8), 운영 관리 콘솔(Vben Admin), Spring Boot 4.0 멀티사이트 빌드·렌더링 백엔드로 구성된 독립 창작 플랫폼입니다.",
    "components": (
      "kkumz-server-admin (Spring Boot 4.0 + AgentScope Java 창작·빌드 백엔드 · Port 9580)\n" +
      "kkumz/web (Vue 3.5 + Vite 8 AI 스튜디오·WebIDE·라이브 렌더러 · Port 9530)\n" +
      "kkumz/admin (Vben Admin 플랫폼 운영 콘솔 · Port 9531)\n" +
      "PostgreSQL 18.6 + pgvector (db/00~05_*.sql 6단계 표준 스키마) & Git Workspace"
    ).split("\n"),
    "highlights": [
      "아이디어 → 브랜드 → 기능 명세 → 라이브 프리뷰 → WebIDE → 무중단 배포 원스톱 파이프라인",
      "업종별(숙박·펜션, 커머스, F&B, 로컬 브랜드, SaaS) 특화 섹션 및 예약·요금 매트릭스 기본 탑재",
      "프로젝트 라운지 기반 팀원 포지션 모집·승인 및 커뮤니티 피드백 루프 내장"
    ]
  },
  "ecosystemArch": {
    "badge": "Ecosystem Mode (SyncVerse 플러그인 연동)",
    "title": "SyncVerse A2A Creative Studio Agent — 멀티에이전트 풀스택 프로덕트 팩토리",
    "summary": "SyncVerse, SyncLLM, SyncCMS, SyncETA와 연동하여 사이트 생성부터 다국어 SEO 콘텐츠 발행, 자동 E2E 시각 검수까지 완전 자동화합니다.",
    "connectedAgents": [
      "SyncVerse (스펙 & 코드 아키텍처 관제)",
      "SyncLLM (멀티모달 카피·로고·코드 생성)",
      "SyncETA (배포 사이트 자동 UI/UX 테스트)"
    ],
    "highlights": [
      "SyncLLM 게이트웨이를 통한 텍스트·이미지·코드 생성 모델 최적 라우팅",
      "배포된 라이브 사이트에 대해 SyncETA가 깨진 링크와 모바일 반응형 레이아웃을 자동 검증",
      "SyncCMS와 연동하여 브랜드 매거진·블로그 다국어 포스트를 주기적으로 자동 발행"
    ]
  },
  "dockerGuide": {
    "workdir": "services/kkumz",
    "devCommand": "docker compose -f docker-compose-dev.yml up -d --build",
    "localCommand": "docker compose -f docker-compose-local.yml up -d",
    "healthCheckCmd": "curl -s http://localhost:9580/api/v1/kkumz/sys/permission/getUserPermissionByToken",
    "endpoints": [
      {
        "label": "AI 창작 스튜디오 & 라운지 (Studio Web App)",
        "url": `http://${LOCAL_HOST}:9530`,
        "note": "브랜드·사이트 생성 스튜디오, WebIDE 및 라이브 렌더러"
      },
      {
        "label": "크리에이터 운영 관리 콘솔 (Admin UI)",
        "url": `http://${LOCAL_HOST}:9531`,
        "note": "프로젝트, 템플릿, 빌드 상태 및 회원 관리"
      },
      {
        "label": "백엔드 API 서버",
        "url": `http://${LOCAL_HOST}:9580/api/v1/kkumz`,
        "note": "Spring Boot 4.0 표준 Canonical API"
      }
    ]
  },
  "siblings": [
    {
        "id": "sync-verse",
        "name": "SyncVerse",
        "subtitle": "전사 에이전트 통합 관제 및 스펙 스튜디오",
        "tierBadge": "Tier 1 Orchestrator",
        "subdomain": "verse.empasy.io",
        "iconBoxClass": "agent-icon-verse",
        "faIconClass": "fas fa-network-wired",
        "portalUrl": "https://verse.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-verse.html"
    },
    {
        "id": "sync-boot",
        "name": "SyncBoot",
        "subtitle": "표준 백오피스 프레임워크 및 운영 에이전트",
        "tierBadge": "Tier 3 Foundation",
        "subdomain": "boot.empasy.io",
        "iconBoxClass": "agent-icon-boot",
        "faIconClass": "fas fa-server",
        "portalUrl": "https://boot.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-boot.html"
    },
    {
        "id": "sync-cms",
        "name": "SyncCMS",
        "subtitle": "콘텐츠 자동 작성·번역 및 검색 품질 진단",
        "tierBadge": "Tier 3 Content",
        "subdomain": "cms.empasy.io",
        "iconBoxClass": "agent-icon-cms",
        "faIconClass": "fas fa-layer-group",
        "portalUrl": "https://cms.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-cms.html"
    },
    {
        "id": "sync-shop",
        "name": "SyncShop",
        "subtitle": "MSA 이커머스 및 마진 보호 가격 관리",
        "tierBadge": "Tier 3 Commerce",
        "subdomain": "shop.empasy.io",
        "iconBoxClass": "agent-icon-shop",
        "faIconClass": "fas fa-shopping-bag",
        "portalUrl": "https://shop.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-shop.html"
    },
    {
        "id": "sync-ops",
        "name": "SyncOps",
        "subtitle": "SI 프로젝트 손익·정산 및 연구노트 관리",
        "tierBadge": "Tier 3 Enterprise PMO",
        "subdomain": "ops.empasy.io",
        "iconBoxClass": "agent-icon-ops",
        "faIconClass": "fas fa-briefcase",
        "portalUrl": "https://ops.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-ops.html"
    },
    {
        "id": "sync-llm",
        "name": "SyncLLM",
        "subtitle": "AI 게이트웨이 및 토큰 비용·보안 관제",
        "tierBadge": "Tier 2 AI Gateway",
        "subdomain": "llm.empasy.io",
        "iconBoxClass": "agent-icon-llm",
        "faIconClass": "fas fa-microchip",
        "portalUrl": "https://llm.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-llm.html"
    },
    {
        "id": "sync-insight",
        "name": "SyncInsight",
        "subtitle": "자연어 SQL 데이터 분석 및 딥리서치 BI",
        "tierBadge": "Tier 2 Intelligence",
        "subdomain": "insight.empasy.io",
        "iconBoxClass": "agent-icon-insight",
        "faIconClass": "fas fa-chart-line",
        "portalUrl": "https://insight.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-insight.html"
    },
    {
        "id": "sync-crawl",
        "name": "SyncCrawl",
        "subtitle": "셀렉터 자가 복구 기반 웹 데이터 추출",
        "tierBadge": "Tier 4 Data Agent",
        "subdomain": "crawl.empasy.io",
        "iconBoxClass": "agent-icon-crawl",
        "faIconClass": "fas fa-spider",
        "portalUrl": "https://crawl.empasy.io/ko/",
        "corporateUrl": "https://empasy.io/ko/solutions/sync-crawl.html"
    }
]
};
