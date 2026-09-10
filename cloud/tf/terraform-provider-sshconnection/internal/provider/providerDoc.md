The SSH provider supplies one ephemeral resource, `ssh_connection`. It shells out to the `ssh` executable on `PATH`; it
does not implement SSH itself. The connection is started when the ephemeral resource opens and stopped when the
operation no longer needs it.

This provider has no configuration arguments.

It could foreseeably take arguments such as specifying the path of the ssh program to use, picking ssh implementation
(native go vs command line openssh) and setting default ssh options. 
