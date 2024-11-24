# GenericResourcesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**named_resource_spec** | [**GenericResourcesInnerNamedResourceSpec**](GenericResourcesInnerNamedResourceSpec.md) |  | [optional] 
**discrete_resource_spec** | [**GenericResourcesInnerDiscreteResourceSpec**](GenericResourcesInnerDiscreteResourceSpec.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.generic_resources_inner import GenericResourcesInner

# TODO update the JSON string below
json = "{}"
# create an instance of GenericResourcesInner from a JSON string
generic_resources_inner_instance = GenericResourcesInner.from_json(json)
# print the JSON string representation of the object
print(GenericResourcesInner.to_json())

# convert the object into a dict
generic_resources_inner_dict = generic_resources_inner_instance.to_dict()
# create an instance of GenericResourcesInner from a dict
generic_resources_inner_from_dict = GenericResourcesInner.from_dict(generic_resources_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


