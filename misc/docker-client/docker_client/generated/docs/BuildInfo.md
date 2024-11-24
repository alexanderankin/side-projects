# BuildInfo


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** |  | [optional] 
**stream** | **str** |  | [optional] 
**error** | **str** |  | [optional] 
**error_detail** | [**ErrorDetail**](ErrorDetail.md) |  | [optional] 
**status** | **str** |  | [optional] 
**progress** | **str** |  | [optional] 
**progress_detail** | [**ProgressDetail**](ProgressDetail.md) |  | [optional] 
**aux** | [**ImageID**](ImageID.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.build_info import BuildInfo

# TODO update the JSON string below
json = "{}"
# create an instance of BuildInfo from a JSON string
build_info_instance = BuildInfo.from_json(json)
# print the JSON string representation of the object
print(BuildInfo.to_json())

# convert the object into a dict
build_info_dict = build_info_instance.to_dict()
# create an instance of BuildInfo from a dict
build_info_from_dict = BuildInfo.from_dict(build_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


