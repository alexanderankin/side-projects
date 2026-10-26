# QEMU offline packaging integration test

Install the harness and test tools into the existing virtual environment, then
run from the project root:

```sh
tests/.venv/bin/python -m pip install -e './tests[test]'
tests/.venv/bin/qemu-offline-test
```

`tests/.venv/bin/python -m qemu_offline` is an equivalent entry point.
Use `--project /path/to/pom-xml` when running from another directory. The default
cloud image is resolved relative to that project, while an explicit `--image`
is resolved relative to the current directory.

`tests/pyproject.toml` defines the installable package, test dependencies,
console command, pytest discovery, and Ruff configuration. Python sources are
under `tests/src` to keep them separate from the Java sources:

```text
tests/
  pyproject.toml
  requirements.txt
  README.md
  src/qemu_offline/
    __init__.py
    __main__.py
    runner.py
    runs.py
    guest.sh
    verify.gradle
  test/
    test_runner.py
    test_runs.py
```

The guest scripts are installed as package data. Tests use normal package imports.

Requires an ARM64 macOS or Linux host, Python 3.11+, `qemu-system-aarch64`,
`qemu-img`, `xorriso`, `ssh-keygen`, and ARM64 UEFI firmware. QEMU tries **HVF then KVM**;
it fails if neither works and never uses TCG. On Linux, the user needs access
to `/dev/kvm`. An x86 host cannot hardware accelerate this ARM64 image.

The default image is the existing cache at
`../../../../../cloud/vm/qemu-cloud-init/build/resolute-server-cloudimg-arm64.img`.
The runner creates a fresh 24 GiB copy-on-write overlay; the cached image is
not modified. Each run uses 4 GiB RAM and two CPUs by default.

1. Boot with QEMU user networking. Cloud-init installs OpenJDK 25 and runs
   `./gradlew -DcreatingCache=true clean build bootJar shadowJar goOfflineMavenLocal`, exporting
   into a dedicated Maven repository in the guest.
2. Shut down and boot that overlay with **`-nic none`**, with no network backend
   or cloud-init seed attached. Assert that the guest has only loopback. Run
   `./gradlew --offline -DcreatingCache=false clean build bootJar shadowJar` and resolve every
   resolvable configuration from `mavenLocal()`. The Gradle user home is fresh
   except for the wrapper distribution; project caches and build outputs were
   removed. A final negative control must fail resolution with an empty Maven
   repository and another fresh Gradle home. Both Spring Boot and Shadow must
   actually apply and produce their archives; plugin resolution also uses only
   `mavenLocal()` in the offline phase.

Both Gradle commands explicitly set `-Dmaven.repo.local` to the test repository.
`-DcreatingCache` takes precedence over the existing `-PcreatingCache` option.
This verifies compilation, packaging, and dependency resolution; it does not
start the Spring application or external database/message broker services.

`goOfflineMavenLocal` resolves application and buildscript configurations and
exports the downloaded Gradle module cache in Maven layout. That includes plugin
marker POMs, plugin implementation JARs and their dependencies, parent POMs,
imported BOMs, and Gradle `.module` metadata. Use a dedicated `GRADLE_USER_HOME`
outside this suite too if you want to avoid exporting unrelated cached modules.
Plugin JARs are applied through `plugins {}` instead of being packaged as
application dependencies.

No SSH access or host port forwarding is used during the test. Results come from the serial
console. A zero QEMU exit alone is insufficient: each guest phase must emit its
run-specific PASS marker. A phase times out after 30 minutes by default.
Interrupting the runner terminates the VM.

`build/qemu-tests/run-*/` contains `phase-*-stdout.log`, `phase-*-stderr.log`, exact QEMU
commands as `phase-command-*.json`, `junit.xml`, the seed, the overlay, and a
run-specific SSH key (`ssh_ed25519`). Artifacts are retained
on success and failure for diagnosis; remove old run directories when finished.
While running, follow the displayed run directory's `phase-online-stdout.log` or `phase-offline-stdout.log`
with `tail -f`.

## Explore a successful run offline

```sh
tests/.venv/bin/qemu-offline-test list
tests/.venv/bin/qemu-offline-test test RUN_DIRECTORY_NAME
# Choose another SSH port if needed:
tests/.venv/bin/qemu-offline-test test RUN_DIRECTORY_NAME --ssh-port 2223
```

`list` shows each run's test status and whether exploration is ready. `test`
only prints commands; it does not start QEMU or change the saved disk. The same
commands are printed automatically after a successful test run. Copy the QEMU
command into one terminal, then the SSH command into another once the VM boots.

The QEMU command uses the successful offline boot's hardware configuration and
adds `-snapshot`, so disk changes made during exploration are discarded on exit.
It uses `restrict=on,ipv6=off` user networking with a single SSH forward bound to
`127.0.0.1`. The guest cannot reach the host or Internet; only the explicitly
forwarded SSH connection is allowed. This mode has a virtual NIC for SSH; the
automated offline proof still uses **`-nic none`**. See the
[QEMU networking and snapshot options](https://www.qemu.org/docs/master/system/invocation.html).

SSH logs in as `ubuntu` with the private key saved in that run directory. Inside
the VM:

```sh
sudo -i
cd /opt/offline-test/project
GRADLE_USER_HOME=/opt/offline-test/gradle-offline ./gradlew --offline \
  -DcreatingCache=false -Dmaven.repo.local=/opt/offline-test/m2 clean build bootJar shadowJar
```

The Maven repository and both Gradle homes are under `/opt/offline-test`. The
offline Gradle home now contains caches from the successful offline build. For
another cold-cache experiment, copy only its `wrapper/` into a new Gradle home
and remove the project's `.gradle/` and `build/` directories.

Use `sudo poweroff` to end the session. Successful runs disable the test's boot
service, so exploration does not automatically rerun the suite and shut down.
Older runs remain listed but cannot be explored through this command: make a
fresh run to provision its SSH key and the updated guest lifecycle. Failed or
incomplete runs are also rejected. Keep the run's private key private.

Overrides:

```sh
tests/.venv/bin/qemu-offline-test --image /path/to/arm64.img --firmware /path/to/QEMU_EFI.fd \
  --memory 4096 --cpus 2 --timeout 2400
```

Fast harness checks with pytest (no VM needed), using the virtual environment
and dependencies in `tests/`:

```sh
tests/.venv/bin/python -m pytest -c tests/pyproject.toml tests/test
```

Format and lint the Python code with Ruff:

```sh
tests/.venv/bin/ruff format tests
tests/.venv/bin/ruff check tests
```

References: [QEMU accelerator fallback and networking options](https://www.qemu.org/docs/master/system/invocation.html),
[Gradle system and project properties](https://docs.gradle.org/current/userguide/build_environment.html).
