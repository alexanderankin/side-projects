# PluginsInfo

Available plugins per type.  <p><br /></p>  > **Note**: Only unmanaged (V1) plugins are included in this list. > V1 plugins are \"lazily\" loaded, and are not returned in this list > if there is no resource using the plugin. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**volume** | **List[str]** | Names of available volume-drivers, and network-driver plugins. | [optional] 
**network** | **List[str]** | Names of available network-drivers, and network-driver plugins. | [optional] 
**authorization** | **List[str]** | Names of available authorization plugins. | [optional] 
**log** | **List[str]** | Names of available logging-drivers, and logging-driver plugins. | [optional] 

## Example

```python
from docker_client.generated.models.plugins_info import PluginsInfo

# TODO update the JSON string below
json = "{}"
# create an instance of PluginsInfo from a JSON string
plugins_info_instance = PluginsInfo.from_json(json)
# print the JSON string representation of the object
print(PluginsInfo.to_json())

# convert the object into a dict
plugins_info_dict = plugins_info_instance.to_dict()
# create an instance of PluginsInfo from a dict
plugins_info_from_dict = PluginsInfo.from_dict(plugins_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


