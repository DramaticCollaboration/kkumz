import { watch, onUnmounted } from 'vue';
import { buildUUID } from '/@/utils/uuid';
import { useGlobSetting } from '/@/hooks/setting';
import { useUserStore } from '/@/store/modules/user';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';
import { isArray } from '/@/utils/is';
import { getToken } from '/@/utils/auth';

// vxe socket
const vs = {
  // 동일한 사용자와 다른 페이지의 웹소켓을 식별하는 데 사용되는 페이지의 고유 ID입니다.
  pageId: buildUUID(),
  // 웹소켓 객체
  ws: null,
  // 일부 상수
  constants: {
    // 메시지 유형
    TYPE: 'type',
    // 메시지 데이터
    DATA: 'data',
    // 메시지 유형：심장 박동 감지
    TYPE_HB: 'heart_beat',
    // 메시지 유형：vxe 테이블 데이터 업데이트
    TYPE_UVT: 'update_vxe_table',
  },
  // 심장 박동 감지
  heartCheck: {
    // 간격 시간, 하트비트 메시지를 보내는 빈도
    interval: 10000,
    // 하트비트 메시지 시간 초과, 하트비트 메시지에 응답하지 않은 후 다시 연결하는 데 걸리는 시간
    timeout: 6000,
    timeoutTimer: -1,
    clear() {
      clearTimeout(this.timeoutTimer);
      return this;
    },
    start() {
      vs.sendMessage(vs.constants.TYPE_HB, '');
      // 일정 시간이 지나도 재설정되지 않으면 백엔드가 적극적으로 연결 해제되었음을 의미합니다.
      this.timeoutTimer = window.setTimeout(() => {
        vs.reconnect();
      }, this.timeout);
      return this;
    },
    // 하트비트 메시지 반환
    back() {
      this.clear();
      window.setTimeout(() => this.start(), this.interval);
    },
  },

  /** WebSocket 초기화 */
  initialWebSocket() {
    if (this.ws === null) {
      const userId = useUserStore().getUserInfo?.id;
      const domainURL = useGlobSetting().uploadUrl!;
      const domain = domainURL.replace('https://', 'wss://').replace('http://', 'ws://');
      const url = `${domain}/vxeSocket/${userId}/${this.pageId}`;
      let token = (getToken() || '') as string;
      this.ws = new WebSocket(url, [token]);
      this.ws.onopen = this.on.open.bind(this);
      this.ws.onerror = this.on.error.bind(this);
      this.ws.onmessage = this.on.message.bind(this);
      this.ws.onclose = this.on.close.bind(this);
    }
  },

  // 메시지 보내기
  sendMessage(type, message) {
    try {
      let ws = this.ws;
      if (ws != null && ws.readyState === ws.OPEN) {
        ws.send(
          JSON.stringify({
            type: type,
            data: message,
          })
        );
      }
    } catch (err: any) {
      console.warn('【JVxeWebSocket】메시지를 보내지 못했습니다.：(' + err.code + ')');
    }
  },

  /** 전역 VXE 테이블 바인딩 */
  tableMap: new Map(),
  /** 바인딩 추가 */
  addBind(map, key, value: VmArgs) {
    let binds = map.get(key);
    if (isArray(binds)) {
      binds.push(value);
    } else {
      map.set(key, [value]);
    }
  },
  /** 바인딩 제거 */
  removeBind(map, key, value: VmArgs) {
    let binds = map.get(key);
    if (isArray(binds)) {
      for (let i = 0; i < binds.length; i++) {
        let bind = binds[i];
        if (bind === value) {
          binds.splice(i, 1);
          break;
        }
      }
      if (binds.length === 0) {
        map.delete(key);
      }
    } else {
      map.delete(key);
    }
  },
  // 통화 바인딩 Form
  callBind(map, key, callback) {
    let binds = map.get(key);
    if (isArray(binds)) {
      binds.forEach(callback);
    }
  },

  lockReconnect: false,
  /** 다시 연결해 보세요 */
  reconnect() {
    if (this.lockReconnect) return;
    this.lockReconnect = true;
    setTimeout(() => {
      if (this.ws && this.ws.close) {
        this.ws.close();
      }
      this.ws = null;
      console.info('【JVxeWebSocket】다시 연결해 보세요...');
      this.initialWebSocket();
      this.lockReconnect = false;
    }, 5000);
  },

  on: {
    open() {
      console.info('【JVxeWebSocket】연결 성공');
      this.heartCheck.start();
    },
    error(e) {
      console.warn('【JVxeWebSocket】연결 오류가 발생했습니다.:', e);
      this.reconnect();
    },
    message(e) {
      // 메시지를 분석하세요
      let json;
      try {
        json = JSON.parse(e.data);
      } catch (e: any) {
        console.warn('【JVxeWebSocket】구문 분석할 수 없는 메시지를 받았습니다.:', e.data);
        return;
      }
      let type = json[this.constants.TYPE];
      let data = json[this.constants.DATA];
      switch (type) {
        // 심장 박동 감지
        case this.constants.TYPE_HB:
          this.heartCheck.back();
          break;
        // Form 데이터 업데이트
        case this.constants.TYPE_UVT:
          this.callBind(this.tableMap, data.socketKey, (args) => this.onVM.onUpdateTable(args, ...data.args));
          break;
        default:
          console.warn('【JVxeWebSocket】인식할 수 없는 메시지 유형이 수신되었습니다.:' + type);
          break;
      }
    },
    close(e) {
      console.info('【JVxeWebSocket】연결이 닫혔습니다:', e);
      this.reconnect();
    },
  },

  onVM: {
    /** Form 업데이트 메시지 수신 */
    onUpdateTable({ props, data, methods }: VmArgs, row, caseId) {
      if (data.caseId !== caseId) {
        const tableRow = methods.getIfRowById(row.id).row;
        // 부분적으로 업데이트된 데이터 유지
        if (tableRow) {
          if (props.reloadEffect) {
            data.reloadEffectRowKeysMap[row.id] = true;
          }
          Object.assign(tableRow, row, { id: tableRow.id });
          methods.getXTable().reloadRow(tableRow);
        }
      }
    },
  },
} as {
  ws: Nullable<WebSocket>;
} & Recordable;

type VmArgs = {
  props: JVxeTableProps;
  data: JVxeDataProps;
  methods: JVxeTableMethods;
};

export function useWebSocket(props: JVxeTableProps, data: JVxeDataProps, methods) {
  const args: VmArgs = { props, data, methods };
  watch(
    () => props.socketReload,
    (socketReload: boolean) => {
      if (socketReload) {
        vs.initialWebSocket();
        vs.addBind(vs.tableMap, props.socketKey, args);
      } else {
        vs.removeBind(vs.tableMap, props.socketKey, args);
      }
    },
    { immediate: true }
  );

  /** 업데이트 행에 소켓 메시지 보내기 */
  function socketSendUpdateRow(row) {
    vs.sendMessage(vs.constants.TYPE_UVT, {
      socketKey: props.socketKey,
      args: [row, data.caseId],
    });
  }

  onUnmounted(() => {
    vs.removeBind(vs.tableMap, props.socketKey, args);
  });

  return {
    socketSendUpdateRow,
  };
}
