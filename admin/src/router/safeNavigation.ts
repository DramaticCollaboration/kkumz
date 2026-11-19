import type { RouteLocationRaw, Router } from 'vue-router';

const SAFE_NAVIGATION_PATCHED_KEY = '__safeNavigationPatched__';

export function isValidNavigationLocation(to: unknown): to is RouteLocationRaw {
  if (to == null) {
    return false;
  }
  if (typeof to === 'string') {
    return to.trim().length > 0;
  }
  if (typeof to === 'object') {
    const location = to as {
      path?: unknown;
      fullPath?: unknown;
      name?: unknown;
      query?: unknown;
      hash?: unknown;
      params?: unknown;
    };
    const hasNonEmptyRoutePath = (value: unknown) => typeof value === 'string' && value.trim().length > 0;
    const hasValidRouteName = (value: unknown) => hasNonEmptyRoutePath(value) || typeof value === 'symbol';
    // name 우선 검사: vue-router 에서 named route 는 path 없이도 유효하므로
    // path 부재만 보고 차단하면 정상 navigation 까지 막힌다.
    if (hasValidRouteName(location.name)) {
      return true;
    }
    if (hasNonEmptyRoutePath(location.path) || hasNonEmptyRoutePath(location.fullPath)) {
      return true;
    }
    if (Object.prototype.hasOwnProperty.call(location, 'query') || Object.prototype.hasOwnProperty.call(location, 'hash') || Object.prototype.hasOwnProperty.call(location, 'params')) {
      return true;
    }
    return false;
  }
  return false;
}

export function patchRouterNavigationSafeguard(router: Router) {
  const target = router as Router & { [SAFE_NAVIGATION_PATCHED_KEY]?: boolean };
  if (target[SAFE_NAVIGATION_PATCHED_KEY]) {
    return;
  }
  target[SAFE_NAVIGATION_PATCHED_KEY] = true;

  const rawPush = router.push.bind(router);
  const rawReplace = router.replace.bind(router);

  router.push = ((to, ...rest) => {
    if (!isValidNavigationLocation(to)) {
      console.warn('[router] ignored push with empty location', to);
      return Promise.resolve();
    }
    return rawPush(to, ...rest);
  }) as typeof router.push;

  router.replace = ((to, ...rest) => {
    if (!isValidNavigationLocation(to)) {
      console.warn('[router] ignored replace with empty location', to);
      return Promise.resolve();
    }
    return rawReplace(to, ...rest);
  }) as typeof router.replace;
}
