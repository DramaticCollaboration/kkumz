import tsPlugin from '@typescript-eslint/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import vueParser from 'vue-eslint-parser';

const typescriptCompatibilityPlugin = {
  ...tsPlugin,
  rules: {
    ...tsPlugin.rules,
    'ban-types': tsPlugin.rules['ban-types'] ?? {
      meta: {
        type: 'problem',
        docs: {
          description:
            'compatibility shim for legacy eslint-disable comments after @typescript-eslint removed ban-types',
        },
        schema: [],
      },
      create() {
        return {};
      },
    },
  },
};

const piniaCompatibilityPlugin = {
  rules: {
    'deprecated-define-store-id': {
      meta: {
        type: 'problem',
        docs: {
          description:
            'disallow Pinia v3-incompatible defineStore({ id }) declarations',
        },
        messages: {
          deprecatedDefineStoreId:
            "Pinia v3 removed defineStore({ id }). Use defineStore('id', {...}) instead.",
        },
        schema: [],
      },
      create(context) {
        const piniaDefineStoreNames = new Set();
        return {
          ImportDeclaration(node) {
            if (node.source.value !== 'pinia') {
              return;
            }
            for (const specifier of node.specifiers) {
              if (
                specifier.type === 'ImportSpecifier' &&
                specifier.imported.type === 'Identifier' &&
                specifier.imported.name === 'defineStore'
              ) {
                piniaDefineStoreNames.add(specifier.local.name);
              }
            }
          },
          CallExpression(node) {
            if (
              node.callee.type !== 'Identifier' ||
              !piniaDefineStoreNames.has(node.callee.name)
            ) {
              return;
            }
            const firstArg = node.arguments[0];
            if (!firstArg || firstArg.type !== 'ObjectExpression') {
              return;
            }
            const hasDeprecatedId = firstArg.properties.some((prop) => {
              if (prop.type !== 'Property') {
                return false;
              }
              return (
                (prop.key.type === 'Identifier' && prop.key.name === 'id') ||
                (prop.key.type === 'Literal' && prop.key.value === 'id')
              );
            });
            if (hasDeprecatedId) {
              context.report({ node: firstArg, messageId: 'deprecatedDefineStoreId' });
            }
          },
        };
      },
    },
    'never-export-initialized-store': {
      meta: {
        type: 'problem',
        docs: {
          description:
            'disallow exporting an initialized Pinia store instance from module scope',
        },
        messages: {
          initializedStore:
            'Export the useXxxStore function, not an initialized Pinia store instance.',
        },
        schema: [],
      },
      create(context) {
        return {
          ExportNamedDeclaration(node) {
            const declaration = node.declaration;
            if (!declaration || declaration.type !== 'VariableDeclaration') {
              return;
            }

            for (const declarator of declaration.declarations) {
              const init = declarator.init;
              if (!init || init.type !== 'CallExpression') {
                continue;
              }
              const callee = init.callee;
              if (
                callee.type === 'Identifier' &&
                /^use[A-Z].*Store$/.test(callee.name)
              ) {
                context.report({ node: init, messageId: 'initializedStore' });
              }
            }
          },
        };
      },
    },
  },
};

export default [
  {
    linterOptions: {
      reportUnusedDisableDirectives: false,
    },
    ignores: [
      'dist/**',
      'node_modules/**',
      'coverage/**',
      'public/**',
      'docs/**',
      '.husky/**',
      '.local/**',
      '.vscode/**',
      '.idea/**',
      'bin/**',
      'src/api/generated/**',
      'src/assets/icons/js/iconfont.js',
      'build/caches/**',
      '*.md',
      '*.sh',
      '*.woff',
      '*.ttf',
      'Dockerfile',
    ],
  },
  {
    files: ['**/*.{js,ts,tsx}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        ecmaFeatures: {
          jsx: true,
        },
      },
      globals: {
        Nullable: 'readonly',
        TimeoutHandle: 'readonly',
        DeepPartial: 'readonly',
        window: 'readonly',
        document: 'readonly',
        localStorage: 'readonly',
        console: 'readonly',
        process: 'readonly',
        URL: 'readonly',
      },
    },
    plugins: {
      '@typescript-eslint': typescriptCompatibilityPlugin,
      pinia: piniaCompatibilityPlugin,
    },
    rules: {
      'pinia/deprecated-define-store-id': 'error',
      'pinia/never-export-initialized-store': 'error',
    },
  },
  {
    files: ['**/*.vue'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      parser: vueParser,
      parserOptions: {
        parser: tsParser,
        ecmaVersion: 'latest',
        sourceType: 'module',
        extraFileExtensions: ['.vue'],
        ecmaFeatures: {
          jsx: true,
        },
      },
    },
    plugins: {
      '@typescript-eslint': typescriptCompatibilityPlugin,
      pinia: piniaCompatibilityPlugin,
    },
    rules: {
      'pinia/deprecated-define-store-id': 'error',
      'pinia/never-export-initialized-store': 'error',
    },
  },
];
