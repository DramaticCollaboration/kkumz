/**
 * 다시 패키징하지 않고 후반 작업 구성을 위한 외부 구성 파일 생성
 */
import { GLOB_CONFIG_FILE_NAME, OUTPUT_DIR } from '../constant';
import { createHash } from 'node:crypto';
import fs, { writeFileSync } from 'fs-extra';
import colors from 'picocolors';

import { getEnvConfig, getRootPath } from '../utils';
import { getConfigFileName } from '../getConfigFileName';

import pkg from '../../package.json';

interface CreateConfigParams {
  configName: string;
  config: any;
  configFileName?: string;
}

const BUILT_INDEX_HTML_PATH = `${OUTPUT_DIR}/index.html`;
const escapeRegExp = (source: string) => source.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
const APP_CONFIG_SCRIPT_PATTERN = new RegExp(
  `<script\\b[^>]*src=["'][^"']*${escapeRegExp(GLOB_CONFIG_FILE_NAME)}(?:\\?[^"']*)?["'][^>]*>\\s*</script>`,
);

function normalizePublicPath(publicPath: string | undefined): string {
  const trimmedPath = publicPath?.trim() || '/';

  if (/^https?:\/\//.test(trimmedPath)) {
    return trimmedPath.endsWith('/') ? trimmedPath : `${trimmedPath}/`;
  }

  if (trimmedPath === '.' || trimmedPath === './') {
    return '/';
  }

  const absolutePath = trimmedPath.startsWith('/') ? trimmedPath : `/${trimmedPath}`;
  return absolutePath.endsWith('/') ? absolutePath : `${absolutePath}/`;
}

export function getAppConfigScriptPath(publicPath = process.env.VITE_PUBLIC_PATH): string {
  return `${normalizePublicPath(publicPath)}${GLOB_CONFIG_FILE_NAME}`;
}

export function getAppConfigCacheBust(config: unknown): string {
  const canonicalConfig = JSON.stringify(config, (_key, value) =>
    value && typeof value === 'object' && !Array.isArray(value)
      ? Object.fromEntries(Object.entries(value).sort(([left], [right]) => left.localeCompare(right)))
      : value,
  );
  return createHash('sha256').update(canonicalConfig).digest('hex').slice(0, 16);
}

export function injectConfigScriptTag(
  indexHtmlSource: string,
  publicPath = process.env.VITE_PUBLIC_PATH,
  cacheBust = pkg.version,
): string {
  if (APP_CONFIG_SCRIPT_PATTERN.test(indexHtmlSource)) {
    return indexHtmlSource;
  }

  const moduleEntryScriptPattern =
    /\s*<script\b[^>]*type="module"[^>]*><\/script>|\s*<script\b[^>]*type="module"[^>]*src="[^"]+"[^>]*><\/script>/;
  const appConfigScriptTag = `\n    <script src="${getAppConfigScriptPath(publicPath)}?v=${cacheBust}"></script>`;
  const patchedIndexHtmlSource = indexHtmlSource.replace(moduleEntryScriptPattern, (moduleScriptTag) => {
    return `${appConfigScriptTag}${moduleScriptTag}`;
  });

  if (patchedIndexHtmlSource === indexHtmlSource) {
    throw new Error(`failed to inject ${GLOB_CONFIG_FILE_NAME} into ${BUILT_INDEX_HTML_PATH}`);
  }

  return patchedIndexHtmlSource;
}

function createConfig(params: CreateConfigParams) {
  const { configName, config, configFileName } = params;
  try {
    const windowConf = `window.${configName}`;
    // Ensure that the variable will not be modified
    let configStr = `${windowConf}=${JSON.stringify(config)};`;
    configStr += `
      Object.freeze(${windowConf});
      Object.defineProperty(window, "${configName}", {
        configurable: false,
        writable: false,
      });
    `.replace(/\s/g, '');

    fs.mkdirp(getRootPath(OUTPUT_DIR));
    writeFileSync(getRootPath(`${OUTPUT_DIR}/${configFileName}`), configStr);

    console.log(colors.cyan(`[${pkg.name}]`) + ` - configuration file is build successfully:`);
    console.log(colors.gray(OUTPUT_DIR + '/' + colors.green(configFileName)) + '\n');
    return getAppConfigCacheBust(config);
  } catch (error) {
    console.log(colors.red('configuration file configuration file failed to package:\n' + error));
    throw error;
  }
}

export function injectConfigScriptIntoBuiltHtml(cacheBust = pkg.version) {
  const indexHtmlPath = getRootPath(BUILT_INDEX_HTML_PATH);

  if (!fs.existsSync(indexHtmlPath)) {
    throw new Error(`built html file was not found: ${BUILT_INDEX_HTML_PATH}`);
  }

  const indexHtmlSource = fs.readFileSync(indexHtmlPath, 'utf8');
  const patchedIndexHtmlSource = injectConfigScriptTag(indexHtmlSource, process.env.VITE_PUBLIC_PATH, cacheBust);

  writeFileSync(indexHtmlPath, patchedIndexHtmlSource);
}

export function runBuildConfig() {
  const config = getEnvConfig();
  const configFileName = getConfigFileName(config);
  return createConfig({ config, configName: configFileName, configFileName: GLOB_CONFIG_FILE_NAME });
}
