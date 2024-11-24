# ConfigSpec


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | User-defined name of the config. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**data** | **str** | Base64-url-safe-encoded ([RFC 4648](https://tools.ietf.org/html/rfc4648#section-5)) config data.  | [optional] 
**templating** | [**Driver**](Driver.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.config_spec import ConfigSpec

# TODO update the JSON string below
json = "{}"
# create an instance of ConfigSpec from a JSON string
config_spec_instance = ConfigSpec.from_json(json)
# print the JSON string representation of the object
print(ConfigSpec.to_json())

# convert the object into a dict
config_spec_dict = config_spec_instance.to_dict()
# create an instance of ConfigSpec from a dict
config_spec_from_dict = ConfigSpec.from_dict(config_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


