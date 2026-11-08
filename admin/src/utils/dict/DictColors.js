const whiteColor = '#ffffff'
const slateTextColor = '#334155'

export const Colors = [
  // Enterprise Semantic & Neutral Solid Palette
  ['#475569', whiteColor],
  ['#0284C7', whiteColor],
  ['#0D9488', whiteColor],
  ['#10B981', whiteColor],
  ['#F59E0B', whiteColor],
  ['#EF4444', whiteColor],
  ['#334155', whiteColor],
  ['#1E293B', whiteColor],
  ['#64748B', whiteColor],
  ['#0F172A', whiteColor],
  // Enterprise Low-Saturation Surface Palette
  ['#F1F5F9', slateTextColor],
  ['#E0F2FE', '#0369A1'],
  ['#CCFBF1', '#0F766E'],
  ['#D1FAE5', '#047857'],
  ['#FEF3C7', '#B45309'],
  ['#FFE4E6', '#BE123C'],
  ['#E2E8F0', slateTextColor],
  ['#F8FAFC', slateTextColor],
  ['#CBD5E1', '#1E293B'],
  ['#E5E7EB', '#374151'],
]

export const NONE_COLOR = ['#f1f5f9', slateTextColor]

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
