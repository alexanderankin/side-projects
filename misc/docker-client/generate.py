from json import dumps
from pathlib import Path

from openapi_python_generator.__main__ import main
from requests import get
from yaml import safe_load

# https://docs.docker.com/reference/api/engine/version/v1.45.yaml


source = "https://docs.docker.com/reference/api/engine/version/v1.45.yaml"
source_fname = source.split("/")[-1]
source_target = Path.home() / ".cache" / "docker-client-generate" / source_fname
if not source_target.exists():
    source_target.parent.mkdir(parents=True, exist_ok=True)
    conversion_url = f"https://converter.swagger.io/api/convert?url={source}"
    source_target.write_text(get(conversion_url).text)

# do not convert yaml to json here because it actually also needs converted from 2.0 to 3.0

# source_json = Path.home() / ".cache" / "docker-client-generate" / (source_fname + ".json")
# if not source_json.exists():
#     source_json.write_text(dumps(safe_load(source_target.read_text())))

output_dir = Path(__file__).parent / "docker_client" / "generated"
output_dir.mkdir(parents=True, exist_ok=True)

main([str(source_target), str(output_dir)])
