# RestartPolicy

The behavior to apply when the container exits. The default is not to restart.  An ever increasing delay (double the previous delay, starting at 100ms) is added before each restart to prevent flooding the server. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | - Empty string means not to restart - &#x60;no&#x60; Do not automatically restart - &#x60;always&#x60; Always restart - &#x60;unless-stopped&#x60; Restart always except when the user has manually stopped the container - &#x60;on-failure&#x60; Restart only when the container exit code is non-zero  | [optional] 
**maximum_retry_count** | **int** | If &#x60;on-failure&#x60; is used, the number of times to retry before giving up.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.restart_policy import RestartPolicy

# TODO update the JSON string below
json = "{}"
# create an instance of RestartPolicy from a JSON string
restart_policy_instance = RestartPolicy.from_json(json)
# print the JSON string representation of the object
print(RestartPolicy.to_json())

# convert the object into a dict
restart_policy_dict = restart_policy_instance.to_dict()
# create an instance of RestartPolicy from a dict
restart_policy_from_dict = RestartPolicy.from_dict(restart_policy_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


