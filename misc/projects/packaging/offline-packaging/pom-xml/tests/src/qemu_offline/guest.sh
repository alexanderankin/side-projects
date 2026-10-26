#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

set -E

# Getty can reset the serial TTY while provisioning packages. Disable it before
# opening the console so a hangup cannot silently discard the final marker.
systemctl mask --now serial-getty@ttyAMA0.service
exec > >(tee -a /var/log/offline-packaging.log > /dev/ttyAMA0) 2>&1

phase=${1}
run_id=$(cat /opt/offline-test/run-id)
log() { echo "QEMU_TEST:${run_id}:${phase}: $@"; }
log "START"

finish() {
    result=$?
    trap - EXIT
    if (( result == 0 )); then log "PASS"; else log "FAIL: ${result}"; fi
    sync
    systemctl poweroff --no-block
    exit "${result}"
}
trap finish EXIT
trap 'echo "Failed at line ${LINENO}: ${BASH_COMMAND}" >&2' ERR

export HOME=/root
cd /opt/offline-test/project

gradle_user_home_online=/opt/offline-test/gradle-online
mkdir -p ${gradle_user_home_online}
gradle_user_home_set_online() { log "setting gradle home to 'online'"; export GRADLE_USER_HOME=${gradle_user_home_online}; }
gradle_user_home_offline=/opt/offline-test/gradle-offline
mkdir -p ${gradle_user_home_offline}
gradle_user_home_set_offline() { log "setting gradle home to 'offline'"; export GRADLE_USER_HOME=${gradle_user_home_offline}; }

gradle_common_args=(--no-daemon --console=plain --max-workers=2 --no-build-cache --stacktrace)

maven_repo_local=/opt/offline-test/m2
mkdir -p ${maven_repo_local}

if [[ ${phase} == online ]]; then
  gradle_user_home_set_online

  export DEBIAN_FRONTEND=noninteractive
  apt-get -o Acquire::Retries=3 update
  apt-get -o Acquire::Retries=3 install -y openjdk-25-jdk-headless ca-certificates

  ./gradlew "${gradle_common_args[@]}" -DcreatingCache=true -Dmaven.repo.local="${maven_repo_local}" clean build bootJar shadowJar goOfflineMavenLocal

  log "testing maven_repo_local for *.pom's (in ${maven_repo_local})"
  test -n "$(find "${maven_repo_local}" -name '*.pom' -print -quit)"
  log "testing build/libs for *.jar's"
  test -n "$(find build/libs -name '*.jar' -print -quit)"

  log "cleaning up build (.gradle, build)"
  rm -rf .gradle build

  log "copying gradle wrapper"
  cp -a "${GRADLE_USER_HOME}/wrapper" /opt/offline-test/gradle-offline/

  log "enabling 'offline-packaging.service'"
  systemctl enable offline-packaging.service

  log "disabling waiting for a removed interface or cloud-init's network datasource"
  touch /etc/cloud/cloud-init.disabled
  systemctl mask systemd-networkd-wait-online.service NetworkManager-wait-online.service
elif [[ $phase == offline ]]; then
  gradle_user_home_set_offline

  interfaces=$(find /sys/class/net -mindepth 1 -maxdepth 1 -printf '%f\n')
  [[ $interfaces == lo ]] || { log 'failed to verify that only loopback exists in the guest.'; exit 1; }

  log "ensuring that no caches exist in the offline one ahead of time"
  test ! -e "${GRADLE_USER_HOME}/caches"
  test ! -e .gradle
  test ! -e build

  log "Checking that build works offline"
  ./gradlew "${gradle_common_args[@]}" --offline -DcreatingCache=false -Dmaven.repo.local="${maven_repo_local}" \
      -I /opt/offline-test/verify.gradle clean build bootJar shadowJar verifyOfflineDependencies
  test -n "$(find build/libs -name '*.jar' -print -quit)"

  log "Checking the opposite: should fail with empty gradle/maven home"
  mkdir -p /opt/offline-test/gradle-empty /opt/offline-test/empty-m2
  cp -a "$GRADLE_USER_HOME/wrapper" /opt/offline-test/gradle-empty/

  if GRADLE_USER_HOME=/opt/offline-test/gradle-empty bash ./gradlew "${gradle_common_args[@]}" --offline \
      -DcreatingCache=false -Dmaven.repo.local=/opt/offline-test/empty-m2 \
      --project-cache-dir /opt/offline-test/empty-project-cache \
      -I /opt/offline-test/verify.gradle verifyOfflineDependencies > /opt/offline-test/negative.log 2>&1; then
      log 'ERROR: dependency resolution unexpectedly succeeded with an empty Maven repository.'
      exit 1
  fi

  grep -Eq 'Could not (resolve|find)|Plugin .* was not found' /opt/offline-test/negative.log
  cat /opt/offline-test/negative.log | pr -to 2

  log 'Verified: empty Maven repository fails dependency resolution.'
else
  log "Unknown phase: $phase"
  exit 1
fi
