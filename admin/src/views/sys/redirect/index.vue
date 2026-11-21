<template>
  <div class="redirect-container" role="status" aria-live="polite">
    <div class="redirect-card">
      <div class="redirect-spinner" aria-hidden="true"></div>
      <p class="redirect-title">{{ t('appUi.ka1a57f97') }}</p>
      <p class="redirect-desc">{{ targetPathText }}</p>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed, unref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useMultipleTabStore } from '/@/store/modules/multipleTab';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { currentRoute, replace } = useRouter();
  const { params, query } = unref(currentRoute);
  const { path } = params;
  const tabStore = useMultipleTabStore();
  const redirectPageParam = tabStore.redirectPageParam;
  const _path = Array.isArray(path) ? path.join('/') : String(path || '');
  const normalizedPath = _path.startsWith('/') ? _path : '/' + _path;

  const targetPathText = computed(() => {
    if (redirectPageParam?.redirect_type === 'name' && redirectPageParam.name) {
      return t('appUi.k4653b794', { p0: redirectPageParam.name });
    }
    return normalizedPath && normalizedPath !== '/' ? t('appUi.k8dabe023', { p0: normalizedPath }) : t('appUi.k73c87a9f');
  });

  if (redirectPageParam) {
    if (redirectPageParam.redirect_type === 'name') {
      replace({
        name: redirectPageParam.name,
        query: redirectPageParam.query,
        params: redirectPageParam.params,
      });
    } else {
      replace({
        path: normalizedPath,
        query,
      });
    }
  } else if (normalizedPath && normalizedPath !== '/') {
    replace({
      path: normalizedPath,
      query,
    });
  }
</script>

<style scoped>
  .redirect-container {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 45vh;
    width: 100%;
    padding: 24px;
    box-sizing: border-box;
  }

  .redirect-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px 28px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.85);
    border: 1px solid #e2e8f0;
  }

  .redirect-spinner {
    width: 24px;
    height: 24px;
    border: 2.5px solid #cbd5e1;
    border-top-color: #2563eb;
    border-radius: 50%;
    animation: redirect-spin 0.7s linear infinite;
  }

  .redirect-title {
    margin: 4px 0 0;
    font-size: 13px;
    font-weight: 600;
    color: #1e293b;
  }

  .redirect-desc {
    margin: 0;
    font-size: 11px;
    color: #64748b;
  }

  @keyframes redirect-spin {
    to {
      transform: rotate(360deg);
    }
  }
</style>
