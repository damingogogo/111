#!/usr/bin/env bash
set -euo pipefail

APP_DIR=/opt/emotion-ai
WEB_DIR=/var/www/emotion-ai-admin
ENV_DIR=/etc/emotion-ai
ENV_FILE="${ENV_DIR}/emotion-ai.env"
JAR_SOURCE="${1:-$HOME/emotion-ai-0.0.1-SNAPSHOT.jar}"
WEB_ARCHIVE_SOURCE="${2:-$HOME/admin-web-dist.tar.gz}"
DOMAIN=2ezqs7.top

sudo mkdir -p "${APP_DIR}" "${WEB_DIR}" "${ENV_DIR}"
sudo chown -R ubuntu:ubuntu "${APP_DIR}" "${WEB_DIR}"
sudo chmod 750 "${ENV_DIR}"

sudo mysql -e "CREATE DATABASE IF NOT EXISTS emotion_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if [ ! -f "${ENV_FILE}" ]; then
  APP_DB_PASSWORD="$(openssl rand -base64 24 | tr -d '\n')"
  sudo tee "${ENV_FILE}" >/dev/null <<EOF
SPRING_PROFILES_ACTIVE=prod
MYSQL_URL=jdbc:mysql://localhost:3306/emotion_ai?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
MYSQL_USERNAME=emotion_app
MYSQL_PASSWORD=${APP_DB_PASSWORD}
EOF
  sudo chmod 600 "${ENV_FILE}"
fi

APP_DB_PASSWORD="$(sudo awk -F= '/^MYSQL_PASSWORD=/{print $2}' "${ENV_FILE}")"
sudo mysql <<SQL
CREATE USER IF NOT EXISTS 'emotion_app'@'localhost' IDENTIFIED BY '${APP_DB_PASSWORD}';
ALTER USER 'emotion_app'@'localhost' IDENTIFIED BY '${APP_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON emotion_ai.* TO 'emotion_app'@'localhost';
FLUSH PRIVILEGES;
SQL

install -m 644 "${JAR_SOURCE}" "${APP_DIR}/emotion-ai.jar"
rm -rf /tmp/emotion-ai-admin-dist
mkdir -p /tmp/emotion-ai-admin-dist
case "${WEB_ARCHIVE_SOURCE}" in
  *.tar.gz|*.tgz)
    tar -xzf "${WEB_ARCHIVE_SOURCE}" -C /tmp/emotion-ai-admin-dist
    ;;
  *)
    unzip -q "${WEB_ARCHIVE_SOURCE}" -d /tmp/emotion-ai-admin-dist
    ;;
esac
rm -rf "${WEB_DIR:?}/"*
cp -R /tmp/emotion-ai-admin-dist/* "${WEB_DIR}/"

sudo tee /etc/systemd/system/emotion-ai.service >/dev/null <<EOF
[Unit]
Description=Emotion AI Spring Boot Service
After=network.target mysql.service
Requires=mysql.service

[Service]
User=ubuntu
WorkingDirectory=${APP_DIR}
EnvironmentFile=${ENV_FILE}
ExecStart=/usr/bin/java -jar ${APP_DIR}/emotion-ai.jar
Restart=always
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable emotion-ai
sudo systemctl restart emotion-ai

sudo tee /etc/nginx/sites-available/emotion-ai >/dev/null <<EOF
server {
    listen 80;
    server_name ${DOMAIN} www.${DOMAIN} 42.193.142.116;

    root ${WEB_DIR};
    index index.html;

    client_max_body_size 40m;

    location /api/ {
        proxy_pass http://127.0.0.1:18763/api/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 120s;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
EOF

sudo ln -sfn /etc/nginx/sites-available/emotion-ai /etc/nginx/sites-enabled/emotion-ai
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx

for _ in $(seq 1 60); do
  if curl -fsS http://127.0.0.1:18763/api/admin/tables >/dev/null; then
    break
  fi
  sleep 1
done

systemctl --no-pager --full status emotion-ai | sed -n '1,18p'
curl -fsS http://127.0.0.1:18763/api/admin/tables >/dev/null
curl -fsS http://127.0.0.1/api/admin/tables >/dev/null
echo "DEPLOY_OK"
