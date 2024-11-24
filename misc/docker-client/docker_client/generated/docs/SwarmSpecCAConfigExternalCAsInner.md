# SwarmSpecCAConfigExternalCAsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**protocol** | **str** | Protocol for communication with the external CA (currently only &#x60;cfssl&#x60; is supported).  | [optional] [default to 'cfssl']
**url** | **str** | URL where certificate signing requests should be sent.  | [optional] 
**options** | **Dict[str, str]** | An object with key/value pairs that are interpreted as protocol-specific options for the external CA driver.  | [optional] 
**ca_cert** | **str** | The root CA certificate (in PEM format) this external CA uses to issue TLS certificates (assumed to be to the current swarm root CA certificate if not provided).  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_spec_ca_config_external_cas_inner import SwarmSpecCAConfigExternalCAsInner

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpecCAConfigExternalCAsInner from a JSON string
swarm_spec_ca_config_external_cas_inner_instance = SwarmSpecCAConfigExternalCAsInner.from_json(json)
# print the JSON string representation of the object
print(SwarmSpecCAConfigExternalCAsInner.to_json())

# convert the object into a dict
swarm_spec_ca_config_external_cas_inner_dict = swarm_spec_ca_config_external_cas_inner_instance.to_dict()
# create an instance of SwarmSpecCAConfigExternalCAsInner from a dict
swarm_spec_ca_config_external_cas_inner_from_dict = SwarmSpecCAConfigExternalCAsInner.from_dict(swarm_spec_ca_config_external_cas_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


