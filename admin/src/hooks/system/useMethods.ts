import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useGlobSetting } from '/@/hooks/setting';

const { createMessage, createWarningModal } = useMessage();
const glob = useGlobSetting();

/**
 * 내보낸 파일 xlsx의 MIME 유형
 */
export const XLSX_MIME_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
/**
 * 내보낸 파일 xlsx의 파일 접미사
 */
export const XLSX_FILE_SUFFIX = '.xlsx';

export function useMethods() {
  /**
   * xls 내보내기
   * @param name
   * @param url
   */
  async function exportXls(name, url, params, isXlsx = false) {
    const data = await defHttp.get({ url: url, params: params, responseType: 'blob', timeout: 60000 }, { isTransformResponse: false });
    if (!data) {
      createMessage.warning('파일 다운로드 실패');
      return;
    }
    let reader = new FileReader()
    reader.readAsText(data, 'utf-8')
    reader.onload = async () => {
      if(reader.result){
        if(reader.result.toString().indexOf("success") !=-1){
          const { success, message } = JSON.parse(reader.result.toString());
          if (!success) {
            createMessage.warning("내보내기 실패, 실패 이유："+ message);
          }else{
            exportExcel(name, isXlsx, data);
          }
          return;
        }
      }
      exportExcel(name, isXlsx, data);
    }
  }

  /**
   * xls 가져오기
   * @param 데이터 가져온 데이터
   * @param url
   * @param 성공 후 성공 콜백
   */
  async function importXls(data, url, success) {
    const isReturn = (fileInfo) => {
      try {
        if (fileInfo.code === 201) {
          let {
            message,
            result: { msg, fileUrl, fileName },
          } = fileInfo;
          let href = glob.uploadUrl + fileUrl;
          createWarningModal({
            title: message,
            centered: false,
            content: `<div>
                                <span>${msg}</span><br/> 
                                <span>구체적인 내용은 부탁드립니다<a href = ${href} download = ${fileName}> 다운로드하려면 클릭하세요 </a> </span> 
                              </div>`,
          });
        } else if (fileInfo.code === 500 || fileInfo.code === 510) {
          createMessage.error(fileInfo.message || `${data.file.name} 가져오기 실패`);
        } else {
          createMessage.success(fileInfo.message || `${data.file.name} 파일이 성공적으로 업로드되었습니다`);
        }
      } catch (error) {
        console.log('가져온 데이터가 비정상입니다.', error);
      } finally {
        typeof success === 'function' ? success(fileInfo) : '';
      }
    };
    await defHttp.uploadFile({ url }, { file: data.file }, { success: isReturn });
  }

  return {
    handleExportXls: (name: string, url: string, params?: object) => exportXls(name, url, params),
    handleImportXls: (data, url, success) => importXls(data, url, success),
    handleExportXlsx: (name: string, url: string, params?: object) => exportXls(name, url, params, true),
  };

  /**
   * 엑셀 내보내기
   * @param name
   * @param isXlsx
   * @param data
   */
  function exportExcel(name, isXlsx, data) {
    if (!name || typeof name != 'string') {
      name = '파일 내보내기';
    }
    let blobOptions = { type: 'application/vnd.ms-excel' };
    let fileSuffix = '.xls';
    if (isXlsx) {
      blobOptions['type'] = XLSX_MIME_TYPE;
      fileSuffix = XLSX_FILE_SUFFIX;
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data], blobOptions), name + fileSuffix);
    } else {
      let url = window.URL.createObjectURL(new Blob([data], blobOptions));
      let link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', name + fileSuffix);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //전체 제거 요소 다운로드
      window.URL.revokeObjectURL(url); //블롭 객체 해제
    }
  }
}
