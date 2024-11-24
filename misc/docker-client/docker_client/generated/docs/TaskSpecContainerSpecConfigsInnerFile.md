# TaskSpecContainerSpecConfigsInnerFile

File represents a specific target that is backed by a file.  <p><br /><p>  > **Note**: `Configs.File` and `Configs.Runtime` are mutually exclusive 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name represents the final filename in the filesystem.  | [optional] 
**uid** | **str** | UID represents the file UID. | [optional] 
**gid** | **str** | GID represents the file GID. | [optional] 
**mode** | **int** | Mode represents the FileMode of the file. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_container_spec_configs_inner_file import TaskSpecContainerSpecConfigsInnerFile

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecConfigsInnerFile from a JSON string
task_spec_container_spec_configs_inner_file_instance = TaskSpecContainerSpecConfigsInnerFile.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecConfigsInnerFile.to_json())

# convert the object into a dict
task_spec_container_spec_configs_inner_file_dict = task_spec_container_spec_configs_inner_file_instance.to_dict()
# create an instance of TaskSpecContainerSpecConfigsInnerFile from a dict
task_spec_container_spec_configs_inner_file_from_dict = TaskSpecContainerSpecConfigsInnerFile.from_dict(task_spec_container_spec_configs_inner_file_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


