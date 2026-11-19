export interface ProductScreenSlide {
  idx: number;
  tabLabel: string;
  title: string;
  route: string;
  summary: string;
  rawUrl: string;
  annotatedUrl: string;
}

export interface ProductQuickTryPoint {
  step: number;
  menuPath: string;
  title: string;
  tip: string;
}

export interface ProductVisualGuide {
  productId: string;
  roleEasyLabel: string;
  quickTryPoints: ProductQuickTryPoint[];
  screenshots: ProductScreenSlide[];
}

function buildScreens(
  items: Array<{ idx: number; tabLabel: string; title: string; route: string; summary: string }>
): ProductScreenSlide[] {
  return items.map((item) => {
    const num = String(item.idx).padStart(2, '0');
    return {
      idx: item.idx,
      tabLabel: item.tabLabel,
      title: item.title,
      route: item.route,
      summary: item.summary,
      rawUrl: `./images/screenshots/screen_${num}.png`,
      annotatedUrl: `./images/screenshots/screen_${num}_annotated.png`,
    };
  });
}

export const PRODUCT_VISUAL_GUIDES: Record<string, ProductVisualGuide> = {
  'sync-verse': {
    productId: 'sync-verse',
    roleEasyLabel: '전사 AI 에이전트 총괄 지휘 · 4단계 HITL 승인 관제',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '대시보드 > 커맨드 센터 (/dashboard/overview)',
        title: '전사 에이전트 상태 및 자율 과업 흐름 확인',
        tip: '현재 가동 중인 도메인 에이전트들과 실시간 A2A 파이프라인 처리 현황을 한눈에 확인합니다.',
      },
      {
        step: 2,
        menuPath: '거버넌스 > HITL 승인 센터 (/governance/hitl-approval)',
        title: '4단계 권한 분리 승인 및 변경 전후 비교',
        tip: 'AI가 제안한 기획·코드·DB·배포 변경안의 전/후 Diff를 비교하고 승인 또는 반려를 눌러보세요.',
      },
      {
        step: 3,
        menuPath: '프로젝트 > 3-in-1 품질 점검 (/projects/site-audit)',
        title: '코드 품질 · 보안 · 웹 노출 통합 감사 리포트 조회',
        tip: '배포 전 자동 수행된 정적 분석, E2E 테스트(SyncETA 연동), 사이트 진단 결과를 확인합니다.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '통합 관제 대시보드',
        title: '전사 A2A 자율 운영 커맨드 센터',
        route: '/dashboard/overview',
        summary: '전사 도메인 에이전트 가동 상태, 실시간 과업 파이프라인, SLA 준수율을 한눈에 모니터링합니다.',
      },
      {
        idx: 2,
        tabLabel: '품질 검증 파이프라인',
        title: 'AI 테스트케이스 및 품질 검증 관제',
        route: '/projects/task-list',
        summary: '요구사항 기반 자동 도출된 테스트케이스와 SyncETA 연동 검증 통과율을 추적합니다.',
      },
      {
        idx: 3,
        tabLabel: '3-in-1 통합 품질 점검',
        title: '코드 품질 · 웹 노출 · 장애 원인 통합 진단',
        route: '/projects/site-audit',
        summary: '소스 코드 정적 분석, 웹 접근성/SEO 진단, 장애 근본 원인(RCA) 리포트를 통합 조회합니다.',
      },
      {
        idx: 4,
        tabLabel: '4단계 HITL 승인·롤백',
        title: '4단계 순차 승인 및 이전 버전 복원',
        route: '/governance/hitl-approval',
        summary: '기획(PM) → 코드(Tech Lead) → DB(DBA) → 배포(DevOps) 직무분리 결재와 즉시 롤백을 제어합니다.',
      },
    ]),
  },
  'sync-insight': {
    productId: 'sync-insight',
    roleEasyLabel: '자연어 SQL 데이터 분석 · 멀티소스 딥리서치 BI',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '데이터 분석 > NL2SQL 워크벤치 (/analytics/nl2sql)',
        title: '한국어 질문으로 실시간 SQL 및 시각화 차트 생성',
        tip: '"최근 분기 카테고리별 매출 추이 알려줘"를 입력하거나 샘플 질문을 클릭해 생성된 SQL과 차트를 확인하세요.',
      },
      {
        step: 2,
        menuPath: '데이터 분석 > 딥리서치 캔버스 (/analytics/research)',
        title: '사내 DB + 외부 시장 동향 결합 AI 요약 리포트 확인',
        tip: '단순 수치 조회를 넘어 매출 변동 원인까지 교차 분석한 경영진 보고용 인사이트 리포트를 살펴보세요.',
      },
      {
        step: 3,
        menuPath: '데이터 거버넌스 > 데이터 리니지 맵 (/search/lineage)',
        title: '테이블·컬럼 출처 및 영향도 그래프 탐색',
        tip: '지표 산출에 사용된 원천 테이블과 시맨틱 모델 간의 계보(Lineage)를 시각적으로 확인합니다.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '자연어 SQL 워크벤치',
        title: '자연어 SQL(NL2SQL) 변환 및 차트 시각화',
        route: '/analytics/nl2sql',
        summary: '한국어 질문을 표준 SQL로 자동 변환하고 실행 계획 검증 후 즉시 인터랙티브 차트로 시각화합니다.',
      },
      {
        idx: 2,
        tabLabel: '딥리서치 분석 캔버스',
        title: '멀티소스 딥리서치 및 AI 인사이트 리포트',
        route: '/analytics/research',
        summary: '내부 운영 지표와 외부 산업 데이터를 결합해 원인 진단과 전략 제언이 담긴 리포트를 생성합니다.',
      },
      {
        idx: 3,
        tabLabel: '데이터소스 연결 관리',
        title: '다중 DB 연결 목록 및 상태 관제',
        route: '/analytics/datasources',
        summary: 'PostgreSQL, MySQL, 데이터웨어하우스 등 이기종 DB 연결 상태와 스키마 동기화를 관리합니다.',
      },
      {
        idx: 4,
        tabLabel: '데이터 리니지 맵',
        title: '전사 데이터 흐름 및 데이터 목록',
        route: '/search/lineage',
        summary: '원천 테이블부터 BI 지표까지의 데이터 흐름과 컬럼 단위 영향도를 그래프로 추적합니다.',
      },
    ]),
  },
  'sync-llm': {
    productId: 'sync-llm',
    roleEasyLabel: '통합 LLM 라우팅 · 비용 절감 캐시 · 개인정보 마스킹 게이트웨이',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '대시보드 > AI 게이트웨이 분석 (/dashboard/analysis)',
        title: '실시간 토큰 비용 절감액 및 시맨틱 캐시 히트율 확인',
        tip: '부서별·모델별 API 호출량과 pgvector 시맨틱 캐시를 통해 절약된 비용 지표를 확인합니다.',
      },
      {
        step: 2,
        menuPath: 'LLM 관리 > 프로바이더 & 폴백 (/llm/providers)',
        title: '멀티 LLM 연결 상태 및 서킷브레이커 우선순위 점검',
        tip: '특정 클라우드 LLM 장애 시 어떤 백업 모델로 자동 우회되는지 라우팅 정책을 살펴보세요.',
      },
      {
        step: 3,
        menuPath: '감사/보안 > 프롬프트 감사 로그 (/llm/request-logs)',
        title: '주민번호·연락처 등 개인정보(PII) 자동 마스킹 내역 확인',
        tip: '외부 LLM으로 전송되기 전 민감정보가 어떻게 비식별화 처리되었는지 실제 감사 로그를 확인합니다.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '비용·캐시 대시보드',
        title: 'AI 게이트웨이 비용 및 캐시 효율 대시보드',
        route: '/dashboard/analysis',
        summary: '전사 LLM 토큰 소비량, 캐시 히트 비용 절감액, 응답 지연시간을 실시간으로 모니터링합니다.',
      },
      {
        idx: 2,
        tabLabel: '멀티 LLM 폴백 설정',
        title: '멀티 LLM 프로바이더 및 무중단 폴백 설정',
        route: '/llm/providers',
        summary: 'OpenAI, Anthropic, Google, 로컬 오픈소스 모델의 헬스체크와 무중단 폴백 체인을 설정합니다.',
      },
      {
        idx: 3,
        tabLabel: '모델별 라우팅 정책',
        title: '모델 단가·한도 및 난이도 기반 라우팅',
        route: '/llm/models',
        summary: '단순 요약은 경량 모델로, 고난도 추론은 프론티어 모델로 자동 분기하는 비용 최적화 규칙을 관리합니다.',
      },
      {
        idx: 4,
        tabLabel: '프롬프트 감사·마스킹',
        title: '프롬프트 감사 로그 및 개인정보 자동 마스킹',
        route: '/llm/request-logs',
        summary: '모든 프롬프트 입출력을 투명하게 기록하고 개인정보·기밀정보 비식별화 처리 결과를 감사합니다.',
      },
    ]),
  },
  'sync-apim': {
    productId: 'sync-apim',
    roleEasyLabel: '미등록 섀도우 API 자동 탐지 · 실시간 공격 차단 · 민감정보 보호',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '대시보드 > API 보안 관제 상황판 (/dashboard/analysis)',
        title: '보호 호스트 · 활성 API 인벤토리 및 실시간 위협 차단 현황 확인',
        tip: '전사 게이트웨이로 유입되는 트래픽 추이와 최근 차단된 공격 유형(SQLi, SSRF 등)을 한눈에 파악합니다.',
      },
      {
        step: 2,
        menuPath: 'API 카탈로그 > 엔드포인트 & 스펙 진단 (/apim/endpoints)',
        title: '미등록 섀도우 API 식별 및 OpenAPI 스펙 드리프트 점검',
        tip: '문서에 없던 미등록 API나 정의되지 않은 민감 데이터 필드가 노출되는 엔드포인트를 즉시 찾아냅니다.',
      },
      {
        step: 3,
        menuPath: '위협 관제 > 실시간 탐지 패킷 포렌식 (/apim/detect-events)',
        title: '차단된 공격 패킷 원문 분석 및 민감정보 마스킹 정책 확인',
        tip: '공격 요청의 헤더·바디 페이로드 원문을 조회하고 원클릭 차단 정책을 체험해 보세요.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: 'API 보안 관제판',
        title: 'API 보안 관제 및 실시간 위협 대시보드',
        route: '/dashboard/analysis',
        summary: '보호 중인 호스트, 활성 API 엔드포인트 수, 실시간 공격 차단 현황을 직관적으로 관제합니다.',
      },
      {
        idx: 2,
        tabLabel: '섀도우 API 자동 탐지',
        title: 'API 인벤토리 자동 수집 및 섀도우 API 탐지',
        route: '/apim/endpoints',
        summary: '실제 트래픽에서 API 목록과 데이터 필드를 자동 추출하여 관리 사각지대의 섀도우 API를 색출합니다.',
      },
      {
        idx: 3,
        tabLabel: 'OpenAPI 스펙 검증',
        title: 'OpenAPI 3.0 명세 검증 및 드리프트 진단',
        route: '/apim/openapi-specs',
        summary: '등록된 표준 API 명세서와 실제 운영 응답 스키마를 비교해 미정의 필드 노출을 즉시 경고합니다.',
      },
      {
        idx: 4,
        tabLabel: '위협 패킷 포렌식',
        title: '공격 패킷 포렌식 분석 및 민감정보 마스킹',
        route: '/apim/detect-events',
        summary: 'SQLi·RCE·비정상 접근 패킷 원문을 초고속 검색하고 계좌·주민번호 등 민감정보를 실시간 마스킹합니다.',
      },
    ]),
  },
  'sync-boot': {
    productId: 'sync-boot',
    roleEasyLabel: '인증 · 조직/권한(RBAC) · 한국형 전자결재 · 감사로그 표준 백오피스',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '쇼케이스 > 통합 운영 분석 리포트 (/showcase/analytics-report)',
        title: '엔터프라이즈 백오피스 표준 대시보드 및 리포트 확인',
        tip: '기업 내부 시스템에서 바로 활용 가능한 KPI 카드, 차트, 엑셀 내보내기 표준 UI를 체험합니다.',
      },
      {
        step: 2,
        menuPath: '시스템 관리 > 사용자 · 역할 권한 관리 (/system/user)',
        title: '부서 트리 연동 및 메뉴·버튼 단위 동적 권한(RBAC) 제어',
        tip: '역할(Role)별로 접근 가능한 메뉴와 화면 내 버튼 권한을 체크박스로 즉시 제어해 보세요.',
      },
      {
        step: 3,
        menuPath: '쇼케이스 > 한국형 전자결재 위저드 (/showcase/approval-wizard)',
        title: '다단계 기안·결재선 지정 및 승인 워크플로우 체험',
        tip: '한국 기업 실무에 맞춘 기안서 작성, 결재선 지정, 반려/승인 이력 관리를 직접 눌러보세요.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '통합 운영 리포트',
        title: '엔터프라이즈 표준 백오피스 운영 리포트',
        route: '/showcase/analytics-report',
        summary: '전사 핵심 운영 지표, 기간별 통계 차트, 데이터 그리드가 결합된 엔터프라이즈 표준 화면입니다.',
      },
      {
        idx: 2,
        tabLabel: 'RBAC 조직·권한 관리',
        title: 'RBAC 기반 사용자·부서·역할 권한 관리',
        route: '/system/user',
        summary: '조직도 트리와 연동하여 사용자 계정, 직무 역할, 메뉴·버튼 단위 세밀한 권한을 단일 화면에서 관리합니다.',
      },
      {
        idx: 3,
        tabLabel: '한국형 전자결재',
        title: '한국형 전자결재 위저드 및 승인 워크플로우',
        route: '/showcase/approval-wizard',
        summary: '복잡한 품의·기안 양식 작성부터 다단계 결재선 지정, 실시간 승인 상태 추적을 지원합니다.',
      },
      {
        idx: 4,
        tabLabel: '시스템 감사 로그',
        title: '실시간 시스템 감사 로그 및 보안 추적',
        route: '/monitor/log',
        summary: '누가, 언제, 어떤 데이터를 조회·변경했는지 모든 관리자 액션과 API 호출 이력을 투명하게 기록합니다.',
      },
    ]),
  },
  'sync-cms': {
    productId: 'sync-cms',
    roleEasyLabel: '멀티사이트 콘텐츠 관리 · AI SEO/접근성 자동 점검 · 다국어 번역',
    quickTryPoints: [
      {
        step: 1,
        menuPath: 'CMS 관리 > 콘텐츠 원고 관리 (/cms/content)',
        title: 'AI 멀티채널 에디터 및 단계별 승인·발행 워크플로우 체험',
        tip: '보도자료·공지사항·블로그 원고를 작성하고 검수 요청부터 예약 발행까지의 흐름을 확인하세요.',
      },
      {
        step: 2,
        menuPath: 'AI 스튜디오 > SEO·접근성 자동 감사 (/cms/ai/audit)',
        title: '발행 전 SEO 점수 · 웹 접근성 · 민감정보 노출 원클릭 진단',
        tip: '작성된 콘텐츠가 검색엔진/AI 답변(GEO)에 잘 노출되는지, 접근성 위반 요소가 없는지 즉시 점검합니다.',
      },
      {
        step: 3,
        menuPath: 'AI 스튜디오 > 다국어 자동 현지화 (/cms/ai/localization)',
        title: '브랜드 용어집(Glossary) 기반 영·일·중 다국어 자동 번역',
        tip: '고유 제품명과 전문 용어의 일관성을 유지하면서 다국어 페이지로 즉시 변환되는 과정을 살펴보세요.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '멀티사이트 대시보드',
        title: '멀티사이트 콘텐츠 및 트래픽 통합 대시보드',
        route: '/dashboard/analysis',
        summary: '다수의 브랜드 사이트·포털의 콘텐츠 발행 현황, 방문 트래픽, 검수 대기 건수를 통합 관제합니다.',
      },
      {
        idx: 2,
        tabLabel: 'AI 콘텐츠 에디터',
        title: 'AI 아티클 에디터 및 승인·발행 워크플로우',
        route: '/cms/content',
        summary: '리치 에디터에서 초안 생성부터 편집장 승인, 버전 비교 및 원클릭 롤백까지 안전하게 관리합니다.',
      },
      {
        idx: 3,
        tabLabel: 'SEO·접근성 자동 진단',
        title: 'SEO / GEO 및 웹 접근성(WCAG) 자동 진단',
        route: '/cms/ai/audit',
        summary: '검색엔진 및 생성형 AI 노출 최적화 점수와 이미지 대체텍스트 등 접근성 준수 여부를 자동 진단합니다.',
      },
      {
        idx: 4,
        tabLabel: '다국어 자동 현지화',
        title: '브랜드 용어집 기반 다국어 자동 번역·현지화',
        route: '/cms/ai/localization',
        summary: '기업 전용 용어집을 준수하여 국문 콘텐츠를 영어·일본어 등 다국어 채널용으로 즉시 현지화합니다.',
      },
    ]),
  },
  'sync-shop': {
    productId: 'sync-shop',
    roleEasyLabel: '8대 MSA 이커머스 · AI 자동 가격 최적화 · 재고/프로모션 관리',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '대시보드 > 커머스 통합 관제 (/dashboard/analysis)',
        title: '실시간 주문액 · 마진율 · 베스트셀러 지표 확인',
        tip: '일별 매출 추이, 카테고리별 판매 비중, 처리 대기 중인 주문/클레임 현황을 한눈에 확인합니다.',
      },
      {
        step: 2,
        menuPath: '상품 관리(PMS) > 상품 목록 (/pms/product)',
        title: '최소 마진 보호 기준 및 AI 가격 조정 내역 확인',
        tip: '원가 이하 판매를 원천 차단하는 마진 하한선 설정과 AI 가격 최적화 카탈로그를 살펴보세요.',
      },
      {
        step: 3,
        menuPath: '마케팅(SMS) > 타임딜 · 플래시 세일 (/sms/flash)',
        title: '한정 수량 타임딜 및 타겟 쿠폰 프로모션 설정',
        tip: '시간대별 특가 세일과 고객 세그먼트별 쿠폰 캠페인이 어떻게 운영되는지 직접 확인해 보세요.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '커머스 매출·마진 관제',
        title: '실시간 주문·매출·마진율 통합 대시보드',
        route: '/dashboard/analysis',
        summary: '실시간 거래액(GMV), 평균 마진율, 주문 처리 상태 및 재고 경보를 한 화면에서 모니터링합니다.',
      },
      {
        idx: 2,
        tabLabel: 'AI 가격 최적화·상품',
        title: '최소 마진 보호 기준 기반 AI 가격 최적화',
        route: '/pms/product',
        summary: '상품별 원가·최소 마진율을 보호하면서 시장 가격 변동에 맞춰 최적 판매가를 자동 산출합니다.',
      },
      {
        idx: 3,
        tabLabel: '주문·배송·클레임 관리',
        title: '통합 주문 풀필먼트 및 클레임 처리',
        route: '/oms/order',
        summary: '결제 완료부터 출고 지시, 송장 연동, 취소·반품·교환 클레임까지 전 과정을 일괄 처리합니다.',
      },
      {
        idx: 4,
        tabLabel: '타임딜·쿠폰 프로모션',
        title: '타임딜 프로모션 및 타겟 쿠폰 캠페인',
        route: '/sms/flash',
        summary: '체류 재고 소진과 매출 부스팅을 위한 타임딜 스케줄링 및 맞춤형 할인 쿠폰을 관리합니다.',
      },
    ]),
  },
  'sync-ops': {
    productId: 'sync-ops',
    roleEasyLabel: 'SI 프로젝트 실투입 손익 관제 · 인력 가동률 · 보증보험/하도급 관리',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '경영 대시보드 > 전사 수주·손익 상황판 (/executive/dashboard)',
        title: '전사 수주 잔고 · 매출 · 실투입원가 공헌이익 한눈에 보기',
        tip: '진행 중인 모든 SI/SM 프로젝트의 목표 마진 대비 실제 손익 현황을 즉시 파악합니다.',
      },
      {
        step: 2,
        menuPath: '경영 대시보드 > 프로젝트 손익 매트릭스 (/executive/profit-matrix)',
        title: '계약단가 대 실투입 인건비 비교 및 적자 위험 프로젝트 식별',
        tip: '공수가 초과 투입되어 마진이 잠식되고 있는 프로젝트를 색상 신호등으로 즉시 찾아냅니다.',
      },
      {
        step: 3,
        menuPath: '계약/컴플라이언스 > 보증·하도급 관제 (/contract/compliance-alerts)',
        title: '4대 보증보험 만기 및 하도급 법정 기한 알림 확인',
        tip: '입찰·계약·선급금·하자보증 증권 갱신일과 하도급법 준수 체크리스트를 확인하세요.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '전사 수주·손익 상황판',
        title: '전사 수주·매출·공헌이익 경영 대시보드',
        route: '/executive/dashboard',
        summary: '전사 수주 목표 달성률, 프로젝트별 공헌이익, 월별 현금흐름을 실시간으로 집계합니다.',
      },
      {
        idx: 2,
        tabLabel: '프로젝트 손익 매트릭스',
        title: '계약단가 대 실투입원가 손익 매트릭스',
        route: '/executive/profit-matrix',
        summary: '계획 M/M 대비 실제 투입 공수를 실시간 대조하여 마진 잠식 위험을 조기에 경보합니다.',
      },
      {
        idx: 3,
        tabLabel: '보증보험·하도급 관제',
        title: '4대 보증보험 및 하도급 법규 준수 관제',
        route: '/contract/compliance-alerts',
        summary: '보증증권 발급·만기 일정과 하도급 대금 지급 기한 등 법적 의무 사항을 누락 없이 관리합니다.',
      },
      {
        idx: 4,
        tabLabel: '인력 배정·가동률 간트',
        title: 'SW 기술등급 기반 인력 배정 및 가동률 관리',
        route: '/talent/assignment-gantt',
        summary: '엔지니어별 기술등급, 프로젝트 철수/투입 일정, 유휴(벤치) 인력 가동률을 시각적으로 배치합니다.',
      },
    ]),
  },
  'sync-eta': {
    productId: 'sync-eta',
    roleEasyLabel: 'UI 변경 시 스크립트 자동 복구(Self-Healing) · E2E 테스트 자동화',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '대시보드 > QA 품질보증 대시보드 (/eta/dashboard)',
        title: '전사 E2E 시나리오 통과율 및 자가치유 복구 건수 확인',
        tip: '웹·모바일 테스트 스위트의 실행 통과율과 AI가 자동으로 복구한 스크립트 건수를 확인합니다.',
      },
      {
        step: 2,
        menuPath: 'AI 에이전트 > 시맨틱 자가치유 내역 (/eta/agent/healing)',
        title: '깨진 CSS/XPath 선택자를 AI가 자동 복구한 전/후 비교',
        tip: '배포 후 변경된 DOM 요소를 AI가 어떻게 인식하고 새 선택자로 치유했는지 상세 로그를 살펴보세요.',
      },
      {
        step: 3,
        menuPath: 'AI 에이전트 > 시각적 회귀 분석 (/eta/agent/visual)',
        title: '배포 전/후 화면 픽셀·레이아웃 깨짐(Visual Diff) 자동 검출',
        tip: '기능은 동작하더라도 버튼 겹침이나 폰트 깨짐 같은 UI 결함을 찾아내는 비교 화면을 확인하세요.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: 'QA 품질보증 대시보드',
        title: 'E2E 테스트 통과율 및 품질보증 대시보드',
        route: '/eta/dashboard',
        summary: '전체 테스트 시나리오 통과율, 평균 실행 시간, 자가치유 성공률을 실시간으로 모니터링합니다.',
      },
      {
        idx: 2,
        tabLabel: 'DOM 자가치유 내역',
        title: '시맨틱 DOM 자가치유(Self-Healing) 복구 내역',
        route: '/eta/agent/healing',
        summary: 'UI 개편으로 변경된 셀렉터를 문맥 기반으로 자동 매핑하여 테스트 중단을 방지합니다.',
      },
      {
        idx: 3,
        tabLabel: '시각적 회귀 분석',
        title: '시각적 회귀 분석(Visual Diff) 및 UI 검증',
        route: '/eta/agent/visual',
        summary: '이전 배포 버전과 현재 화면을 정밀 비교해 미세한 UI 틀어짐까지 시각적으로 하이라이트합니다.',
      },
      {
        idx: 4,
        tabLabel: '실행 이력·증적 리포트',
        title: '멀티 브라우저 실행 이력 및 스텝별 증적',
        route: '/eta/execution/histories',
        summary: '각 테스트 단계별 스크린샷, 네트워크 로그, 콘솔 에러 증적을 타임라인으로 제공합니다.',
      },
    ]),
  },
  'sync-crawl': {
    productId: 'sync-crawl',
    roleEasyLabel: '외부 웹/경쟁사 데이터 자동 수집 · 개인정보 비식별화 · RAG 정제',
    quickTryPoints: [
      {
        step: 1,
        menuPath: '대시보드 > 수집 파이프라인 관제 (/dashboard/analysis)',
        title: '일일 수집 처리량 · 성공률 · 자가치유 현황 확인',
        tip: '등록된 타겟 사이트별 수집 성공률과 시간대별 데이터 유입량을 한눈에 확인합니다.',
      },
      {
        step: 2,
        menuPath: '수집 설계 > 수집 사이트 (/crawl/source)',
        title: '수집 대상 URL · 스케줄 주기 및 셀렉터 설정 확인',
        tip: '코딩 없이 수집 주기와 추출 규칙을 설정하고 사이트 개편 시 자동 복구되는 옵션을 살펴보세요.',
      },
      {
        step: 3,
        menuPath: '수집 결과 > 수집 문서 (/crawl/document)',
        title: '정규화된 클린 데이터 및 PII 비식별화 결과 확인',
        tip: '광고·노이즈가 제거되고 AI 벡터 검색(RAG)에 바로 쓸 수 있게 정제된 데이터를 확인합니다.',
      },
    ],
    screenshots: buildScreens([
      {
        idx: 1,
        tabLabel: '수집 관제 대시보드',
        title: '웹 데이터 수집 처리량 및 성공률 관제 대시보드',
        route: '/dashboard/analysis',
        summary: '수집 워커 가동 상태, 도메인별 수집 성공률, 일일 정제 데이터 적재량을 실시간 관제합니다.',
      },
      {
        idx: 2,
        tabLabel: '타겟 사이트 설정',
        title: '타겟 수집 사이트 등록 및 자가치유 선택자 설정',
        route: '/crawl/source',
        summary: '경쟁사 몰, 공공 고시, 뉴스 등 타겟 URL과 수집 스케줄, DOM 변경 자동 추적 규칙을 관리합니다.',
      },
      {
        idx: 3,
        tabLabel: '정제 데이터셋 조회',
        title: '수집 데이터 정규화 · PII 비식별화 · RAG 데이터셋',
        route: '/crawl/document',
        summary: '수집된 원문을 구조화된 스키마로 변환하고 개인정보를 마스킹한 고품질 데이터셋을 조회합니다.',
      },
      {
        idx: 4,
        tabLabel: '프록시·WAF 정책 제어',
        title: '프록시 순환 · WAF 정책 및 수집 안전 규칙',
        route: '/ops/waf',
        summary: 'robots.txt 준수, 요청 속도 제한(Rate Limit), 프록시 풀 상태 등 안전한 수집 정책을 제어합니다.',
      },
    ]),
  },
};

export function getProductVisualGuide(productId: string): ProductVisualGuide | undefined {
  return PRODUCT_VISUAL_GUIDES[productId];
}
