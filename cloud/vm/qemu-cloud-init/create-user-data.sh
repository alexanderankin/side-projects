#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euo pipefail
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

cat <<EOF
#cloud-config
user:
  name: ubuntu
  sudo: ALL=(ALL) NOPASSWD:ALL
  groups: sudo, users, admin
  shell: /bin/bash
  lock_passwd: false
  passwd: "$(mkpasswd ubuntu)"
  hashed_passwd: "$(mkpasswd ubuntu)"
  ssh_authorized_keys: $(cat \
    <(cat ~/.ssh/id_rsa.pub) \
    <(curl -fSsL https://github.com/alexanderankin.keys) \
    | sed -e '/^$/d' -e 's/^/"/' -e 's/$/"/' \
    | jq -sc)

users:
  - default
  - name: testuser
    groups: users
    shell: /bin/bash
    lock_passwd: false
    passwd: "$(mkpasswd testuser)"
    hashed_passwd: "$(mkpasswd testuser)"
    ssh_authorized_keys: $(cat \
      <(cat ~/.ssh/id_rsa.pub) \
      | sed -e '/^$/d' -e 's/^/"/' -e 's/$/"/' \
      | jq -sc)

ssh_pwauth: true
disable_root: true

package_update: true
packages: [ htop, jq, curl, wget, git, net-tools, gpg ]

EOF
exit 0

cat <<'EOF'
runcmd:
  - |
    echo welcome to the "'user data' part of the 'user-data'"
    rm -vf /etc/apt/apt.conf.d/99timeout
    echo 'DPkg::Lock::Timeout=60;' | tee -a /etc/apt/apt.conf.d/99timeout
    echo 'Acquire::http::Timeout "10";' | tee -a /etc/apt/apt.conf.d/99timeout
    echo 'Acquire::ftp::Timeout "10";' | tee -a /etc/apt/apt.conf.d/99timeout

    _log_with_timestamp() { echo "$(date +%FT%T%z) $@"; }

    lslocks -u | grep -q /var/lib/apt/lists/lock && {
      _log_with_timestamp "something is doing background things with apt"
      counter=0
      while lslocks -u | grep -q /var/lib/apt/lists/lock; do
        if ! (( $counter % 5 )); then _log_with_timestamp "still waiting after $counter seconds"; fi
        counter=$((counter+1))
        if ! (( $counter < 55 )); then _log_with_timestamp killing apt; kill $(pgrep apt); fi
        if ! (( $counter < 60 )); then _log_with_timestamp killing apt; kill -9 $(pgrep apt); fi
        if ! (( $counter < 65 )); then _log_with_timestamp breaking; break; fi
        sleep 1
      done
    }

    wget -q https://packagecloud.io/fdio/release/gpgkey -O /usr/share/keyrings/vpp.asc
    echo "deb [signed-by=/usr/share/keyrings/vpp.asc] https://packagecloud.io/fdio/master/ubuntu $(. /etc/os-release; echo $VERSION_CODENAME) main" | tee /etc/apt/sources.list.d/vpp.list
    apt-get update
    DEBIAN_FRONTEND=noninteractive apt-get install vpp vpp-plugin-core vpp-plugin-dpdk -y
EOF
