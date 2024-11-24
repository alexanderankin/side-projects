# ResourcesUlimitsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of ulimit | [optional] 
**soft** | **int** | Soft limit | [optional] 
**hard** | **int** | Hard limit | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.resources_ulimits_inner import ResourcesUlimitsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ResourcesUlimitsInner from a JSON string
resources_ulimits_inner_instance = ResourcesUlimitsInner.from_json(json)
# print the JSON string representation of the object
print(ResourcesUlimitsInner.to_json())

# convert the object into a dict
resources_ulimits_inner_dict = resources_ulimits_inner_instance.to_dict()
# create an instance of ResourcesUlimitsInner from a dict
resources_ulimits_inner_from_dict = ResourcesUlimitsInner.from_dict(resources_ulimits_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


