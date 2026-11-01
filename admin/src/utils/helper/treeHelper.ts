interface TreeHelperConfig {
  id: string;
  children: string;
  pid: string;
}

// 기본 구성
const DEFAULT_CONFIG: TreeHelperConfig = {
  id: 'id',
  children: 'children',
  pid: 'pid',
};

// 구성을 가져옵니다.  객체.할당은 하나 이상의 소스 객체에서 대상 객체로 복사합니다.
const getConfig = (config: Partial<TreeHelperConfig>) => Object.assign({}, DEFAULT_CONFIG, config);

// tree from list
// 목록의 트리
export function listToTree<T = any>(list: any[], config: Partial<TreeHelperConfig> = {}): T[] {
  const conf = getConfig(config) as TreeHelperConfig;
  const nodeMap = new Map();
  const result: T[] = [];
  if (!list || !Array.isArray(list)) return result;
  const { id, children, pid } = conf;

  for (const node of list) {
    node[children] = node[children] || [];
    nodeMap.set(node[id], node);
  }
  for (const node of list) {
    const parent = nodeMap.get(node[pid]);
    (parent ? parent[children] : result).push(node);
  }
  return result;
}

export function treeToList<T = any>(tree: any, config: Partial<TreeHelperConfig> = {}): T {
  config = getConfig(config);
  if (!tree || !Array.isArray(tree)) return [] as any;
  const { children } = config;
  const result: any = [...tree];
  for (let i = 0; i < result.length; i++) {
    if (!result[i][children!]) continue;
    result.splice(i + 1, 0, ...result[i][children!]);
  }
  return result;
}

export function findNode<T = any>(tree: any, func: Fn, config: Partial<TreeHelperConfig> = {}): T | null {
  config = getConfig(config);
  if (!tree || !Array.isArray(tree)) return null;
  const { children } = config;
  const list = [...tree];
  for (const node of list) {
    if (func(node)) return node;
    node[children!] && list.push(...node[children!]);
  }
  return null;
}

export function findNodeAll<T = any>(tree: any, func: Fn, config: Partial<TreeHelperConfig> = {}): T[] {
  config = getConfig(config);
  if (!tree || !Array.isArray(tree)) return [];
  const { children } = config;
  const list = [...tree];
  const result: T[] = [];
  for (const node of list) {
    func(node) && result.push(node);
    node[children!] && list.push(...node[children!]);
  }
  return result;
}

export function findPath<T = any>(tree: any, func: Fn, config: Partial<TreeHelperConfig> = {}): T | T[] | null {
  config = getConfig(config);
  if (!tree || !Array.isArray(tree)) return null;
  const path: T[] = [];
  const list = [...tree];
  const visitedSet = new Set();
  const { children } = config;
  while (list.length) {
    const node = list[0];
    if (visitedSet.has(node)) {
      path.pop();
      list.shift();
    } else {
      visitedSet.add(node);
      node[children!] && list.unshift(...node[children!]);
      path.push(node);
      if (func(node)) {
        return path;
      }
    }
  }
  return null;
}

export function findPathAll(tree: any, func: Fn, config: Partial<TreeHelperConfig> = {}) {
  config = getConfig(config);
  if (!tree || !Array.isArray(tree)) return [];
  const path: any[] = [];
  const list = [...tree];
  const result: any[] = [];
  const visitedSet = new Set(),
    { children } = config;
  while (list.length) {
    const node = list[0];
    if (visitedSet.has(node)) {
      path.pop();
      list.shift();
    } else {
      visitedSet.add(node);
      node[children!] && list.unshift(...node[children!]);
      path.push(node);
      func(node) && result.push([...path]);
    }
  }
  return result;
}

export function filter<T = any>(
  tree: T[],
  func: (n: T) => boolean,
  // 부분적으로는 T의 모든 속성을 선택적으로 만듭니다.
  config: Partial<TreeHelperConfig> = {}
): T[] {
  // 구성 가져오기
  config = getConfig(config);
  const children = config.children as string;

  function listFilter(list: T[]) {
    if (!list || !Array.isArray(list)) return [];
    return list
      .map((node: any) => ({ ...node }))
      .filter((node) => {
        // 하위 항목이 포함된 항목에 대해 자체 함수 listFilter를 다시 호출하는 재귀 호출
        node[children] = node[children] && listFilter(node[children]);
        // 필터링을 위해 전달된 콜백 기능을 실행합니다.
        return func(node) || (node[children] && node[children].length);
      });
  }

  return listFilter(tree);
}

export function forEach<T = any>(tree: T[], func: (n: T) => any, config: Partial<TreeHelperConfig> = {}): void {
  config = getConfig(config);
  if (!tree || !Array.isArray(tree)) return;
  const list: any[] = [...tree];
  const { children } = config;
  for (let i = 0; i < list.length; i++) {
    //func가 true를 반환하면 노드 수가 많고 브라우저 지연을 유발하는 시나리오에서 의미 없는 루프를 방지하기 위해 순회를 종료합니다.
    if (func(list[i])) {
      return;
    }
    children && list[i][children] && list.splice(i + 1, 0, ...list[i][children]);
  }
}

/**
 * @description: Extract tree specified structure
 * @description: 트리의 지정된 구조를 추출합니다.
 */
export function treeMap<T = any>(treeData: T[], opt: { children?: string; conversion: Fn }): T[] {
  if (!treeData || !Array.isArray(treeData)) return [];
  return treeData.map((item) => treeMapEach(item, opt));
}

/**
 * @description: Extract tree specified structure
 * @description: 트리의 지정된 구조를 추출합니다.
 */
export function treeMapEach(data: any, { children = 'children', conversion }: { children?: string; conversion: Fn }) {
  const haveChildren = Array.isArray(data[children]) && data[children].length > 0;
  const conversionData = conversion(data) || {};
  if (haveChildren) {
    return {
      ...conversionData,
      [children]: data[children].map((i: number) =>
        treeMapEach(i, {
          children,
          conversion,
        })
      ),
    };
  } else {
    return {
      ...conversionData,
    };
  }
}

/**
 * 트리 구조를 재귀적으로 탐색
 * @param 트리데이터 트리
 * @param 콜백 콜백
 * @param parentNode 상위 노드
 */
export function eachTree(treeDatas: any[], callBack: Fn, parentNode = {}) {
  if (!treeDatas || !Array.isArray(treeDatas)) return;
  treeDatas.forEach((element) => {
    const newNode = callBack(element, parentNode) || element;
    if (element.children) {
      eachTree(element.children, callBack, newNode);
    }
  });
}
