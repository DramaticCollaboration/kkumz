export interface SimulatorScenario {
  key: string;
  labelKo: string;
  labelEn: string;
  inputTitleKo: string;
  inputTitleEn: string;
  inputPayload: string;
  engineStageKo: string;
  engineStageEn: string;
  outputTitleKo: string;
  outputTitleEn: string;
  outputPayload: string;
  metricBadgeKo: string;
  metricBadgeEn: string;
}

export interface TechBlogArticle {
  id: string;
  category: string;
  date: string;
  readTime: string;
  titleKo: string;
  titleEn: string;
  summaryKo: string;
  summaryEn: string;
  sectionsKo: Array<{ heading: string; body: string; code?: string }>;
  sectionsEn: Array<{ heading: string; body: string; code?: string }>;
}

export interface ProductEnhancementPack {
  showcaseType: 'TYPE_A' | 'TYPE_B' | 'TYPE_C' | 'TYPE_D';
  showcaseBadgeKo: string;
  showcaseBadgeEn: string;
  showcaseTitleKo: string;
  showcaseTitleEn: string;
  subtitleEn: string;
  summaryEn: string;
  impactValueEn: string;
  impactTitleEn: string;
  scenarios: SimulatorScenario[];
  articles: TechBlogArticle[];
}

const TYPE_MAP: Record<string, 'TYPE_A' | 'TYPE_B' | 'TYPE_C' | 'TYPE_D'> = {
  'sync-verse': 'TYPE_A',
  'sync-boot': 'TYPE_A',
  'sync-eta': 'TYPE_A',
  'sync-apim': 'TYPE_B',
  'sync-watch': 'TYPE_B',
  'sync-llm': 'TYPE_B',
  'sync-crawl': 'TYPE_B',
  'sync-insight': 'TYPE_B',
  'sync-ops': 'TYPE_C',
  'sync-cms': 'TYPE_C',
  'sync-shop': 'TYPE_C',
  'today-care': 'TYPE_D',
  'awesome-fit': 'TYPE_D',
  'kkumz': 'TYPE_D',
};

const TYPE_TITLES: Record<'TYPE_A' | 'TYPE_B' | 'TYPE_C' | 'TYPE_D', { badgeKo: string; badgeEn: string; titleKo: string; titleEn: string }> = {
  TYPE_A: {
    badgeKo: 'TYPE A · 개발 · QA · 오케스트레이션 인터랙티브 워크벤치',
    badgeEn: 'TYPE A · Dev, QA & Orchestration Interactive Workbench',
    titleKo: '스펙 설계 · 동적 권한 핫스왑 · Self-Healing DOM 복구 시뮬레이터',
    titleEn: 'Spec Studio, Dynamic RBAC Hot-Swap & Self-Healing DOM Simulator',
  },
  TYPE_B: {
    badgeKo: 'TYPE B · 보안 · 인프라 · AI 데이터 엔지니어링 인스펙터',
    badgeEn: 'TYPE B · Security, AI Gateway & Data Engineering Inspector',
    titleKo: 'eBPF 패킷 차단 · CVE 듀얼 컴파일 · 토큰 FinOps · NL2SQL 시뮬레이터',
    titleEn: 'eBPF Packet Guard, CVE Dual Compiler, Token FinOps & NL2SQL Simulator',
  },
  TYPE_C: {
    badgeKo: 'TYPE C · 엔터프라이즈 비즈니스 자율 운영 시뮬레이터',
    badgeEn: 'TYPE C · Enterprise Autonomous Business Operations Simulator',
    titleKo: 'SI 다차원 손익 피벗 · 6개 국어 AEO/SEO 진단 · 커머스 마진 가드',
    titleEn: 'SI Profit Pivot, 6-Language AEO/SEO Audit & Commerce Margin Guard',
  },
  TYPE_D: {
    badgeKo: 'TYPE D · 버티컬 산업 특화 멀티플랫폼 워크플로우 쇼케이스',
    badgeEn: 'TYPE D · Vertical Industry Multi-Platform Workflow Showcase',
    titleKo: '3-Way 시니어 돌봄 · 도매-오픈마켓 무인 풀필먼트 · 11단계 AI 창작 스튜디오',
    titleEn: '3-Way Senior Care, Wholesale Fulfillment & 11-Step AI Creative Studio',
  },
};

