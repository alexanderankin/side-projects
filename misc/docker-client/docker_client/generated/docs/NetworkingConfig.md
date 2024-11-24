# NetworkingConfig

NetworkingConfig represents the container's networking configuration for each of its interfaces. It is used for the networking configs specified in the `docker create` and `docker network connect` commands. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**endpoints_config** | [**Dict[str, EndpointSettings]**](EndpointSettings.md) | A mapping of network name to endpoint configuration for that network. The endpoint configuration can be left empty to connect to that network with no particular endpoint configuration.  | [optional] 

## Example

```python
from docker_client.generated.models.networking_config import NetworkingConfig

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkingConfig from a JSON string
networking_config_instance = NetworkingConfig.from_json(json)
# print the JSON string representation of the object
print(NetworkingConfig.to_json())

# convert the object into a dict
networking_config_dict = networking_config_instance.to_dict()
# create an instance of NetworkingConfig from a dict
networking_config_from_dict = NetworkingConfig.from_dict(networking_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


