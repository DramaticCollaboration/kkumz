<template>
  <div ref="chartRef" :style="{ height, width }"></div>
</template>
<script lang="ts">
  import { useI18n } from '/@/hooks/web/useI18n';
  import { defineComponent, PropType, ref, Ref, reactive, watchEffect } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { cloneDeep } from 'lodash-es';
  export default defineComponent({
    name: 'Radar',
    props: {
      chartData: {
        type: Array,
        default: () => [],
      },
      option: {
        type: Object,
        default: () => ({}),
      },
      width: {
        type: String as PropType<string>,
        default: '100%',
      },
      height: {
        type: String as PropType<string>,
        default: 'calc(100vh - 78px)',
      },
    },
    setup(props) {
      const { t } = useI18n();
      const chartRef = ref<HTMLDivElement | null>(null);
      const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);
      const option = reactive({
        title: {
          text: t('appUi.k9745b355'),
        },
        legend: {
          data: [t('appUi.ka64d3c0d')],
        },
        radar: {
          indicator: [{ name: t('appUi.keb62b8f9') }, { name: t('appUi.kc795eca5') }, { name: t('appUi.k59c9dbcf') }, { name: t('appUi.ka0fceb1f') }, { name: t('appUi.k95d65a88') }, { name: t('appUi.k36c79575') }],
        },
        series: [
          {
            type: 'radar' as 'custom',
            data: [
              {
                value: [82, 70, 60, 55, 90, 66],
                name: t('appUi.ka64d3c0d'),
              },
            ],
          },
        ],
      });

      watchEffect(() => {
        props.chartData && initCharts();
      });

      function initCharts() {
        if (props.option) {
          Object.assign(option, cloneDeep(props.option));
        }
        //범례 유형
        let typeArr = Array.from(new Set(props.chartData.map((item) => item.type)));
        //레이더 데이터
        let indicator = Array.from(
          new Set(
            props.chartData.map((item) => {
              let { name, max } = item;
              return { name, max };
            })
          )
        );

        let data = [];
        typeArr.forEach((type) => {
          let obj = { name: type };
          let chartArr = props.chartData.filter((item) => type === item.type);
          obj['value'] = chartArr.map((item) => item.value);
          //데이터데이터
          data.push(obj);
        });
        option.radar.axisName = indicator;
        option.series[0]['data'] = data;
        setOptions(option);
      }
      return { chartRef };
    },
  });
</script>
