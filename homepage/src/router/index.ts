import { createRouter, createWebHistory } from 'vue-router';
import ProductOverviewView from '@/views/ProductOverviewView.vue';
import ProductFeaturesView from '@/views/ProductFeaturesView.vue';
import ProductPricingView from '@/views/ProductPricingView.vue';
import ProductDocsView from '@/views/ProductDocsView.vue';
import ProductDownloadView from '@/views/ProductDownloadView.vue';
import ProductBlogView from '@/views/ProductBlogView.vue';

const PORTAL_SUBPATHS = new Set([
  'verse',
  'boot',
  'cms',
  'shop',
  'ops',
  'llm',
  'insight',
  'crawl',
  'eta',
  'apim',
  'watch',
  'todaycare',
  'awesomefit',
  'kkumz',
]);

function resolveRouterBase(): string {
  if (typeof window === 'undefined') {
    return '/';
  }
  const firstSeg = (window.location.pathname.split('/').filter(Boolean)[0] || '').toLowerCase();
  if (PORTAL_SUBPATHS.has(firstSeg)) {
    return `/${firstSeg}/`;
  }
  return '/';
}

const router = createRouter({
  history: createWebHistory(resolveRouterBase()),
  routes: [
    {
      path: '/',
      redirect: '/ko',
    },
    {
      path: '/:locale(ko|en)',
      name: 'overview',
      component: ProductOverviewView,
    },
    {
      path: '/:locale(ko|en)/features',
      name: 'features',
      component: ProductFeaturesView,
    },
    {
      path: '/:locale(ko|en)/pricing',
      name: 'pricing',
      component: ProductPricingView,
    },
    {
      path: '/:locale(ko|en)/docs',
      name: 'docs',
      component: ProductDocsView,
    },
    {
      path: '/:locale(ko|en)/download',
      name: 'download',
      component: ProductDownloadView,
    },
    {
      path: '/:locale(ko|en)/blog',
      name: 'blog',
      component: ProductBlogView,
    },
    {
      path: '/features',
      redirect: '/ko/features',
    },
    {
      path: '/pricing',
      redirect: '/ko/pricing',
    },
    {
      path: '/docs',
      redirect: '/ko/docs',
    },
    {
      path: '/download',
      redirect: '/ko/download',
    },
    {
      path: '/blog',
      redirect: '/ko/blog',
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/ko',
    },
  ],
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth', top: 80 };
    }
    return { top: 0 };
  },
});

export default router;
