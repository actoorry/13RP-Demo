#!/usr/bin/env bash
# ============================================================
# 13RP-Demo 国内网络一键部署脚本（Docker + Docker Compose）
# 适用：已安装 Docker + Docker Compose 的 Linux 服务器（国内网络）
#
# 用法：
#   方式1（推荐）：git clone 后  cd 13RP-Demo && bash scripts/13rp-deploy.sh
#   方式2（免克隆）：curl -fsSL <本脚本 raw 地址> | bash
#   方式3：空目录直接执行本脚本（自动 clone 到 13RP-Demo 子目录）
#
# 特性：
#   · 幂等：重复执行安全（已有代码/镜像则跳过）
#   · 国内网络适配：GitHub 克隆自动降级镜像；Docker 基础镜像从国内源预拉并打官方 tag
#   · Dockerfile 内置阿里云 Maven 镜像 + 淘宝 npm 镜像（构建期依赖也走国内）
#   · 端口：前端 8088 / 后端 8080
# ============================================================
set -e

REPO="https://github.com/actoorry/13RP-Demo.git"
# GitHub 克隆镜像前缀（直连失败按顺序降级）
GH_MIRRORS=(
  "https://ghproxy.com/"
  "https://ghproxy.net/"
  "https://gitclone.com/github.com/"
)
# Docker Hub 国内镜像源（预拉基础镜像按顺序降级）
DH_MIRRORS=(
  "docker.1ms.run"
  "docker.m.daocloud.io"
  "docker.1panel.live"
)
# 需要预拉的基础镜像（与两个 Dockerfile 的 FROM 一致）
BASE_IMAGES=(
  "maven:3.9-eclipse-temurin-21"
  "node:20-alpine"
  "nginx:alpine"
  "eclipse-temurin:21-jre"
)

log()  { echo -e "\033[32m[13RP]\033[0m $*"; }
warn() { echo -e "\033[33m[13RP!]\033[0m $*"; }
fail() { echo -e "\033[31m[13RP✗]\033[0m $*"; exit 1; }

# ---------- 1. 准备代码目录 ----------
ensure_repo() {
  if [ -f "docker-compose.yml" ]; then
    log "[1/5] 当前目录已是 13RP-Demo，跳过克隆"
    return 0
  fi
  if [ ! -d "13RP-Demo/.git" ]; then
    log "[1/5] 克隆代码（直连失败自动切换国内镜像）..."
    if ! git clone --depth 1 "$REPO" 2>/dev/null; then
      ok=""
      for m in "${GH_MIRRORS[@]}"; do
        log "  直连失败，尝试镜像: ${m}${REPO}"
        if git clone --depth 1 "${m}${REPO}" 2>/dev/null; then
          ok="yes"
          break
        fi
      done
      [ -n "$ok" ] || fail "克隆失败：请手动执行 git clone $REPO 后重试"
    fi
  fi
  cd 13RP-Demo
  log "  代码就绪: $(pwd)"
}

# ---------- 2. 预拉基础镜像（国内源 + 打官方 tag，幂等） ----------
ensure_images() {
  log "[2/5] 预拉 Docker 基础镜像（国内源，已存在则跳过）..."
  for img in "${BASE_IMAGES[@]}"; do
    if docker image inspect "$img" >/dev/null 2>&1; then
      log "  $img 已存在，跳过"
      continue
    fi
    pulled=""
    for src in "${DH_MIRRORS[@]}"; do
      full="$src/library/$img"
      log "  拉取 $full ..."
      if docker pull "$full" >/dev/null 2>&1; then
        docker tag "$full" "$img"
        pulled="yes"
        break
      fi
    done
    if [ -z "$pulled" ]; then
      warn "  国内源均失败，尝试直连 Docker Hub..."
      docker pull "$img" >/dev/null 2>&1 || warn "  $img 拉取失败（稍后重跑脚本会重试）"
    fi
  done
}

# ---------- 3. 构建并启动 ----------
build_up() {
  log "[3/5] docker compose 构建并启动（首次构建约 5-15 分钟）..."
  docker compose up -d --build
}

# ---------- 4. 等待后端健康 ----------
wait_health() {
  log "[4/5] 等待服务就绪（最长 60s）..."
  for i in $(seq 1 30); do
    if curl -sf http://localhost:8088/api/health >/dev/null 2>&1; then
      log "✅ 后端健康检查通过"
      return 0
    fi
    sleep 2
  done
  warn "健康检查超时：请执行 docker compose logs -f 查看日志定位问题"
}

# ---------- 5. 输出 ----------
show_info() {
  log "[5/5] 部署完成 ✅"
  echo ""
  echo "=============================================="
  echo "  13RP-Demo 已部署"
  echo "  前端地址 : http://<服务器IP>:8088"
  echo "  后端健康 : http://<服务器IP>:8080/api/health"
  echo ""
  echo "  常用命令:"
  echo "    docker compose ps            # 查看状态"
  echo "    docker compose logs -f       # 查看日志"
  echo "    docker compose down          # 停止服务"
  echo "    git pull && docker compose up -d --build   # 更新"
  echo "=============================================="
}

ensure_repo
ensure_images
build_up
wait_health
show_info
