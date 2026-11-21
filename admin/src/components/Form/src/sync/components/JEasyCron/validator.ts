import { $t } from '/@/locales';
import CronParser from 'cron-parser';
import type { ValidatorRule } from 'ant-design-vue/lib/form/interface';

const cronRule: ValidatorRule = {
  validator({}, value) {
    // 기재하지 않으시면 확인이 되지 않습니다.
    if (!value) {
      return Promise.resolve();
    }
    const values: string[] = value.split(' ').filter((item) => !!item);
    if (values.length > 7) {
      return Promise.reject($t('appUi.k412f228f'));
    }
    // 항목 7을 확인하세요.
    let val: string = value;
    if (values.length === 7) {
      const year = values[6];
      if (year !== '*' && year !== '?') {
        let yearValues: string[] = [];
        if (year.indexOf('-') >= 0) {
          yearValues = year.split('-');
        } else if (year.indexOf('/')) {
          yearValues = year.split('/');
        } else {
          yearValues = [year];
        }
        // 모두 숫자인지 확인
        const checkYear = yearValues.some((item) => isNaN(Number(item)));
        if (checkYear) {
          return Promise.reject($t('appUi.k9677108b') + year);
        }
      }
      // 처음 6개 항목을 가져옵니다.
      val = values.slice(0, 6).join(' ');
    }
    // 연도 없이 6자리
    // 5자리에는 초나 연도가 없습니다.
    try {
      const iter = CronParser.parseExpression(val);
      iter.next();
      return Promise.resolve();
    } catch (e) {
      return Promise.reject($t('appUi.k88043a4b') + e);
    }
  },
};

export default cronRule.validator;
