# OCIPlatform

Describes the platform which the image in the manifest runs on, as defined in the [OCI Image Index Specification](https://github.com/opencontainers/image-spec/blob/v1.0.1/image-index.md). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**architecture** | **str** | The CPU architecture, for example &#x60;amd64&#x60; or &#x60;ppc64&#x60;.  | [optional] 
**os** | **str** | The operating system, for example &#x60;linux&#x60; or &#x60;windows&#x60;.  | [optional] 
**os_version** | **str** | Optional field specifying the operating system version, for example on Windows &#x60;10.0.19041.1165&#x60;.  | [optional] 
**os_features** | **List[str]** | Optional field specifying an array of strings, each listing a required OS feature (for example on Windows &#x60;win32k&#x60;).  | [optional] 
**variant** | **str** | Optional field specifying a variant of the CPU, for example &#x60;v7&#x60; to specify ARMv7 when architecture is &#x60;arm&#x60;.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.oci_platform import OCIPlatform

# TODO update the JSON string below
json = "{}"
# create an instance of OCIPlatform from a JSON string
oci_platform_instance = OCIPlatform.from_json(json)
# print the JSON string representation of the object
print(OCIPlatform.to_json())

# convert the object into a dict
oci_platform_dict = oci_platform_instance.to_dict()
# create an instance of OCIPlatform from a dict
oci_platform_from_dict = OCIPlatform.from_dict(oci_platform_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


