# TaskSpec

User modifiable task configuration.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**plugin_spec** | [**TaskSpecPluginSpec**](TaskSpecPluginSpec.md) |  | [optional] 
**container_spec** | [**TaskSpecContainerSpec**](TaskSpecContainerSpec.md) |  | [optional] 
**network_attachment_spec** | [**TaskSpecNetworkAttachmentSpec**](TaskSpecNetworkAttachmentSpec.md) |  | [optional] 
**resources** | [**TaskSpecResources**](TaskSpecResources.md) |  | [optional] 
**restart_policy** | [**TaskSpecRestartPolicy**](TaskSpecRestartPolicy.md) |  | [optional] 
**placement** | [**TaskSpecPlacement**](TaskSpecPlacement.md) |  | [optional] 
**force_update** | **int** | A counter that triggers an update even if no relevant parameters have been changed.  | [optional] 
**runtime** | **str** | Runtime is the type of runtime specified for the task executor.  | [optional] 
**networks** | [**List[NetworkAttachmentConfig]**](NetworkAttachmentConfig.md) | Specifies which networks the service should attach to. | [optional] 
**log_driver** | [**TaskSpecLogDriver**](TaskSpecLogDriver.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec import TaskSpec

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpec from a JSON string
task_spec_instance = TaskSpec.from_json(json)
# print the JSON string representation of the object
print(TaskSpec.to_json())

# convert the object into a dict
task_spec_dict = task_spec_instance.to_dict()
# create an instance of TaskSpec from a dict
task_spec_from_dict = TaskSpec.from_dict(task_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


