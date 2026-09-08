#!/usr/bin/env bash
set -eu -o pipefail

rpcbind -w
exec ganesha.nfsd -F -L STDOUT
