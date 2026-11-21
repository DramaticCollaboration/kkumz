import { $t } from '/@/locales';
import { VxeTablePropTypes } from 'vxe-table';
import { isArray } from '/@/utils/is';
import { HandleArgs } from './useColumns';
import { replaceProps } from '../utils/enhancedUtils';

export function useValidateRules(args: HandleArgs) {
  const { data } = args;
  const col = args.col!;
  let rules: VxeTablePropTypes.EditRules[] = [];
  if (isArray(col.validateRules)) {
    for (let rule of col.validateRules) {
      let replace = {
        message: replaceProps(col, rule.message),
      };
      if (rule.unique || rule.pattern === 'only') {
        // 고유한 유효성 검사기
        rule.validator = uniqueValidator(args);
      } else if (rule.pattern) {
        // 비어 있지 않음
        if (rule.pattern === fooPatterns[0].value) {
          rule.required = true;
          delete rule.pattern;
        } else {
          // 온라인 Form과의 호환성을 위한 특별 규칙
          for (let foo of fooPatterns) {
            if (foo.value === rule.pattern) {
              rule.pattern = foo.pattern;
              break;
            }
          }
        }
      } else if (typeof rule.handler === 'function') {
        // 사용자 정의 기능 확인
        rule.validator = handlerConvertToValidator;
      }
      rules.push(Object.assign({}, rule, replace));
    }
  }
  data.innerEditRules[col.key] = rules;
}

/** 고유한 유효성 검사기 */
function uniqueValidator({ methods }: HandleArgs) {
  return function (event) {
    const { cellValue, column, rule } = event;
    if (cellValue == '') return Promise.resolve();
    let tableData = methods.getTableData();
    let findCount = 0;
    for (let rowData of tableData) {
      if (rowData[column.params.key] === cellValue) {
        if (++findCount >= 2) {
          return Promise.reject(new Error(rule.message));
        }
      }
    }
    return Promise.resolve();
  };
}

/** 이전 버전의 핸들러가 새 버전의 Validator로 변환됩니다. */
function handlerConvertToValidator(event) {
  const { column, rule } = event;
  return new Promise((resolve, reject) => {
    rule.handler(event, (flag, msg) => {
      let message = rule.message;
      if (typeof msg === 'string') {
        message = replaceProps(column.params, msg);
      }
      if (flag == null) {
        resolve(message);
      } else if (!!flag) {
        resolve(message);
      } else {
        reject(new Error(message));
      }
    });
  });
}

// 온라인 규칙과 호환 가능
const fooPatterns = [
  { title: $t('appUi.k20a9ceb7'), value: '*', pattern: /^.+$/ },
  { title: $t('appUi.k788c6b9d'), value: 'n6-16', pattern: /^\d{6,16}$/ },
  { title: $t('appUi.k329d2603'), value: '*6-16', pattern: /^.{6,16}$/ },
  { title: $t('appUi.kab45087b'), value: 's6-18', pattern: /^[a-z|A-Z]{6,18}$/ },
  {
    title: 'URL',
    value: 'url',
    pattern: /^((ht|f)tps?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/,
  },
  { title: $t('appUi.k3c37764a'), value: 'e', pattern: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/ },
  { title: $t('appUi.kfbf7b5bb'), value: 'm', pattern: /^010([-\s]?)([0-9]{4})([-\s]?)([0-9]{4})$/ },
  { title: $t('appUi.kcd21379e'), value: 'p', pattern: /^\d{6}$/ },
  { title: $t('appUi.k53bf9898'), value: 's', pattern: /^[A-Z|a-z]+$/ },
  { title: $t('appUi.kdd588621'), value: 'n', pattern: /^-?\d+(\.?\d+|\d?)$/ },
  { title: $t('appUi.kf0729903'), value: 'z', pattern: /^-?\d+$/ },
  {
    title: $t('appUi.k2ce295bd'),
    value: 'money',
    pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,5}))$/,
  },
];
