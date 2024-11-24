# VolumeListResponse

Volume list response

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**volumes** | [**List[Volume]**](Volume.md) | List of volumes | [optional] 
**warnings** | **List[str]** | Warnings that occurred when fetching the list of volumes.  | [optional] 

## Example

```python
from docker_client.generated.models.volume_list_response import VolumeListResponse

# TODO update the JSON string below
json = "{}"
# create an instance of VolumeListResponse from a JSON string
volume_list_response_instance = VolumeListResponse.from_json(json)
# print the JSON string representation of the object
print(VolumeListResponse.to_json())

# convert the object into a dict
volume_list_response_dict = volume_list_response_instance.to_dict()
# create an instance of VolumeListResponse from a dict
volume_list_response_from_dict = VolumeListResponse.from_dict(volume_list_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


