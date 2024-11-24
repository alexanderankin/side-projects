# TaskSpecPluginSpec

Plugin spec for the service.  *(Experimental release only.)*  <p><br /></p>  > **Note**: ContainerSpec, NetworkAttachmentSpec, and PluginSpec are > mutually exclusive. PluginSpec is only used when the Runtime field > is set to `plugin`. NetworkAttachmentSpec is used when the Runtime > field is set to `attachment`. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The name or &#39;alias&#39; to use for the plugin. | [optional] 
**remote** | **str** | The plugin image reference to use. | [optional] 
**disabled** | **bool** | Disable the plugin once scheduled. | [optional] 
**plugin_privilege** | [**List[PluginPrivilege]**](PluginPrivilege.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_plugin_spec import TaskSpecPluginSpec

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecPluginSpec from a JSON string
task_spec_plugin_spec_instance = TaskSpecPluginSpec.from_json(json)
# print the JSON string representation of the object
print(TaskSpecPluginSpec.to_json())

# convert the object into a dict
task_spec_plugin_spec_dict = task_spec_plugin_spec_instance.to_dict()
# create an instance of TaskSpecPluginSpec from a dict
task_spec_plugin_spec_from_dict = TaskSpecPluginSpec.from_dict(task_spec_plugin_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


