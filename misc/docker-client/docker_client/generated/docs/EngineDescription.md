# EngineDescription

EngineDescription provides information about an engine.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**engine_version** | **str** |  | [optional] 
**labels** | **Dict[str, str]** |  | [optional] 
**plugins** | [**List[EngineDescriptionPluginsInner]**](EngineDescriptionPluginsInner.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.engine_description import EngineDescription

# TODO update the JSON string below
json = "{}"
# create an instance of EngineDescription from a JSON string
engine_description_instance = EngineDescription.from_json(json)
# print the JSON string representation of the object
print(EngineDescription.to_json())

# convert the object into a dict
engine_description_dict = engine_description_instance.to_dict()
# create an instance of EngineDescription from a dict
engine_description_from_dict = EngineDescription.from_dict(engine_description_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


