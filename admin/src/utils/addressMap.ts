export interface DaumPostcodeData {
  zonecode: string;
  address: string;
  addressEnglish: string;
  addressType: 'R' | 'J';
  userSelectedType: 'R' | 'J';
  noSelected: 'Y' | 'N';
  userLanguageType: 'K' | 'E';
  roadAddress: string;
  roadAddressEnglish: string;
  jibunAddress: string;
  jibunAddressEnglish: string;
  autoRoadAddress: string;
  autoJibunAddress: string;
  buildingCode: string;
  buildingName: string;
  apartment: 'Y' | 'N';
  sido: string;
  sigungu: string;
  sigunguCode: string;
  roadnameCode: string;
  bcode: string;
  roadname: string;
  bname: string;
  bname1: string;
  bname2: string;
}

export interface GeocodeResultItem {
  placeId: string;
  displayName: string;
  roadAddress: string;
  postCode: string;
  buildingName?: string;
  lat: number;
  lon: number;
}

declare global {
  interface Window {
    daum?: {
      Postcode: new (options: {
        oncomplete: (data: DaumPostcodeData) => void;
        onresize?: (size: { width: number; height: number }) => void;
        width?: string | number;
        height?: string | number;
        animation?: boolean;
        autoMapping?: boolean;
      }) => {
        open: (openOptions?: { q?: string; popupTitle?: string; autoClose?: boolean }) => void;
        embed: (element: HTMLElement, embedOptions?: { q?: string; autoClose?: boolean }) => void;
      };
    };
    L?: any;
  }
}

let daumScriptPromise: Promise<void> | null = null;
let leafletPromise: Promise<any> | null = null;

/**
 * 카카오(다음) 우편번호 스크립트 동적 로더
 */
