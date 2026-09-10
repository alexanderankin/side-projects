.PHONY: run
run:
	TF_CLI_CONFIG_FILE=../terraform-provider-sshconnection/build/fs-mirror.tfrc tofu apply -auto-approve

.PHONY: init
init:
	rm -f .terraform.lock.hcl
	TF_CLI_CONFIG_FILE=../terraform-provider-sshconnection/build/fs-mirror.tfrc tofu init
