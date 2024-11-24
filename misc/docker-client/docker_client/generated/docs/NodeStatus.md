# NodeStatus

NodeStatus represents the status of a node.  It provides the current status of the node, as seen by the manager. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**state** | [**NodeState**](NodeState.md) |  | [optional] 
**message** | **str** |  | [optional] 
**addr** | **str** | IP address of the node. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.node_status import NodeStatus

# TODO update the JSON string below
json = "{}"
# create an instance of NodeStatus from a JSON string
node_status_instance = NodeStatus.from_json(json)
# print the JSON string representation of the object
print(NodeStatus.to_json())

# convert the object into a dict
node_status_dict = node_status_instance.to_dict()
# create an instance of NodeStatus from a dict
node_status_from_dict = NodeStatus.from_dict(node_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


