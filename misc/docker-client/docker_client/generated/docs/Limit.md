# Limit

An object describing a limit on resources which can be requested by a task. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**nano_cpus** | **int** |  | [optional] 
**memory_bytes** | **int** |  | [optional] 
**pids** | **int** | Limits the maximum number of PIDs in the container. Set &#x60;0&#x60; for unlimited.  | [optional] [default to 0]

## Example

```python
from docker_client.generated.docker_client.generated.models.limit import Limit

# TODO update the JSON string below
json = "{}"
# create an instance of Limit from a JSON string
limit_instance = Limit.from_json(json)
# print the JSON string representation of the object
print(Limit.to_json())

# convert the object into a dict
limit_dict = limit_instance.to_dict()
# create an instance of Limit from a dict
limit_from_dict = Limit.from_dict(limit_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


