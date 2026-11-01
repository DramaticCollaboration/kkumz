/**
 *
 * tagName을 기반으로 상위 노드 가져오기
 *
 * @param dom 첫 번째 수준 dom 노드
 * @param tagName 태그 이름, 대소문자를 구분하지 않음
 */
export function getParentNodeByTagName(dom: HTMLElement, tagName: string = 'body'): HTMLElement | null {
  if (tagName === 'body') {
    return document.body;
  }
  if (dom.parentElement) {
    if (dom.parentElement.tagName.toLowerCase() === tagName.trim().toLowerCase()) {
      return dom.parentElement;
    } else {
      return getParentNodeByTagName(dom.parentElement, tagName);
    }
  } else {
    return null;
  }
}
