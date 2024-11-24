# HostConfigAllOfLogConfig

The logging configuration for this container

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | **str** |  | [optional] 
**config** | **Dict[str, str]** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.host_config_all_of_log_config import HostConfigAllOfLogConfig

# TODO update the JSON string below
json = "{}"
# create an instance of HostConfigAllOfLogConfig from a JSON string
host_config_all_of_log_config_instance = HostConfigAllOfLogConfig.from_json(json)
# print the JSON string representation of the object
print(HostConfigAllOfLogConfig.to_json())

# convert the object into a dict
host_config_all_of_log_config_dict = host_config_all_of_log_config_instance.to_dict()
# create an instance of HostConfigAllOfLogConfig from a dict
host_config_all_of_log_config_from_dict = HostConfigAllOfLogConfig.from_dict(host_config_all_of_log_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


