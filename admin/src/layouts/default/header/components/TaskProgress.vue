<template>
  <div class="px-3 flex items-center cursor-pointer" @click="togglePopover">
    <a-popover v-model:open="visible" title="백그라운드 작업 현황" trigger="click" placement="bottomRight">
      <template #content>
        <div class="w-80 p-2">
          <!-- 백그라운드 비동기 작업 진행률 및 상태 렌더링 -->
          <div v-if="tasks.length === 0" class="text-center text-gray-500 py-4">
            진행 중인 작업이 없습니다.
          </div>
          <div v-else>
            <div v-for="task in tasks" :key="task.id" class="mb-4 last:mb-0">
              <div class="flex justify-between text-xs mb-1">
                <span class="font-bold truncate" style="max-width: 200px">{{ task.name }}</span>
                <span :class="task.status === 'ERROR' ? 'text-red-500' : 'text-blue-500'">{{ task.progress }}%</span>
              </div>
              <a-progress 
                :percent="task.progress" 
                :status="task.status === 'ERROR' ? 'exception' : (task.progress === 100 ? 'success' : 'active')" 
                size="small" 
                :showInfo="false" 
              />
              <div class="text-xs text-gray-400 mt-1 truncate">{{ task.description }}</div>
            </div>
          </div>
        </div>
      </template>
      
      <a-badge :count="activeTasksCount" dot>
        <Icon icon="ant-design:cloud-sync-outlined" :class="{ 'animate-spin': activeTasksCount > 0 }" />
      </a-badge>
    </a-popover>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted, onUnmounted } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { usePolling } from '/@/hooks/web/usePolling';
  // import { getBackgroundTasks } from '/@/api/sys/task'; // 가정된 API

  const visible = ref(false);
  const tasks = ref<any[]>([]);

  const activeTasksCount = computed(() => {
    return tasks.value.filter(t => t.progress < 100 && t.status !== 'ERROR').length;
  });

  const togglePopover = () => {
    visible.value = !visible.value;
  };

  import { defHttp } from '/@/utils/http/axios';

  // 백그라운드 활성 태스크 주기적 갱신
  const fetchTasks = async () => {
    try {
      const res = await defHttp.get<any>({ url: '/api/v1/kkumz/automation/logs', params: { pageNo: 1, pageSize: 5 } }, { isPolling: true });
      const records = Array.isArray(res) ? res : (res?.records || []);
      tasks.value = records;
    } catch (e) {
      tasks.value = [];
    }
  };

  usePolling(fetchTasks, 30000); // 30초 단위로 조회
</script>
