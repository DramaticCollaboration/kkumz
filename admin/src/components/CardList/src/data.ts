import { ref } from 'vue';
//라인 수
export const grid = ref(12);
// 슬라이더 속성
export const useSlider = (min = 6, max = 12) => {
  // 각 행에 숫자 슬라이더 표시
  const getMarks = () => {
    const l = {};
    for (let i = min; i < max + 1; i++) {
      l[i] = {
        style: {
          color: '#fff',
        },
        label: i,
      };
    }
    return l;
  };
  return {
    min,
    max,
    marks: getMarks(),
    step: 1,
  };
};
