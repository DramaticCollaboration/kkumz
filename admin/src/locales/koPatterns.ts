/**
 * Korean locale linguistic constants & address/message matching rules.
 * Centralizes Korean-specific normalization rules under src/locales/.
 */

/** Native endonym displayed in the language switcher dropdown */
export const KOREAN_NATIVE_LABEL = '한국어';

/** Korean country name filtered out from Nominatim reverse-geocoding display names */
export const KOREAN_COUNTRY_NAME = '대한민국';

/** Korean road/dong suffix test used by Daum Postcode extra address formatting */
export const KOREAN_BNAME_SUFFIX_RE = /[동|로|가]$/g;

/** Korean administrative suffix regexes for district extraction */
export const KOREAN_DONG_SUFFIX_RE = /[동읍면리가]$/;
export const KOREAN_SIGUNGU_SUFFIX_RE = /[시군구]$/;
export const KOREAN_EUP_MYEON_DONG_RE = /[동읍면리]$/;

/** Korean province official name -> short name normalization rules */
export const KOREAN_PROVINCE_SHORT_RULES: Array<[RegExp, string]> = [
  [/^서울특별시/, '서울'],
  [/^부산광역시/, '부산'],
  [/^대구광역시/, '대구'],
  [/^인천광역시/, '인천'],
  [/^광주광역시/, '광주'],
  [/^대전광역시/, '대전'],
  [/^울산광역시/, '울산'],
  [/^세종특별자치시/, '세종'],
  [/^경기도/, '경기'],
  [/^강원특별자치도|^강원도/, '강원'],
  [/^충청북도/, '충북'],
  [/^충청남도/, '충남'],
  [/^전북특별자치도|^전라북도/, '전북'],
  [/^전라남도/, '전남'],
  [/^경상북도/, '경북'],
  [/^경상남도/, '경남'],
  [/^제주특별자치도|^제주도/, '제주'],
];

/** Known Korean si-gun-gu statutory 5-digit district codes */
export const KOREAN_SIGUNGU_CODE_MAP: Record<string, string> = {
  공주시: '44150',
  강남구: '11680',
  서초구: '11650',
  송파구: '11710',
  종로구: '11110',
  중구: '11140',
  유성구: '30200',
  서구: '30170',
  세종특별자치시: '36110',
};

/** Korean auth/session error keywords returned by backend 500 auth checks */
export const KOREAN_AUTH_ERROR_KEYWORDS = ['로그인', '토큰'] as const;

/** Generic OK/success response message pattern including Korean defaults */
export const GENERIC_SUCCESS_MESSAGE_RE = /^(ok|success|성공|작업 성공|작업성공|쿼리 성공)$/i;

/** MobileCardList index column title and status field matcher */
export const KOREAN_INDEX_COLUMN_TITLE = '순번';
export const STATUS_FIELD_NAME_RE = /status|state|level|severity|상태/i;
