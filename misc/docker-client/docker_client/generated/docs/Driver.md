# Driver

Driver represents a driver (network, logging, secrets).

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the driver. | 
**options** | **Dict[str, str]** | Key/value map of driver-specific options. | [optional] 

## Example

```python
from docker_client.generated.models.driver import Driver

# TODO update the JSON string below
json = "{}"
# create an instance of Driver from a JSON string
driver_instance = Driver.from_json(json)
# print the JSON string representation of the object
print(Driver.to_json())

# convert the object into a dict
driver_dict = driver_instance.to_dict()
# create an instance of Driver from a dict
driver_from_dict = Driver.from_dict(driver_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


