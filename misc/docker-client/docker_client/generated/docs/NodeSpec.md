# NodeSpec


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name for the node. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**role** | **str** | Role of the node. | [optional] 
**availability** | **str** | Availability of the node. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.node_spec import NodeSpec

# TODO update the JSON string below
json = "{}"
# create an instance of NodeSpec from a JSON string
node_spec_instance = NodeSpec.from_json(json)
# print the JSON string representation of the object
print(NodeSpec.to_json())

# convert the object into a dict
node_spec_dict = node_spec_instance.to_dict()
# create an instance of NodeSpec from a dict
node_spec_from_dict = NodeSpec.from_dict(node_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


