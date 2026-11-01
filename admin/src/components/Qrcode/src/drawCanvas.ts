import { toCanvas } from 'qrcode';
import type { QRCodeRenderersOptions } from 'qrcode';
import { RenderQrCodeParams, ContentType } from './typing';
import { cloneDeep } from 'lodash-es';

export const renderQrCode = ({ canvas, content, width = 0, options: params = {} }: RenderQrCodeParams) => {
  const options = cloneDeep(params);
  // 오류 허용률 기본적으로 내용이 적은 QR 코드에는 높은 오류 허용률이 적용되고, 내용이 많은 QR 코드에는 낮은 오류 허용률이 적용됩니다.
  options.errorCorrectionLevel = options.errorCorrectionLevel || getErrorCorrectionLevel(content);

  return getOriginWidth(content, options).then((_width: number) => {
    options.scale = width === 0 ? undefined : (width / _width) * 4;
    return toCanvas(canvas, content, options);
  });
};

// 스케일링을 통해 올바른 QrCode 크기를 얻을 수 있도록 원본 QrCode의 크기를 가져옵니다.
function getOriginWidth(content: ContentType, options: QRCodeRenderersOptions) {
  const _canvas = document.createElement('canvas');
  return toCanvas(_canvas, content, options).then(() => _canvas.width);
}

// 내용이 적은 QrCode의 경우 내결함성 비율을 높입니다.
function getErrorCorrectionLevel(content: ContentType) {
  if (content.length > 36) {
    return 'M';
  } else if (content.length > 16) {
    return 'Q';
  } else {
    return 'H';
  }
}
