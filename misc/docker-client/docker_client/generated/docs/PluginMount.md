# PluginMount


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | 
**description** | **str** |  | 
**settable** | **List[str]** |  | 
**source** | **str** |  | 
**destination** | **str** |  | 
**type** | **str** |  | 
**options** | **List[str]** |  | 

## Example

```python
from docker_client.generated.models.plugin_mount import PluginMount

# TODO update the JSON string below
json = "{}"
# create an instance of PluginMount from a JSON string
plugin_mount_instance = PluginMount.from_json(json)
# print the JSON string representation of the object
print(PluginMount.to_json())

# convert the object into a dict
plugin_mount_dict = plugin_mount_instance.to_dict()
# create an instance of PluginMount from a dict
plugin_mount_from_dict = PluginMount.from_dict(plugin_mount_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


