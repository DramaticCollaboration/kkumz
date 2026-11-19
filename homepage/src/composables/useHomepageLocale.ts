import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

export type HomepageLocale = 'ko' | 'en';

export function useHomepageLocale() {
  const route = useRoute();
  const router = useRouter();

  const locale = computed<HomepageLocale>(() => {
    const raw = String(route.params.locale || 'ko').toLowerCase();
    return raw === 'en' ? 'en' : 'ko';
  });

  const isEn = computed(() => locale.value === 'en');

  function localePath(subpath = ''): string {
    const clean = subpath.replace(/^\/+|\/+$/g, '');
    return clean ? `/${locale.value}/${clean}` : `/${locale.value}`;
  }

  function switchLocale(target: HomepageLocale): void {
    const currentPath = route.path;
    const stripped = currentPath.replace(/^\/(ko|en)(?=\/|$)/i, '');
    const nextPath = `/${target}${stripped || ''}`;
    router.push({ path: nextPath, query: route.query, hash: route.hash });
  }

  function toggleLocale(): void {
    switchLocale(locale.value === 'ko' ? 'en' : 'ko');
  }

  function tr(koText: string, enText: string): string {
    return locale.value === 'en' ? enText : koText;
  }

  return {
    locale,
    isEn,
    localePath,
    switchLocale,
    toggleLocale,
    tr,
  };
}
