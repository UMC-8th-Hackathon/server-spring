#!/bin/bash

# UMC Hackathon 서버 관리 스크립트
# 사용법: ./server-manage.sh [start|stop|restart|status|logs]

SERVER_IP="13.124.254.204"
KEY_PATH="/Users/kang-youngmin/Desktop/keypair/umc-hackathon.pem"
SERVICE_NAME="umc-hackathon.service"

case "$1" in
    start)
        echo "🚀 서비스 시작 중..."
        ssh -i "$KEY_PATH" "ec2-user@$SERVER_IP" "sudo systemctl start $SERVICE_NAME"
        ;;
    stop)
        echo "🛑 서비스 중지 중..."
        ssh -i "$KEY_PATH" "ec2-user@$SERVER_IP" "sudo systemctl stop $SERVICE_NAME"
        ;;
    restart)
        echo "🔄 서비스 재시작 중..."
        ssh -i "$KEY_PATH" "ec2-user@$SERVER_IP" "sudo systemctl restart $SERVICE_NAME"
        ;;
    status)
        echo "📊 서비스 상태 확인 중..."
        ssh -i "$KEY_PATH" "ec2-user@$SERVER_IP" "sudo systemctl status $SERVICE_NAME --no-pager"
        ;;
    logs)
        echo "📝 로그 확인 중..."
        ssh -i "$KEY_PATH" "ec2-user@$SERVER_IP" "sudo journalctl -u $SERVICE_NAME -f"
        ;;
    test)
        echo "🧪 애플리케이션 테스트 중..."
        ssh -i "$KEY_PATH" "ec2-user@$SERVER_IP" "curl -s http://localhost:8080 > /dev/null && echo '✅ 애플리케이션이 정상적으로 실행 중입니다!' || echo '❌ 애플리케이션 실행에 실패했습니다.'"
        ;;
    *)
        echo "사용법: $0 {start|stop|restart|status|logs|test}"
        echo ""
        echo "명령어 설명:"
        echo "  start   - 서비스 시작"
        echo "  stop    - 서비스 중지"
        echo "  restart - 서비스 재시작"
        echo "  status  - 서비스 상태 확인"
        echo "  logs    - 실시간 로그 확인"
        echo "  test    - 애플리케이션 연결 테스트"
        exit 1
        ;;
esac 