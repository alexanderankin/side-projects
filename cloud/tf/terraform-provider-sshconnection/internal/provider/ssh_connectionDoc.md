Starts `ssh -N` and waits up to 60 seconds for authentication, local listeners, and server acknowledgement of remote
forwards. Stops SSH on failure, cancellation, or close. Readiness does not check the services behind the forwards. SSH
must be available on `PATH`. User and system SSH configuration files are disabled with `-F none`; specify connection
settings in Terraform.
