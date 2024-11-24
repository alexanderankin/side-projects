# TaskSpecPlacementPreferencesInnerSpread


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**spread_descriptor** | **str** | label descriptor, such as &#x60;engine.labels.az&#x60;.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_placement_preferences_inner_spread import TaskSpecPlacementPreferencesInnerSpread

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecPlacementPreferencesInnerSpread from a JSON string
task_spec_placement_preferences_inner_spread_instance = TaskSpecPlacementPreferencesInnerSpread.from_json(json)
# print the JSON string representation of the object
print(TaskSpecPlacementPreferencesInnerSpread.to_json())

# convert the object into a dict
task_spec_placement_preferences_inner_spread_dict = task_spec_placement_preferences_inner_spread_instance.to_dict()
# create an instance of TaskSpecPlacementPreferencesInnerSpread from a dict
task_spec_placement_preferences_inner_spread_from_dict = TaskSpecPlacementPreferencesInnerSpread.from_dict(task_spec_placement_preferences_inner_spread_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


