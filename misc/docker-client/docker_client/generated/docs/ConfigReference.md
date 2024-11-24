# ConfigReference

The config-only network source to provide the configuration for this network. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**network** | **str** | The name of the config-only network that provides the network&#39;s configuration. The specified network must be an existing config-only network. Only network names are allowed, not network IDs.  | [optional] 

## Example

```python
from docker_client.generated.models.config_reference import ConfigReference

# TODO update the JSON string below
json = "{}"
# create an instance of ConfigReference from a JSON string
config_reference_instance = ConfigReference.from_json(json)
# print the JSON string representation of the object
print(ConfigReference.to_json())

# convert the object into a dict
config_reference_dict = config_reference_instance.to_dict()
# create an instance of ConfigReference from a dict
config_reference_from_dict = ConfigReference.from_dict(config_reference_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


