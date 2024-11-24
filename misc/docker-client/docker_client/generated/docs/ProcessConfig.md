# ProcessConfig


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**privileged** | **bool** |  | [optional] 
**user** | **str** |  | [optional] 
**tty** | **bool** |  | [optional] 
**entrypoint** | **str** |  | [optional] 
**arguments** | **List[str]** |  | [optional] 

## Example

```python
from docker_client.generated.models.process_config import ProcessConfig

# TODO update the JSON string below
json = "{}"
# create an instance of ProcessConfig from a JSON string
process_config_instance = ProcessConfig.from_json(json)
# print the JSON string representation of the object
print(ProcessConfig.to_json())

# convert the object into a dict
process_config_dict = process_config_instance.to_dict()
# create an instance of ProcessConfig from a dict
process_config_from_dict = ProcessConfig.from_dict(process_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


