# ResourceObject

An object describing the resources which can be advertised by a node and requested by a task. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**nano_cpus** | **int** |  | [optional] 
**memory_bytes** | **int** |  | [optional] 
**generic_resources** | [**List[GenericResourcesInner]**](GenericResourcesInner.md) | User-defined resources can be either Integer resources (e.g, &#x60;SSD&#x3D;3&#x60;) or String resources (e.g, &#x60;GPU&#x3D;UUID1&#x60;).  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.resource_object import ResourceObject

# TODO update the JSON string below
json = "{}"
# create an instance of ResourceObject from a JSON string
resource_object_instance = ResourceObject.from_json(json)
# print the JSON string representation of the object
print(ResourceObject.to_json())

# convert the object into a dict
resource_object_dict = resource_object_instance.to_dict()
# create an instance of ResourceObject from a dict
resource_object_from_dict = ResourceObject.from_dict(resource_object_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


