# ExecInspectResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**can_remove** | **bool** |  | [optional] 
**detach_keys** | **str** |  | [optional] 
**id** | **str** |  | [optional] 
**running** | **bool** |  | [optional] 
**exit_code** | **int** |  | [optional] 
**process_config** | [**ProcessConfig**](ProcessConfig.md) |  | [optional] 
**open_stdin** | **bool** |  | [optional] 
**open_stderr** | **bool** |  | [optional] 
**open_stdout** | **bool** |  | [optional] 
**container_id** | **str** |  | [optional] 
**pid** | **int** | The system process ID for the exec process. | [optional] 

## Example

```python
from docker_client.generated.models.exec_inspect_response import ExecInspectResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ExecInspectResponse from a JSON string
exec_inspect_response_instance = ExecInspectResponse.from_json(json)
# print the JSON string representation of the object
print(ExecInspectResponse.to_json())

# convert the object into a dict
exec_inspect_response_dict = exec_inspect_response_instance.to_dict()
# create an instance of ExecInspectResponse from a dict
exec_inspect_response_from_dict = ExecInspectResponse.from_dict(exec_inspect_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


