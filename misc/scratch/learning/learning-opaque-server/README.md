# Local OPAQUE example

This Spring Boot server exposes the [Hofmann OPAQUE endpoints](https://github.com/codeheadsystems/hofmann-elimination)
at `/opaque/**`. The client performs the RFC 9807 registration and login handshakes; the password is never sent to the
server.

Run from the repository root:

```sh
export OPRF_MASTER_KEY_HEX="$(openssl rand -hex 32)"
./gradlew :misc:scratch:learning:learning-opaque-server:run
```

In another terminal:

```sh
./gradlew :misc:scratch:learning:learning-opaque-client:installDist
misc/scratch/learning/learning-opaque-client/build/install/learning-opaque-client/bin/learning-opaque-client register --user alice
misc/scratch/learning/learning-opaque-client/build/install/learning-opaque-client/bin/learning-opaque-client login --user alice
```

The client prompts for the password in a terminal. Use `--server http://host:port` before the subcommand to change the
URL. For automation or Gradle `run`, add `--password-stdin` to either subcommand and pipe in one password line.

This is a local learning example. Credentials and sessions are held in memory. The server binds to loopback and allows
ephemeral keys, so restarting it loses registered users. For a persistent deployment, configure a durable credential
store and stable OPAQUE/JWT keys, and serve registration over HTTPS.
