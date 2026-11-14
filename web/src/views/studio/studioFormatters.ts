import { formatSiteTypeLabel } from '@/api/kkumzApi';

export interface StudioStepMeta {
  no: number;
  displayNo: number;
  title: string;
  desc: string;
  optional: boolean;
}

export const easySteps: StudioStepMeta[] = [
  { no: 1, displayNo: 1, title: '1. 어떤 사이트를 만들까요?', desc: '아이디어 한 줄 입력 & AI 자동 구성', optional: false },
  { no: 2, displayNo: 2, title: '2. 이름 & 로고 고르기', desc: 'AI 추천 브랜드명 · 고화질 로고 선택', optional: false },
  { no: 4, displayNo: 3, title: '3. 내 사이트 꾸미기 & 바로 오픈', desc: '실시간 화면 수정 & 1초 즉시 오픈', optional: false },
];

export const steps: StudioStepMeta[] = [
  { no: 1, displayNo: 1, title: '1. 아이디어 정의', desc: '사이트 유형 & 프롬프트', optional: false },
  { no: 2, displayNo: 2, title: '2. 네이밍 & 로고', desc: '브랜드명 8종 & 고화질 로고', optional: false },
  { no: 3, displayNo: 3, title: '3. 기능명세서 & 화면설계', desc: '블루프린트 & 버전 관리', optional: false },
  { no: 4, displayNo: 4, title: '4. 실시간 프리뷰 편집', desc: 'AI 대화·비주얼·상품 편집', optional: false },
  { no: 5, displayNo: 5, title: '5. WebIDE 코드 편집', desc: '개발자용 코드 커스텀', optional: true },
  { no: 6, displayNo: 6, title: '6. 빌드 & 배포', desc: '라이브 발행 & 버전 롤백', optional: false },
];

export const engineFilterOptions = [
  { code: 'ALL', label: '전체 기능' },
  { code: 'SYNC_SHOP', label: '쇼핑 · 예약 · 요금표' },
  { code: 'SYNC_CMS', label: '홍보 · 갤러리 · 신청폼' },
  { code: 'AI_CORE', label: 'AI 자동상담 · 커뮤니티' },
];

export function sanitizeBrandStoryText(raw: any): string {
  if (!raw) return '';
  return String(raw).trim();
}

export function escapeXmlText(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

export function renderLogoWithBrand(
  svgCode: any,
  overrideBrandName?: string,
  fallbackBrandName?: string,
): string {
  const rawSvg = String(svgCode || '');
  if (!rawSvg) return '';
  const targetBrand = (overrideBrandName || fallbackBrandName || '').trim();
  if (!targetBrand) return rawSvg;

  const escapedBrand = escapeXmlText(targetBrand);
  const firstChar = escapeXmlText(targetBrand.charAt(0));

  return rawSvg.replace(/(<text\b[^>]*>)([^<]+)(<\/text>)/gi, (full, openTag, innerText, closeTag) => {
    const trimmed = String(innerText || '').trim();
    if (!trimmed) return full;
    const upper = trimmed.toUpperCase();
    if (/\b(OFFICIAL|BRAND|ARCHIVE|STUDIO|COLLECTION|SINCE)\b|EST\./.test(upper)) {
      return full;
    }
    if (trimmed.length === 1) {
      return `${openTag}${firstChar}${closeTag}`;
    }
    return `${openTag}${escapedBrand}${closeTag}`;
  });
}

export function formatEngineBadge(engineCode: string): string {
  if (engineCode === 'SYNC_SHOP') return '쇼핑·예약 기능';
  if (engineCode === 'SYNC_CMS') return '홍보·콘텐츠 기능';
  if (engineCode === 'AI_CORE') return 'AI 스마트 기능';
  return engineCode || '기본 기능';
}

export function formatHeroStyleLabel(
  code: string,
  heroStyles: Array<{ code: string; label: string; desc?: string }> = [],
): string {
  const found = heroStyles.find((h) => h.code === code);
  if (found) return found.label;
  if (code === 'FULL_SCENIC') return '풀스크린 파노라마 배너';
  if (code === 'SPLIT_VISUAL') return '좌우 분할 시그니처 쇼케이스';
  if (code === 'EDITORIAL_STORY') return '매거진 에디토리얼 커버';
  if (code === 'PROMO_COMMERCE') return '커머스 프로모션 포커스';
  return code || '표준 히어로 배너';
}

export function parseHighlightsFromMarkdown(raw: string): string[] {
  const lines = (raw || '')
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0 && !line.startsWith('#'))
    .map((line) => line.replace(/^[\-*\d.\s✓]+/, '').replace(/\*\*/g, '').trim())
    .filter((line) => line.length > 4);
  return lines.slice(0, 8);
}

