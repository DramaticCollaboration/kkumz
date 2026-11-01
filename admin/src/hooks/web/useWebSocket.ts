// noinspection JSUnusedGlobalSymbols

import { unref } from 'vue';
import { useWebSocket, WebSocketResult } from '@vueuse/core';
import { getToken } from '/@/utils/auth';

let result: WebSocketResult<any>;
const listeners = new Map();

/**
 * 전역적으로 한 번만 실행하면 되는 WebSocket 링크를 활성화합니다.
 * @param url
 */
export function connectWebSocket(url: string) {
  let token = (getToken() || '') as string;
  result = useWebSocket(url, {
    // 자동으로 다시 연결 (오류가 발생하면 최대 10번까지 연결을 반복할 수 있습니다.)
    autoReconnect: {
      retries : 10,
      delay : 5000
    },
    // 심장 박동 감지
    heartbeat: {
      message: "ping",
      interval: 55000
    },
    protocols: [token],
  });
  if (result) {
    result.open = onOpen;
    result.close = onClose;

    const ws = unref(result.ws);
    if(ws!=null){
      ws.onerror = onError;
      ws.onmessage = onMessage;
      ws.onopen = onOpen;
      ws.onclose = onClose;
    }
  }
}

function onOpen() {
  console.log('[WebSocket] 연결 성공');
}

function onClose(e) {
  console.log('[WebSocket] 연결 끊김：', e);
}

function onError(e) {
  console.log('[WebSocket] 연결 오류가 발생했습니다.: ', e);
}

function onMessage(e) {
  console.debug('[WebSocket] -----메시지 수신-------', e.data);
  try {
    if(e==='ping'){
      return;
    }
    const data = JSON.parse(e.data);
    for (const callback of listeners.keys()) {
      try {
        callback(data);
      } catch (err) {
        console.error(err);
      }
    }
  } catch (err) {
    console.error('[WebSocket] 데이터 구문 분석에 실패했습니다.：', err);
  }
}


/**
 * WebSocket 메시지 수신 추가
 * @param callback
 */
export function onWebSocket(callback: (data: object) => any) {
  if (!listeners.has(callback)) {
    if (typeof callback === 'function') {
      listeners.set(callback, null);
    } else {
      console.debug('[WebSocket] WebSocket 메시지 리스너를 추가하지 못했습니다.：전달된 매개변수가 메소드가 아닙니다.');
    }
  }
}

/**
 * WebSocket 메시지 수신 비활성화
 *
 * @param callback
 */
export function offWebSocket(callback: (data: object) => any) {
  listeners.delete(callback);
}

export function useMyWebSocket() {
  return result;
}
