/**
 * Used to parse the .env.development proxy configuration
 */
import type { ProxyOptions } from 'vite';

type ProxyItem = [string, string];

type ProxyList = ProxyItem[];

type ProxyTargetList = Record<string, ProxyOptions>;

const httpsRE = /^https:\/\//;

/**
 * Generate proxy
 * @param list
 */
export function createProxy(list: ProxyList = []) {
  const ret: ProxyTargetList = {};
  for (const [prefix, target] of list) {
    const isHttps = httpsRE.test(target);

    // https://github.com/http-party/node-http-proxy#options
    ret[prefix] = {
      target: target,
      changeOrigin: true,
      ws: true,
      // Transparent Proxy Mandate: Do not strip /api prefix for /api/v1 endpoints, forward 1:1 to backend
      rewrite: (path) => path,
      // https is require secure=false
      
      ...(isHttps ? { secure: false } : {}),
      configure: (proxy) => {
        proxy.on('proxyReq', (proxyReq, req) => {
          console.log(`[Proxy Request] ${req.method} ${req.url} -> ${target}${proxyReq.path}`);
        });
        proxy.on('proxyRes', (proxyRes, req) => {
          console.log(`[Proxy Response] ${req.method} ${req.url} ${proxyRes.statusCode}`);
        });
        proxy.on('error', (err, req) => {
          console.error(`[Proxy Error] ${req.method} ${req.url} : ${err.message}`);
        });
      },
    };
  }
  return ret;
}
