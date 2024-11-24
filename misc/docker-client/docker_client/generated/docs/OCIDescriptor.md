# OCIDescriptor

A descriptor struct containing digest, media type, and size, as defined in the [OCI Content Descriptors Specification](https://github.com/opencontainers/image-spec/blob/v1.0.1/descriptor.md). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**media_type** | **str** | The media type of the object this schema refers to.  | [optional] 
**digest** | **str** | The digest of the targeted content.  | [optional] 
**size** | **int** | The size in bytes of the blob.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.oci_descriptor import OCIDescriptor

# TODO update the JSON string below
json = "{}"
# create an instance of OCIDescriptor from a JSON string
oci_descriptor_instance = OCIDescriptor.from_json(json)
# print the JSON string representation of the object
print(OCIDescriptor.to_json())

# convert the object into a dict
oci_descriptor_dict = oci_descriptor_instance.to_dict()
# create an instance of OCIDescriptor from a dict
oci_descriptor_from_dict = OCIDescriptor.from_dict(oci_descriptor_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


