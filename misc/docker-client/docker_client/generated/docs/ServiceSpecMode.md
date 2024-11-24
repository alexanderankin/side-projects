# ServiceSpecMode

Scheduling mode for the service.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**replicated** | [**ServiceSpecModeReplicated**](ServiceSpecModeReplicated.md) |  | [optional] 
**var_global** | **object** |  | [optional] 
**replicated_job** | [**ServiceSpecModeReplicatedJob**](ServiceSpecModeReplicatedJob.md) |  | [optional] 
**global_job** | **object** | The mode used for services which run a task to the completed state on each valid node.  | [optional] 

## Example

```python
from docker_client.generated.models.service_spec_mode import ServiceSpecMode

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceSpecMode from a JSON string
service_spec_mode_instance = ServiceSpecMode.from_json(json)
# print the JSON string representation of the object
print(ServiceSpecMode.to_json())

# convert the object into a dict
service_spec_mode_dict = service_spec_mode_instance.to_dict()
# create an instance of ServiceSpecMode from a dict
service_spec_mode_from_dict = ServiceSpecMode.from_dict(service_spec_mode_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


