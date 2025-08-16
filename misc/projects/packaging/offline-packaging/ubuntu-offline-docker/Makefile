autoinstall.yaml: install.yaml
	cat install.yaml | yaml2json | jq '{ autoinstall: . }' | json2yaml > autoinstall.yaml
