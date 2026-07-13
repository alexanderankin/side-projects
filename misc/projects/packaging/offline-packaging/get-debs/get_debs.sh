#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";
cd "$dir"

mkdir -p build/

ubuntu_version=${UBUNTU_VERSION}

if ! docker image inspect ubuntu:${ubuntu_version} >/dev/null 2>&1 ; then echo "unknown ubuntu: ${ubuntu_version}"; exit 1; fi

c_tmp="--rm -it"
c_batch="-e DEBIAN_FRONTEND=noninteractive"
c_no_apt_conf="--mount type=tmpfs,destination=/etc/apt/apt.conf.d"
c_v="-v ./build/output-${ubuntu_version}:/output"

docker create --name get-debs $c_tmp $c_batch $c_no_apt_conf $c_v --init ubuntu:${ubuntu_version} sleep infinity

docker start get-debs
trap 'docker stop get-debs ; while docker inspect get-debs > /dev/null 2>&1 ; do sleep 1; done' EXIT

if [[ -z ${CLEAN:-} ]]
then
if [[ -d build/output-${ubuntu_version}/archives ]]; then docker cp build/output-${ubuntu_version}/archives get-debs:/var/cache/apt; fi
if [[ -d build/output-${ubuntu_version}/lists ]]; then docker cp build/output-${ubuntu_version}/lists get-debs:/var/lib/apt; fi
if [[ -d build/output-${ubuntu_version}/sources ]]; then docker cp build/output-${ubuntu_version}/lists get-debs:/etc/apt/sources.list.d; fi
fi

