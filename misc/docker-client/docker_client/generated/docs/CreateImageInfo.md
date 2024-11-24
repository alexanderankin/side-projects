# CreateImageInfo


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** |  | [optional] 
**error** | **str** |  | [optional] 
**error_detail** | [**ErrorDetail**](ErrorDetail.md) |  | [optional] 
**status** | **str** |  | [optional] 
**progress** | **str** |  | [optional] 
**progress_detail** | [**ProgressDetail**](ProgressDetail.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.create_image_info import CreateImageInfo

# TODO update the JSON string below
json = "{}"
# create an instance of CreateImageInfo from a JSON string
create_image_info_instance = CreateImageInfo.from_json(json)
# print the JSON string representation of the object
print(CreateImageInfo.to_json())

# convert the object into a dict
create_image_info_dict = create_image_info_instance.to_dict()
# create an instance of CreateImageInfo from a dict
create_image_info_from_dict = CreateImageInfo.from_dict(create_image_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