export function ensureDaumPostcodeLoaded(): Promise<void> {
  if (typeof window !== 'undefined' && window.daum?.Postcode) {
    return Promise.resolve();
  }
  if (daumScriptPromise) {
    return daumScriptPromise;
  }
  daumScriptPromise = new Promise((resolve, reject) => {
    const existing = document.querySelector('script[src*="postcode.v2.js"]') as HTMLScriptElement | null;
    if (existing) {
      if (window.daum?.Postcode) {
        resolve();
        return;
      }
      existing.addEventListener('load', () => resolve(), { once: true });
      existing.addEventListener('error', () => reject(new Error('우편번호 스크립트 로드 실패')), { once: true });
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => {
      daumScriptPromise = null;
      reject(new Error('우편번호 서비스를 불러오지 못했습니다.'));
    };
    document.head.appendChild(script);
  });
  return daumScriptPromise;
}

/**
 * Leaflet 인터랙티브 지도 라이브러리 동적 로더
 */
export function ensureLeafletLoaded(): Promise<any> {
  if (typeof window !== 'undefined' && window.L) {
    return Promise.resolve(window.L);
  }
  if (leafletPromise) {
    return leafletPromise;
  }
  leafletPromise = new Promise((resolve, reject) => {
    if (!document.querySelector('link[href*="leaflet.css"]')) {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }

    const existing = document.querySelector('script[src*="leaflet.js"]') as HTMLScriptElement | null;
    if (existing) {
      if (window.L) {
        resolve(window.L);
        return;
      }
      existing.addEventListener('load', () => resolve(window.L), { once: true });
      existing.addEventListener('error', () => reject(new Error('지도 스크립트 로드 실패')), { once: true });
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
    script.async = true;
    script.onload = () => resolve(window.L);
    script.onerror = () => {
      leafletPromise = null;
      reject(new Error('지도 라이브러리를 불러오지 못했습니다.'));
    };
    document.head.appendChild(script);
  });
  return leafletPromise;
}

/**
 * 다음 우편번호 응답 데이터를 표준 주소 포맷으로 변환
 */
export function parseDaumPostcodeData(data: DaumPostcodeData): {
  postCode: string;
  address: string;
  buildingHint: string;
} {
  const baseAddr =
    data.userSelectedType === 'R'
      ? data.roadAddress || data.autoRoadAddress || data.address
      : data.jibunAddress || data.autoJibunAddress || data.address;

  const extraParts: string[] = [];
  if (data.userSelectedType === 'R') {
    if (data.bname && /[동|로|가]$/g.test(data.bname)) {
      extraParts.push(data.bname);
    }
    if (data.buildingName) {
      extraParts.push(data.buildingName);
    }
  }

  const extraSuffix = extraParts.length > 0 ? ` (${extraParts.join(', ')})` : '';
  return {
    postCode: data.zonecode || '',
    address: `${baseAddr}${extraSuffix}`.trim(),
    buildingHint: data.buildingName || '',
  };
}

/**
 * 한국어 주소 다단계 정규화 후보 생성 (지오코딩 적중률 향상)
 */
function buildKoreanAddressCandidates(rawQuery: string): string[] {
  const cleaned = rawQuery.trim();
  if (!cleaned) return [];

  const candidates = new Set<string>();
  candidates.add(cleaned);

  // 1) 괄호 부가정보 제거: "서울특별시 중구 소공로 51 (회현동1가, 한빛은행본점)" -> "서울특별시 중구 소공로 51"
  const withoutParens = cleaned.replace(/\([^)]*\)/g, ' ').replace(/\s+/g, ' ').trim();
  if (withoutParens) {
    candidates.add(withoutParens);
  }

  // 2) 시/도 명칭 축약 버전 추가
  const shortProvince = withoutParens
    .replace(/^서울특별시/, '서울')
    .replace(/^부산광역시/, '부산')
    .replace(/^대구광역시/, '대구')
    .replace(/^인천광역시/, '인천')
    .replace(/^광주광역시/, '광주')
    .replace(/^대전광역시/, '대전')
    .replace(/^울산광역시/, '울산')
    .replace(/^세종특별자치시/, '세종')
    .replace(/^경기도/, '경기')
    .replace(/^강원특별자치도|^강원도/, '강원')
    .replace(/^충청북도/, '충북')
    .replace(/^충청남도/, '충남')
    .replace(/^전북특별자치도|^전라북도/, '전북')
    .replace(/^전라남도/, '전남')
    .replace(/^경상북도/, '경북')
    .replace(/^경상남도/, '경남')
    .replace(/^제주특별자치도|^제주도/, '제주')
    .trim();
  if (shortProvince) {
    candidates.add(shortProvince);
  }

  // 3) 상세 번지/층/호수 제외 후 도로명/동 중심 검색 후보 추가
  const tokens = withoutParens.split(/\s+/);
  if (tokens.length >= 3) {
    // 앞 3어절 (예: 서울특별시 중구 회현동1가 / 세종특별자치시 도움6로 42)
    candidates.add(tokens.slice(0, 3).join(' '));
  }
  if (tokens.length >= 2) {
    // 번지수 제거된 도로명/동 (예: 서울특별시 중구 회현동1가)
    const withoutTrailingNumber = withoutParens.replace(/\s+\d+(?:-\d+)?$/, '').trim();
    if (withoutTrailingNumber) {
      candidates.add(withoutTrailingNumber);
    }
  }

  return Array.from(candidates);
}

/**
 * Nominatim 응답 객체에서 한국형 주소 문자열과 우편번호 추출
 */
function formatNominatimAddress(item: any): GeocodeResultItem {
  const addr = item.address || {};
  const sido = addr.province || addr.city || addr.state || '';
  const sigungu = addr.borough || addr.county || addr.city_district || addr.town || '';
  const roadOrDong = addr.road || addr.quarter || addr.suburb || addr.neighbourhood || addr.village || '';
  const houseNo = addr.house_number || '';
  const building = addr.building || addr.amenity || addr.office || addr.commercial || '';

  const parts = [sido, sigungu, roadOrDong, houseNo].filter(Boolean);
  // 중복 제거 (예: 서울특별시 서울특별시)
  const dedupedParts = parts.filter((part, idx) => idx === 0 || part !== parts[idx - 1]);
  let roadAddress = dedupedParts.join(' ').trim();

  if (!roadAddress && item.display_name) {
    roadAddress = String(item.display_name)
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s && s !== '대한민국' && !/^\d{5}$/.test(s))
      .reverse()
      .join(' ');
  }

  if (building && !roadAddress.includes(building)) {
    roadAddress = `${roadAddress} (${building})`.trim();
  }

  const rawPostCode = String(addr.postcode || '').trim();
  const postCodeMatch = rawPostCode.match(/\d{5}/);

  return {
    placeId: String(item.place_id || `${item.lat},${item.lon}`),
    displayName: item.display_name || roadAddress,
    roadAddress: roadAddress || String(item.display_name || ''),
    postCode: postCodeMatch ? postCodeMatch[0] : rawPostCode,
    buildingName: building || item.name || undefined,
    lat: Number(item.lat),
    lon: Number(item.lon),
  };
}

