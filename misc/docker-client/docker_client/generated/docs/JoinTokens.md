# JoinTokens

JoinTokens contains the tokens workers and managers need to join the swarm. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**worker** | **str** | The token workers can use to join the swarm.  | [optional] 
**manager** | **str** | The token managers can use to join the swarm.  | [optional] 

## Example

```python
from docker_client.generated.models.join_tokens import JoinTokens

# TODO update the JSON string below
json = "{}"
# create an instance of JoinTokens from a JSON string
join_tokens_instance = JoinTokens.from_json(json)
# print the JSON string representation of the object
print(JoinTokens.to_json())

# convert the object into a dict
join_tokens_dict = join_tokens_instance.to_dict()
# create an instance of JoinTokens from a dict
join_tokens_from_dict = JoinTokens.from_dict(join_tokens_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


