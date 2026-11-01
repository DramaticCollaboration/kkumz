export type DynamicViewsRecord = Record<string, () => Promise<Recordable>>;

/** 등록된 모듈의 동적 페이지 */
export const packageViews: DynamicViewsRecord = {};

/**
 * 동적 라우팅 페이지 등록
 * @param 이 모듈 아래의 모든 페이지를 가져오는 getViews 메소드
 */
export function registerDynamicRouter(getViews: () => DynamicViewsRecord) {
  if (typeof getViews === 'function') {
    let dynamicViews = getViews();
    Object.keys(dynamicViews).forEach((key) => {
      // 라우팅을 통해 식별할 수 있도록 동적 페이지의 키를 처리합니다.
      let newKey = key.replace('./src/views', '../../views');
      packageViews[newKey] = dynamicViews[key];
    });
  }
}
