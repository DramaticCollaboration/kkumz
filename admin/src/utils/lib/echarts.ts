import * as echarts from 'echarts/core';

import { BarChart, LineChart, PieChart, MapChart, PictorialBarChart, RadarChart } from 'echarts/charts';

import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  PolarComponent,
  AriaComponent,
  ParallelComponent,
  LegendComponent,
  RadarComponent,
  ToolboxComponent,
  DataZoomComponent,
  VisualMapComponent,
  TimelineComponent,
  CalendarComponent,
  GraphicComponent,
} from 'echarts/components';

// TODO SVG 렌더링으로 전환하려면 SVGRenderer를 내보내세요.
//  그리고 echarts.use에 넣고 CanvasRenderer를 주석 처리하세요.
import { /*SVGRenderer*/ CanvasRenderer } from 'echarts/renderers';

echarts.use([
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  PolarComponent,
  AriaComponent,
  ParallelComponent,
  BarChart,
  LineChart,
  PieChart,
  MapChart,
  RadarChart,
  // TODO 온라인 차트 적응형 인쇄와 호환되어야 하므로 흐려질 수 있는 CanvasRenderer로 변경합니다.
  CanvasRenderer,
  PictorialBarChart,
  RadarComponent,
  ToolboxComponent,
  DataZoomComponent,
  VisualMapComponent,
  TimelineComponent,
  CalendarComponent,
  GraphicComponent,
]);

export default echarts;
