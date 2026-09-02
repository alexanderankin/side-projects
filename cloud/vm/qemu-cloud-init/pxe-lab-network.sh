#!/usr/bin/env bash
set -euo pipefail

namespace=pxe-server
bridge=pxe-br
server_link=pxes0
namespace_link=pxens0
client_tap=pxe-tap
server_address=192.168.50.1/24

usage() {
    echo "usage: sudo $0 {up USER|down|status|exec COMMAND...}" >&2
    exit 2
}

require_root() {
    if [[ $EUID -ne 0 ]]; then
        echo "this action must run as root" >&2
        exit 1
    fi
}

namespace_exists() {
    ip netns list | awk '{print $1}' | grep -Fxq "$namespace"
}

link_exists() {
    ip link show dev "$1" >/dev/null 2>&1
}

network_up() {
    local owner=${1:-}
    [[ -n $owner ]] || usage
    require_root
    id "$owner" >/dev/null

    if namespace_exists || link_exists "$bridge" || link_exists "$server_link" || link_exists "$client_tap"; then
        echo "the PXE lab network already exists or is only partially configured" >&2
        echo "inspect with '$0 status' or reset it with 'sudo $0 down'" >&2
        exit 1
    fi

    ip netns add "$namespace"
    trap 'network_down >/dev/null 2>&1 || true' ERR

    ip link add "$bridge" type bridge
    ip link add "$server_link" type veth peer name "$namespace_link"
    ip link set "$namespace_link" netns "$namespace"
    ip tuntap add dev "$client_tap" mode tap user "$owner"

    ip link set "$server_link" master "$bridge"
    ip link set "$client_tap" master "$bridge"
    ip link set "$bridge" up
    ip link set "$server_link" up
    ip link set "$client_tap" up

    ip -n "$namespace" link set lo up
    ip -n "$namespace" link set "$namespace_link" up
    ip -n "$namespace" address add "$server_address" dev "$namespace_link"
    ip -n "$namespace" route add 255.255.255.255/32 dev "$namespace_link"

    trap - ERR
    echo "PXE lab network is up; server namespace address is $server_address"
}

network_down() {
    require_root

    if link_exists "$client_tap"; then
        ip link delete "$client_tap"
    fi
    if link_exists "$server_link"; then
        ip link delete "$server_link"
    fi
    if link_exists "$bridge"; then
        ip link delete "$bridge"
    fi
    if namespace_exists; then
        ip netns delete "$namespace"
    fi
}

network_status() {
    require_root
    echo "namespace:"
    ip netns list | awk -v ns="$namespace" '$1 == ns { found=1; print } END { if (!found) print "  down" }'
    echo "links:"
    for link in "$bridge" "$server_link" "$client_tap"; do
        if link_exists "$link"; then
            ip -brief link show dev "$link"
        else
            echo "  $link: down"
        fi
    done
    if namespace_exists; then
        echo "namespace addresses:"
        ip -n "$namespace" -brief address
    fi
}

network_exec() {
    shift
    [[ $# -gt 0 ]] || usage
    require_root
    if ! namespace_exists; then
        echo "PXE lab network is down; run 'sudo $0 up USER' first" >&2
        exit 1
    fi
    exec ip netns exec "$namespace" "$@"
}

case ${1:-} in
    up) network_up "${2:-}" ;;
    down) network_down ;;
    status) network_status ;;
    exec) network_exec "$@" ;;
    *) usage ;;
esac
