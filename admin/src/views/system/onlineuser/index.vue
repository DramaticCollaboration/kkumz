<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--액션바-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="system-online-user" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { columns, searchFormSchema } from './OnlineUser.data';
  import { list, forceLogout } from './OnlineUser.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext } = useListPage({
    designScope: 'online-user',
    tableProps: {
      rowKey: 'token',
      title: '실시간 접속자 목록',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 140,
        title: '작업',
      },
      showIndexColumn: true,
      rowSelection: null,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const { createMessage } = useMessage();

  // 액션바
  function getTableAction(record) {
    return [
      {
        label: '강제 로그아웃',
        color: 'error',
        popConfirm: {
          title: `사용자 [${record.realname || record.username}] 세션을 강제로 종료하시겠습니까?`,
          confirm: handleForce.bind(null, record),
        },
      },
    ];
  }

  /**
   * 강제 로그아웃
   * @param record
   */
  async function handleForce(record) {
    try {
      const res = await forceLogout({ token: record.token });
      if (res && (res.success || res.code === 200)) {
        reload();
        createMessage.success(`사용자 [${record.realname || record.username}] 세션 강제 종료가 완료되었습니다.`);
      } else {
        createMessage.warning(res?.message || '강제 로그아웃 처리에 실패했습니다.');
      }
    } catch (e: any) {
      createMessage.error(e?.message || '강제 로그아웃 처리 중 오류가 발생했습니다.');
    }
  }
</script>

<style scoped>
</style>
