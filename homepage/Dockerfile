FROM node:22-alpine AS builder

WORKDIR /app
RUN corepack enable && corepack prepare pnpm@10.28.2 --activate

COPY . .
RUN if [ ! -f dist/index.html ] || [ -n "$(find src public index.html -newer dist/index.html 2>/dev/null)" ]; then \
      rm -rf dist && \
      pnpm install --no-frozen-lockfile && \
      pnpm run build-only; \
    fi

FROM nginx:1.27-alpine
COPY default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
