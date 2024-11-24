# VolumeUsageData

Usage details about the volume. This information is used by the `GET /system/df` endpoint, and omitted in other endpoints. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**size** | **int** | Amount of disk space used by the volume (in bytes). This information is only available for volumes created with the &#x60;\&quot;local\&quot;&#x60; volume driver. For volumes created with other volume drivers, this field is set to &#x60;-1&#x60; (\&quot;not available\&quot;)  | [default to -1]
**ref_count** | **int** | The number of containers referencing this volume. This field is set to &#x60;-1&#x60; if the reference-count is not available.  | [default to -1]

## Example

```python
from docker_client.generated.models.volume_usage_data import VolumeUsageData

# TODO update the JSON string below
json = "{}"
# create an instance of VolumeUsageData from a JSON string
volume_usage_data_instance = VolumeUsageData.from_json(json)
# print the JSON string representation of the object
print(VolumeUsageData.to_json())

# convert the object into a dict
volume_usage_data_dict = volume_usage_data_instance.to_dict()
# create an instance of VolumeUsageData from a dict
volume_usage_data_from_dict = VolumeUsageData.from_dict(volume_usage_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


