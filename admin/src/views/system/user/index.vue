<!--
  @component SystemUserManagement
  @description System user account and role mapping management view
  @features
    - User account lifecycle: create, edit, freeze (Status=2), permanent delete
    - Password reset with salted SHA-256 hashing policy
    - N:M user-role and department assignment modal
-->
<template>
  <div>
    <!--참고표-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> {{ t('appUi.k1ac11e9b') }}</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" :disabled="isDisabledAuth('system:user:export')"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
        <a-button type="primary" @click="openModal(true, {})" preIcon="ant-design:hdd-outlined"> {{ t('appUi.kfc81e222') }}</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                {{ t('appUi.kfc81e222') }}
              </a-menu-item>
              <a-menu-item key="2" @click="batchFrozen(2)">
                <Icon icon="ant-design:lock-outlined"></Icon>
                {{ t('appUi.k8ff58636') }}
              </a-menu-item>
              <a-menu-item key="3" @click="batchFrozen(1)">
                <Icon icon="ant-design:unlock-outlined"></Icon>
                {{ t('appUi.kbbf831ad') }}
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >{{ t('appUi.k39eb4aed') }}
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--액션바-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!--사용자 서랍-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--비밀번호 변경-->
    <PasswordModal @register="registerPasswordModal" @success="reload" />
    <!--사용자 에이전트-->
    <UserAgentModal @register="registerAgentModal" @success="reload" />
    <!--삭제-->
    <UserRecycleBinModal @register="registerModal" @success="reload" />
    <!-- 사직접수 팝업창 -->
    <UserQuitAgentModal @register="registerQuitAgentModal" @success="reload" />
    <!-- 퇴직자 목록 팝업창 -->
    <UserQuitModal @register="registerQuitModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="system-user" setup>
  //TS 구문
  import { ref, computed, unref } from 'vue';
  import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
  import UserDrawer from './UserDrawer.vue';
  import UserRecycleBinModal from './UserRecycleBinModal.vue';
  import PasswordModal from './PasswordModal.vue';
  import UserAgentModal from './UserAgentModal.vue';
  import UserQuitAgentModal from './UserQuitAgentModal.vue';
  import UserQuitModal from './UserQuitModal.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './user.data';
  import { list, deleteUser, batchDeleteUser, getImportUrl, getExportUrl, frozenBatch } from './user.api';
  import {usePermission} from "/@/hooks/web/usePermission";
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage, createConfirm } = useMessage();
  const { isDisabledAuth } = usePermission();
  //서랍 등록
  const [registerDrawer, { openDrawer }] = useDrawer();
  //삭제 모델
  const [registerModal, { openModal }] = useModal();
  //비밀번호 모델
  const [registerPasswordModal, { openModal: openPasswordModal }] = useModal();
  //에이전트 모델
  const [registerAgentModal, { openModal: openAgentModal }] = useModal();
  //사임한 에이전트 모델
  const [registerQuitAgentModal, { openModal: openQuitAgentModal }] = useModal();
  //사임한 사용자 목록 모델
  const [registerQuitModal, { openModal: openQuitModal }] = useModal();

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'user-list',
    tableProps: {
      title: t('appUi.k874f9343'),
      api: list,
      columns: columns,
      size: 'small',
      formConfig: {
        // labelWidth: 200,
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      beforeFetch: (params) => {
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      },
    },
    exportConfig: {
      name: t('appUi.k874f9343'),
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //테이블 데이터 등록
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  /**
   * 새로운 이벤트
   */
  function handleCreate() {
    openDrawer(true, {
      isUpdate: false,
      showFooter: true,
    });
  }
  /**
   * 이벤트 편집
   */
  async function handleEdit(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }
  /**
   *세부
   */
  async function handleDetail(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }
  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    if ('admin' == record.username) {
      createMessage.warning(t('appUi.k3606bf41'));
      return;
    }
    await deleteUser({ id: record.id }, reload);
  }
  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    let hasAdmin = unref(selectedRows).filter((item) => item.username == 'admin');
    if (unref(hasAdmin).length > 0) {
      createMessage.warning(t('appUi.k3606bf41'));
      return;
    }
    await batchDeleteUser({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }
  /**
   * 성공적인 콜백
   */
  function handleSuccess() {
    reload();
  }

  /**
   * 비밀번호 변경 팝업창 열기
   */
  function handleChangePassword(username) {
    openPasswordModal(true, { username });
  }
  /**
   * 에이전트 팝업창 열기
   */
  function handleAgentSettings(userName) {
    openAgentModal(true, { userName });
  }
  /**
   * 냉동 및 해동
   */
  async function handleFrozen(record, status) {
    if ('admin' == record.username) {
      createMessage.warning(t('appUi.k3606bf41'));
      return;
    }
    await frozenBatch({ ids: record.id, status: status }, reload);
  }
  /**
   * 일괄 동결 및 해동
   */
  function batchFrozen(status) {
    let hasAdmin = selectedRows.value.filter((item) => item.username == 'admin');
    if (unref(hasAdmin).length > 0) {
      createMessage.warning(t('appUi.k3606bf41'));
      return;
    }
    createConfirm({
      iconType: 'warning',
      title: t('appUi.kf3aa24bb'),
      content: (status == 1 ? t('appUi.kbbf831ad') : t('appUi.k8ff58636')) + t('appUi.k6996dcaf'),
      onOk: async () => {
        await frozenBatch({ ids: unref(selectedRowKeys).join(','), status: status }, reload);
      },
    });
  }


  /**
   * 액션바
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: t('appUi.kd482e14b'),
        onClick: handleEdit.bind(null, record),
        // ifShow: () => hasPermission('system:user:edit'),
      },
    ];
  }
  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: t('appUi.kbb446431'),
        onClick: handleDetail.bind(null, record),
      },
      {
        label: t('appUi.k81973897'),
        //auth: 'user:changepwd',
        onClick: handleChangePassword.bind(null, record.username),
      },
      {
        label: t('appUi.kfc81e222'),
        popConfirm: {
          title: t('appUi.ka6e94ce1'),
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: t('appUi.k8ff58636'),
        ifShow: record.status == 1,
        popConfirm: {
          title: t('appUi.k612febf2'),
          confirm: handleFrozen.bind(null, record, 2),
        },
      },
      {
        label: t('appUi.kbbf831ad'),
        ifShow: record.status == 2,
        popConfirm: {
          title: t('appUi.kb81bca0a'),
          confirm: handleFrozen.bind(null, record, 1),
        },
      },
    ];
  }

  /**
   * 사임
   * @param userName
   */
  function handleQuit(userName) {
    //발신상담원 팝업창 열기
    openQuitAgentModal(true, { userName });
  }
</script>

<style scoped></style>