/**
 * 주소 또는 장소 키워드로 지도 좌표 및 주소 목록 검색 (OpenStreetMap Nominatim 연동)
 */
export async function searchAddressGeocode(query: string, limit = 6): Promise<GeocodeResultItem[]> {
  const candidates = buildKoreanAddressCandidates(query);
  if (candidates.length === 0) return [];

  for (const candidate of candidates) {
    const url = new URL('https://nominatim.openstreetmap.org/search');
    url.searchParams.set('format', 'jsonv2');
    url.searchParams.set('q', candidate);
    url.searchParams.set('countrycodes', 'kr');
    url.searchParams.set('addressdetails', '1');
    url.searchParams.set('accept-language', 'ko');
    url.searchParams.set('limit', String(limit));

    const resp = await fetch(url.toString(), {
      headers: {
        Accept: 'application/json',
      },
    });
    if (!resp.ok) {
      continue;
    }
    const data = await resp.json();
    if (Array.isArray(data) && data.length > 0) {
      return data.map(formatNominatimAddress);
    }
  }

  return [];
}

/**
 * 지도 클릭 좌표(위도/경도) 기반 역지오코딩 주소 변환
 */
export async function reverseGeocode(lat: number, lon: number): Promise<GeocodeResultItem | null> {
  const url = new URL('https://nominatim.openstreetmap.org/reverse');
  url.searchParams.set('format', 'jsonv2');
  url.searchParams.set('lat', String(lat));
  url.searchParams.set('lon', String(lon));
  url.searchParams.set('addressdetails', '1');
  url.searchParams.set('accept-language', 'ko');

  const resp = await fetch(url.toString(), {
    headers: {
      Accept: 'application/json',
    },
  });
  if (!resp.ok) {
    return null;
  }
  const data = await resp.json();
  if (!data || data.error) {
    return null;
  }
  return formatNominatimAddress(data);
}

/**
 * 카카오맵 / 네이버 지도 외부 바로가기 URL 생성
 */
export function getKakaoMapUrl(address: string): string {
  const q = encodeURIComponent(address.replace(/\([^)]*\)/g, '').trim() || address.trim());
  return `https://map.kakao.com/?q=${q}`;
}

export function getNaverMapUrl(address: string): string {
  const q = encodeURIComponent(address.replace(/\([^)]*\)/g, '').trim() || address.trim());
  return `https://map.naver.com/p/search/${q}`;
}

/**
 * 사업자등록번호 자동 하이픈 포맷터 (000-00-00000)
 */
export function formatBusinessNo(value?: string): string {
  if (!value) return '';
  const digits = value.replace(/\D/g, '').slice(0, 10);
  if (digits.length <= 3) return digits;
  if (digits.length <= 5) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 5)}-${digits.slice(5, 10)}`;
}

/**
 * 법인등록번호 자동 하이픈 포맷터 (000000-0000000)
 */
export function formatCorporateNo(value?: string): string {
  if (!value) return '';
  const digits = value.replace(/\D/g, '').slice(0, 13);
  if (digits.length <= 6) return digits;
  return `${digits.slice(0, 6)}-${digits.slice(6, 13)}`;
}

/**
 * 전화번호 / 팩스번호 자동 하이픈 포맷터
 */
export function formatPhoneNo(value?: string): string {
  if (!value) return '';
  const digits = value.replace(/\D/g, '').slice(0, 11);
  if (!digits) return '';

  if (digits.startsWith('02')) {
    if (digits.length <= 2) return digits;
    if (digits.length <= 5) return `${digits.slice(0, 2)}-${digits.slice(2)}`;
    if (digits.length <= 9) return `${digits.slice(0, 2)}-${digits.slice(2, 5)}-${digits.slice(5)}`;
    return `${digits.slice(0, 2)}-${digits.slice(2, 6)}-${digits.slice(6, 10)}`;
  }

  if (digits.length === 8 && /^1\d{3}/.test(digits)) {
    return `${digits.slice(0, 4)}-${digits.slice(4, 8)}`;
  }

  if (digits.length <= 3) return digits;
  if (digits.length <= 6) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  if (digits.length <= 10) return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
}
