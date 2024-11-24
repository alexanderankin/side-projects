# ImageInspectMetadata

Additional metadata of the image in the local cache. This information is local to the daemon, and not part of the image itself. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**last_tag_time** | **str** | Date and time at which the image was last tagged in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  This information is only available if the image was tagged locally, and omitted otherwise.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.image_inspect_metadata import ImageInspectMetadata

# TODO update the JSON string below
json = "{}"
# create an instance of ImageInspectMetadata from a JSON string
image_inspect_metadata_instance = ImageInspectMetadata.from_json(json)
# print the JSON string representation of the object
print(ImageInspectMetadata.to_json())

# convert the object into a dict
image_inspect_metadata_dict = image_inspect_metadata_instance.to_dict()
# create an instance of ImageInspectMetadata from a dict
image_inspect_metadata_from_dict = ImageInspectMetadata.from_dict(image_inspect_metadata_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


