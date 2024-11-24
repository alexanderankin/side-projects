# ContainerWaitResponse

OK response to ContainerWait operation

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status_code** | **int** | Exit code of the container | 
**error** | [**ContainerWaitExitError**](ContainerWaitExitError.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.container_wait_response import ContainerWaitResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerWaitResponse from a JSON string
container_wait_response_instance = ContainerWaitResponse.from_json(json)
# print the JSON string representation of the object
print(ContainerWaitResponse.to_json())

# convert the object into a dict
container_wait_response_dict = container_wait_response_instance.to_dict()
# create an instance of ContainerWaitResponse from a dict
container_wait_response_from_dict = ContainerWaitResponse.from_dict(container_wait_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


