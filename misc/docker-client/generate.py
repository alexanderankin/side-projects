import os
from re import compile, M
import shutil
import subprocess
from pathlib import Path

from requests import get

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

generator_url = "https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/7.10.0/openapi-generator-cli-7.10.0.jar"
generator_url_fname = generator_url.split("/")[-1]
generator_url_target = Path.home() / ".cache" / "docker-client-generate" / generator_url_fname
if not generator_url_target.exists():
    generator_url_target.parent.mkdir(parents=True, exist_ok=True)
    generator_url_target.write_bytes(get(generator_url).content)

output_dir = Path(__file__).parent / "docker_client" / "generated"
output_dir.mkdir(parents=True, exist_ok=True)

java = shutil.which("java")
if not java:
    raise ChildProcessError("no java in PATH")

# from wget -N 'https://repo1.maven.org/maven2/org/openapitools/openapi-generator/7.10.0/openapi-generator-7.10.0.jar'
generator = str(generator_url_target)

# subprocess.run(executable=java, args=["java", "-jar", generator, "help", "generate"], encoding="utf-8", check=True)
try:
    subprocess.run(
        executable=java,
        args=[
            "java",
            "-jar",
            generator,
            "generate",
            "-g",
            "python",
            "--package-name",
            "docker_client.generated",
            "-i",
            str(source_target),
            "-o",
            str(output_dir)
        ],
        encoding="utf-8",
        check=True
    )

    replacement_strs = [
        ("^from docker_client.generated", "^from docker_client.generated.docker_client.generated"),
        ("^import docker_client.generated", "^import docker_client.generated.docker_client.generated"),
        # ("^from pydantic", "^from docker_client.pydantic_shim"),
        # ("^import pydantic", "^import docker_client.generated.docker_client.generated"),
    ]
    replacements = [(compile(p1, M), compile(p2, M)) for p1, p2 in replacement_strs]

    for walked_dir, dirs, files in os.walk(top=output_dir):
        # print(f"{walked_dir} has {dirs}, {files}")
        for file in files:
            file_path = Path(walked_dir) / file
            file_text = file_path.read_text()
            original_text = file_text

            for replace_from, replace_to in replacements:
                if replace_from.search(file_text) and not replace_to.search(file_text):
                    file_text = replace_from.sub(replace_to.pattern[1:], file_text)

            if original_text != file_text:
                file_path.write_text(file_text)

except subprocess.CalledProcessError as e:
    print(e.output)
    print(e.stderr)
    raise e
