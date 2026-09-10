The SSH provider supplies one ephemeral resource, `sshconnection_connection`. It runs the system `ssh` executable on `PATH`, starts the connection when the resource opens, and stops it when the operation no longer needs it.

SSH always uses `-F none`, ignoring user and system SSH configuration files. Specify hosts, users, ports, custom identity paths, and forwards in Terraform. Normal SSH agent use, default identity-file discovery, and known-hosts checks still apply. The deprecated `config_file` attribute accepts only `none`.

Opening waits up to 60 seconds for authentication and forwarding setup, including server acknowledgement of remote forwards. It does not check services behind the forwards. Consumers must depend on the ephemeral resource to keep the tunnel open while using it.

Custom `ssh_option` values that conflict with settings required for readiness and process ownership are rejected. This provider has no provider-level configuration arguments.
