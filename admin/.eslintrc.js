// @ts-check
const { defineConfig } = require('eslint-define-config');
module.exports = defineConfig({
  root: true,
  env: {
    browser: true,
    node: true,
    es6: true,
  },
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@typescript-eslint/parser',
    ecmaVersion: 2020,
    sourceType: 'module',
    jsxPragma: 'React',
    ecmaFeatures: {
      jsx: true,
    },
  },
  extends: [
    'plugin:vue/vue3-recommended',
    'plugin:@typescript-eslint/recommended',
    'prettier',
    'plugin:prettier/recommended',
    'plugin:jest/recommended',
  ],
  rules: {
    'vue/script-setup-uses-vars': 'error',
    '@typescript-eslint/ban-ts-ignore': 'off',
    '@typescript-eslint/explicit-function-return-type': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/no-var-requires': 'off',
    '@typescript-eslint/no-empty-function': 'off',
    'vue/custom-event-name-casing': 'off',
    'no-use-before-define': 'off',
    '@typescript-eslint/no-use-before-define': 'off',
    '@typescript-eslint/ban-ts-comment': 'off',
    '@typescript-eslint/ban-types': 'off',
    '@typescript-eslint/no-non-null-assertion': 'off',
    '@typescript-eslint/explicit-module-boundary-types': 'off',
    '@typescript-eslint/no-unused-vars': [
      'error',
      {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
      },
    ],
    'no-unused-vars': [
      'error',
      {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
      },
    ],
    'space-before-function-paren': 'off',

    'vue/attributes-order': 'off',
    'vue/one-component-per-file': 'off',
    'vue/html-closing-bracket-newline': 'off',
    'vue/max-attributes-per-line': 'off',
    'vue/multiline-html-element-content-newline': 'off',
    'vue/singleline-html-element-content-newline': 'off',
    'vue/attribute-hyphenation': 'off',
    'vue/require-default-prop': 'off',
    'vue/html-self-closing': [
      'error',
      {
        html: {
          void: 'always',
          normal: 'never',
          component: 'always',
        },
        svg: 'always',
        math: 'always',
      },
    ],
    'no-restricted-syntax': [
      'error',
      {
        selector:
          "VariableDeclarator[id.name=/^(actorOptions|supplierOptions|agentOptions|userOptions|defaultRepos|myApprovalQueue|mockData|sampleData|dummyData|projectList|starterPresets|samplePrompts|samplePresets)$/][init.type='ArrayExpression'][init.elements.0.type='ObjectExpression']",
        message:
          '[Zero-Mock AST Gate] 인라인 Mock/옵션 객체 배열 하드코딩이 금지되어 있습니다. 백엔드 API(defHttp) 또는 DB 공통 사전을 조회하십시오.',
      },
      {
        selector:
          "VariableDeclarator[id.name=/^(actorOptions|supplierOptions|agentOptions|userOptions|defaultRepos|myApprovalQueue|mockData|sampleData|dummyData|projectList|starterPresets|samplePrompts|samplePresets)$/][init.type='CallExpression'][init.callee.name='ref'][init.arguments.0.type='ArrayExpression'][init.arguments.0.elements.0.type='ObjectExpression']",
        message:
          '[Zero-Mock AST Gate] ref([{...}]) 형태의 인라인 Mock/옵션 객체 배열 하드코딩이 금지되어 있습니다. 백엔드 API(defHttp)를 통해 동적 바인딩하십시오.',
      },
      {
        selector:
          "CatchClause AssignmentExpression[right.type='ArrayExpression'][right.elements.0.type='ObjectExpression']",
        message:
          '[Zero-Mock AST Gate] catch 블록 내에서 실패를 은폐하고 가짜 객체 배열을 대입하는 행위가 금지되어 있습니다. 에러 피드백을 투명하게 표시하십시오.',
      },
      {
        selector:
          "Literal[value=/^https?:\\/\\/(localhost|127\\.0\\.0\\.1|0\\.0\\.0\\.0)(:\\d+)?(\\/|$)/i]",
        message:
          '[Zero-Mock AST Gate] 소스 코드 내 localhost/127.0.0.1 엔드포인트 하드코딩이 금지되어 있습니다. 환경변수(.env) 설정을 사용하십시오.',
      },
      {
        selector:
          "Literal[value=/^(test123|test_user|dummy_\\w+|temp_token|sample_key|mock_token|todo_replace)$/i]",
        message:
          '[Zero-Mock AST Gate] 임시 테스트용 플레이스홀더 리터럴(test123, dummy_* 등) 사용이 금지되어 있습니다.',
      },
    ],
  },
});
