# ImageSummary


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | ID is the content-addressable ID of an image.  This identifier is a content-addressable digest calculated from the image&#39;s configuration (which includes the digests of layers used by the image).  Note that this digest differs from the &#x60;RepoDigests&#x60; below, which holds digests of image manifests that reference the image.  | 
**parent_id** | **str** | ID of the parent image.  Depending on how the image was created, this field may be empty and is only set for images that were built/created locally. This field is empty if the image was pulled from an image registry.  | 
**repo_tags** | **List[str]** | List of image names/tags in the local image cache that reference this image.  Multiple image tags can refer to the same image, and this list may be empty if no tags reference the image, in which case the image is \&quot;untagged\&quot;, in which case it can still be referenced by its ID.  | 
**repo_digests** | **List[str]** | List of content-addressable digests of locally available image manifests that the image is referenced from. Multiple manifests can refer to the same image.  These digests are usually only available if the image was either pulled from a registry, or if the image was pushed to a registry, which is when the manifest is generated and its digest calculated.  | 
**created** | **int** | Date and time at which the image was created as a Unix timestamp (number of seconds since EPOCH).  | 
**size** | **int** | Total size of the image including all layers it is composed of.  | 
**shared_size** | **int** | Total size of image layers that are shared between this image and other images.  This size is not calculated by default. &#x60;-1&#x60; indicates that the value has not been set / calculated.  | 
**virtual_size** | **int** | Total size of the image including all layers it is composed of.  Deprecated: this field is omitted in API v1.44, but kept for backward compatibility. Use Size instead. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | 
**containers** | **int** | Number of containers using this image. Includes both stopped and running containers.  This size is not calculated by default, and depends on which API endpoint is used. &#x60;-1&#x60; indicates that the value has not been set / calculated.  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.image_summary import ImageSummary

# TODO update the JSON string below
json = "{}"
# create an instance of ImageSummary from a JSON string
image_summary_instance = ImageSummary.from_json(json)
# print the JSON string representation of the object
print(ImageSummary.to_json())

# convert the object into a dict
image_summary_dict = image_summary_instance.to_dict()
# create an instance of ImageSummary from a dict
image_summary_from_dict = ImageSummary.from_dict(image_summary_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


