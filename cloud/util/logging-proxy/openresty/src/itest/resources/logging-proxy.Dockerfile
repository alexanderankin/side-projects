FROM ubuntu:22.04
# refer to https://gist.github.com/alexanderankin/d9e084f64f5166250710170cec97d7bd
SHELL ["bash", "-c"]
RUN \
  set -eux -o pipefail; \
  apt update && \
  # install wget and gpg for apt key downloading
  apt install -y wget gpg gnupg-l10n- && \
  # add openresty key
  wget -O - https://openresty.org/package/pubkey.gpg \
    | gpg --dearmor \
    | tee /etc/apt/trusted.gpg.d/openresty.gpg \
    > /dev/null \
  && \
  arch=$(case $(uname -m) in x86_64) echo amd64;; arm64) echo arm64;; *) echo all;; esac) && \
  c=$(. /etc/os-release; echo $VERSION_CODENAME) && \
  \
  # add openresty repo
  # 'lsb_release -sc' not available in minimal envs
  echo "deb [arch=$arch signed-by=/etc/apt/trusted.gpg.d/openresty.gpg] https://openresty.org/package/ubuntu $c main" \
    | tee /etc/apt/sources.list.d/openresty.list \
    > /dev/null \
  && \
  apt update \
  && \
  # no curl and no perl
  #apt install -y openresty curl- perl- \
  apt install -y --no-install-recommends openresty

# hide openresty
RUN \
   rm -rf /usr/local/openresty/nginx/html/ ; \
   mkdir -p /usr/local/openresty/nginx/html/ && \
   echo '<h1>It Works!</h1>' > /usr/local/openresty/nginx/html/index.html && \
   sed -i 's/http {/http { server_tokens off; more_clear_headers server;/' /usr/local/openresty/nginx/conf/nginx.conf

CMD ["openresty", "-g", "daemon off;"]
