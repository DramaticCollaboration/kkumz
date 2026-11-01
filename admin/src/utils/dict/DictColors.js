const whiteColor = '#ffffff'
const blackColor = '#666666'

export const Colors = [
  // 배경색, 글자색
  ['#2196F3', whiteColor],
  ['#08C9C9', whiteColor],
  ['#00C345', whiteColor],
  ['#FAD714', whiteColor],
  ['#FF9300', whiteColor],
  ['#F52222', whiteColor],
  ['#EB2F96', whiteColor],
  ['#7500EA', whiteColor],
  ['#2D46C4', whiteColor],
  ['#484848', whiteColor],
  // --------------------
  ['#C9E6FC', blackColor],
  ['#C3F2F2', blackColor],
  ['#C2F1D2', blackColor],
  ['#FEF6C6', blackColor],
  ['#FFE5C2', blackColor],
  ['#FDCACA', blackColor],
  ['#FACDE6', blackColor],
  ['#DEC2FA', blackColor],
  ['#CCD2F1', blackColor],
  ['#D3D3D3', blackColor],
]

export const NONE_COLOR = ['#e9e9e9', blackColor]

/**
 * 색상 반복자를 반환합니다. 각 호출은 색상을 모두 사용하면 처음부터 시작합니다.
 * @param {number} initIndex 초기 색상 인덱스
 * @returns {{getIndex: function, next: function}}
 */
export function getColorIterator(initIndex = 0) {
  let index = initIndex;
  if (index < 0 || index >= Colors.length) {
    index = 0;
  }
  return {
    getIndex: () => index,
    next() {
      const color = Colors[index];
      index = (index + 1) % Colors.length;
      return color;
    },
  }
}

/**
 * 색상을 기반으로 현재 좌표와 색상을 가져옵니다.
 */
export function getItemColor(color) {
  if(!color){
    return NONE_COLOR[1];
  }
  let colorIndex = Colors.findIndex((value)=>{
    return value[0] === color;
  })
  if(colorIndex === -1){
    return NONE_COLOR[1];
  }
  return Colors[colorIndex][1];
}
