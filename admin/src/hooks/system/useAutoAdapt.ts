import { ref } from 'vue';
import { ScreenSizeEnum } from '/@/enums/sizeEnum';
import { useWindowSizeFn } from '/@/hooks/event/useWindowSizeFn';
// useAdapt 메소드 매개변수 정의
interface AdaptOptions {
  // xl>1200
  xl?: string | number;
  // xl>992
  lg?: string | number;
  // xl>768
  md?: string | number;
  // xl>576
  sm?: string | number;
  // xl>480
  xs?: string | number;
  //xl<기본값 480
  mindef?: string | number;
  //기본값
  def?: string | number;
}
export function useAdapt(props?: AdaptOptions) {
  //기본 너비
  const width = ref<string | number>(props?.def || '600px');
  //너비 얻기
  useWindowSizeFn(calcWidth, 100, { immediate: true });
  //너비 계산
  function calcWidth() {
    let windowWidth = document.documentElement.clientWidth;
    switch (true) {
      case windowWidth > ScreenSizeEnum.XL:
        width.value = props?.xl || '600px';
        break;
      case windowWidth > ScreenSizeEnum.LG:
        width.value = props?.lg || '600px';
        break;
      case windowWidth > ScreenSizeEnum.MD:
        width.value = props?.md || '600px';
        break;
      case windowWidth > ScreenSizeEnum.SM:
        width.value = props?.sm || '500px';
        break;
      case windowWidth > ScreenSizeEnum.XS:
        width.value = props?.xs || '400px';
        break;
      default:
        width.value = props?.mindef || '300px';
        break;
    }
  }
  return { width, calcWidth };
}
