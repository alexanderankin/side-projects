# ManagerStatus

ManagerStatus represents the status of a manager.  It provides the current status of a node's manager component, if the node is a manager. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**leader** | **bool** |  | [optional] [default to False]
**reachability** | [**Reachability**](Reachability.md) |  | [optional] 
**addr** | **str** | The IP address and port at which the manager is reachable.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.manager_status import ManagerStatus

# TODO update the JSON string below
json = "{}"
# create an instance of ManagerStatus from a JSON string
manager_status_instance = ManagerStatus.from_json(json)
# print the JSON string representation of the object
print(ManagerStatus.to_json())

# convert the object into a dict
manager_status_dict = manager_status_instance.to_dict()
# create an instance of ManagerStatus from a dict
manager_status_from_dict = ManagerStatus.from_dict(manager_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


