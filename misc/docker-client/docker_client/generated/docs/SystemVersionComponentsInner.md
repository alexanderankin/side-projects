# SystemVersionComponentsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the component  | 
**version** | **str** | Version of the component  | 
**details** | **object** | Key/value pairs of strings with additional information about the component. These values are intended for informational purposes only, and their content is not defined, and not part of the API specification.  These messages can be printed by the client as information to the user.  | [optional] 

## Example

```python
from docker_client.generated.models.system_version_components_inner import SystemVersionComponentsInner

# TODO update the JSON string below
json = "{}"
# create an instance of SystemVersionComponentsInner from a JSON string
system_version_components_inner_instance = SystemVersionComponentsInner.from_json(json)
# print the JSON string representation of the object
print(SystemVersionComponentsInner.to_json())

# convert the object into a dict
system_version_components_inner_dict = system_version_components_inner_instance.to_dict()
# create an instance of SystemVersionComponentsInner from a dict
system_version_components_inner_from_dict = SystemVersionComponentsInner.from_dict(system_version_components_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


