import { isString } from '/@/utils/is';
import { RenderQrCodeParams, LogoType } from './typing';
export const drawLogo = ({ canvas, logo }: RenderQrCodeParams) => {
  if (!logo) {
    return new Promise((resolve) => {
      resolve((canvas as HTMLCanvasElement).toDataURL());
    });
  }
  const canvasWidth = (canvas as HTMLCanvasElement).width;
  const { logoSize = 0.15, bgColor = '#ffffff', borderSize = 0.05, crossOrigin, borderRadius = 8, logoRadius = 0 } = logo as LogoType;

  const logoSrc: string = isString(logo) ? logo : logo.src;
  const logoWidth = canvasWidth * logoSize;
  const logoXY = (canvasWidth * (1 - logoSize)) / 2;
  const logoBgWidth = canvasWidth * (logoSize + borderSize);
  const logoBgXY = (canvasWidth * (1 - logoSize - borderSize)) / 2;

  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  // 로고 배경색
  canvasRoundRect(ctx)(logoBgXY, logoBgXY, logoBgWidth, logoBgWidth, borderRadius);
  ctx.fillStyle = bgColor;
  ctx.fill();

  // logo
  const image = new Image();
  if (crossOrigin || logoRadius) {
    image.setAttribute('crossOrigin', crossOrigin || 'anonymous');
  }
  image.src = logoSrc;

  // 이미지 그리기를 사용하면 도메인 간 상황을 피할 수 있습니다.
  const drawLogoWithImage = (image: CanvasImageSource) => {
    ctx.drawImage(image, logoXY, logoXY, logoWidth, logoWidth);
  };

  // 더 많은 기능을 위해 캔버스를 사용하여 그리기
  const drawLogoWithCanvas = (image: HTMLImageElement) => {
    const canvasImage = document.createElement('canvas');
    canvasImage.width = logoXY + logoWidth;
    canvasImage.height = logoXY + logoWidth;
    const imageCanvas = canvasImage.getContext('2d');
    if (!imageCanvas || !ctx) return;
    imageCanvas.drawImage(image, logoXY, logoXY, logoWidth, logoWidth);

    canvasRoundRect(ctx)(logoXY, logoXY, logoWidth, logoWidth, logoRadius);
    if (!ctx) return;
    const fillStyle = ctx.createPattern(canvasImage, 'no-repeat');
    if (fillStyle) {
      ctx.fillStyle = fillStyle;
      ctx.fill();
    }
  };

  // 캔버스에 로고를 그려주세요
  return new Promise((resolve) => {
    image.onload = () => {
      logoRadius ? drawLogoWithCanvas(image) : drawLogoWithImage(image);
      resolve((canvas as HTMLCanvasElement).toDataURL());
    };
  });
};

// 둥근 모서리를 그리는 데는 복사된 방법이 사용됩니다.
function canvasRoundRect(ctx: CanvasRenderingContext2D) {
  return (x: number, y: number, w: number, h: number, r: number) => {
    const minSize = Math.min(w, h);
    if (r > minSize / 2) {
      r = minSize / 2;
    }
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.arcTo(x + w, y, x + w, y + h, r);
    ctx.arcTo(x + w, y + h, x, y + h, r);
    ctx.arcTo(x, y + h, x, y, r);
    ctx.arcTo(x, y, x + w, y, r);
    ctx.closePath();
    return ctx;
  };
}
