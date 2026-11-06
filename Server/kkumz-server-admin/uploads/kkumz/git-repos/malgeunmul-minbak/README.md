# 맑은물 민박 (`/s/malgeunmul-minbak`)

> 공주 시골 냇가 옆, 맑고 차가운 물소리와 따뜻한 황토 온돌이 어우러진 사계절 정겨운 시골 스테이입니다.
> Kkumz 5단계 WebIDE 및 Git 개발자 워크스페이스에서 프로비저닝된 **Vue 3 + Vite** 공식 소스 리포지토리입니다.

## 1. 발급된 전용 키로 Git Clone 실행

발급받은 `malgeunmul-minbak-deploy-key.pem` 파일을 `~/.ssh/` 경로에 저장한 뒤 아래 명령어로 클론하세요.

```bash
chmod 600 ~/.ssh/malgeunmul-minbak-deploy-key.pem
GIT_SSH_COMMAND="ssh -i ~/.ssh/malgeunmul-minbak-deploy-key.pem -o IdentitiesOnly=yes" git clone -b main git@github.com:kkumz-sites/malgeunmul-minbak.git
cd malgeunmul-minbak
```

## 2. 로컬 개발 서버 실행 (Kkumz 실데이터 자동 연동)

```bash
cp .env.example .env.local
pnpm install
pnpm dev
```

- 로컬 실행 시 `.env.local`에 설정된 사이트 전용 런타임 키(`ak_site_malgeunmul_e44c0342d91e`)를 통해 Kkumz 백엔드의 업로드 사진·문서(`/api/v1/kkumz/assets/files/...`), SyncShop 상품, SyncCMS 매거진·예약폼 API가 실시간 연동됩니다.