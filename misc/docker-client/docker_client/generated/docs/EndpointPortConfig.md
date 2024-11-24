# EndpointPortConfig


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | [optional] 
**protocol** | **str** |  | [optional] 
**target_port** | **int** | The port inside the container. | [optional] 
**published_port** | **int** | The port on the swarm hosts. | [optional] 
**publish_mode** | **str** | The mode in which port is published.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  - \&quot;ingress\&quot; makes the target port accessible on every node,   regardless of whether there is a task for the service running on   that node or not. - \&quot;host\&quot; bypasses the routing mesh and publish the port directly on   the swarm node where that service is running.  | [optional] [default to 'ingress']

## Example

```python
from docker_client.generated.models.endpoint_port_config import EndpointPortConfig

# TODO update the JSON string below
json = "{}"
# create an instance of EndpointPortConfig from a JSON string
endpoint_port_config_instance = EndpointPortConfig.from_json(json)
# print the JSON string representation of the object
print(EndpointPortConfig.to_json())

# convert the object into a dict
endpoint_port_config_dict = endpoint_port_config_instance.to_dict()
# create an instance of EndpointPortConfig from a dict
endpoint_port_config_from_dict = EndpointPortConfig.from_dict(endpoint_port_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


