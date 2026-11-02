<template>
  <div class="kkumz-root">
    <!-- 커뮤니티 & 탐색 전용 심플 1단 GNB -->
    <header v-if="isCommunityLayout" class="kkumz-gnb">
      <div class="gnb-inner">
        <div class="brand-area" @click="router.push('/feed')">
          <div class="logo-mark">
            <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="36" height="36" rx="10" fill="url(#gnbGrad)" />
              <path
                d="M11 10V26M11 18L20 10M15 15L21.5 26"
                stroke="white"
                stroke-width="3.2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle cx="26" cy="11" r="2.6" fill="#FDE047" />
              <defs>
                <linearGradient id="gnbGrad" x1="0" y1="0" x2="36" y2="36" gradientUnits="userSpaceOnUse">
                  <stop stop-color="#4F46E5" />
                  <stop offset="1" stop-color="#9333EA" />
                </linearGradient>
              </defs>
            </svg>
          </div>
          <div class="brand-titles">
            <span class="brand-ko">꿈즈 Kkumz</span>
            <span class="brand-sub">AI 브랜드 스튜디오 & 창업 커뮤니티</span>
          </div>
        </div>

        <nav class="nav-links">
          <router-link to="/feed" class="nav-item" :class="{ active: route.name === 'FeedView' }">
            쇼케이스 피드
          </router-link>
          <router-link to="/lounge" class="nav-item" :class="{ active: route.name === 'ProjectLoungeView' }">
            팀빌딩 라운지
          </router-link>
          <router-link to="/workspace" class="nav-item" :class="{ active: route.name === 'WorkspaceView' }">
            내 워크스페이스
          </router-link>
        </nav>

        <div class="gnb-actions">
          <div class="notif-wrapper">
            <button class="notif-btn" type="button" @click="toggleNotifDropdown">
              <span class="notif-icon" style="font-size: 11px; font-weight: 800; color: #334155">알림</span>
              <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount }}</span>
            </button>
            <div v-if="showNotifDropdown" class="notif-dropdown">
              <div class="notif-header">
                <strong>실시간 알림</strong>
                <button type="button" class="read-all-btn" @click="markAllRead">모두 읽음</button>
              </div>
              <div v-if="notifications.length === 0" class="notif-empty">새로운 알림이 없습니다.</div>
              <ul v-else class="notif-list">
                <li
                  v-for="n in notifications"
                  :key="n.id"
                  :class="{ unread: !n.isRead }"
                  @click="openNotifLink(n)"
                >
                  <div class="notif-title">{{ n.title }}</div>
                  <div class="notif-msg">{{ n.message }}</div>
                </li>
              </ul>
            </div>
          </div>

          <button class="primary-cta" type="button" @click="router.push('/studio')">
            + 새 아이디어 시작
          </button>
        </div>
      </div>
    </header>

    <main :class="isCommunityLayout ? 'kkumz-main-community' : 'kkumz-main-full'">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { kkumzApi } from '@/api/kkumzApi';

const route = useRoute();
const router = useRouter();

const isCommunityLayout = computed(() => route.meta.layout === 'community');

const notifications = ref<any[]>([]);
const showNotifDropdown = ref(false);

const unreadCount = computed(() => notifications.value.filter((n) => !n.isRead).length);

async function loadNotifications() {
  try {
    notifications.value = (await kkumzApi.listNotifications()) || [];
  } catch {
    notifications.value = [];
  }
}

function toggleNotifDropdown() {
  showNotifDropdown.value = !showNotifDropdown.value;
}

async function markAllRead() {
  try {
    await kkumzApi.markAllNotificationsRead();
    notifications.value = notifications.value.map((n) => ({ ...n, isRead: true }));
  } catch {
    // ignore
  }
}

function openNotifLink(n: any) {
  showNotifDropdown.value = false;
  if (n.linkUrl) {
    router.push(n.linkUrl);
  }
}

onMounted(() => {
  loadNotifications();
});
</script>

<style scoped>
.kkumz-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.kkumz-gnb {
  position: sticky;
  top: 0;
  z-index: 50;
  height: 68px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
}
.gnb-inner {
  max-width: 1320px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.brand-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}
.logo-mark {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}
.brand-titles {
  display: flex;
  flex-direction: column;
}
.brand-ko {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}
.brand-sub {
  font-size: 11px;
  color: #64748b;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-item {
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  color: #475569;
  transition: all 0.15s ease;
}
.nav-item:hover {
  background: #f1f5f9;
  color: #0f172a;
}
.nav-item.active {
  background: #eef2ff;
  color: #4f46e5;
}
.gnb-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.notif-wrapper {
  position: relative;
}
.notif-btn {
  position: relative;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.notif-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 5px;
  border-radius: 999px;
}
.notif-dropdown {
  position: absolute;
  top: 46px;
  right: 0;
  width: 320px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.12);
  padding: 12px;
  z-index: 100;
}
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
}
.read-all-btn {
  border: none;
  background: none;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.notif-empty {
  padding: 20px 0;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
}
.notif-list {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  max-height: 260px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.notif-list li {
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fafc;
  cursor: pointer;
}
.notif-list li.unread {
  background: #eef2ff;
  border-left: 3px solid #4f46e5;
}
.notif-title {
  font-size: 12px;
  font-weight: 700;
  color: #0f172a;
}
.notif-msg {
  font-size: 11px;
  color: #475569;
  margin-top: 2px;
}
.primary-cta {
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
}
.primary-cta:hover {
  background: linear-gradient(135deg, #4338ca, #4f46e5);
}
.kkumz-main-community {
  flex: 1;
  max-width: 1320px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 24px 64px;
}
.kkumz-main-full {
  flex: 1;
  width: 100%;
}
</style>
