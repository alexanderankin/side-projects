# PortStatus

represents the port status of a task's host ports whose service has published host ports

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**ports** | [**List[EndpointPortConfig]**](EndpointPortConfig.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.port_status import PortStatus

# TODO update the JSON string below
json = "{}"
# create an instance of PortStatus from a JSON string
port_status_instance = PortStatus.from_json(json)
# print the JSON string representation of the object
print(PortStatus.to_json())

# convert the object into a dict
port_status_dict = port_status_instance.to_dict()
# create an instance of PortStatus from a dict
port_status_from_dict = PortStatus.from_dict(port_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


