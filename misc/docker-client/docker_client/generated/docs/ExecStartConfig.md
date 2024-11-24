# ExecStartConfig


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**detach** | **bool** | Detach from the command. | [optional] 
**tty** | **bool** | Allocate a pseudo-TTY. | [optional] 
**console_size** | **List[int]** | Initial console size, as an &#x60;[height, width]&#x60; array. | [optional] 

## Example

```python
from docker_client.generated.models.exec_start_config import ExecStartConfig

# TODO update the JSON string below
json = "{}"
# create an instance of ExecStartConfig from a JSON string
exec_start_config_instance = ExecStartConfig.from_json(json)
# print the JSON string representation of the object
print(ExecStartConfig.to_json())

# convert the object into a dict
exec_start_config_dict = exec_start_config_instance.to_dict()
# create an instance of ExecStartConfig from a dict
exec_start_config_from_dict = ExecStartConfig.from_dict(exec_start_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


