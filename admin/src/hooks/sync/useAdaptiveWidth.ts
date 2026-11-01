/**
 * 적응형 너비 생성자
 *
 * @time 2022-4-8
 * @author Empasy
 */
import { ref } from 'vue';
import { useDebounceFn, tryOnUnmounted } from '@vueuse/core';
import { useEventListener } from '/@/hooks/event/useEventListener';

// key = js 연산자 + 숫자
const defWidthConfig: configType = {
  '<=565': '100%',
  '<=1366': '800px',
  '<=1600': '600px',
  '<=1920': '600px',
  '>1920': '500px',
};

type configType = Record<string, string | number>;

/**
 * 적응 폭
 *
 * @param widthConfig 너비 구성은 defWidthConfig 구성을 참조하세요.
 * @param 기본 구성을 병합할지 여부 지정
 * @param 디바운스 디바운스 밀리초
 */
export function useAdaptiveWidth(widthConfig = defWidthConfig, assign = true, debounce = 50) {
  const widthConfigAssign = assign ? Object.assign({}, defWidthConfig, widthConfig) : widthConfig;
  const configKeys = Object.keys(widthConfigAssign);

  const adaptiveWidth = ref<string | number>();

  /**
   * 너비 계산
   * @param innerWidth
   */
  function calcWidth(innerWidth) {
    let width;
    for (const key of configKeys) {
      try {
        // js 작업을 통해
        let flag = new Function(`return ${innerWidth} ${key}`)();
        if (flag) {
          width = widthConfigAssign[key];
          break;
        }
      } catch (e) {
        console.error(e);
      }
    }
    if (width) {
      adaptiveWidth.value = width;
    } else {
      console.warn('일치하는 적응 너비가 없습니다.');
    }
  }

  // 초기 계산
  calcWidth(window.innerWidth);

  // 크기 조정 이벤트 듣기
  const { removeEvent } = useEventListener({
    el: window,
    name: 'resize',
    listener: useDebounceFn(() => calcWidth(window.innerWidth), debounce),
  });
  // 구성요소 제거 시 청취 이벤트 취소
  tryOnUnmounted(() => removeEvent());

  return { adaptiveWidth };
}

/**
 * 서랍 적응 폭
 */
export function useDrawerAdaptiveWidth() {
  return useAdaptiveWidth(
    {
      '<=620': '100%',
      '<=1600': 600,
      '<=1920': 650,
      '>1920': 700,
    },
    false
  );
}
