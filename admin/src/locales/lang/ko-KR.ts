import { genMessage } from '../helper';
import antdLocale from 'ant-design-vue/es/locale/ko_KR';
import 'dayjs/locale/ko';

const modules = import.meta.glob('./ko-KR/**/*.ts', { eager: true });
export default {
  message: {
    ...genMessage(modules as Recordable<Recordable>, 'ko-KR'),
    antdLocale,
  },
  dateLocale: null,
  dateLocaleName: 'ko',
};
