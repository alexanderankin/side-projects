# NetworkPruneResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**networks_deleted** | **List[str]** | Networks that were deleted | [optional] 

## Example

```python
from docker_client.generated.models.network_prune_response import NetworkPruneResponse

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkPruneResponse from a JSON string
network_prune_response_instance = NetworkPruneResponse.from_json(json)
# print the JSON string representation of the object
print(NetworkPruneResponse.to_json())

# convert the object into a dict
network_prune_response_dict = network_prune_response_instance.to_dict()
# create an instance of NetworkPruneResponse from a dict
network_prune_response_from_dict = NetworkPruneResponse.from_dict(network_prune_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


