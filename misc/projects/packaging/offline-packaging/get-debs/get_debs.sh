#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eux -o pipefail
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";
cd "$dir"

mkdir -p build/

ubuntu_version=${UBUNTU_VERSION:-24.04}

if ! docker image inspect ubuntu:${ubuntu_version} >/dev/null 2>&1 ; then echo "unknown ubuntu: ${ubuntu_version}"; exit 1; fi

c_tmp="--rm -it"
c_batch="-e DEBIAN_FRONTEND=noninteractive"
c_no_apt_conf="--mount type=tmpfs,destination=/etc/apt/apt.conf.d"
c_v="-v ./build/output-${ubuntu_version}:/output"

docker create --name get-debs $c_tmp $c_batch $c_no_apt_conf $c_v --init ubuntu:${ubuntu_version} sleep infinity

docker start get-debs
trap 'docker stop get-debs' EXIT

if [[ -z ${CLEAN:-} ]]
then
if [[ -d build/output-${ubuntu_version}/archives ]]; then docker cp build/output-${ubuntu_version}/archives get-debs:/var/cache/apt; fi
if [[ -d build/output-${ubuntu_version}/lists ]]; then docker cp build/output-${ubuntu_version}/lists get-debs:/var/lib/apt; fi
if [[ -d build/output-${ubuntu_version}/sources ]]; then docker cp build/output-${ubuntu_version}/lists get-debs:/etc/apt/sources.list.d; fi
fi

docker exec get-debs bash -c '
  echo "[$(date --iso=s)] Welcome to get_debs.sh!"
  mkdir -p /output/{archives,lists,sources};

  status_updated=
  apt update >/dev/null 2>&1 && status_updated=true || status_updated=false;
  echo "[$(date --iso=s)] Updated with apt update: ${status_updated}"
  if ! [[ ${status_updated} == "true" ]]; then echo "not successful: update"; exit 1; fi;

  status_base=
  echo "tzdata tzdata/Areas select America"             | debconf-set-selections;
  echo "tzdata tzdata/Zones/America select Los_Angeles" | debconf-set-selections;
  apt-get -o APT::Keep-Downloaded-Packages=true install -y htop nmap jq fdisk vim nginx-full curl wget net-tools openssh-server openssh-client software-properties-common docker.io docker-compose-v2 openjdk-21-jre-headless >/dev/null 2>&1 && status_base=true || status_base=false;
  echo "[$(date --iso=s)] Installed base packages and utilities: ${status_base}"
  if ! [[ ${status_base} == "true" ]]; then echo "not successful: base"; exit 1; fi;

  status_dead_snakes=
  add-apt-repository ppa:deadsnakes/ppa -y && status_dead_snakes=true || status_dead_snakes=false;
  echo "[$(date --iso=s)] Installed deadsnakes repo: ${status_dead_snakes}"
  if ! [[ ${status_dead_snakes} == "true" ]]; then echo "not successful: deadsnakes repo"; exit 1; fi;

  status_python_311=
  apt-get -o APT::Keep-Downloaded-Packages=true install -y python3.11-venv python3.11-dev python3.11-full python-is-python3 >/dev/null 2>&1 && status_python_311=true || status_python_311=false;
  echo "[$(date --iso=s)] Installed python: ${status_dead_snakes}"

  status_k8s_prereq=
  apt-get install -y apt-transport-https ca-certificates curl gnupg >/dev/null 2>&1 && status_k8s_prereq=true || status_k8s_prereq=false
  echo "[$(date --iso=s)] Installed Kubernetes pre-reqs: ${status_k8s_prereq}"
  if ! [[ ${status_k8s_prereq} == "true" ]]; then echo "not successful: k8s_prereq"; exit 1; fi;

  status_k8s=
  curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.34/deb/Release.key | gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
  echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.34/deb/ /" | tee /etc/apt/sources.list.d/kubernetes.list > /dev/null
  chmod 644 /etc/apt/keyrings/kubernetes-apt-keyring.gpg /etc/apt/sources.list.d/kubernetes.list
  { apt update >/dev/null 2>&1 && apt-get install -y cri-tools kubeadm kubectl kubelet kubernetes-cni >/dev/null 2>&1; } && status_k8s=true || status_k8s=false
  if ! [[ ${status_k8s} == "true" ]]; then echo "not successful: k8s_prereq"; exit 1; fi;

  echo "[$(date --iso=s)] Copying to output"
  cp -v /etc/apt/sources.list /output/sources/;
  cp -v /etc/apt/sources.list.d/* /output/sources/;
  cp -v /var/lib/apt/lists/* /output/lists/;
  cp -v /var/cache/apt/archives/*.deb /output/archives;
  echo "[$(date --iso=s)] Done!"
'

echo "copy with 'rsync -rt --ignore-existing --exclude=lock build/output-${ubuntu_version}/ \$destination/output-${ubuntu_version}/'"
