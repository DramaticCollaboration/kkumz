# Admin Console (Frontend)

## 모듈 설명
Kkumz 시스템의 관리자용 프론트엔드 애플리케이션입니다. Vue 3, Vite, TypeScript 및 Ant Design Vue를 기반으로 구축되어 빠른 렌더링 성능과 모던 UI를 제공합니다.

## 실행하는 방법
1. **패키지 설치**:
   ```bash
   pnpm install
   ```
2. **개발 서버 실행**:
   ```bash
   pnpm run dev
   ```
3. **운영 빌드**:
   ```bash
   pnpm run build
   ```

## 개발자 가이드
- **프레임워크 스택**: Vue 3 (Composition API), Vite, Pinia (상태관리), Vue Router를 주요 기술 스택으로 사용합니다.
- **환경 변수**: `.env.development` 및 `.env.production` 파일에서 백엔드 API 서버 경로 및 전역 변수를 설정합니다.
- **코드 컨벤션**: ESLint, Prettier, Stylelint를 사용하여 코드 포맷을 관리합니다.

## 설치 파일 패키징 (Maven Exec Plugin 사용)
프론트엔드 빌드 결과물을 Maven 빌드 프로세스에 통합하여 패키징하기 위해 Maven Exec Plugin을 사용합니다.

1. 프로젝트 루트(`Admin`)에서 다음 명령어를 실행하여 빌드 및 패키징을 수행합니다.
   ```bash
   mvn clean package
   mvn exec:exec -Dexec.executable="sh" -Dexec.args="package.sh"
   ```
   *(윈도우 환경: `mvn exec:exec -Dexec.executable="cmd" -Dexec.args="/c package.bat"`)*
2. 생성된 `target/*.tar.gz` (또는 `.zip`) 파일을 운영 환경에 배포합니다.
   - 내부적으로 `pnpm run build`를 호출하고 결과물을 압축합니다.
