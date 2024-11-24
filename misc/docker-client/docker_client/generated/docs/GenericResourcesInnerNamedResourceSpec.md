# GenericResourcesInnerNamedResourceSpec


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**kind** | **str** |  | [optional] 
**value** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.generic_resources_inner_named_resource_spec import GenericResourcesInnerNamedResourceSpec

# TODO update the JSON string below
json = "{}"
# create an instance of GenericResourcesInnerNamedResourceSpec from a JSON string
generic_resources_inner_named_resource_spec_instance = GenericResourcesInnerNamedResourceSpec.from_json(json)
# print the JSON string representation of the object
print(GenericResourcesInnerNamedResourceSpec.to_json())

# convert the object into a dict
generic_resources_inner_named_resource_spec_dict = generic_resources_inner_named_resource_spec_instance.to_dict()
# create an instance of GenericResourcesInnerNamedResourceSpec from a dict
generic_resources_inner_named_resource_spec_from_dict = GenericResourcesInnerNamedResourceSpec.from_dict(generic_resources_inner_named_resource_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


