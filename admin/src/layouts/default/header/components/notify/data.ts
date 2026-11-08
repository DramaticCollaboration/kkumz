export interface ListItem {
  id: string;
  avatar: string;
  // 알림의 제목 내용
  title: string;
  // 제목에 취소선 표시 여부
  titleDelete?: boolean;
  datetime: string;
  type: string;
  read?: boolean;
  description: string;
  clickClose?: boolean;
  extra?: string;
  color?: string;
  // 우선순위
  priority?: string;
}

export enum PriorityTypes {
  // 낮은 우선순위, 일반 메시지
  L = 'L',
  // 중간 우선순위, 중요한 메시지
  M = 'M',
  // 높은 우선순위, 긴급 메시지
  H = 'H',
}

export interface TabItem {
  key: string;
  name: string;
  list: ListItem[];
  unreadlist?: ListItem[];
  count: number;
}

export const tabListData: TabItem[] = [
  {
    key: '1',
    name: '통지하다',
    list: [
      {
        id: '000000001',
        avatar: '/logo.png',
        title: '14개의 새로운 주간 뉴스레터를 받았습니다.',
        description: '',
        datetime: '2017-08-09',
        type: '1',
      },
      {
        id: '000000002',
        avatar: '/logo.png',
        title: '추천하신 후보자가 3차 면접에 합격했습니다.',
        description: '',
        datetime: '2017-08-08',
        type: '1',
      },
      {
        id: '000000003',
        avatar: '/logo.png',
        title: '이 템플릿은 여러 알림 유형을 구별할 수 있습니다.',
        description: '',
        datetime: '2017-08-07',
        // read: true,
        type: '1',
      },
      {
        id: '000000004',
        avatar: '/logo.png',
        title: '왼쪽의 아이콘은 다양한 유형을 구별하는 데 사용됩니다.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000005',
        avatar: '/logo.png',
        title: '제목은 자동으로 줄임표를 표시하도록 설정할 수 있습니다. 이 예에서는 제목 줄 수를 1줄로 설정했습니다. 내용이 1줄을 초과하면 자동으로 잘리고 전체 제목이 표시되도록 도구 설명이 지원됩니다.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000006',
        avatar: '/logo.png',
        title: '왼쪽의 아이콘은 다양한 유형을 구별하는 데 사용됩니다.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000007',
        avatar: '/logo.png',
        title: '왼쪽의 아이콘은 다양한 유형을 구별하는 데 사용됩니다.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000008',
        avatar: '/logo.png',
        title: '왼쪽의 아이콘은 다양한 유형을 구별하는 데 사용됩니다.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000009',
        avatar: '/logo.png',
        title: '왼쪽의 아이콘은 다양한 유형을 구별하는 데 사용됩니다.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000010',
        avatar: '/logo.png',
        title: '왼쪽의 아이콘은 다양한 유형을 구별하는 데 사용됩니다.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
    ],
    count: 0,
  },
  {
    key: '2',
    name: '시스템 메시지',
    list: [
      {
        id: '000000006',
        avatar: '/logo.png',
        title: '담당자가 귀하에게 댓글을 달았습니다.',
        description: '설명 정보 설명 정보 설명 정보',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
      {
        id: '000000007',
        avatar: '/logo.png',
        title: '담당자가 답변했습니다.',
        description: '이 템플릿은 귀하와 상호작용한 사람을 상기시키는 데 사용됩니다.',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
      {
        id: '000000008',
        avatar: '/logo.png',
        title: '제목',
        description:
          '여기에서 메시지가 얼마나 오랫동안 처리되는지 테스트하려면 여기로 마우스를 이동하세요.본 예시에서 설정한 설명 줄 수는 최대 2줄입니다. 2줄을 초과하는 설명 내용은 생략되며, 전체 내용은 툴팁을 통해 확인하실 수 있습니다.',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
    ],
    count: 0,
  },
  // {
  //   key: '3',
  //   name: '할 일',
  //   list: [
  //     {
  //       id: '000000009',
  //       avatar: '',
  //       title: '작업 이름',
  //       description: '해당 작업은 2017-01-12 20:00 이전에 시작되어야 합니다.',
  //       datetime: '',
  //       extra: '시작되지 않음',
  //       color: '',
  //       type: '3',
  //     },
  //     {
  //       id: '000000010',
  //       avatar: '',
  //       title: '써드파티 긴급 코드 변경',
  //       description: '담당자는 2017년 1월 7일 이전에 코드 변경 작업을 완료해야 합니다.',
  //       datetime: '',
  //       extra: '곧 만료됨',
  //       color: 'red',
  //       type: '3',
  //     },
  //     {
  //       id: '000000011',
  //       avatar: '',
  //       title: '정보보호 시험',
  //       description: '2017년 1월 9일 이전에 업데이트 및 릴리스를 완료하도록 담당자를 임명했습니다.',
  //       datetime: '',
  //       extra: '8일 걸렸어요',
  //       color: 'default',
  //       type: '3',
  //     },
  //     {
  //       id: '000000012',
  //       avatar: '',
  //       title: 'ABCD 버전 출시됨',
  //       description: '2017년 1월 9일 이전에 업데이트 및 릴리스를 완료하도록 담당자를 임명했습니다.',
  //       datetime: '',
  //       extra: '진행 중',
  //       color: 'blue',
  //       type: '3',
  //     },
  //   ],
  // },
];
