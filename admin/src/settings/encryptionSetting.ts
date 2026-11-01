import { isDevMode } from '/@/utils/env';

// 캐시 기본 만료 시간
export const DEFAULT_CACHE_TIME = 60 * 60 * 24 * 7;

// 캐시 암호화를 켜면 암호화 키가 암호화됩니다.AES 암호화 사용
export const cacheCipher = {
  key: '_11111000001111@',
  iv: '@11111000001111_',
};

// 캐시 암호화 여부, 기본 프로덕션 환경 암호화
export const enableStorageEncryption = false;
