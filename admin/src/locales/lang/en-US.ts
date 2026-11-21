import { genMessage } from '../helper';
import antdLocale from 'ant-design-vue/es/locale/en_US';
import 'dayjs/locale/en';

const modules = import.meta.glob('./en-US/**/*.ts', { eager: true });
export default {
  message: {
    ...genMessage(modules as Recordable<Recordable>, 'en-US'),
    antdLocale,
  },
  dateLocale: null,
  dateLocaleName: 'en',
};
