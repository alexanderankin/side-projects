#!/bin/bash
# not sure why it needs wrapped but runs only this way
bash -c '
apt-get update
apt-get install ca-certificates curl -y
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update

apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

systemctl start docker
systemctl enable docker

usermod -aG docker vminit
docker run -p 8000:8080 --log-opt max-file=10 --log-opt max-size=100m --restart=always -e SERVER_PORT=8080 -e LOGGED_ROUTES_DEFAULT_ROUTE_BASE_URL=https://ankin.info daveankin/logging-proxy:netty
' 2>&1 | tee /tmp/start-uplog
