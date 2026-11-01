export const useCodeHinting = (CodeMirror, keywords, language) => {
  const currentKeywords: any = [...keywords];
  const codeHintingMount = (coder) => {
    if (keywords.length) {
      coder.setOption('mode', language);
      setTimeout(() => {
        coder!.on('cursorActivity', function () {
          coder?.showHint({
            completeSingle: false,
            // container: containerRef.value
          });
        });
      }, 1e3);
    }
  };

  const codeHintingRegistry = () => {
    // 맞춤 키워드(.이전 수준)
    const customKeywords: string[] = [];

    currentKeywords.forEach((item) => {
      if (item.superiors) {
        customKeywords.push(item.superiors);
      }
    });
    const funcsHint = (cm, callback) => {
      // 커서 위치 가져오기
      const cur = cm.getCursor();
      // 현재 단어About 정보 얻기
      const token = cm.getTokenAt(cur);
      const start = token.start;
      const end = cur.ch;
      const str = token.string;
      let recordKeyword = null;
      console.log('커서 위치：', cur, '단어 정보：', token, `start:${start},end:${end},str:${str}`);

      if (str.length) {
        if (str === '.') {
          // 찾다.이전에 정의된 키워드가 있었나요?
          const curLineCode = cm.getLine(cur.line);
          for (let i = 0, len = customKeywords.length; i < len; i++) {
            const k = curLineCode.substring(-1, customKeywords[i].length);
            if (customKeywords.includes(k)) {
              recordKeyword = k;
              break;
            }
          }
        }
        const findIdx = (a, b) => a.toLowerCase().indexOf(b.toLowerCase());
        let list = currentKeywords.filter((item) => {
          if (recordKeyword) {
            // 특정 개체의 속성이나 메서드를 확인하세요.
            return item.superiors === recordKeyword;
          } else {
            // 전역 속성 또는 메서드 확인
            return item.superiors == undefined;
          }
        });
        if (str === '.') {
          if (recordKeyword == null) {
            list = [];
          }
        } else {
          list = list
            .filter((item) => {
              const { text } = item;
              const index = findIdx(text, str);
              let result = text.startsWith('.') ? index === 1 : index === 0;
              return result;
            })
            .sort((a, b) => {
              if (findIdx(a.text, str) < findIdx(b.text, str)) {
                return -1;
              } else {
                return 1;
              }
            });
        }

        if (list.length === 1) {
          // 하나만 있는 경우에는 직접 입력해도 됩니다. 마지막 단어를 입력할 때는 프롬프트를 제거해야 합니다.
          const item = list[0];
          if (item.text === str || item.text.substring(1) === str) {
            list = [];
          }
        }
        if (list.length) {
          // str이 점이 아닌 경우 점 제거
          if (str != '.') {
            list = list.map((item) => {
              if (item.text.indexOf('.') === 0) {
                return { ...item, text: item.text.substring(1) };
              }
              return item;
            });
          }
          callback({
            list: list,
            from: CodeMirror.Pos(cur.line, start),
            to: CodeMirror.Pos(cur.line, end),
          });
          const item = currentKeywords[0];
          if (item?.desc) {
            setTimeout(() => {
              const elem: HTMLUListElement = document.querySelector('.CodeMirror-hints')!;
              if (elem) {
                const childElems = elem.children;
                Array.from(childElems).forEach((item) => {
                  const displayText = item.textContent;
                  const findItem = currentKeywords.find((item) => item.displayText === displayText);
                  if (findItem) {
                    item.setAttribute('title', findItem.desc);
                  }
                });
              }
            }, 0);
          }
        } else {
        }
      }
    };
    funcsHint.async = true;
    funcsHint.supportsSelection = true;
    // 자동완성
    keywords.length && CodeMirror.registerHelper('hint', language, funcsHint);
  };
  return {
    codeHintingRegistry,
    codeHintingMount,
  };
};
