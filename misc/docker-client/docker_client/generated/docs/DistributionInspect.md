# DistributionInspect

Describes the result obtained from contacting the registry to retrieve image metadata. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**descriptor** | [**OCIDescriptor**](OCIDescriptor.md) |  | 
**platforms** | [**List[OCIPlatform]**](OCIPlatform.md) | An array containing all platforms supported by the image.  | 

## Example

```python
from docker_client.generated.models.distribution_inspect import DistributionInspect

# TODO update the JSON string below
json = "{}"
# create an instance of DistributionInspect from a JSON string
distribution_inspect_instance = DistributionInspect.from_json(json)
# print the JSON string representation of the object
print(DistributionInspect.to_json())

# convert the object into a dict
distribution_inspect_dict = distribution_inspect_instance.to_dict()
# create an instance of DistributionInspect from a dict
distribution_inspect_from_dict = DistributionInspect.from_dict(distribution_inspect_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


