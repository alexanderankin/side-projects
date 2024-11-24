# ContainerWaitExitError

container waiting error, if any

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**message** | **str** | Details of an error | [optional] 

## Example

```python
from docker_client.generated.models.container_wait_exit_error import ContainerWaitExitError

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerWaitExitError from a JSON string
container_wait_exit_error_instance = ContainerWaitExitError.from_json(json)
# print the JSON string representation of the object
print(ContainerWaitExitError.to_json())

# convert the object into a dict
container_wait_exit_error_dict = container_wait_exit_error_instance.to_dict()
# create an instance of ContainerWaitExitError from a dict
container_wait_exit_error_from_dict = ContainerWaitExitError.from_dict(container_wait_exit_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


