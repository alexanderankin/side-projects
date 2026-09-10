# Make the SSH provider understandable and easier to change

## 1. Goal and compatibility

Make the code readable to someone who understands Terraform and SSH but does not know Go. Prefer explicit steps and
descriptive names over clever helpers, extra interfaces, or compact syntax.

Preserve the test project's essential behavior:

1. Terraform opens the ephemeral connection.
2. SSH creates `127.0.0.1:18080 → remote 127.0.0.1:80`.
3. Opening waits for SSH authentication and forwarding setup.
4. The dependent `curl` command uses the tunnel.
5. Closing the resource, failed startup, or provider termination cleans up SSH.

The existing `go test -race ./...` passes. It covers real SSH forwarding and supervisor shutdown, but there are no
provider lifecycle tests.

Include bug fixes alongside the cleanup. The intentional compatibility changes are rejecting conflicting custom options
and disabling external SSH configuration.

## 2. Make configuration explicit

### Disable external SSH configuration

Always launch SSH with `-F none`. This skips both user and system SSH configuration, as documented in
the [OpenSSH manual](https://man.openbsd.org/ssh#F).

- Keep `config_file` temporarily as a deprecated attribute so existing users receive an actionable migration error.
- Accept omission or `none`; reject every other value before starting a process.
- Explain that host aliases, usernames, ports, identities, and forwards previously supplied by SSH config must move into
  Terraform.
- Preserve ordinary SSH agent use, default identity-file discovery, and known-hosts checks.
- Reject configuration-only directives such as `Include`, `Host`, and `Match` in `ssh_option`, with an explanation
  rather than a delayed SSH failure.
- Do not add a config inspection subprocess or implement an SSH config parser.

The local OpenSSH source confirms that generated jump-host commands inherit `-F none`. Document that other destination
options, such as an identity file, are not automatically jump-host options. Explicit user-supplied `ProxyCommand`
programs remain user-controlled; the provider does not rewrite their commands.

### Define required options once

Replace the policy split between argument construction and process startup with one small, ordered definition. Each
entry contains its name, required value, and plain-English reason.

| Setting                   | Required value           | Reason                                                     |
|---------------------------|--------------------------|------------------------------------------------------------|
| `LogLevel`                | `DEBUG1`                 | Readiness detection reads SSH's startup messages.          |
| `ExitOnForwardFailure`    | `yes`                    | A failed forward must fail opening.                        |
| `ForkAfterAuthentication` | `no`                     | SSH must remain owned by the supervisor.                   |
| `ControlMaster`           | `no`                     | Do not create a shared SSH master.                         |
| `ControlPath`             | `none`                   | Do not attach to an existing master.                       |
| `ControlPersist`          | `no`                     | Do not leave a persistent shared connection.               |
| `BatchMode`               | `yes`                    | Do not wait for interactive authentication.                |
| `ClearAllForwardings`     | `no`                     | Do not silently erase requested forwards.                  |
| `SessionType`             | `none`, enforced by `-N` | This resource opens a connection without a remote command. |

Use that definition for argument generation and conflict diagnostics. Remove the duplicate `ExitOnForwardFailure`
argument.

For custom `ssh_option` blocks:

- Compare names case-insensitively.
- Accept matching managed values; omit redundant copies from the final command.
- Recognize the `DEBUG` alias for `DEBUG1`.
- Reject conflicting values at the offending block, explaining the required value and why it matters.
- Check every occurrence so duplicate blocks cannot conceal a conflict.
- Require names to be single option keywords, without whitespace or `=`; preserve ordinary option values.
- Preserve ordering and OpenSSH handling for other options, including repeatable options. Do not build a general OpenSSH
  configuration validator.

Validate known values during Terraform validation, then validate again before startup when all required values must be
available.

## 3. Simplify the implementation and fix fragile behavior

### Argument construction

Make one function produce the actual arguments that will be executed. Its output should no longer differ from the launch
path because another function silently rewrites the model.

- Build arguments in visible stages: required settings, Terraform attributes and forwards, custom options, destination.
- Keep log-file handling outside argument construction. The provider copies SSH logs itself; it must not pass `-E`.
- Preserve `quiet` as a compatibility attribute without allowing it to suppress readiness messages. Document that
  routine output is already captured internally.
- Preserve omitted versus explicitly empty forwarding bind addresses.
- Format IPv6 forwarding addresses correctly, accepting already bracketed addresses without double brackets.
- Stop on decoding errors instead of constructing partial arguments. Reject unresolved required values at startup.
- Reject an empty destination or one beginning with `-`, and use the SSH option terminator before the destination.

Keep the existing Terraform-aware model. Introducing a second model and conversion layer would add work for readers
without a current need.

### Terraform lifecycle

Separate the long schema declaration from the open/close lifecycle so readers can find either independently.

Make `Open` read as a short sequence: decode, validate, allocate tracking token, start, wait, register.

- Rename `processes` to `connections` and use `connection` instead of `process` for stored values.
- Preserve the shared resource instance: separate framework calls must find the same connection registry.
- Replace `strconv.Quote` plus `fmt.Sscanf` with JSON encoding and decoding. Framework private values must be valid
  JSON; retain the existing private key and JSON-string representation.
- Give the 60-second startup timeout a descriptive constant.
- Ensure every failure after startup closes the connection, including cancellation observed before registration.
- Keep slow shutdown outside the registry lock.
- Preserve harmless repeated closes and missing-token closes; report malformed private values clearly.
- Remove the redundant model alias and commented-out `instance_token` code.

Keep the small `Connection` interface because it provides a useful testing boundary. Remove speculative commentary about
future backends.

### Process ownership and readiness

Keep the supervisor architecture. The pipe is necessary because its closure lets the supervisor detect provider
termination independently of Terraform callbacks.

- Explain which process owns each pipe end, who closes it, and who waits for each child process to exit.
- Use descriptive names such as `sshExited`, `providerDisconnected`, and `readySignaled`.
- Explain channels as completion signals, mutexes as protection for shared data, and `sync.Once` as the guarantee that
  cleanup runs only once.
- Consolidate startup cleanup so pipe and log-file failures do not leave resources behind.
- Preserve the rule that canceling `WaitReady` cancels the wait; the owning caller must close the connection.
- Distinguish unexpected clean SSH exit from an exit carrying an error, avoiding messages containing `(<nil>)`.

Move readiness parsing into a dedicated file within the existing connection package.

- Separate consuming complete log lines from updating readiness state.
- Name the three recognized messages and the 16 KiB diagnostic limit.
- Keep the existing condition: entered the client loop, and all pending remote-forward replies arrived.
- Preserve timeout behavior for unrecognized messages; do not substitute sleeps or target-service probes.
- Discard an oversized line until its newline instead of interpreting a truncated suffix as a new message.
- Parse and retain diagnostics even if the optional log copy fails. Record the first file-write error, stop attempting
  that copy, and report it during cleanup without preventing SSH shutdown.

These changes should use small ordinary functions, not a generic process framework or configurable state-machine
library.

## 4. Write `internal/how_it_works.md`

Write this as a guided explanation for a non-Go programmer, with technical reference details afterward.

Include:

1. **Start with the Terraform example.** Trace `destination`, `listen`, and `depends_on` through opening, the dependent
   command, and closing.
2. **Show the process diagram.** Terraform → provider → supervisor → SSH, plus the pipe whose closure triggers cleanup.
3. **Give a reading order.** Explain the responsibilities of the provider, connection, and supervisor packages.
4. **Explain the Go needed here.** Structs, methods, interfaces, returned errors, `defer`, goroutines, channels,
   mutexes, and `sync.Once`, using short examples from this code.
5. **Show a representative SSH command.** Explain each provider-required setting and where custom options fit.
6. **Explain readiness.** Authentication and forwarding setup are checked; the remote HTTP/database service is not.
7. **Explain lifecycle ownership.** Private tokens identify in-memory connections; they are not operating-system process
   IDs.
8. **Explain configuration and logging.** Cover `-F none`, conflict errors, agent/key discovery, jump-host limitations,
   `quiet`, and provider-managed log copying.
9. **Explain failure paths and testing.** Include timeout, cancellation, failed forwarding, missing SSH, logging
   failure, and provider termination.
10. **Give a change checklist.** Adding an option requires reviewing its schema, argument mapping, conflicts, tests, and
    documentation.

Reference the inspected OpenSSH checkout at commit `7fe3b24c9`, particularly `process_config_files`,
`ssh_init_forwarding`, `forwarding_success`, and `client_loop`. Explain that the installed SSH executable can be a
different version.

Link the document from the README. Correct the Go-version mismatch and the documentation-generation instructions: the
README currently describes a separate `generated-docs/` tree, but the checked-in generator does not specify that output
location. Keep generated descriptions aligned with the schema.

## 5. Tests, implementation order, and acceptance

Implement in this order: establish missing regression tests, simplify configuration and argument construction, clean up
lifecycle and readiness code, then finish the walkthrough and public descriptions.

Add focused coverage for:

- Every managed-option conflict, matching duplicates, mixed-case names, and ordinary custom options.
- Rejected external config paths, unconditional `-F none`, and jump-command inheritance using OpenSSH configuration
  output.
- Prevention of `ClearAllForwardings=yes` silently removing the tunnel.
- Actual launch arguments, null/unknown values, empty bind addresses, and IPv6 formatting.
- Split log writes, Windows-style line endings, delayed forwarding confirmation, repeated messages, oversized lines, and
  log-copy failures.
- Provider startup failure, readiness failure, cancellation, successful registration, malformed tokens, independent
  connections, and repeated close.
- Supervisor startup failure, natural SSH exit, and provider termination.
- Existing real-SSH scenarios: usable forwarding, delayed/rejected remote forwarding, occupied local ports,
  cancellation, and released listeners.

Run `go test -race ./...` and `go vet ./...`.

Keep the test project's tunnel, dependency, and `curl` behavior as the acceptance scenario. Any host-specific settings
previously inherited from SSH config must be made explicit before its live run. Report that live result separately from
isolated tests; do not claim compatibility with `ankin.info` merely because local tests pass.

The readability acceptance check is equally important: a reader should be able to locate where options are decided,
where SSH starts, what “ready” means, and who stops it without understanding the entire repository first.
