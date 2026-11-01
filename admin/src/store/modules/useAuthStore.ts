/**
 * @fileoverview 세부 인증 상태 및 SSO 세션 토큰 연동 스토어
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  // State
  const userId = ref<string | null>(null);
  const department = ref<string | null>(null);
  const userRole = ref<'GOVERNANCE_ADMIN' | 'SECURITY_ADMIN' | 'IT_OPERATOR' | null>(null);

  // Getters
  const isExecutiveView = computed(() => userRole.value === 'GOVERNANCE_ADMIN' || userRole.value === 'SECURITY_ADMIN');
  const isOperatorView = computed(() => userRole.value === 'IT_OPERATOR');

  // Actions
  const setAuthData = (id: string, dept: string, role: typeof userRole.value) => {
    userId.value = id; department.value = dept; userRole.value = role;
  };

  return { userId, department, userRole, isExecutiveView, isOperatorView, setAuthData };
});
