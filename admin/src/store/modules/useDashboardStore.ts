/**
 * @fileoverview 대시보드 위젯 사용자 개인화(위치, 크기, 활성화 여부) 및 기간 필터 전역 상태
 */
import { defineStore } from 'pinia';
import { reactive, ref } from 'vue';

export interface GlobalFilters {
  period: 'Q1' | 'Q2' | 'Q3' | 'Q4' | 'CUSTOM';
  dateRange: { start: string; end: string };
  sensitivityGrade: 'ALL' | 'GRADE_1' | 'GRADE_2'; // 1/2등급 필터
  privacyType: string[]; // ['PRV_SSN', 'PRV_IPIN'] 등
}

export interface DrillDownContext {
  isActive: boolean;
  sourceWidget: 'COMPLIANCE_GAUGE' | 'TOP5_RANKING' | 'TODO_LIST' | null;
  targetSystemId: string | null;
  targetPrivacyType: string | null;
}

export const useDashboardStore = defineStore('dashboard', () => {
  // State
  const filters = reactive<GlobalFilters>({
    period: 'Q1', dateRange: { start: '', end: '' }, sensitivityGrade: 'ALL', privacyType: []
  });
  const drillDownCtx = reactive<DrillDownContext>({
    isActive: false, sourceWidget: null, targetSystemId: null, targetPrivacyType: null
  });

  // Actions
  const updateFilters = (newFilters: Partial<GlobalFilters>) => { Object.assign(filters, newFilters); };
  const triggerDrillDown = (widget: DrillDownContext['sourceWidget'], systemId?: string) => {
    drillDownCtx.isActive = true;
    drillDownCtx.sourceWidget = widget;
    drillDownCtx.targetSystemId = systemId || null;
  };
  const clearDrillDown = () => { drillDownCtx.isActive = false; };

  return { filters, drillDownCtx, updateFilters, triggerDrillDown, clearDrillDown };
});
