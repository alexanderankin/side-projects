#!/bin/bash
# standard bash script prefix
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euxo pipefail  # Debug mode, exit on first error, print commands
full="$(readlink -f "$BASH_SOURCE")"; dir=${full%\/*}; file=${full##*/};

input_file="$(readlink -f "$1")"
output_dir="$(readlink -f "$2")"

cd "${dir}"

mkdir -p build/
cp "${input_file}" build/

input_file_name="$(basename "${input_file}")"
input_file="build/${input_file_name}"

mkdir -p "${output_dir}"

ffmpeg -i "${input_file}" -vn -acodec pcm_s16le build/output.wav
