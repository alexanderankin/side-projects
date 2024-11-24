# PortBinding

PortBinding represents a binding between a host IP address and a host port. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**host_ip** | **str** | Host IP address that the container&#39;s port is mapped to. | [optional] 
**host_port** | **str** | Host port number that the container&#39;s port is mapped to. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.port_binding import PortBinding

# TODO update the JSON string below
json = "{}"
# create an instance of PortBinding from a JSON string
port_binding_instance = PortBinding.from_json(json)
# print the JSON string representation of the object
print(PortBinding.to_json())

# convert the object into a dict
port_binding_dict = port_binding_instance.to_dict()
# create an instance of PortBinding from a dict
port_binding_from_dict = PortBinding.from_dict(port_binding_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


