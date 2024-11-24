# SystemAuthResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **str** | The status of the authentication | 
**identity_token** | **str** | An opaque token used to authenticate a user after a successful login | [optional] 

## Example

```python
from docker_client.generated.models.system_auth_response import SystemAuthResponse

# TODO update the JSON string below
json = "{}"
# create an instance of SystemAuthResponse from a JSON string
system_auth_response_instance = SystemAuthResponse.from_json(json)
# print the JSON string representation of the object
print(SystemAuthResponse.to_json())

# convert the object into a dict
system_auth_response_dict = system_auth_response_instance.to_dict()
# create an instance of SystemAuthResponse from a dict
system_auth_response_from_dict = SystemAuthResponse.from_dict(system_auth_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