export function getProductEnhancementPack(
  productId: string,
  productName: string,
  canonicalApiPrefix: string,
  mcpTools: string[],
  dbTables: string[]
): ProductEnhancementPack {
  const showcaseType = TYPE_MAP[productId] || 'TYPE_A';
  const meta = TYPE_TITLES[showcaseType];
  const tool0 = mcpTools[0] || 'execute_agent_task';
  const tool1 = mcpTools[1] || 'verify_domain_state';
  const table0 = dbTables[0] || 'sys_permission';
  const table1 = dbTables[1] || 'sys_config';

  const scenarios: SimulatorScenario[] = [
    {
      key: 'sc-1',
      labelKo: '시나리오 01 · 자율 분석 및 정책 검증',
      labelEn: 'Scenario 01 · Autonomous Analysis & Policy Check',
      inputTitleKo: `${productName} 인바운드 요청 페이로드`,
      inputTitleEn: `${productName} Inbound Request Payload`,
      inputPayload: `POST ${canonicalApiPrefix}/agent/invoke\n{\n  "tool": "${tool0}",\n  "targetTable": "${table0}",\n  "strictGuardrail": true\n}`,
      engineStageKo: `AgentScope Java ReAct 추론 → ${table0} 무결성 검증 → 무중단 파이프라인 실행`,
      engineStageEn: `AgentScope Java ReAct Reasoning → ${table0} Integrity Check → Zero-Downtime Execution`,
      outputTitleKo: '실시간 실행 결과 및 감사 증적',
      outputTitleEn: 'Real-Time Execution Result & Audit Trail',
      outputPayload: `{\n  "status": "COMPLETED",\n  "toolExecuted": "${tool0}",\n  "verifiedSchema": "${table0}",\n  "latencyMs": 14,\n  "rollbackSnapshotReady": true\n}`,
      metricBadgeKo: '응답 지연 14ms · 무결성 100%',
      metricBadgeEn: 'Latency 14ms · 100% Integrity',
    },
    {
      key: 'sc-2',
      labelKo: '시나리오 02 · 실시간 핫스왑 및 자가 복구',
      labelEn: 'Scenario 02 · Real-Time Hot-Swap & Self-Healing',
      inputTitleKo: '변경 감지 및 자동 보정 트리거',
      inputTitleEn: 'Drift Detection & Auto-Remediation Trigger',
      inputPayload: `POST ${canonicalApiPrefix}/mcp/execute\n{\n  "tool": "${tool1}",\n  "resource": "${table1}",\n  "mode": "SELF_HEALING"\n}`,
      engineStageKo: `변경 전후 Diff 비교 → 가드레일 임계치 판정 → ${table1} 무중단 핫스왑 반영`,
      engineStageEn: `Before/After Diff Comparison → Threshold Check → ${table1} Live Hot-Swap`,
      outputTitleKo: '자가 복구 및 스냅샷 체크포인트',
      outputTitleEn: 'Self-Healing & Snapshot Checkpoint',
      outputPayload: `{\n  "status": "HEALED_AND_SYNCED",\n  "toolExecuted": "${tool1}",\n  "affectedRows": 12,\n  "confidence": 0.994\n}`,
      metricBadgeKo: '복구 정확도 99.4% · 무중단 반영',
      metricBadgeEn: '99.4% Healing Accuracy · Zero Downtime',
    },
    {
      key: 'sc-3',
      labelKo: '시나리오 03 · 4단계 HITL 승인 및 1-Click 롤백',
      labelEn: 'Scenario 03 · 4-Stage HITL Approval & 1-Click Rollback',
      inputTitleKo: 'SyncVerse A2A 거버넌스 연동 패킷',
      inputTitleEn: 'SyncVerse A2A Governance Envelope',
      inputPayload: `POST ${canonicalApiPrefix}/governance/checkpoint\n{\n  "originAgent": "${productId.toUpperCase()}",\n  "action": "COMMIT_OR_REVERT",\n  "tables": ["${table0}", "${table1}"]\n}`,
      engineStageKo: 'PM 기획 → Tech Lead 코드 → DBA 스키마 → DevOps 배포 4단계 직무분리 승인',
      engineStageEn: 'PM Spec → Tech Lead Code → DBA Schema → DevOps Deploy 4-Stage SoD Approval',
      outputTitleKo: '원자적 트랜잭션 확정 및 복원 해시',
      outputTitleEn: 'Atomic Transaction Commit & Restore Point',
      outputPayload: `{\n  "approvalState": "APPROVED_4_OF_4",\n  "canonicalPrefix": "${canonicalApiPrefix}",\n  "restorePoint": "chk_${productId}_v409"\n}`,
      metricBadgeKo: '직무분리 4단계 통과 · 즉시 복원 가능',
      metricBadgeEn: '4-Stage SoD Passed · Instant Revert Ready',
    },
  ];

  const articles: TechBlogArticle[] = [
    {
      id: `${productId}-spring-boot-4-migration`,
      category: 'Architecture & Runtime',
      date: '2026-11-19',
      readTime: '7 min read',
      titleKo: `${productName} Spring Boot 4.0 · Jakarta EE 11 · Jackson 3 전면 전환 아키텍처`,
      titleEn: `${productName} Architecture on Spring Boot 4.0, Jakarta EE 11 & Jackson 3`,
      summaryKo: `JDK 25 가상 스레드(Virtual Threads)와 Spring Framework 7.0, tools.jackson.* 기반 고성능 직렬화 파이프라인을 통해 ${productName}의 처리량과 메모리 효율을 극대화한 엔지니어링 사례를 소개합니다.`,
      summaryEn: `How ${productName} leverages JDK 25 Virtual Threads, Spring Framework 7.0, and tools.jackson.* serialization to achieve high throughput and low tail latency.`,
      sectionsKo: [
        {
          heading: '1. JDK 25 가상 스레드와 Tomcat 11 비동기 파이프라인',
          body: `${productName} 백엔드는 레거시 블로킹 스레드 풀의 한계를 극복하기 위해 Spring Boot 4.0.9와 JDK 25 Virtual Threads를 전면 도입했습니다. MCP SSE 스트리밍과 동시 DB/Redis I/O 요청에서도 컨텍스트 스위칭 오버헤드 없이 일정한 응답 속도를 유지합니다.`,
          code: `spring:\n  threads:\n    virtual:\n      enabled: true\nserver:\n  tomcat:\n    threads:\n      max: 200`,
        },
        {
          heading: '2. Jackson 3 (tools.jackson.*) 및 SpringDoc 3.2.0 표준화',
          body: `직렬화 계층을 tools.jackson.databind.json.JsonMapper와 GenericJacksonJsonRedisSerializer로 일원화하여 불변 빌더 기반의 타입 안정성을 확보하고, 모든 REST 엔드포인트를 ${canonicalApiPrefix} 단일 규격으로 표준화했습니다.`,
        },
      ],
      sectionsEn: [
        {
          heading: '1. JDK 25 Virtual Threads & Tomcat 11 Pipeline',
          body: `${productName} runs on Spring Boot 4.0.9 with JDK 25 Virtual Threads enabled by default, sustaining high concurrency across MCP SSE streams and PostgreSQL 18.6 queries without thread exhaustion.`,
          code: `spring:\n  threads:\n    virtual:\n      enabled: true\nserver:\n  tomcat:\n    threads:\n      max: 200`,
        },
        {
          heading: '2. Jackson 3 (tools.jackson.*) & SpringDoc 3.2.0 Unification',
          body: `All serialization pipelines use tools.jackson.databind.json.JsonMapper and GenericJacksonJsonRedisSerializer under the canonical ${canonicalApiPrefix} namespace.`,
        },
      ],
    },
    {
      id: `${productId}-agentscope-a2a-protocol`,
      category: 'AI Agent & MCP',
      date: '2026-11-10',
      readTime: '6 min read',
      titleKo: `AgentScope Java 기반 ${productName} 자율 도구(@SyncMcpTool) 설계와 트랜잭션 경계 분리`,
      titleEn: `Designing ${productName} Autonomous @SyncMcpTool with AgentScope Java`,
      summaryKo: `장시간 소요되는 LLM 추론과 데이터베이스 @Transactional 경계를 엄격히 분리하여 커넥션 풀 고갈을 방지하고 A2A 협업 안정성을 보장하는 설계 원칙을 다룹니다.`,
      summaryEn: `Decoupling long-running LLM reasoning from database @Transactional boundaries to prevent connection pool starvation in multi-agent workflows.`,
      sectionsKo: [
        {
          heading: '1. 트랜잭션 외부 추론 및 원자적 상태 반영',
          body: `${productName}의 MCP 도구(${mcpTools.slice(0, 3).join(', ')})는 외부 네트워크 및 LLM 호출을 트랜잭션 외부에서 수행한 뒤, 검증된 결과만 짧은 DB 트랜잭션으로 ${dbTables.slice(0, 2).join(', ')} 테이블에 반영합니다.`,
          code: `// Canonical MCP Tool Endpoint\nPOST ${canonicalApiPrefix}/mcp/messages\nContent-Type: application/json`,
        },
      ],
      sectionsEn: [
        {
          heading: '1. Out-of-Transaction Reasoning & Atomic Persistence',
          body: `${productName} MCP tools (${mcpTools.slice(0, 3).join(', ')}) execute LLM and network I/O outside database transactions, persisting validated state changes atomically into ${dbTables.slice(0, 2).join(', ')}.`,
          code: `// Canonical MCP Tool Endpoint\nPOST ${canonicalApiPrefix}/mcp/messages\nContent-Type: application/json`,
        },
      ],
    },
    {
      id: `${productId}-postgresql-18-ssot-governance`,
      category: 'Database & Security',
      date: '2026-10-28',
      readTime: '5 min read',
      titleKo: `PostgreSQL 18.6 6-File SSOT 스키마와 DB 동적 메뉴(PermissionModeEnum.BACK) 거버넌스`,
      titleEn: `PostgreSQL 18.6 6-File SSOT Schema & DB-Driven Dynamic Menu Governance`,
      summaryKo: `프론트엔드 정적 라우트 하드코딩을 전면 배제하고 sys_permission 및 도메인 전용 테이블(${dbTables.slice(0, 3).join(', ')})만으로 화면 권한과 비즈니스 상태를 제어하는 아키텍처를 설명합니다.`,
      summaryEn: `Eliminating hardcoded frontend routes in favor of 100% database-driven menus (sys_permission) and deterministic 6-file PostgreSQL 18.6 initialization.`,
      sectionsKo: [
        {
          heading: '1. 6-File SSOT 데이터베이스 초기화 표준',
          body: `00_extensions.sql부터 05_sample_dev.sql까지 6단계 표준 스크립트만으로 클린 환경에서 단 1회 실행으로 최신 스키마와 역할별 메뉴 트리를 완벽히 구성합니다.`,
        },
      ],
      sectionsEn: [
        {
          heading: '1. 6-File SSOT Database Initialization Standard',
          body: `From 00_extensions.sql through 05_sample_dev.sql, clean environments initialize the complete schema and role-based menu tree in a single deterministic pass.`,
        },
      ],
    },
  ];

  return {
    showcaseType,
    showcaseBadgeKo: meta.badgeKo,
    showcaseBadgeEn: meta.badgeEn,
    showcaseTitleKo: meta.titleKo,
    showcaseTitleEn: meta.titleEn,
    subtitleEn: `${productName} — Autonomous Enterprise AI Agent & Standalone Platform (${canonicalApiPrefix})`,
    summaryEn: `${productName} operates either as a 100% standalone enterprise solution on Spring Boot 4.0 + PostgreSQL 18.6 + Vue 3.5, or as an A2A domain agent plugged into the SyncVerse ecosystem via MCP JSON-RPC 2.0.`,
    impactValueEn: '10x Faster Ops',
    impactTitleEn: `Enterprise Automation & Zero-Downtime Governance with ${productName}`,
    scenarios,
    articles,
  };
}
