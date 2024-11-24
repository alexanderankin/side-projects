# ConfigCreateRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | User-defined name of the config. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**data** | **str** | Base64-url-safe-encoded ([RFC 4648](https://tools.ietf.org/html/rfc4648#section-5)) config data.  | [optional] 
**templating** | [**Driver**](Driver.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.config_create_request import ConfigCreateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ConfigCreateRequest from a JSON string
config_create_request_instance = ConfigCreateRequest.from_json(json)
# print the JSON string representation of the object
print(ConfigCreateRequest.to_json())

# convert the object into a dict
config_create_request_dict = config_create_request_instance.to_dict()
# create an instance of ConfigCreateRequest from a dict
config_create_request_from_dict = ConfigCreateRequest.from_dict(config_create_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


