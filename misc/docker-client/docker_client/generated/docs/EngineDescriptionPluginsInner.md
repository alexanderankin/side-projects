# EngineDescriptionPluginsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | **str** |  | [optional] 
**name** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.models.engine_description_plugins_inner import EngineDescriptionPluginsInner

# TODO update the JSON string below
json = "{}"
# create an instance of EngineDescriptionPluginsInner from a JSON string
engine_description_plugins_inner_instance = EngineDescriptionPluginsInner.from_json(json)
# print the JSON string representation of the object
print(EngineDescriptionPluginsInner.to_json())

# convert the object into a dict
engine_description_plugins_inner_dict = engine_description_plugins_inner_instance.to_dict()
# create an instance of EngineDescriptionPluginsInner from a dict
engine_description_plugins_inner_from_dict = EngineDescriptionPluginsInner.from_dict(engine_description_plugins_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


