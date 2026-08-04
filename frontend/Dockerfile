# ============================================================
# 13RP-Demo 前端 Dockerfile
# Vue3 + Vite6 + Element Plus
# 多阶段构建：node 构建产物，nginx 托管运行
# ============================================================

# ---------- 阶段 1：构建 ----------
FROM node:20-alpine AS build
WORKDIR /app

# 配置淘宝 npm 镜像加速（国内网络），置于 npm ci 之前
RUN npm config set registry https://registry.npmmirror.com

# 先只复制依赖清单，npm ci 按 lockfile 精确安装（Docker layer 缓存：清单不变则依赖层不重建）
COPY package.json package-lock.json ./
RUN npm ci

# 复制剩余源码并生产构建
COPY . .
RUN npm run build

# ---------- 阶段 2：运行（Nginx） ----------
FROM nginx:alpine

# 复制构建产物与 Nginx 配置（/api、/ws 代理到 backend 容器）
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
