# ImageID

Image ID or Digest

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.image_id import ImageID

# TODO update the JSON string below
json = "{}"
# create an instance of ImageID from a JSON string
image_id_instance = ImageID.from_json(json)
# print the JSON string representation of the object
print(ImageID.to_json())

# convert the object into a dict
image_id_dict = image_id_instance.to_dict()
# create an instance of ImageID from a dict
image_id_from_dict = ImageID.from_dict(image_id_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


