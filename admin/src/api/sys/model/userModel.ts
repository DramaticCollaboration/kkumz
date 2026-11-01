/**
 * @description: Login interface parameters
 */
export interface LoginParams {
  username: string;
  password: string;
}

export interface ThirdLoginParams {
  token: string;
  thirdType: string;
}

export interface RoleInfo {
  roleName: string;
  value: string;
}

/**
 * @description: Login interface return value
 */
export interface LoginResultModel {
  userId: string | number;
  token: string;
  role: RoleInfo;
  userInfo?: Record<string, unknown>;
}

/**
 * @description: Get user information return value
 */
export interface GetUserInfoModel {
  roles: RoleInfo[];
  // 사용자 ID
  userId: string | number;
  // 사용자 이름
  username: string;
  // 실명
  realname: string;
  // 아바타
  avatar: string;
  // 소개하다
  desc?: string;
  // 사용자 정보
  userInfo?: Record<string, unknown>;
  // 사전 항목 캐싱
  sysAllDictItems?: Record<string, unknown>;
}

/**
 * @description: Get user information return value
 */
export interface GetResultModel {
  code: number;
  message: string;
  result: object;
  success: Boolean;
}
