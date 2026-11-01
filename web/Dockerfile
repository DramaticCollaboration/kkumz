# ===================================================================
# Kkumz Web (사용자 포털 & AI 스튜디오) Nginx Web Server (Hybrid Multi-Stage)
# ===================================================================
FROM node:22-alpine AS builder
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@9.12.1 --activate
COPY web/ ./
RUN if [ ! -f dist/index.html ]; then \
      (pnpm install --no-frozen-lockfile || npm install --legacy-peer-deps) && \
      (pnpm run build || npm run build); \
    fi

FROM nginx:alpine
ENV TZ=Asia/Seoul
RUN echo 'server { \
    listen 80; \
    server_name _; \
    root /usr/share/nginx/html; \
    index index.html; \
    gzip on; \
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript; \
    location /api/v1/kkumz/ { \
        proxy_pass http://kkumz-server:8080/api/v1/kkumz/; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; \
    } \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
