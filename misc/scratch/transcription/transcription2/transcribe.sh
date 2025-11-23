#!/bin/bash
# standard bash script prefix
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euo pipefail  # exit on first error, print commands
if [[ -n "${DEBUG:-}" ]]; then set -x; fi # debug mode
full="$(readlink -f "$BASH_SOURCE")"; dir=${full%\/*}; file=${full##*/};

input_file="$(readlink -f "$1")"
output_file="$(readlink -f "$2")"

cd "${dir}"

if ! [[ -f build/input.wav ]]; then
  mkdir -p build/
  ffmpeg -y -i "${input_file}" -vn -acodec pcm_s16le build/input.wav
fi


if ! [[ -f build/output-starts.txt ]]; then
  ffmpeg -i build/input.wav -af silencedetect=noise=-50dB:d=0.5 -f null - 2>&1 \
    | grep '^\[silencedetect ' \
    | sed -e 'N;s/\n/ /' \
    | grep -oE 'silence_(start|end): [0-9.]+' \
    | sed -e 1d \
    | sed -e 'N;s/\n/ /' \
    | awk ' NR == 1 { print "0-" $2 } { print $2 "-" $4 } ' \
    > build/output-starts.txt
fi

mkdir -p build/parts

OPENAI_API_KEY=$(cat ~/.openaitoken)
if [[ -z ${OPENAI_API_KEY} ]]; then echo "no openai token"; exit 1; fi

for each_start in $(cat build/output-starts.txt); do
  echo "queueing start/end ${each_start}"

  diff=$(awk "BEGIN { print ${each_start/*-/} - ${each_start/-*/} }")
  if awk "BEGIN { exit !($diff < 1.5) }"; then
    echo "start/end ${each_start} is shorter than 1.5 seconds which is too short"
    continue
  fi

  jobs_count=$(jobs -p | wc -l)
  queue_attempts=0
  success=
  while (( queue_attempts++ < 60 )); do
    jobs_count=$(jobs -p | wc -l)
    if (( jobs_count < 100 )); then
      success=true
      break
    else
      sleep 1
    fi
  done

  if [[ ${success} == true ]]; then
    {
      echo "running ${each_start} in bg"
      part_wav=build/parts/part-${each_start}.wav
      part_wav_log=build/parts/part-${each_start}.wav.log
      if ! [[ -f ${part_wav} ]]; then
        echo "for ${each_start} - part_wav being created"
        ffmpeg -y -ss ${each_start/-*/} -i build/input.wav -t ${diff} ${part_wav} \
          > ${part_wav_log} 2>&1;
      else
        echo "for ${each_start}, part_wav already exists"
      fi

      part_txt=build/parts/part-${each_start}.txt
      part_txt_log=build/parts/part-${each_start}.txt.log
      if ! [[ -f ${part_txt} ]]; then
        echo "for ${each_start} - part_txt being created"
        curl https://api.openai.com/v1/audio/transcriptions \
          -H "Authorization: Bearer ${OPENAI_API_KEY}" \
          -H "Content-Type: multipart/form-data" \
          -F "file=@${part_wav}" \
          -F "model=gpt-4o-transcribe" \
          -F "response_format=text" \
          -o ${part_txt} \
          > ${part_txt_log} 2>&1
      else
        echo "for ${each_start} - part_txt already exists"
      fi
    } &
  else
    echo "not success while queueing start/end ${each_start}"
    break
  fi
done
wait

echo -e "Transcript:\n\n\n" > ${output_file}
for each_start in $(cat build/output-starts.txt); do
  if [[ -s build/parts/part-${each_start}.txt ]]; then
    echo "section ${each_start}" >> ${output_file}
    cat build/parts/part-${each_start}.txt >> ${output_file}
    echo >> ${output_file}
  fi
done
