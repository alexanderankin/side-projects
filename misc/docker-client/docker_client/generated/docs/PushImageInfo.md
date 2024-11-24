# PushImageInfo


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**error** | **str** |  | [optional] 
**status** | **str** |  | [optional] 
**progress** | **str** |  | [optional] 
**progress_detail** | [**ProgressDetail**](ProgressDetail.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.push_image_info import PushImageInfo

# TODO update the JSON string below
json = "{}"
# create an instance of PushImageInfo from a JSON string
push_image_info_instance = PushImageInfo.from_json(json)
# print the JSON string representation of the object
print(PushImageInfo.to_json())

# convert the object into a dict
push_image_info_dict = push_image_info_instance.to_dict()
# create an instance of PushImageInfo from a dict
push_image_info_from_dict = PushImageInfo.from_dict(push_image_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


