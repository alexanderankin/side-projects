FROM debian:bookworm-slim AS build

ARG OPENSSL_VER=openssl-3.5.0
ARG NGINX_VER=1.29.0

ENV DEBIAN_FRONTEND=noninteractive

# Build prerequisites
RUN apt-get update && apt-get install -y --no-install-recommends \
        build-essential ca-certificates git wget curl \
        zlib1g-dev libpcre3-dev libssl-dev pkgconf && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /usr/local/src

RUN wget https://nginx.org/download/nginx-${NGINX_VER}.tar.gz
RUN wget https://www.openssl.org/source/${OPENSSL_VER}.tar.gz
RUN \
    tar xzf ${OPENSSL_VER}.tar.gz && \
    cd ${OPENSSL_VER} && \
    ./Configure linux-x86_64 no-tests shared \
        --prefix=/opt/openssl && \
    make -j"$(nproc)" && \
    # headers + libs only
    make install_sw && \
    mkdir -p /opt/openssl/ssl/ && \
    cp $(find . -name openssl.cnf | head -n1 || :;) /opt/openssl/ssl/openssl.cnf

ENV PATH="/opt/openssl/bin:${PATH}" \
    LD_LIBRARY_PATH="/opt/openssl/lib:${LD_LIBRARY_PATH}"

RUN \
    tar xzf nginx-${NGINX_VER}.tar.gz && \
    cd nginx-${NGINX_VER} && \
    ./configure \
        --prefix=/etc/nginx \
        --sbin-path=/usr/sbin/nginx \
        --conf-path=/etc/nginx/nginx.conf \
        --error-log-path=/var/log/nginx/error.log \
        --http-log-path=/var/log/nginx/access.log \
        --pid-path=/run/nginx.pid \
        --with-threads \
        --with-http_ssl_module \
        --with-http_v2_module \
        --with-openssl=../${OPENSSL_VER} \
        --with-openssl-opt="no-tests" && \
    make -j"$(nproc)" && \
    make install && \
    strip /usr/sbin/nginx


FROM debian:bookworm-slim

# runtime deps for NGINX + the custom OpenSSL
RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates curl libpcre3 zlib1g && \
    rm -rf /var/lib/apt/lists/*

# copy nginx tree & OpenSSL libs from builder
COPY --from=build /usr/sbin/nginx           /usr/sbin/nginx
COPY --from=build /etc/nginx                /etc/nginx
COPY --from=build /opt/openssl          /opt/openssl
COPY --from=build /opt/openssl/ssl/openssl.cnf          /opt/openssl/ssl/openssl.cnf
COPY --from=build /opt/openssl/lib64          /opt/openssl/lib
ENV LD_LIBRARY_PATH=/opt/openssl/lib

RUN mkdir -p /var/log/nginx

RUN <<EOT bash
    set -eux -o pipefail
    echo "creating the certs" &&
    ( cd /etc/nginx && OPENSSL_CONF=/opt/openssl/ssl/openssl.cnf openssl req -x509 -newkey rsa:4096 -sha256 -days 365 -nodes \
      -keyout "server.key" \
      -out "server.crt" \
      -subj "/CN=localhost"; ) && \
    printf '%s\n' \
        "worker_processes 1;" \
        "" \
        "events {" \
        "    worker_connections  1024;" \
        "}" \
        "" \
        "http {" \
        "    include       mime.types;" \
        "    default_type  application/octet-stream;" \
        "    sendfile      on;" \
        "    keepalive_timeout 65;" \
        "" \
        "    server {" \
        "        listen 443 ssl;" \
        "        server_name localhost;" \
        "" \
        "        ssl_certificate     server.crt;" \
        "        ssl_certificate_key server.key;" \
        "        ssl_protocols       TLSv1.3;" \
        "        ssl_conf_command    Groups X25519MLKEM768;" \
        "        # works with nginx but not chrome: ssl_conf_command    Groups MLKEM768;" \
        "" \
        "        location / {" \
        "            root  html;" \
        "            index index.html index.htm;" \
        "        }" \
        "" \
        "        error_page 500 502 503 504 /50x.html;" \
        "        location = /50x.html {" \
        "            root html;" \
        "        }" \
        "    }" \
        "}" \
        > /etc/nginx/nginx-ssl.conf

    echo hi
EOT

EXPOSE 80 443
STOPSIGNAL SIGQUIT
# run with "docker run test -g 'daemon off;' -c /etc/nginx/nginx-ssl.conf" for ssl
CMD ["nginx", "-g", "daemon off;"]


# default nginx config:
 #worker_processes  1;
 #events {
 #    worker_connections  1024;
 #}
 #http {
 #    include       mime.types;
 #    default_type  application/octet-stream;
 #    sendfile        on;
 #    keepalive_timeout  65;
 #    server {
 #        listen       80;
 #        server_name  localhost;
 #        location / {
 #            root   html;
 #            index  index.html index.htm;
 #        }
 #        error_page   500 502 503 504  /50x.html;
 #        location = /50x.html {
 #            root   html;
 #        }
 #    }
 #}
