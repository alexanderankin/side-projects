# TaskSpecPlacementPreferencesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**spread** | [**TaskSpecPlacementPreferencesInnerSpread**](TaskSpecPlacementPreferencesInnerSpread.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_placement_preferences_inner import TaskSpecPlacementPreferencesInner

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecPlacementPreferencesInner from a JSON string
task_spec_placement_preferences_inner_instance = TaskSpecPlacementPreferencesInner.from_json(json)
# print the JSON string representation of the object
print(TaskSpecPlacementPreferencesInner.to_json())

# convert the object into a dict
task_spec_placement_preferences_inner_dict = task_spec_placement_preferences_inner_instance.to_dict()
# create an instance of TaskSpecPlacementPreferencesInner from a dict
task_spec_placement_preferences_inner_from_dict = TaskSpecPlacementPreferencesInner.from_dict(task_spec_placement_preferences_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


