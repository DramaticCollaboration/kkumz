<template>
    <a-time-range-picker v-model:value="rangeValue" @change="handleChange" :placeholder="placeholder" :valueFormat="format" :format="format"/>
</template>

<script>
    import { defineComponent, ref, watch } from 'vue';
    import { propTypes } from '/@/utils/propTypes';
    import { Form } from 'ant-design-vue';

    const placeholder = ['시작 시간', '종료 시간']
    /**
     * 시간-시간 구성요소About 범위 쿼리
     */
    export default defineComponent({
        name: "JRangeTime",
        props:{
            value: propTypes.string.def(''),
            format: propTypes.string.def('HH:mm:ss'),
            placeholder: propTypes.string.def(''),
        },
        emits:['change', 'update:value'],
        setup(props, {emit}){
            const rangeValue = ref([])
            const formItemContext = Form.useInjectFormItemContext();

            watch(()=>props.value, (val)=>{
                if(val){
                    rangeValue.value = val.split(',')
                }else{
                    rangeValue.value = []
                }
            }, {immediate: true});


            function handleChange(arr){
                let str = ''
                if(arr && arr.length>0){
                    if(arr[1] && arr[0]){
                        str = arr.join(',')
                    }
                }
                emit('change', str);
                emit('update:value', str);
                formItemContext.onFieldChange();
            }
            return {
                rangeValue,
                placeholder,
                handleChange
            }
        }
    });
</script>