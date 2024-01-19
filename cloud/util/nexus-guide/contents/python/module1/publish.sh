#!/usr/bin/env bash

if [[ "${0}" != "${BASH_SOURCE[0]}" ]]; then echo "should not be sourced">&2; return 1; fi

dir="$(dirname "$BASH_SOURCE")";

cd $dir

poetry build

password=password
base=http://localhost:8081

blobStoreName=$(curl -u admin:$password $base/service/rest/v1/blobstores -s | python -c 'import json,sys; print(json.load(sys.stdin)[0]["name"])')
#prints: "default"

# if it -f (fails) with 404, then create it
curl -f -u admin:$password $base/service/rest/v1/repositories/nexus-guide-python || {
  echo not found, creating nexus-guide-python ; \
  curl -u admin:$password $base/service/rest/v1/repositories/pypi/hosted \
    -X POST -H 'content-type: application/json' \
    -d '{
      "name": "nexus-guide-python",
      "online": true,
      "storage": {
        "blobStoreName": "'$blobStoreName'",
        "strictContentTypeValidation": true,
        "writePolicy": "allow"
      }
    }'
  }

poetry config repositories.nexus-guide-python http://localhost:8081/repository/nexus-guide-python/
poetry publish --repository nexus-guide-python --username admin --password $password
