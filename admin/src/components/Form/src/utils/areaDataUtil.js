import REGION_DATA from 'china-area-data';
import { cloneDeep } from 'lodash-es';

// 코드를 한자 대형 객체로 변환
const CodeToText = {};
// 한자를 코드 대형 객체로 변환
const TextToCode = {};
const provinceObject = REGION_DATA['86']; // 지방 객체
const regionData = [];
let provinceAndCityData = [];

CodeToText[''] = '모두';

// 지방을 계산하다
for (const prop in provinceObject) {
  regionData.push({
    value: prop, // 지방 코드 값
    label: provinceObject[prop], // 지방 한자
  });
  CodeToText[prop] = provinceObject[prop];
  TextToCode[provinceObject[prop]] = {
    code: prop,
  };
  TextToCode[provinceObject[prop]]['모두'] = {
    code: '',
  };
}
// 시장 계산
for (let i = 0, len = regionData.length; i < len; i++) {
  const provinceCode = regionData[i].value;
  const provinceText = regionData[i].label;
  const provinceChildren = [];
  for (const prop in REGION_DATA[provinceCode]) {
    provinceChildren.push({
      value: prop,
      label: REGION_DATA[provinceCode][prop],
    });
    CodeToText[prop] = REGION_DATA[provinceCode][prop];
    TextToCode[provinceText][REGION_DATA[provinceCode][prop]] = {
      code: prop,
    };
    TextToCode[provinceText][REGION_DATA[provinceCode][prop]]['모두'] = {
      code: '',
    };
  }
  if (provinceChildren.length) {
    regionData[i].children = provinceChildren;
  }
}
provinceAndCityData = cloneDeep(regionData);

// 계산 영역
for (let i = 0, len = regionData.length; i < len; i++) {
  const province = regionData[i].children;
  const provinceText = regionData[i].label;
  if (province) {
    for (let j = 0, len = province.length; j < len; j++) {
      const cityCode = province[j].value;
      const cityText = province[j].label;
      const cityChildren = [];
      for (const prop in REGION_DATA[cityCode]) {
        cityChildren.push({
          value: prop,
          label: REGION_DATA[cityCode][prop],
        });
        CodeToText[prop] = REGION_DATA[cityCode][prop];
        TextToCode[provinceText][cityText][REGION_DATA[cityCode][prop]] = {
          code: prop,
        };
      }
      if (cityChildren.length) {
        province[j].children = cityChildren;
      }
    }
  }
}

// "모두" 옵션 추가
const provinceAndCityDataPlus = cloneDeep(provinceAndCityData);
provinceAndCityDataPlus.unshift({
  value: '',
  label: '모두',
});
for (let i = 0, len = provinceAndCityDataPlus.length; i < len; i++) {
  const province = provinceAndCityDataPlus[i].children;
  if (province && province.length) {
    province.unshift({
      value: '',
      label: '모두',
    });
    for (let j = 0, len = province.length; j < len; j++) {
      const city = province[j].children;
      if (city && city.length) {
        city.unshift({
          value: '',
          label: '모두',
        });
      }
    }
  }
}

const regionDataPlus = cloneDeep(regionData);
regionDataPlus.unshift({
  value: '',
  label: '모두',
});
for (let i = 0, len = regionDataPlus.length; i < len; i++) {
  const province = regionDataPlus[i].children;
  if (province && province.length) {
    province.unshift({
      value: '',
      label: '모두',
    });

    for (let j = 0, len = province.length; j < len; j++) {
      const city = province[j].children;
      if (city && city.length) {
        city.unshift({
          value: '',
          label: '모두',
        });
      }
    }
  }
}
//지방 데이터
const provinceOptions = [];
for (const prop in provinceObject) {
  provinceOptions.push({
    value: prop, // 지방 코드 값
    label: provinceObject[prop], // 지방 한자
  });
}
/**
 * 코드를 기반으로 드롭다운 옵션의 데이터 가져오기
 * @param code
 * @returns []
 */
function getDataByCode(code) {
  let data = [];
  for (const prop in REGION_DATA[code]) {
    data.push({
      value: prop, // 지방 코드 값
      label: REGION_DATA[code][prop], // 지방 한자
    });
  }
  return data;
}

/**
 * 모든 지방과 도시의 레벨을 확인하세요
 * @type {Array}
 */
const pca = [];
Object.keys(provinceObject).map((province) => {
  pca.push({ id: province, text: provinceObject[province], pid: '86', index: 1 });
  const cityObject = REGION_DATA[province];
  Object.keys(cityObject).map((city) => {
    pca.push({ id: city, text: cityObject[city], pid: province, index: 2 });
    const areaObject = REGION_DATA[city];
    if (areaObject) {
      Object.keys(areaObject).map((area) => {
        pca.push({ id: area, text: areaObject[area], pid: city, index: 3 });
      });
    }
  });
});

/**
 * 코드를 기반으로 한 반전 값
 * @param code
 * @param level
 * @returns {Array}
 */
function getRealCode(code, level) {
  let arr = [];
  getPcode(code, arr, level);
  return arr;
}
function getPcode(id, arr, index) {
  for (let item of pca) {
    if (item.id === id && item.index == index) {
      arr.unshift(id);
      if (item.pid != '86') {
        getPcode(item.pid, arr, --index);
      }
    }
  }
}
export { provinceAndCityData, regionData, provinceAndCityDataPlus, regionDataPlus, getDataByCode, provinceOptions, getRealCode };
