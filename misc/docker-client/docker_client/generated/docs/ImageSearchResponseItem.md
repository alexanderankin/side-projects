# ImageSearchResponseItem


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**description** | **str** |  | [optional] 
**is_official** | **bool** |  | [optional] 
**is_automated** | **bool** | Whether this repository has automated builds enabled.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is deprecated and will always be \&quot;false\&quot;.  | [optional] 
**name** | **str** |  | [optional] 
**star_count** | **int** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.image_search_response_item import ImageSearchResponseItem

# TODO update the JSON string below
json = "{}"
# create an instance of ImageSearchResponseItem from a JSON string
image_search_response_item_instance = ImageSearchResponseItem.from_json(json)
# print the JSON string representation of the object
print(ImageSearchResponseItem.to_json())

# convert the object into a dict
image_search_response_item_dict = image_search_response_item_instance.to_dict()
# create an instance of ImageSearchResponseItem from a dict
image_search_response_item_from_dict = ImageSearchResponseItem.from_dict(image_search_response_item_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


