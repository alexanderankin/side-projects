# TaskSpecContainerSpecConfigsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**file** | [**TaskSpecContainerSpecConfigsInnerFile**](TaskSpecContainerSpecConfigsInnerFile.md) |  | [optional] 
**runtime** | **object** | Runtime represents a target that is not mounted into the container but is used by the task  &lt;p&gt;&lt;br /&gt;&lt;p&gt;  &gt; **Note**: &#x60;Configs.File&#x60; and &#x60;Configs.Runtime&#x60; are mutually &gt; exclusive  | [optional] 
**config_id** | **str** | ConfigID represents the ID of the specific config that we&#39;re referencing.  | [optional] 
**config_name** | **str** | ConfigName is the name of the config that this references, but this is just provided for lookup/display purposes. The config in the reference will be identified by its ID.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_container_spec_configs_inner import TaskSpecContainerSpecConfigsInner

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecConfigsInner from a JSON string
task_spec_container_spec_configs_inner_instance = TaskSpecContainerSpecConfigsInner.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecConfigsInner.to_json())

# convert the object into a dict
task_spec_container_spec_configs_inner_dict = task_spec_container_spec_configs_inner_instance.to_dict()
# create an instance of TaskSpecContainerSpecConfigsInner from a dict
task_spec_container_spec_configs_inner_from_dict = TaskSpecContainerSpecConfigsInner.from_dict(task_spec_container_spec_configs_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


