import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/feed',
    },
    {
      path: '/feed',
      name: 'FeedView',
      component: () => import('@/views/FeedView.vue'),
      meta: { layout: 'community', title: '쇼케이스 피드' },
    },
    {
      path: '/lounge/:siteSlug?',
      name: 'ProjectLoungeView',
      component: () => import('@/views/ProjectLoungeView.vue'),
      meta: { layout: 'community', title: '팀빌딩 라운지' },
    },
    {
      path: '/workspace',
      name: 'WorkspaceView',
      component: () => import('@/views/WorkspaceView.vue'),
      meta: { layout: 'community', title: '내 워크스페이스' },
    },
    {
      path: '/studio/:ideaId?',
      name: 'StudioView',
      component: () => import('@/views/StudioView.vue'),
      meta: { layout: 'studio', title: 'AI 브랜드·사이트 스튜디오' },
    },
    {
      path: '/s/:siteSlug',
      name: 'LiveSiteRendererView',
      component: () => import('@/views/LiveSiteRendererView.vue'),
      meta: { layout: 'live', title: '라이브 멀티사이트' },
    },
  ],
});

export default router;
