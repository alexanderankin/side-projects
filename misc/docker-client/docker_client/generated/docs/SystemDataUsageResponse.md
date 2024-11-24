# SystemDataUsageResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**layers_size** | **int** |  | [optional] 
**images** | [**List[ImageSummary]**](ImageSummary.md) |  | [optional] 
**containers** | [**List[ContainerSummary]**](ContainerSummary.md) |  | [optional] 
**volumes** | [**List[Volume]**](Volume.md) |  | [optional] 
**build_cache** | [**List[BuildCache]**](BuildCache.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.system_data_usage_response import SystemDataUsageResponse

# TODO update the JSON string below
json = "{}"
# create an instance of SystemDataUsageResponse from a JSON string
system_data_usage_response_instance = SystemDataUsageResponse.from_json(json)
# print the JSON string representation of the object
print(SystemDataUsageResponse.to_json())

# convert the object into a dict
system_data_usage_response_dict = system_data_usage_response_instance.to_dict()
# create an instance of SystemDataUsageResponse from a dict
system_data_usage_response_from_dict = SystemDataUsageResponse.from_dict(system_data_usage_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


