# DeviceRequest

A request for devices to be sent to device drivers

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**driver** | **str** |  | [optional] 
**count** | **int** |  | [optional] 
**device_ids** | **List[str]** |  | [optional] 
**capabilities** | **List[List[str]]** | A list of capabilities; an OR list of AND lists of capabilities.  | [optional] 
**options** | **Dict[str, str]** | Driver-specific options, specified as a key/value pairs. These options are passed directly to the driver.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.device_request import DeviceRequest

# TODO update the JSON string below
json = "{}"
# create an instance of DeviceRequest from a JSON string
device_request_instance = DeviceRequest.from_json(json)
# print the JSON string representation of the object
print(DeviceRequest.to_json())

# convert the object into a dict
device_request_dict = device_request_instance.to_dict()
# create an instance of DeviceRequest from a dict
device_request_from_dict = DeviceRequest.from_dict(device_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


