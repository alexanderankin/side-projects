# Platform

Platform represents the platform (Arch/OS). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**architecture** | **str** | Architecture represents the hardware architecture (for example, &#x60;x86_64&#x60;).  | [optional] 
**os** | **str** | OS represents the Operating System (for example, &#x60;linux&#x60; or &#x60;windows&#x60;).  | [optional] 

## Example

```python
from docker_client.generated.models.platform import Platform

# TODO update the JSON string below
json = "{}"
# create an instance of Platform from a JSON string
platform_instance = Platform.from_json(json)
# print the JSON string representation of the object
print(Platform.to_json())

# convert the object into a dict
platform_dict = platform_instance.to_dict()
# create an instance of Platform from a dict
platform_from_dict = Platform.from_dict(platform_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


