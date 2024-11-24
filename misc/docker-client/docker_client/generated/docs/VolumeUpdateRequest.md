# VolumeUpdateRequest

Volume configuration

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**spec** | [**ClusterVolumeSpec**](ClusterVolumeSpec.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.volume_update_request import VolumeUpdateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of VolumeUpdateRequest from a JSON string
volume_update_request_instance = VolumeUpdateRequest.from_json(json)
# print the JSON string representation of the object
print(VolumeUpdateRequest.to_json())

# convert the object into a dict
volume_update_request_dict = volume_update_request_instance.to_dict()
# create an instance of VolumeUpdateRequest from a dict
volume_update_request_from_dict = VolumeUpdateRequest.from_dict(volume_update_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


