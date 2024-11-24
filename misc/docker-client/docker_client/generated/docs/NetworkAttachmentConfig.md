# NetworkAttachmentConfig

Specifies how a service should be attached to a particular network. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**target** | **str** | The target network for attachment. Must be a network name or ID.  | [optional] 
**aliases** | **List[str]** | Discoverable alternate names for the service on this network.  | [optional] 
**driver_opts** | **Dict[str, str]** | Driver attachment options for the network target.  | [optional] 

## Example

```python
from docker_client.generated.models.network_attachment_config import NetworkAttachmentConfig

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkAttachmentConfig from a JSON string
network_attachment_config_instance = NetworkAttachmentConfig.from_json(json)
# print the JSON string representation of the object
print(NetworkAttachmentConfig.to_json())

# convert the object into a dict
network_attachment_config_dict = network_attachment_config_instance.to_dict()
# create an instance of NetworkAttachmentConfig from a dict
network_attachment_config_from_dict = NetworkAttachmentConfig.from_dict(network_attachment_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


