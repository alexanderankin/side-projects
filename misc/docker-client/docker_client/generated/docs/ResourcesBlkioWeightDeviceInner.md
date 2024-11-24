# ResourcesBlkioWeightDeviceInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**path** | **str** |  | [optional] 
**weight** | **int** |  | [optional] 

## Example

```python
from docker_client.generated.models.resources_blkio_weight_device_inner import ResourcesBlkioWeightDeviceInner

# TODO update the JSON string below
json = "{}"
# create an instance of ResourcesBlkioWeightDeviceInner from a JSON string
resources_blkio_weight_device_inner_instance = ResourcesBlkioWeightDeviceInner.from_json(json)
# print the JSON string representation of the object
print(ResourcesBlkioWeightDeviceInner.to_json())

# convert the object into a dict
resources_blkio_weight_device_inner_dict = resources_blkio_weight_device_inner_instance.to_dict()
# create an instance of ResourcesBlkioWeightDeviceInner from a dict
resources_blkio_weight_device_inner_from_dict = ResourcesBlkioWeightDeviceInner.from_dict(resources_blkio_weight_device_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


