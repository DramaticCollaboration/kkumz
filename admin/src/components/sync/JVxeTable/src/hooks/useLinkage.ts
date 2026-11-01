import { watch } from 'vue';
import { isFunction, isPromise, isArray } from '/@/utils/is';
import { JVxeColumn, JVxeDataProps, JVxeTableProps, JVxeLinkageConfig } from '../types';

/**
 * 다단계 연계
 */
export function useLinkage(props: JVxeTableProps, data: JVxeDataProps, methods) {
  // 다단계 연계 구성 구성
  watch(
    () => props.linkageConfig,
    (linkageConfig: JVxeLinkageConfig[]) => {
      data.innerLinkageConfig.clear();
      if (isArray(linkageConfig) && linkageConfig.length > 0) {
        linkageConfig.forEach((config) => {
          let keys = getLinkageKeys(config.key, []);
          // 여러 키가 하나의 참조 주소를 공유합니다.
          let configItem = {
            ...config,
            keys,
            optionsMap: new Map(),
          };
          keys.forEach((k) => data.innerLinkageConfig.set(k, configItem));
        });
      }
    },
    { immediate: true }
  );

  // 연결 키 시퀀스 가져오기
  function getLinkageKeys(key: string, keys: string[]): string[] {
    let col = props.columns?.find((col: JVxeColumn) => col.key === key) as JVxeColumn;
    if (col) {
      keys.push(col.key);
      // 부하직원을 구합니다
      if (col.linkageKey) {
        return getLinkageKeys(col.linkageKey, keys);
      }
    }
    return keys;
  }

  // 연계 에코 데이터 처리
  function handleLinkageBackData(row) {
    if (data.innerLinkageConfig.size > 0) {
      for (let configItem of data.innerLinkageConfig.values()) {
        autoSetLinkageOptionsByData(row, '', configItem, 0);
      }
    }
  }

  /** 【다단계 연계】형제 연결 드롭다운 옵션 가져오기 */
  function getLinkageOptionsSibling(row, col, config, request) {
    // 현재 컬럼이 최상위 컬럼이 아닌 경우
    let key = '';
    if (col.key !== config.key) {
      // 연결 상위 열을 찾으세요.
      let idx = config.keys.findIndex((k) => col.key === k);
      let parentKey = config.keys[idx - 1];
      key = row[parentKey];
      // 연결된 상위 컬럼에 선택된 데이터가 없으면 빈 배열이 바로 반환됩니다.
      if (key === '' || key == null) {
        return [];
      }
    } else {
      key = 'root';
    }
    let options = config.optionsMap.get(key);
    if (!Array.isArray(options)) {
      if (request) {
        let parent = key === 'root' ? '' : key;
        return getLinkageOptionsAsync(config, parent);
      } else {
        options = [];
      }
    }
    return options;
  }

  /** 【다단계 연계】연결 드롭다운 옵션 가져오기（비동기식） */
  function getLinkageOptionsAsync(config, parent) {
    return new Promise((resolve) => {
      let key = parent ? parent : 'root';
      let options;
      if (config.optionsMap.has(key)) {
        options = config.optionsMap.get(key);
        if (isPromise(options)) {
          options.then((opt) => {
            config.optionsMap.set(key, opt);
            resolve(opt);
          });
        } else {
          resolve(options);
        }
      } else if (isFunction(config.requestData)) {
        // 부모를 전달하여 자식을 가져오려면 requestData 메서드를 호출하세요.
        // noinspection JSVoidFunctionReturnValueUsed,TypeScriptValidateJSTypes
        let promise = config.requestData(parent);
        config.optionsMap.set(key, promise);
        promise.then((opt) => {
          config.optionsMap.set(key, opt);
          resolve(opt);
        });
      } else {
        resolve([]);
      }
    });
  }

  // 【다단계 연계】 데이터를 에코하고 자동으로 optionsMap을 채우는 데 사용됩니다.
  function autoSetLinkageOptionsByData(data, parent, config, level) {
    if (level === 0) {
      getLinkageOptionsAsync(config, '');
    } else {
      getLinkageOptionsAsync(config, parent);
    }
    if (config.keys.length - 1 > level) {
      let value = data[config.keys[level]];
      if (value) {
        autoSetLinkageOptionsByData(data, value, config, level + 1);
      }
    }
  }

  // 【다단계 연계】연결된 컴포넌트가 변경되면 하위 컴포넌트를 삭제합니다.
  function handleLinkageSelectChange(row, col, config, value) {
    if (col.linkageKey) {
      getLinkageOptionsAsync(config, value);
      let idx = config.keys.findIndex((k) => k === col.key);
      let values = {};
      for (let i = idx; i < config.keys.length; i++) {
        values[config.keys[i]] = '';
      }
      // 마지막 몇 열의 데이터를 지웁니다.
      methods.setValues([{ rowKey: row.id, values }]);
    }
  }

  return {
    getLinkageOptionsAsync,
    getLinkageOptionsSibling,
    handleLinkageSelectChange,
    handleLinkageBackData,
  };
}