export function isStandardLayoutType(code?: string): boolean {
  return /^(HERO_BANNER_GRID|PRODUCT_CATALOG_GRID|GALLERY_SHOWCASE|MAGAZINE_LIST|RESERVATION_FORM|FAQ_CONCIERGE)$/.test(
    String(code || ''),
  );
}

export function isImageAsset(asset: any): boolean {
  if (!asset) return false;
  const mime = String(asset.mimeType || '').toLowerCase();
  const ext = String(asset.fileExt || '').toLowerCase();
  return (
    mime.startsWith('image/') ||
    /^(jpg|jpeg|png|webp|gif|bmp|avif|svg)$/.test(ext)
  );
}

export function formatFileSize(bytes: any): string {
  const num = Number(bytes || 0);
  if (!num || num <= 0) return '0 KB';
  if (num < 1024) return `${num} B`;
  if (num < 1024 * 1024) return `${(num / 1024).toFixed(1)} KB`;
  return `${(num / (1024 * 1024)).toFixed(2)} MB`;
}

export function formatApiError(
  err: any,
  fallbackDetail: string,
): { detail: string; endpoint: string } {
  const status = err?.response?.status;
  const method = (err?.config?.method || 'POST').toUpperCase();
  const rawUrl = err?.config?.url || '';
  const fullPath = rawUrl
    ? rawUrl.startsWith('/api')
      ? rawUrl
      : `/api/v1/kkumz${rawUrl.startsWith('/') ? '' : '/'}${rawUrl}`
    : '';
  const endpoint = fullPath ? `${method} ${fullPath}${status ? ` (${status})` : ''}` : status ? `HTTP ${status}` : '';

  const serverData = err?.response?.data;
  const serverMsg =
    (typeof serverData === 'object' &&
      (serverData?.message || serverData?.msg || serverData?.error_description)) ||
    '';
  const cleanServerMsg =
    serverMsg && serverMsg !== 'Internal Server Error' && serverMsg !== 'No message available'
      ? String(serverMsg)
      : '';

  let detail = fallbackDetail;
  if (status === 500) {
    detail = cleanServerMsg
      ? `${fallbackDetail} (서버 내부 오류 500: ${cleanServerMsg})`
      : `${fallbackDetail} — 서버 내부 오류가 발생하여 요청을 완료하지 못했습니다. 잠시 후 다시 시도를 누르거나 서버 상태를 확인해 주세요.`;
  } else if (err?.code === 'ECONNABORTED') {
    detail = `${fallbackDetail} — AI 생성 응답 대기 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.`;
  } else if (!err?.response) {
    detail = `${fallbackDetail} — 백엔드 서버와 통신할 수 없습니다. 서버 실행 상태를 확인해 주세요.`;
  } else if (cleanServerMsg) {
    detail = `${fallbackDetail} (${cleanServerMsg})`;
  } else if (status) {
    detail = `${fallbackDetail} (HTTP ${status} 오류)`;
  }

  return { detail, endpoint };
}

export function normalizeRuntime(raw: any): any {
  if (!raw) return null;
  const site = raw.site || raw;
  const rel = raw.release || {};
  return {
    ...site,
    ...raw,
    layoutSchema: raw.layoutSchema || rel.layoutSchema || {},
    designTokens: raw.designTokens || rel.designTokens || {},
  };
}

export { formatSiteTypeLabel };
