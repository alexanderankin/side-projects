variable "ssh_destination" {
  description = "SSH destination used to establish the tunnel (for example, user@example.com)."
  type        = string
}

variable "identity_file" {
  description = "Optional SSH identity file. Normal ssh-agent and config-file discovery applies when null."
  type        = string
  default     = null
}

variable "database_host" {
  description = "Database hostname as seen by the SSH server."
  type        = string
}

variable "database_port" {
  description = "Database port as seen by the SSH server."
  type        = number
  default     = 5432
}

variable "local_port" {
  description = "Local loopback port exposed by the tunnel."
  type        = number
  default     = 15432
}

ephemeral "ssh_connection" "database" {
  destination   = var.ssh_destination
  identity_file = var.identity_file

  listen {
    bind_address = "127.0.0.1"
    port         = tostring(var.local_port)
    host         = var.database_host
    host_port    = tostring(var.database_port)
  }
}