docker exec get-debs bash -c '
  echo "[$(date --iso=s)] Welcome to get_debs.sh!"
  mkdir -p /output/{archives,lists,sources};

  version_number=$(. /etc/os-release; echo $VERSION_ID)
  echo version_number is ${version_number}

  status_updated=
  apt-get update >/dev/null 2>&1 && status_updated=true || status_updated=false;
  echo "[$(date --iso=s)] Updated with apt-get update: ${status_updated}"
  if ! [[ ${status_updated} == "true" ]]; then echo "not successful: update"; exit 1; fi;

  status_base=
  echo "tzdata tzdata/Areas select America"             | debconf-set-selections;
  echo "tzdata tzdata/Zones/America select Los_Angeles" | debconf-set-selections;
  apt-get -o APT::Keep-Downloaded-Packages=true install -y \
    rsync htop nmap apache2-utils tree pv jq fdisk vim nginx-full curl wget net-tools openssh-server openssh-client software-properties-common nodejs xclip dnsmasq \
    openjdk-21-jdk openjdk-21-dbg openjdk-21-doc \
    $(if [[ ${version_number} != 22.04 ]]; then echo " openjdk-25-jdk openjdk-25-dbg openjdk-25-doc "; fi) \
    $(if [[ ${version_number} == 22.04 ]]; then echo " postgresql-14 "; fi) \
    $(if [[ ${version_number} == 24.04 ]]; then echo " postgresql-16 postgresql-16-pgvector "; fi) \
    build-essential >/dev/null 2>&1 && status_base=true || status_base=false;
  echo "[$(date --iso=s)] Installed base packages and utilities: ${status_base}"
  if ! [[ ${status_base} == "true" ]]; then echo "not successful: base"; exit 1; fi;

  status_dead_snakes=
  add-apt-repository ppa:deadsnakes/ppa -y >/dev/null 2>&1 && status_dead_snakes=true || status_dead_snakes=false;
  echo "[$(date --iso=s)] Installed deadsnakes repo: ${status_dead_snakes}"
  if ! [[ ${status_dead_snakes} == "true" ]]; then echo "not successful: deadsnakes repo"; exit 1; fi;

  status_python_311=
  apt-get -o APT::Keep-Downloaded-Packages=true install -y python3.11-venv python3.11-dev python3.11-full python-is-python3 >/dev/null 2>&1 && status_python_311=true || status_python_311=false;
  echo "[$(date --iso=s)] Installed python: ${status_dead_snakes}"

  status_k8s_prereq=
  apt-get -o APT::Keep-Downloaded-Packages=true install -y apt-transport-https ca-certificates curl gnupg >/dev/null 2>&1 && status_k8s_prereq=true || status_k8s_prereq=false
  echo "[$(date --iso=s)] Installed Kubernetes pre-reqs: ${status_k8s_prereq}"
  if ! [[ ${status_k8s_prereq} == "true" ]]; then echo "not successful: k8s_prereq"; exit 1; fi;

  status_k8s=
  curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.34/deb/Release.key --output /etc/apt/keyrings/kubernetes-apt-keyring.asc
  echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.asc] https://pkgs.k8s.io/core:/stable:/v1.34/deb/ /" | tee /etc/apt/sources.list.d/kubernetes.list >/dev/null 2>&1
  chmod 644 /etc/apt/keyrings/kubernetes-apt-keyring.asc /etc/apt/sources.list.d/kubernetes.list
  { apt-get update >/dev/null 2>&1 && apt-get -o APT::Keep-Downloaded-Packages=true install -y cri-tools kubeadm kubectl kubelet kubernetes-cni >/dev/null 2>&1; } && status_k8s=true || status_k8s=false
  echo "[$(date --iso=s)] Installed Kubernetes packages: ${status_updated}"
  if ! [[ ${status_k8s} == "true" ]]; then echo "not successful: status_k8s"; exit 1; fi;

  status_helm=
  curl -fsSL https://packages.buildkite.com/helm-linux/helm-debian/gpgkey --output /usr/share/keyrings/helm.asc
  echo "deb [signed-by=/usr/share/keyrings/helm.asc] https://packages.buildkite.com/helm-linux/helm-debian/any/ any main" | tee /etc/apt/sources.list.d/helm-stable-debian.list >/dev/null 2>&1
  { apt-get update >/dev/null 2>&1 && apt-get -o APT::Keep-Downloaded-Packages=true install -y helm  >/dev/null 2>&1; } && status_helm=true || status_helm=false
  echo "[$(date --iso=s)] Installed helm: ${status_helm}"
  if ! [[ ${status_helm} == "true" ]]; then echo "not successful: helm"; exit 1; fi;

  status_docker=
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  echo "deb [signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release; echo $VERSION_CODENAME) stable" | tee /etc/apt/sources.list.d/docker.list
  { apt-get update >/dev/null 2>&1 && apt-get -o APT::Keep-Downloaded-Packages=true install -y docker-ce docker-ce-cli containerd.io=$(apt-cache madison containerd.io | grep " 2\\.1\\.[0-9]\\+" -m1 | cut -d"|" -f 2 | tr -d " ") docker-buildx-plugin docker-compose-plugin >/dev/null 2>&1; } && status_docker=true || status_docker=false
  if ! [[ ${status_docker} == "true" ]]; then echo "not successful: docker"; exit 1; fi;

  # https://www.rabbitmq.com/blog/2025/07/16/debian-apt-repositories-are-moving
  status_rabbitmq=
  curl -1sLf "https://keys.openpgp.org/vks/v1/by-fingerprint/0A9AF2115F4687BD29803A206B73A36E6026DFCA" --output /usr/share/keyrings/com.rabbitmq.team.asc > /dev/null
  codename=$(. /etc/os-release; echo $VERSION_CODENAME)
  echo "deb [arch=amd64 signed-by=/usr/share/keyrings/com.rabbitmq.team.asc] https://deb1.rabbitmq.com/rabbitmq-erlang/ubuntu/$codename $codename main" | tee /etc/apt/sources.list.d/rabbitmq-erlang.list >/dev/null 2>&1
  echo "deb [arch=amd64 signed-by=/usr/share/keyrings/com.rabbitmq.team.asc] https://deb1.rabbitmq.com/rabbitmq-server/ubuntu/$codename $codename main" | tee /etc/apt/sources.list.d/rabbitmq-server.list >/dev/null 2>&1
  { apt-get update >/dev/null 2>&1 && apt-get -o APT::Keep-Downloaded-Packages=true install -y rabbitmq-server >/dev/null 2>&1; } && status_rabbitmq=true || status_rabbitmq=false
  echo "[$(date --iso=s)] Installed rabbitmq: ${status_rabbitmq}"
  if ! [[ ${status_rabbitmq} == "true" ]]; then echo "not successful: status_rabbitmq"; exit 1; fi;

  echo "[$(date --iso=s)] Copying to output"
  rsync --recursive -u -c /etc/apt/sources.list /output/sources/sources.list;
  rsync --recursive -u -c /etc/apt/sources.list.d/. /output/sources/.;
  rsync --recursive -u -c --exclude lock --exclude partial --exclude auxfiles /var/lib/apt/lists/. /output/lists/.;
  rsync --recursive -u -c --exclude lock /var/cache/apt/archives/. /output/archives/.;
  echo "[$(date --iso=s)] Done!"
'

echo "copy with 'rsync -rt --ignore-existing --exclude=lock build/output-${ubuntu_version}/ \$destination/output-${ubuntu_version}/'"
