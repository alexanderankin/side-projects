# TaskSpecContainerSpecSecretsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**file** | [**TaskSpecContainerSpecSecretsInnerFile**](TaskSpecContainerSpecSecretsInnerFile.md) |  | [optional] 
**secret_id** | **str** | SecretID represents the ID of the specific secret that we&#39;re referencing.  | [optional] 
**secret_name** | **str** | SecretName is the name of the secret that this references, but this is just provided for lookup/display purposes. The secret in the reference will be identified by its ID.  | [optional] 

## Example

```python
from docker_client.generated.models.task_spec_container_spec_secrets_inner import TaskSpecContainerSpecSecretsInner

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecSecretsInner from a JSON string
task_spec_container_spec_secrets_inner_instance = TaskSpecContainerSpecSecretsInner.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecSecretsInner.to_json())

# convert the object into a dict
task_spec_container_spec_secrets_inner_dict = task_spec_container_spec_secrets_inner_instance.to_dict()
# create an instance of TaskSpecContainerSpecSecretsInner from a dict
task_spec_container_spec_secrets_inner_from_dict = TaskSpecContainerSpecSecretsInner.from_dict(task_spec_container_spec_secrets_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


