# TaskSpecNetworkAttachmentSpec

Read-only spec type for non-swarm containers attached to swarm overlay networks.  <p><br /></p>  > **Note**: ContainerSpec, NetworkAttachmentSpec, and PluginSpec are > mutually exclusive. PluginSpec is only used when the Runtime field > is set to `plugin`. NetworkAttachmentSpec is used when the Runtime > field is set to `attachment`. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**container_id** | **str** | ID of the container represented by this task | [optional] 

## Example

```python
from docker_client.generated.models.task_spec_network_attachment_spec import TaskSpecNetworkAttachmentSpec

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecNetworkAttachmentSpec from a JSON string
task_spec_network_attachment_spec_instance = TaskSpecNetworkAttachmentSpec.from_json(json)
# print the JSON string representation of the object
print(TaskSpecNetworkAttachmentSpec.to_json())

# convert the object into a dict
task_spec_network_attachment_spec_dict = task_spec_network_attachment_spec_instance.to_dict()
# create an instance of TaskSpecNetworkAttachmentSpec from a dict
task_spec_network_attachment_spec_from_dict = TaskSpecNetworkAttachmentSpec.from_dict(task_spec_network_attachment_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


