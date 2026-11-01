# 보고서 컴포넌트 문서

## 막대 차트

##### 참고방법

```js
import Bar from '/@/components/chart/Bar.vue';
```

##### 매개변수 목록

| 매개변수 이름    | 유형   | 필수의 | 설명       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | 보고서 데이터 소스 |
| width     | number |      | 보고서 너비   |
| height    | number |      | 보고 높이   |

##### 차트데이터 예시

```json
[
  {
    "name": "1월",
    "value": 320
  },
  {
    "name": "2월",
    "value": 457
  },
  {
    "name": "3월",
    "value": 182
  }
]
```

##### 코드 예시

```html
<template>
  <Bar :chartData="chartData"></Bar>
</template>

<script lang="ts" setup>
  import Bar from '/@/components/chart/Bar.vue';
  const chartData = [
    {
      name: '1월',
      value: 320,
    },
    {
      name: '2월',
      value: 457,
    },
    {
      name: '3월',
      value: 182,
    },
  ];
</script>

<style></style>
```

## 다중 열 막대 차트

##### 참고방법

```js
import BarMulti from '/@/components/chart/BarMulti.vue';
```

##### 매개변수 목록

| 매개변수 이름    | 유형   | 필수의 | 설명       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | 보고서 데이터 소스 |
| width     | number |      | 보고서 너비   |
| height    | number |      | 보고 높이   |

##### 차트데이터 예시

```json
[
  {
    "name": "1월",
    "value": 320,
    "type": "2021"
  },
  {
    "name": "2월",
    "value": 457,
    "type": "2021"
  },
  {
    "name": "3월",
    "value": 182,
    "type": "2021"
  },
  {
    "name": "1월",
    "value": 240,
    "type": "2022"
  },
  {
    "name": "2월",
    "value": 357,
    "type": "2022"
  },
  {
    "name": "3월",
    "value": 456,
    "type": "2022"
  }
]
```

## 스파크라인 막대 차트

세로 막대형 차트와 동일하며 구성만 수정하면 됩니다.

## 영역 차트

##### 참고방법

```js
import Line from '/@/components/chart/Line.vue';
```

##### 매개변수 목록

| 매개변수 이름    | 유형   | 필수의 | 설명       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | 보고서 데이터 소스 |
| width     | number |      | 보고서 너비   |
| height    | number |      | 보고 높이   |
| option    | object |      | 구성 항목     |

##### 차트데이터 예시

```json
[
  {
    "name": "1월",
    "value": 320
  },
  {
    "name": "2월",
    "value": 457
  },
  {
    "name": "3월",
    "value": 182
  }
]
```

## 다선형 차트

##### 참고방법

```js
import LineMulti from '/@/components/chart/LineMulti.vue';
```

##### 매개변수 목록

| 매개변수 이름    | 유형   | 필수의 | 설명       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | 보고서 데이터 소스 |
| width     | number |      | 보고서 너비   |
| height    | number |      | 보고 높이   |
| option    | object |      | 구성 항목     |

##### 차트데이터 예시

동일한 세로 막대형 차트

## 원형 차트

##### 참고방법

```js
import Pie from '/@/components/chart/Pie';
```

##### 매개변수 목록

| 매개변수 이름    | 유형   | 필수의 | 설명       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | 보고서 데이터 소스 |
| width     | number |      | 보고서 너비   |
| height    | number |      | 보고 높이   |
| option    | object |      | 구성 항목     |

##### 차트데이터 예시

```json
[
  { "name": "1월", "value": 40 },
  { "name": "2월", "value": 21 },
  { "name": "3월", "value": 17 },
  { "name": "4월", "value": 13 },
  { "name": "5월", "value": 9 }
]
```

## 레이더 차트

##### 참고방법

```js
import Radar from '/@/components/chart/Radar';
```

##### 매개변수 목록

| 매개변수 이름    | 유형   | 필수의 | 설명       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | 보고서 데이터 소스 |
| width     | number |      | 보고서 너비   |
| height    | number |      | 보고 높이   |
| option    | object |      | 구성 항목     |

##### 차트데이터 예시

```json
[
  { "item": "1월", "score": 40 },
  { "item": "2월", "score": 20 },
  { "item": "3월", "score": 67 },
  { "item": "4월", "score": 43 },
  { "item": "5월", "score": 90 }
]
```

## 계기반

##### 참고방법

```js
import Gauge from '/@/components/chart/Gauge';
```

##### 매개변수 목록

| 매개변수 이름    | 유형   | 필수의 | 설명       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | 보고서 데이터 소스 |
| width     | number |      | 보고서 너비   |
| height    | number |      | 보고 높이   |
| option    | object |      | 구성 항목     |

## 순위 목록

##### 참고방법

```js
import RankList from '@/components/chart/RankList';
```

##### 매개변수 목록

| 매개변수 이름 | 유형   | 필수의 | 설명                     |
| ------ | ------ | ---- | ------------------------ |
| title  | string |      | 보고서 제목                 |
| list   | array  |      | 순위 목록 데이터             |
| height | number |      | 보고서 높이, 기본 적응형 높이 |

##### 목록 예

```json
[
  { "name": "서울 조양1호점", "total": 1981 },
  { "name": "서울 조양점 2호점", "total": 1359 },
  { "name": "서울 조양점 3호점", "total": 1354 },
  { "name": "서울 조양점 4호점", "total": 263 },
  { "name": "서울 조양점 5호점", "total": 446 },
  { "name": "서울 조양점 6호점", "total": 796 }
]
```
