# NodeDescription

NodeDescription encapsulates the properties of the Node as reported by the agent. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**hostname** | **str** |  | [optional] 
**platform** | [**Platform**](Platform.md) |  | [optional] 
**resources** | [**ResourceObject**](ResourceObject.md) |  | [optional] 
**engine** | [**EngineDescription**](EngineDescription.md) |  | [optional] 
**tls_info** | [**TLSInfo**](TLSInfo.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.node_description import NodeDescription

# TODO update the JSON string below
json = "{}"
# create an instance of NodeDescription from a JSON string
node_description_instance = NodeDescription.from_json(json)
# print the JSON string representation of the object
print(NodeDescription.to_json())

# convert the object into a dict
node_description_dict = node_description_instance.to_dict()
# create an instance of NodeDescription from a dict
node_description_from_dict = NodeDescription.from_dict(node_description_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


