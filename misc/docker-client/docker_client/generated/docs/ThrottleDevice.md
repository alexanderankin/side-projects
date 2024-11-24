# ThrottleDevice


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**path** | **str** | Device path | [optional] 
**rate** | **int** | Rate | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.throttle_device import ThrottleDevice

# TODO update the JSON string below
json = "{}"
# create an instance of ThrottleDevice from a JSON string
throttle_device_instance = ThrottleDevice.from_json(json)
# print the JSON string representation of the object
print(ThrottleDevice.to_json())

# convert the object into a dict
throttle_device_dict = throttle_device_instance.to_dict()
# create an instance of ThrottleDevice from a dict
throttle_device_from_dict = ThrottleDevice.from_dict(throttle_device_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


