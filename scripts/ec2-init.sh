#!/bin/bash
#
# EC2 초기 환경 구성 스크립트
# 지원 OS: Amazon Linux 2023, Ubuntu 22.04
#
# 사용법: sudo ./ec2-init.sh
#

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Root 권한 확인
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "이 스크립트는 root 권한으로 실행해야 합니다."
        log_info "사용법: sudo ./ec2-init.sh"
        exit 1
    fi
}

# OS 감지
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        VERSION=$VERSION_ID
    else
        log_error "지원되지 않는 OS입니다."
        exit 1
    fi

    log_info "감지된 OS: $OS $VERSION"
}

# Amazon Linux 2023 설정
setup_amazon_linux() {
    log_info "Amazon Linux 2023 환경 구성을 시작합니다..."

    # 시스템 업데이트
    log_info "시스템 패키지 업데이트 중..."
    dnf update -y

    # Docker 설치
    log_info "Docker 설치 중..."
    dnf install -y docker

    # Docker 서비스 시작 및 활성화
    log_info "Docker 서비스 시작 중..."
    systemctl start docker
    systemctl enable docker

    # Docker Compose v2 설치 (플러그인 방식)
    log_info "Docker Compose 설치 중..."
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    # ec2-user를 docker 그룹에 추가
    log_info "ec2-user를 docker 그룹에 추가 중..."
    usermod -aG docker ec2-user

    # Git 설치 (없는 경우)
    if ! command -v git &> /dev/null; then
        log_info "Git 설치 중..."
        dnf install -y git
    fi
}

# Ubuntu 22.04 설정
setup_ubuntu() {
    log_info "Ubuntu 환경 구성을 시작합니다..."

    # 시스템 업데이트
    log_info "시스템 패키지 업데이트 중..."
    apt-get update -y
    apt-get upgrade -y

    # Docker 설치
    log_info "Docker 설치 중..."
    apt-get install -y docker.io

    # Docker 서비스 시작 및 활성화
    log_info "Docker 서비스 시작 중..."
    systemctl start docker
    systemctl enable docker

    # Docker Compose v2 설치 (플러그인 방식)
    log_info "Docker Compose 설치 중..."
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    # ubuntu 사용자를 docker 그룹에 추가
    log_info "ubuntu 사용자를 docker 그룹에 추가 중..."
    usermod -aG docker ubuntu

    # Git 설치 (없는 경우)
    if ! command -v git &> /dev/null; then
        log_info "Git 설치 중..."
        apt-get install -y git
    fi
}

# 타임존 설정
setup_timezone() {
    log_info "타임존을 Asia/Seoul로 설정 중..."
    timedatectl set-timezone Asia/Seoul
}

# 앱 디렉토리 생성
create_app_directory() {
    local APP_DIR="/home/ec2-user/app"

    # Ubuntu인 경우 경로 변경
    if [ "$OS" = "ubuntu" ]; then
        APP_DIR="/home/ubuntu/app"
    fi

    if [ ! -d "$APP_DIR" ]; then
        log_info "애플리케이션 디렉토리 생성: $APP_DIR"
        mkdir -p "$APP_DIR"

        if [ "$OS" = "ubuntu" ]; then
            chown ubuntu:ubuntu "$APP_DIR"
        else
            chown ec2-user:ec2-user "$APP_DIR"
        fi
    fi
}

# 설치 확인
verify_installation() {
    echo ""
    echo "========================================"
    echo "       설치 확인"
    echo "========================================"

    # Docker 버전
    if command -v docker &> /dev/null; then
        log_info "Docker: $(docker --version)"
    else
        log_error "Docker 설치 실패"
    fi

    # Docker Compose 버전
    if docker compose version &> /dev/null; then
        log_info "Docker Compose: $(docker compose version --short)"
    else
        log_error "Docker Compose 설치 실패"
    fi

    # Git 버전
    if command -v git &> /dev/null; then
        log_info "Git: $(git --version)"
    fi

    # 타임존
    log_info "타임존: $(timedatectl | grep 'Time zone' | awk '{print $3}')"
}

# 완료 메시지
print_completion() {
    local USER_NAME="ec2-user"
    if [ "$OS" = "ubuntu" ]; then
        USER_NAME="ubuntu"
    fi

    echo ""
    echo "========================================"
    echo "       EC2 초기 설정 완료!"
    echo "========================================"
    echo ""
    log_warn "Docker 그룹 적용을 위해 SSH 재접속이 필요합니다."
    echo ""
    echo "다음 단계:"
    echo "  1. exit 명령으로 SSH 연결 종료"
    echo "  2. SSH 재접속"
    echo "  3. docker ps 명령으로 Docker 권한 확인"
    echo ""
    echo "애플리케이션 배포:"
    echo "  cd /home/${USER_NAME}/app"
    echo "  git clone <your-repo-url> v-log"
    echo "  cd v-log"
    echo "  cp .env.example .env"
    echo "  vim .env  # DB 정보 입력"
    echo "  ./deploy.sh --build"
    echo ""
}

# 메인 실행
main() {
    echo "========================================"
    echo "   EC2 초기 환경 구성 스크립트 v1.0"
    echo "========================================"
    echo ""

    check_root
    detect_os

    case $OS in
        amzn|"amazon linux")
            setup_amazon_linux
            ;;
        ubuntu)
            setup_ubuntu
            ;;
        *)
            log_error "지원되지 않는 OS입니다: $OS"
            log_info "지원 OS: Amazon Linux 2023, Ubuntu 22.04"
            exit 1
            ;;
    esac

    setup_timezone
    create_app_directory
    verify_installation
    print_completion
}

main "$@"
