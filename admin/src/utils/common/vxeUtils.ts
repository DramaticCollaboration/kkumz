import { getValueType } from '/@/utils';

export const VALIDATE_FAILED = Symbol();
/**
 * 기본 Form과 모든 하위 Form을 한 번에 확인(새 버전)
 * @param Form 기본 Form Form 개체
 * @param 케이스는 배열을 받으며, 각 항목은 JEditableTable 인스턴스입니다.
 * @returns {Promise<any>}
 */
export async function validateFormModelAndTables(validate, formData, cases, props, autoJumpTab?) {
  if (!(validate && typeof validate === 'function')) {
    throw `validate 매개변수에는 메소드가 필요하지만 전달되는 내용 ${typeof validate}`;
  }
  let dataMap = {};
  let values = await new Promise((resolve, reject) => {
    // 기본 Form 확인
    validate()
      .then(() => {
        for (let data in formData) {
          //데이터가 배열인 경우
          if (formData[data] instanceof Array) {
            let valueType = getValueType(props, data);
            //문자열 유형인 경우 쉼표로 구분된 문자열로 변환해야 합니다.
            if (valueType === 'string') {
              formData[data] = formData[data].join(',');
            }
          }
        }
        resolve(formData);
      })
      .catch(() => {
        reject({ error: VALIDATE_FAILED, index: 0 });
      });
  });
  Object.assign(dataMap, { formValue: values });
  // 모든 하위 테이블의 유효성을 검사하는 Form
  let subData = await validateTables(cases, autoJumpTab);
  // 최종 데이터 병합
  dataMap = Object.assign(dataMap, { tablesValue: subData });
  return dataMap;
}
/**
 * 하나 이상의 테이블의 모든 값을 검증하고 가져옵니다.
 * @param 케이스는 배열을 받으며, 각 항목은 JEditableTable 인스턴스입니다.
 * @param autoJumpTab은 오류가 보고된 탭으로 자동으로 이동합니까?
 */
export function validateTables(cases, autoJumpTab = true) {
  if (!(cases instanceof Array)) {
    throw `'validateTables'기능성'cases'매개변수에 배열이 필요하지만 $가 전달되었습니다.{typeof cases}`;
  }
  return new Promise((resolve, reject) => {
    let tablesData: any = [];
    let index = 0;
    if (!cases || cases.length === 0) {
      resolve(tablesData);
    }
    (function next() {
      let vm = cases[index];
      vm.value.validateTable().then((errMap) => {
        // 확인 통과
        if (!errMap) {
          tablesData[index] = { tableData: vm.value.getTableData() };
          // 검증이 완료되었는지 확인하고, 완료되면 성공을 반환하고, 그렇지 않으면 다음 검증 단계를 계속합니다.
          if (++index === cases.length) {
            resolve(tablesData);
          } else next();
        } else {
          // tabKey를 가져오세요. ATab 컴포넌트 내에 있으면 가져올 수 있습니다.
          let paneKey;
          let tabPane = getVmParentByName(vm.value, 'ATabPane');
          if (tabPane) {
            paneKey = tabPane.$.vnode.key;
            // 자동으로 이 Form으로 이동
            if (autoJumpTab) {
              let tabs = getVmParentByName(tabPane, 'Tabs');
              tabs && tabs.setActiveKey && tabs.setActiveKey(paneKey);
            }
          }
          // 확인되지 않은 Form이 나타나면 다음 확인 단계가 수행되지 않고 바로 실패가 반환됩니다.
          reject({ error: VALIDATE_FAILED, index, paneKey, errMap, subIndex: index });
        }
      });
    })();
  });
}

export function getVmParentByName(vm, name) {
  let parent = vm.$parent;
  if (parent && parent.$options) {
    if (parent.$options.name === name) {
      return parent;
    } else {
      let res = getVmParentByName(parent, name);
      if (res) {
        return res;
      }
    }
  }
  return null;
}
