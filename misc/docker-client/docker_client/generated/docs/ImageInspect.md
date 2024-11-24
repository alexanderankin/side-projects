# ImageInspect

Information about an image in the local image cache. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | ID is the content-addressable ID of an image.  This identifier is a content-addressable digest calculated from the image&#39;s configuration (which includes the digests of layers used by the image).  Note that this digest differs from the &#x60;RepoDigests&#x60; below, which holds digests of image manifests that reference the image.  | [optional] 
**repo_tags** | **List[str]** | List of image names/tags in the local image cache that reference this image.  Multiple image tags can refer to the same image, and this list may be empty if no tags reference the image, in which case the image is \&quot;untagged\&quot;, in which case it can still be referenced by its ID.  | [optional] 
**repo_digests** | **List[str]** | List of content-addressable digests of locally available image manifests that the image is referenced from. Multiple manifests can refer to the same image.  These digests are usually only available if the image was either pulled from a registry, or if the image was pushed to a registry, which is when the manifest is generated and its digest calculated.  | [optional] 
**parent** | **str** | ID of the parent image.  Depending on how the image was created, this field may be empty and is only set for images that were built/created locally. This field is empty if the image was pulled from an image registry.  | [optional] 
**comment** | **str** | Optional message that was set when committing or importing the image.  | [optional] 
**created** | **str** | Date and time at which the image was created, formatted in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  This information is only available if present in the image, and omitted otherwise.  | [optional] 
**docker_version** | **str** | The version of Docker that was used to build the image.  Depending on how the image was created, this field may be empty.  | [optional] 
**author** | **str** | Name of the author that was specified when committing the image, or as specified through MAINTAINER (deprecated) in the Dockerfile.  | [optional] 
**config** | [**ImageConfig**](ImageConfig.md) |  | [optional] 
**architecture** | **str** | Hardware CPU architecture that the image runs on.  | [optional] 
**variant** | **str** | CPU architecture variant (presently ARM-only).  | [optional] 
**os** | **str** | Operating System the image is built to run on.  | [optional] 
**os_version** | **str** | Operating System version the image is built to run on (especially for Windows).  | [optional] 
**size** | **int** | Total size of the image including all layers it is composed of.  | [optional] 
**virtual_size** | **int** | Total size of the image including all layers it is composed of.  Deprecated: this field is omitted in API v1.44, but kept for backward compatibility. Use Size instead.  | [optional] 
**graph_driver** | [**GraphDriverData**](GraphDriverData.md) |  | [optional] 
**root_fs** | [**ImageInspectRootFS**](ImageInspectRootFS.md) |  | [optional] 
**metadata** | [**ImageInspectMetadata**](ImageInspectMetadata.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.image_inspect import ImageInspect

# TODO update the JSON string below
json = "{}"
# create an instance of ImageInspect from a JSON string
image_inspect_instance = ImageInspect.from_json(json)
# print the JSON string representation of the object
print(ImageInspect.to_json())

# convert the object into a dict
image_inspect_dict = image_inspect_instance.to_dict()
# create an instance of ImageInspect from a dict
image_inspect_from_dict = ImageInspect.from_dict(image_inspect_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


