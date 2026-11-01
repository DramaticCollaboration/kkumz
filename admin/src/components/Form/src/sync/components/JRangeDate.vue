<template>
    <a-range-picker v-model:value="rangeValue" @change="handleChange" :show-time="datetime" :placeholder="placeholder" :valueFormat="valueFormat"/>
</template>

<script>
    import { defineComponent, ref, watch, computed } from 'vue';
    import { propTypes } from '/@/utils/propTypes';
    import { Form } from 'ant-design-vue';

    const placeholder = ['시작일', '종료일']
    /**
     * 범위 쿼리에 사용됩니다.
     */
    export default defineComponent({
        name: "JRangeDate",
        props:{
            value: propTypes.string.def(''),
            datetime: propTypes.bool.def(false),
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

            const valueFormat = computed(()=>{
                if(props.datetime === true){
                    return 'YYYY-MM-DD HH:mm:ss'
                }else{
                    return 'YYYY-MM-DD'
                }
            });

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
                valueFormat,
                handleChange
            }
        }
    });
</script>

<style scoped>

</style>