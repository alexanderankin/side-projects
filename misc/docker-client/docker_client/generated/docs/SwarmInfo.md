# SwarmInfo

Represents generic information about swarm. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**node_id** | **str** | Unique identifier of for this node in the swarm. | [optional] [default to '']
**node_addr** | **str** | IP address at which this node can be reached by other nodes in the swarm.  | [optional] [default to '']
**local_node_state** | [**LocalNodeState**](LocalNodeState.md) |  | [optional] [default to LocalNodeState.EMPTY]
**control_available** | **bool** |  | [optional] [default to False]
**error** | **str** |  | [optional] [default to '']
**remote_managers** | [**List[PeerNode]**](PeerNode.md) | List of ID&#39;s and addresses of other managers in the swarm.  | [optional] 
**nodes** | **int** | Total number of nodes in the swarm. | [optional] 
**managers** | **int** | Total number of managers in the swarm. | [optional] 
**cluster** | [**ClusterInfo**](ClusterInfo.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_info import SwarmInfo

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmInfo from a JSON string
swarm_info_instance = SwarmInfo.from_json(json)
# print the JSON string representation of the object
print(SwarmInfo.to_json())

# convert the object into a dict
swarm_info_dict = swarm_info_instance.to_dict()
# create an instance of SwarmInfo from a dict
swarm_info_from_dict = SwarmInfo.from_dict(swarm_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


