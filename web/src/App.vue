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

        <nav class="nav-links desktop-only" aria-label="주 메뉴">
          <router-link to="/feed" class="nav-item" :class="{ active: route.name === 'FeedView' }">
            쇼케이스 피드
          </router-link>
          <router-link to="/lounge" class="nav-item" :class="{ active: route.name === 'ProjectLoungeView' }">
            팀빌딩 라운지
          </router-link>
          <a
            href="/workspace"
            class="nav-item"
            :class="{ active: route.name === 'WorkspaceView' }"
            @click.prevent="handleProtectedNav('/workspace', '내 워크스페이스 조회 및 관리를 위해 로그인해 주세요.')"
          >
            내 워크스페이스
          </a>
        </nav>

        <div class="gnb-actions desktop-only">
          <template v-if="isAuthenticated">
            <div class="user-profile-pill">
              <span class="user-avatar-dot">{{ (kkumzAuth.user?.realname || kkumzAuth.user?.username || 'M').slice(0, 1) }}</span>
              <span class="user-realname">{{ kkumzAuth.user?.realname || kkumzAuth.user?.username }}</span>
              <span class="user-role-tag">{{ formatUserRoleLabel(kkumzAuth.roles) }}</span>
            </div>

            <div class="notif-wrapper">
              <button class="notif-btn" type="button" @click="toggleNotifDropdown">
                <span class="notif-icon" style="font-size: 11px; font-weight: 800; color: #334155">알림</span>
                <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount }}</span>
              </button>
              <div v-if="showNotifDropdown" class="notif-dropdown">
                <div class="notif-header">
                  <strong>내 실시간 알림</strong>
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

            <button class="ghost-auth-btn" type="button" @click="handleLogout">
              로그아웃
            </button>
          </template>

          <template v-else>
            <button class="ghost-auth-btn" type="button" @click="openAuthModal('login')">
              로그인
            </button>
            <button class="outline-auth-btn" type="button" @click="openAuthModal('register')">
              회원가입
            </button>
          </template>

          <button
            class="primary-cta"
            type="button"
            @click="handleProtectedNav('/studio', '새 브랜드 아이디어를 만들고 내 사이트로 저장하려면 로그인해 주세요.')"
          >
            + 새 아이디어 시작
          </button>
        </div>

        <!-- 모바일 전용 햄버거 버튼 (<= 900px) -->
        <button
          type="button"
          class="mobile-menu-btn"
          :aria-expanded="mobileMenuOpen"
          aria-label="메뉴 열기"
          @click="mobileMenuOpen = !mobileMenuOpen"
        >
          <svg v-if="!mobileMenuOpen" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 모바일 드로어 내비게이션 -->
      <div v-if="mobileMenuOpen" class="mobile-nav-drawer">
        <nav class="mobile-nav-links" aria-label="모바일 메뉴">
          <router-link
            to="/feed"
            class="mobile-nav-item"
            :class="{ active: route.name === 'FeedView' }"
            @click="mobileMenuOpen = false"
          >
            쇼케이스 피드
          </router-link>
          <router-link
            to="/lounge"
            class="mobile-nav-item"
            :class="{ active: route.name === 'ProjectLoungeView' }"
            @click="mobileMenuOpen = false"
          >
            팀빌딩 라운지
          </router-link>
          <a
            href="/workspace"
            class="mobile-nav-item"
            :class="{ active: route.name === 'WorkspaceView' }"
            @click.prevent="mobileMenuOpen = false; handleProtectedNav('/workspace', '내 워크스페이스 조회 및 관리를 위해 로그인해 주세요.')"
          >
            내 워크스페이스
          </a>
        </nav>

        <div class="mobile-drawer-divider"></div>

        <div class="mobile-drawer-actions">
          <template v-if="isAuthenticated">
            <div class="mobile-user-row">
              <div class="user-profile-pill">
                <span class="user-avatar-dot">{{ (kkumzAuth.user?.realname || kkumzAuth.user?.username || 'M').slice(0, 1) }}</span>
                <span class="user-realname">{{ kkumzAuth.user?.realname || kkumzAuth.user?.username }}</span>
                <span class="user-role-tag">{{ formatUserRoleLabel(kkumzAuth.roles) }}</span>
              </div>
              <button class="ghost-auth-btn" type="button" @click="mobileMenuOpen = false; handleLogout()">
                로그아웃
              </button>
            </div>
          </template>
          <template v-else>
            <div class="mobile-auth-row">
              <button class="ghost-auth-btn flex-1" type="button" @click="mobileMenuOpen = false; openAuthModal('login')">
                로그인
              </button>
              <button class="outline-auth-btn flex-1" type="button" @click="mobileMenuOpen = false; openAuthModal('register')">
                회원가입
              </button>
            </div>
          </template>

          <button
            class="primary-cta mobile-full-cta"
            type="button"
            @click="mobileMenuOpen = false; handleProtectedNav('/studio', '새 브랜드 아이디어를 만들고 내 사이트로 저장하려면 로그인해 주세요.')"
          >
            + 새 아이디어 시작
          </button>
        </div>
      </div>
    </header>

    <main :class="isCommunityLayout ? 'kkumz-main-community' : 'kkumz-main-full'">
      <router-view />
    </main>

    <!-- 공식 기업 법적 고지 푸터 (커뮤니티 레이아웃 공통) -->
    <footer v-if="isCommunityLayout" class="kkumz-footer">
      <div class="kkumz-footer-inner">
        <div class="footer-top">
          <div class="footer-brand">
            <strong>꿈즈 Kkumz</strong>
            <span>AI 브랜드 스튜디오 &amp; 창업 커뮤니티</span>
          </div>
          <div class="footer-legal-links">
            <a href="https://empasy.io/ko/pages/terms.html" target="_blank" rel="noopener noreferrer">이용약관</a>
            <span class="footer-dot" aria-hidden="true">·</span>
            <a href="https://empasy.io/ko/pages/privacy.html" target="_blank" rel="noopener noreferrer" class="privacy-strong">개인정보 처리방침</a>
          </div>
        </div>
        <div class="footer-corp-info">
          <p>
            <strong>주식회사 엠파시 (Empasy Inc.)</strong> | 대표이사: 오병문 | 사업자등록번호: 547-81-03408 | 혁신성장유형 벤처기업 확인 (제 20261111030084 호)
          </p>
          <p>
            주소: 서울 금천구 가산디지털1로 205-27 2층 208호 | 대표번호: 0507-1360-8169 | 이메일: poh@empasy.com
          </p>
        </div>
        <div class="footer-copy">
          &copy; 2026 주식회사 엠파시 (Empasy Inc.). All rights reserved.
        </div>
      </div>
    </footer>

    <!-- 통합 로그인 · 셀프 회원가입 모달 -->
    <div v-if="kkumzAuth.authModalOpen" class="auth-modal-backdrop" @click.self="closeAuthModal">
      <div class="auth-modal-card">
        <div class="auth-modal-top">
          <div>
            <span class="auth-brand-pill">꿈즈 Kkumz 계정 인증</span>
            <h3>{{ kkumzAuth.authModalTab === 'login' ? '메이커 로그인' : '신규 메이커 회원가입' }}</h3>
            <p class="auth-sub">
              {{
                kkumzAuth.authModalHint ||
                '본인이 만든 브랜드 사이트만 안전하게 수정·배포하고 팀원을 모집할 수 있습니다.'
              }}
            </p>
          </div>
          <button type="button" class="auth-close-btn" @click="closeAuthModal">닫기</button>
        </div>

        <div class="auth-tabs">
          <button
            type="button"
            :class="{ active: kkumzAuth.authModalTab === 'login' }"
            @click="switchAuthTab('login')"
          >
            로그인
          </button>
          <button
            type="button"
            :class="{ active: kkumzAuth.authModalTab === 'register' }"
            @click="switchAuthTab('register')"
          >
            회원가입
          </button>
        </div>

        <div v-if="authError" class="auth-error-banner">{{ authError }}</div>

        <!-- 로그인 탭 -->
        <form v-if="kkumzAuth.authModalTab === 'login'" class="auth-form" @submit.prevent="submitLogin">
          <label class="auth-field">
            <span>아이디</span>
            <input
              v-model="loginForm.username"
              type="text"
              placeholder="예: soyeon.jeju"
              autocomplete="username"
              required
            />
          </label>
          <label class="auth-field">
            <span>비밀번호</span>
            <input
              ref="loginPasswordInput"
              v-model="loginForm.password"
              type="password"
              placeholder="비밀번호 입력"
              autocomplete="current-password"
              required
            />
          </label>
          <button type="submit" class="auth-submit-btn" :disabled="authSubmitting">
            {{ authSubmitting ? '인증 중...' : '로그인' }}
          </button>

          <div v-if="isDemoMode" class="quick-demo-box">
            <div class="quick-demo-head">
              <strong>체험 계정으로 둘러보기</strong>
              <span>클릭 시 샘플 브랜드 체험 계정 선택</span>
            </div>
            <div class="quick-demo-grid">
              <button
                v-for="acc in demoAccounts"
                :key="acc.username"
                type="button"
                class="quick-demo-item"
                :disabled="authSubmitting"
                @click="quickLoginAs(acc.username)"
              >
                <div class="qd-top">
                  <strong>{{ acc.realname }}</strong>
                  <span class="qd-role">{{ acc.badge }}</span>
                </div>
                <div class="qd-brand">{{ acc.brandName }}</div>
                <code class="qd-user">@{{ acc.username }}</code>
              </button>
            </div>
          </div>
        </form>

        <!-- 회원가입 탭 -->
        <form v-else class="auth-form" @submit.prevent="submitRegister">
          <div class="auth-form-row">
            <label class="auth-field">
              <span>아이디 (영문·숫자 3~32자) *</span>
              <input
                v-model="registerForm.username"
                type="text"
                placeholder="예: maker.seoul"
                autocomplete="username"
                required
              />
            </label>
            <label class="auth-field">
              <span>이름 / 메이커 활동명 *</span>
              <input
                v-model="registerForm.realname"
                type="text"
                placeholder="예: 이서윤 대표"
                required
              />
            </label>
          </div>

          <div class="auth-form-row">
            <label class="auth-field">
              <span>비밀번호 (4자 이상) *</span>
              <input
                v-model="registerForm.password"
                type="password"
                placeholder="비밀번호 입력"
                autocomplete="new-password"
                required
              />
            </label>
            <label class="auth-field">
              <span>이메일</span>
              <input
                v-model="registerForm.email"
                type="email"
                placeholder="예: maker@kkumz.kr"
              />
            </label>
          </div>

          <div class="auth-field">
            <span>활동 역할 선택</span>
            <div class="role-select-grid">
              <button
                type="button"
                class="role-option-btn"
                :class="{ active: registerForm.roleCode === 'ROLE_CREATOR' }"
                @click="registerForm.roleCode = 'ROLE_CREATOR'"
              >
                <strong>브랜드 메이커</strong>
                <span>아이디어로 브랜드 사이트 생성 · 운영</span>
              </button>
              <button
                type="button"
                class="role-option-btn"
                :class="{ active: registerForm.roleCode === 'ROLE_DEVELOPER' }"
                @click="registerForm.roleCode = 'ROLE_DEVELOPER'"
              >
                <strong>프론트엔드 개발자</strong>
                <span>팀 합류 · 맞춤 디자인·코드 고도화</span>
              </button>
            </div>
          </div>

          <button type="submit" class="auth-submit-btn" :disabled="authSubmitting">
            {{ authSubmitting ? '가입 처리 중...' : '회원가입 및 바로 시작하기' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  closeAuthModal,
  formatUserRoleLabel,
  isAuthenticated,
  kkumzApi,
  kkumzAuth,
  openAuthModal,
} from '@/api/kkumzApi';

const route = useRoute();
const router = useRouter();

const isCommunityLayout = computed(() => route.meta.layout === 'community');
const isDemoMode = computed(() => import.meta.env.VITE_DEMO_MODE !== 'false');

const mobileMenuOpen = ref(false);
const loginPasswordInput = ref<HTMLInputElement | null>(null);
const notifications = ref<any[]>([]);
const showNotifDropdown = ref(false);
const authSubmitting = ref(false);
const authError = ref('');

const loginForm = reactive({
  username: '',
  password: '',
});

const registerForm = reactive({
  username: '',
  password: '',
  realname: '',
  email: '',
  phone: '',
  roleCode: 'ROLE_CREATOR',
});

const demoAccounts = [
  {
    username: 'soyeon.jeju',
    realname: '한소연 대표',
    badge: '체험',
    brandName: '말차 아뜰리에 제주',
  },
  {
    username: 'minjun.maker',
    realname: '박민준 메이커',
    badge: '체험',
    brandName: '데스크 오빗',
  },
  {
    username: 'jihoon.run',
    realname: '정지훈 크루장',
    badge: '체험',
    brandName: '서울 나이트 런 클럽',
  },
  {
    username: 'hajun.roast',
    realname: '최하준 헤드로스터',
    badge: '체험',
    brandName: '빈 크로니클 서울',
  },
];

const unreadCount = computed(() => notifications.value.filter((n) => !n.isRead).length);

async function loadNotifications() {
  if (!isAuthenticated.value) {
    notifications.value = [];
    return;
  }
  try {
    notifications.value = (await kkumzApi.listNotifications(kkumzAuth.user?.username)) || [];
  } catch {
    notifications.value = [];
  }
}

function toggleNotifDropdown() {
  showNotifDropdown.value = !showNotifDropdown.value;
}

async function markAllRead() {
  try {
    await kkumzApi.markAllNotificationsRead(kkumzAuth.user?.username);
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

function handleProtectedNav(targetPath: string, hintMessage: string) {
  if (isAuthenticated.value) {
    router.push(targetPath);
    return;
  }
  openAuthModal(
    'login',
    () => {
      router.push(targetPath);
    },
    hintMessage,
  );
}

function switchAuthTab(tab: 'login' | 'register') {
  authError.value = '';
  kkumzAuth.authModalTab = tab;
}

async function completeAuthFlow() {
  const cb = kkumzAuth.onAuthSuccess;
  closeAuthModal();
  await loadNotifications();
  if (cb) {
    await cb();
  }
}

async function submitLogin() {
  authError.value = '';
  authSubmitting.value = true;
  try {
    await kkumzApi.login({
      username: loginForm.username.trim(),
      password: loginForm.password,
    });
    loginForm.password = '';
    await completeAuthFlow();
  } catch (e: any) {
    authError.value = e?.response?.data?.message || e?.message || '로그인에 실패했습니다.';
  } finally {
    authSubmitting.value = false;
  }
}

async function quickLoginAs(username: string) {
  const demoPw = (import.meta.env.VITE_DEMO_USER_PW as string) || '';
  loginForm.username = username;
  if (demoPw) {
    loginForm.password = demoPw;
    await submitLogin();
  } else {
    loginPasswordInput.value?.focus();
  }
}

async function submitRegister() {
  authError.value = '';
  authSubmitting.value = true;
  try {
    await kkumzApi.register({
      username: registerForm.username.trim(),
      password: registerForm.password,
      realname: registerForm.realname.trim(),
      email: registerForm.email.trim() || undefined,
      phone: registerForm.phone.trim() || undefined,
      roleCode: registerForm.roleCode,
    });
    registerForm.password = '';
    await completeAuthFlow();
  } catch (e: any) {
    authError.value = e?.response?.data?.message || e?.message || '회원가입 처리 중 오류가 발생했습니다.';
  } finally {
    authSubmitting.value = false;
  }
}

async function handleLogout() {
  showNotifDropdown.value = false;
  await kkumzApi.logout();
  notifications.value = [];
  if (route.path.startsWith('/workspace') || route.path.startsWith('/studio')) {
    router.push('/feed');
  }
}

watch(
  () => route.fullPath,
  () => {
    mobileMenuOpen.value = false;
  },
);

watch(
  () => kkumzAuth.user?.username,
  () => {
    loadNotifications();
  },
);

onMounted(async () => {
  if (kkumzAuth.token) {
    try {
      await kkumzApi.fetchCurrentUser();
    } catch {
      await kkumzApi.logout();
    }
  }
  await loadNotifications();
});
</script>

<style scoped>
.kkumz-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}
.kkumz-gnb {
  position: sticky;
  top: 0;
  z-index: 50;
  min-height: 68px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
}
.gnb-inner {
  max-width: 1320px;
  height: 68px;
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
  flex-shrink: 0;
}
.logo-mark {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}
.brand-titles {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.brand-ko {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  white-space: nowrap;
}
.brand-sub {
  font-size: 11px;
  color: #64748b;
  white-space: nowrap;
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
  white-space: nowrap;
  flex-shrink: 0;
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
  white-space: nowrap;
  flex-shrink: 0;
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
.user-profile-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 6px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}
.user-avatar-dot {
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: #4f46e5;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-realname {
  font-size: 13px;
  font-weight: 800;
  color: #0f172a;
}
.user-role-tag {
  font-size: 11px;
  font-weight: 700;
  color: #4f46e5;
  background: #eef2ff;
  padding: 2px 7px;
  border-radius: 999px;
}
.ghost-auth-btn {
  padding: 8px 13px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.ghost-auth-btn:hover {
  background: #f8fafc;
  color: #0f172a;
}
.outline-auth-btn {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid #c7d2fe;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}
.outline-auth-btn:hover {
  background: #e0e7ff;
}
.auth-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: rgba(15, 23, 42, 0.56);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.auth-modal-card {
  width: 100%;
  max-width: 520px;
  max-height: 92vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 22px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.24);
  padding: 26px;
}
.auth-modal-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.auth-brand-pill {
  display: inline-block;
  font-size: 11px;
  font-weight: 800;
  color: #4f46e5;
  background: #eef2ff;
  padding: 3px 9px;
  border-radius: 999px;
  margin-bottom: 6px;
}
.auth-modal-top h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}
.auth-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}
.auth-close-btn {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #475569;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
  cursor: pointer;
}
.auth-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
  margin: 18px 0 16px;
}
.auth-tabs button {
  padding: 9px 12px;
  border: none;
  border-radius: 9px;
  background: transparent;
  font-size: 13px;
  font-weight: 800;
  color: #64748b;
  cursor: pointer;
}
.auth-tabs button.active {
  background: #fff;
  color: #4f46e5;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.08);
}
.auth-error-banner {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 12px;
  font-weight: 700;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.auth-form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.auth-field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.auth-field span {
  font-size: 12px;
  font-weight: 800;
  color: #334155;
}
.auth-field input {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
  outline: none;
}
.auth-field input:focus {
  border-color: #4f46e5;
}
.role-select-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.role-option-btn {
  text-align: left;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.role-option-btn strong {
  font-size: 13px;
  color: #0f172a;
}
.role-option-btn span {
  font-size: 11px;
  font-weight: 500;
  color: #64748b;
}
.role-option-btn.active {
  border-color: #4f46e5;
  background: #eef2ff;
}
.auth-submit-btn {
  margin-top: 4px;
  padding: 12px 16px;
  border-radius: 11px;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}
.auth-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.quick-demo-box {
  margin-top: 8px;
  padding-top: 14px;
  border-top: 1px dashed #e2e8f0;
}
.quick-demo-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.quick-demo-head strong {
  font-size: 12px;
  color: #0f172a;
}
.quick-demo-head span {
  font-size: 11px;
  color: #64748b;
}
.quick-demo-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.quick-demo-item {
  text-align: left;
  padding: 9px 11px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.15s ease;
}
.quick-demo-item:hover {
  border-color: #6366f1;
  background: #eef2ff;
}
.qd-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.qd-top strong {
  font-size: 12px;
  color: #0f172a;
}
.qd-role {
  font-size: 10px;
  font-weight: 800;
  color: #4f46e5;
  background: #e0e7ff;
  padding: 1px 6px;
  border-radius: 999px;
}
.qd-brand {
  font-size: 11px;
  color: #334155;
  font-weight: 700;
  margin-top: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.qd-user {
  display: block;
  margin-top: 2px;
  font-size: 10px;
  color: #64748b;
}

/* Mobile Header Drawer & Responsive Breakpoints */
.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #0f172a;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}
.mobile-menu-btn svg {
  width: 22px;
  height: 22px;
}
.mobile-nav-drawer {
  display: none;
  border-top: 1px solid #e2e8f0;
  background: #ffffff;
  padding: 14px 16px 18px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.08);
}
.mobile-nav-links {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.mobile-nav-item {
  display: block;
  padding: 11px 14px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  color: #334155;
  white-space: nowrap;
}
.mobile-nav-item.active,
.mobile-nav-item:hover {
  background: #eef2ff;
  color: #4f46e5;
}
.mobile-drawer-divider {
  height: 1px;
  background: #f1f5f9;
  margin: 12px 0;
}
.mobile-drawer-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.mobile-auth-row,
.mobile-user-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.flex-1 {
  flex: 1;
  text-align: center;
}
.mobile-full-cta {
  width: 100%;
  text-align: center;
}

@media (max-width: 960px) {
  .gnb-inner {
    padding: 0 16px;
    gap: 12px;
  }
  .desktop-only {
    display: none !important;
  }
  .mobile-menu-btn {
    display: inline-flex;
  }
  .mobile-nav-drawer {
    display: block;
  }
}

@media (max-width: 768px) {
  .brand-sub {
    display: none;
  }
  .kkumz-main-community {
    padding: 18px 12px 48px;
  }
  .auth-modal-card {
    padding: 20px 16px;
  }
  .auth-form-row,
  .quick-demo-grid,
  .role-select-grid {
    grid-template-columns: 1fr;
  }
}

/* Official Corporate Legal Footer */
.kkumz-footer {
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  color: #475569;
  font-size: 12px;
  padding: 32px 24px 36px;
}
.kkumz-footer-inner {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.footer-top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
}
.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.footer-brand strong {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}
.footer-brand span {
  color: #64748b;
  font-size: 12px;
}
.footer-legal-links {
  display: flex;
  align-items: center;
  gap: 8px;
}
.footer-legal-links a {
  color: #475569;
  font-weight: 600;
  white-space: nowrap;
}
.footer-legal-links a:hover {
  color: #0f172a;
  text-decoration: underline;
}
.footer-legal-links .privacy-strong {
  color: #0f172a;
  font-weight: 700;
}
.footer-dot {
  color: #cbd5e1;
}
.footer-corp-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.6;
  color: #64748b;
  word-break: keep-all;
}
.footer-corp-info p {
  margin: 0;
}
.footer-corp-info strong {
  color: #334155;
}
.footer-copy {
  font-size: 11px;
  color: #94a3b8;
}
</style>
