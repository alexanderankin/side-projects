# Port

An open port on a container

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**ip** | **str** | Host IP address that the container&#39;s port is mapped to | [optional] 
**private_port** | **int** | Port on the container | 
**public_port** | **int** | Port exposed on the host | [optional] 
**type** | **str** |  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.port import Port

# TODO update the JSON string below
json = "{}"
# create an instance of Port from a JSON string
port_instance = Port.from_json(json)
# print the JSON string representation of the object
print(Port.to_json())

# convert the object into a dict
port_dict = port_instance.to_dict()
# create an instance of Port from a dict
port_from_dict = Port.from_dict(port_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


