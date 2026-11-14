<template>
  <section class="studio-access-denied" data-testid="studio-access-denied">
    <div class="access-denied-card">
      <span class="denied-badge">OWNERSHIP PROTECTION · HTTP 403</span>
      <h2>이 프로젝트의 열람 및 수정 권한이 없습니다</h2>
      <p>
        <strong>{{ accessDeniedBrand }}</strong> 프로젝트는
        <strong>{{ accessDeniedOwner }}</strong>님이 만든 프로젝트입니다.<br />
        본인이 만든 아이디어·사이트, 승인된 협업 팀원, 또는 플랫폼 관리자만 AI 스튜디오에서 열람·편집·배포할 수 있습니다.
      </p>
      <div class="denied-actions">
        <button type="button" class="primary-btn" @click="emit('go-workspace')">
          내 워크스페이스로 이동
        </button>
        <button type="button" class="secondary-btn" @click="emit('go-feed')">
          쇼케이스 피드로 이동
        </button>
        <button type="button" class="ghost-btn" @click="emit('switch-account')">
          소유자 계정으로 전환 로그인
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  accessDeniedBrand: string;
  accessDeniedOwner: string;
}>();

const emit = defineEmits<{
  (e: 'go-workspace'): void;
  (e: 'go-feed'): void;
  (e: 'switch-account'): void;
}>();
</script>

<style scoped src="./studio-view.css"></style>
