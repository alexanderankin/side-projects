# ImageDeleteResponseItem


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**untagged** | **str** | The image ID of an image that was untagged | [optional] 
**deleted** | **str** | The image ID of an image that was deleted | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.image_delete_response_item import ImageDeleteResponseItem

# TODO update the JSON string below
json = "{}"
# create an instance of ImageDeleteResponseItem from a JSON string
image_delete_response_item_instance = ImageDeleteResponseItem.from_json(json)
# print the JSON string representation of the object
print(ImageDeleteResponseItem.to_json())

# convert the object into a dict
image_delete_response_item_dict = image_delete_response_item_instance.to_dict()
# create an instance of ImageDeleteResponseItem from a dict
image_delete_response_item_from_dict = ImageDeleteResponseItem.from_dict(image_delete_response_item_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


