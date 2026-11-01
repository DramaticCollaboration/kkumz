import { ref, onMounted, onUnmounted } from 'vue';

/**
 * 범용 1분 주기 Polling Hook
 * 컴포넌트 마운트 시 즉시 실행 후 interval 단위로 콜백을 수행합니다.
 * 컴포넌트 언마운트 시 자동으로 interval을 해제합니다.
 * 
 * @param callback Polling 시 실행할 비동기 함수
 * @param intervalMs 주기 (기본값: 60000ms = 1분)
 */
export function usePolling(callback: () => Promise<void> | void, intervalMs: number = 60000) {
  const isPolling = ref(false);
  let timerId: number | null = null;

  const startPolling = async () => {
    if (isPolling.value) return;
    isPolling.value = true;
    
    // 첫 실행
    try {
      await callback();
    } catch (error) {
      console.error('Polling error on start:', error);
    }
    
    // 주기적 실행
    timerId = window.setInterval(async () => {
      try {
        await callback();
      } catch (error) {
        console.error('Polling error:', error);
      }
    }, intervalMs);
  };

  const stopPolling = () => {
    if (timerId !== null) {
      window.clearInterval(timerId);
      timerId = null;
    }
    isPolling.value = false;
  };

  onMounted(() => {
    startPolling();
  });

  onUnmounted(() => {
    stopPolling();
  });

  return {
    isPolling,
    startPolling,
    stopPolling
  };
}
