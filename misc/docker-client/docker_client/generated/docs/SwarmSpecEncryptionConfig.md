# SwarmSpecEncryptionConfig

Parameters related to encryption-at-rest.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**auto_lock_managers** | **bool** | If set, generate a key and use it to lock data stored on the managers.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_spec_encryption_config import SwarmSpecEncryptionConfig

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpecEncryptionConfig from a JSON string
swarm_spec_encryption_config_instance = SwarmSpecEncryptionConfig.from_json(json)
# print the JSON string representation of the object
print(SwarmSpecEncryptionConfig.to_json())

# convert the object into a dict
swarm_spec_encryption_config_dict = swarm_spec_encryption_config_instance.to_dict()
# create an instance of SwarmSpecEncryptionConfig from a dict
swarm_spec_encryption_config_from_dict = SwarmSpecEncryptionConfig.from_dict(swarm_spec_encryption_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


