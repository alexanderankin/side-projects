# SwarmSpecCAConfig

CA configuration.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**node_cert_expiry** | **int** | The duration node certificates are issued for. | [optional] 
**external_cas** | [**List[SwarmSpecCAConfigExternalCAsInner]**](SwarmSpecCAConfigExternalCAsInner.md) | Configuration for forwarding signing requests to an external certificate authority.  | [optional] 
**signing_ca_cert** | **str** | The desired signing CA certificate for all swarm node TLS leaf certificates, in PEM format.  | [optional] 
**signing_ca_key** | **str** | The desired signing CA key for all swarm node TLS leaf certificates, in PEM format.  | [optional] 
**force_rotate** | **int** | An integer whose purpose is to force swarm to generate a new signing CA certificate and key, if none have been specified in &#x60;SigningCACert&#x60; and &#x60;SigningCAKey&#x60;  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_spec_ca_config import SwarmSpecCAConfig

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpecCAConfig from a JSON string
swarm_spec_ca_config_instance = SwarmSpecCAConfig.from_json(json)
# print the JSON string representation of the object
print(SwarmSpecCAConfig.to_json())

# convert the object into a dict
swarm_spec_ca_config_dict = swarm_spec_ca_config_instance.to_dict()
# create an instance of SwarmSpecCAConfig from a dict
swarm_spec_ca_config_from_dict = SwarmSpecCAConfig.from_dict(swarm_spec_ca_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


