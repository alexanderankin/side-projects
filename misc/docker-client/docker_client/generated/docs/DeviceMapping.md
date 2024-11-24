# DeviceMapping

A device mapping between the host and container

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**path_on_host** | **str** |  | [optional] 
**path_in_container** | **str** |  | [optional] 
**cgroup_permissions** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.models.device_mapping import DeviceMapping

# TODO update the JSON string below
json = "{}"
# create an instance of DeviceMapping from a JSON string
device_mapping_instance = DeviceMapping.from_json(json)
# print the JSON string representation of the object
print(DeviceMapping.to_json())

# convert the object into a dict
device_mapping_dict = device_mapping_instance.to_dict()
# create an instance of DeviceMapping from a dict
device_mapping_from_dict = DeviceMapping.from_dict(device_mapping_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


