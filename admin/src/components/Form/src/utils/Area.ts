import REGION_DATA from 'china-area-data';

/**
 * 면적 속성 모든 유형
 */
interface PlainPca {
  id: string;
  text: string;
  pid: string;
  index: Number;
}

/**
 * 지방 및 지방자치단체 도구 - 목록 지방 및 지방자치단체 컴포넌트의 번역 문제 해결
 */
class Area {
  all: PlainPca[];

  /**
   * 건설자
   * @param express
   */
  constructor(pcaa?) {
    if (!pcaa) {
      pcaa = REGION_DATA;
    }
    let arr: PlainPca[] = [];
    const province = pcaa['86'];
    Object.keys(province).map((key) => {
      arr.push({ id: key, text: province[key], pid: '86', index: 1 });
      const city = pcaa[key];
      Object.keys(city).map((key2) => {
        arr.push({ id: key2, text: city[key2], pid: key, index: 2 });
        const qu = pcaa[key2];
        if (qu) {
          Object.keys(qu).map((key3) => {
            arr.push({ id: key3, text: qu[key3], pid: key2, index: 3 });
          });
        }
      });
    });
    this.all = arr;
  }

  get pca() {
    return this.all;
  }

  getCode(text) {
    if (!text || text.length == 0) {
      return '';
    }
    for (let item of this.all) {
      if (item.text === text) {
        return item.id;
      }
    }
  }
  
  getText(code,index=3) {
    if (!code || code.length == 0) {
      return '';
    }
    let arr = [];
    this.getAreaBycode(code, arr, index);
    return arr.join('/');
  }

  getRealCode(code) {
    let arr = [];
    this.getPcode(code, arr, 3);
    return arr;
  }

  getPcode(id, arr, index) {
    for (let item of this.all) {
      if (item.id === id && item.index == index) {
        arr.unshift(id);
        if (item.pid != '86') {
          this.getPcode(item.pid, arr, --index);
        }
      }
    }
  }

  getAreaBycode(code, arr, index) {
    for (let item of this.all) {
      if (item.id === code && item.index == index) {
        arr.unshift(item.text);
        if (item.pid != '86') {
          this.getAreaBycode(item.pid, arr, --index);
        }
      }
    }
  }
}
const empasyAreaData = new Area();

// 코드를 기반으로 텍스트 찾기
const getAreaTextByCode = function (code) {
  let index = 3;
  if (code && code.includes(',')) {
    index = code.split(",").length;
    code = code.substr(code.lastIndexOf(',') + 1);
  }
  return empasyAreaData.getText(code,index);
};

export { getAreaTextByCode };
