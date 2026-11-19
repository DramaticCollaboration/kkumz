/**
 * Package file volume analysis
 */
import { isReportMode } from '../../utils';

export async function configVisualizerConfig() {
  if (isReportMode()) {
    const { visualizer } = await import('rollup-plugin-visualizer');
    return visualizer({
      filename: './node_modules/.cache/visualizer/stats.html',
      open: true,
      gzipSize: true,
      brotliSize: true,
    }) as Plugin;
  }
  return [];
}