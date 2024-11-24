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
except subprocess.CalledProcessError as e:
    print(e.output)
    print(e.stderr)
    raise e
