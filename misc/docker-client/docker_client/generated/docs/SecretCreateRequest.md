# SecretCreateRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | User-defined name of the secret. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**data** | **str** | Base64-url-safe-encoded ([RFC 4648](https://tools.ietf.org/html/rfc4648#section-5)) data to store as secret.  This field is only used to _create_ a secret, and is not returned by other endpoints.  | [optional] 
**driver** | [**Driver**](Driver.md) |  | [optional] 
**templating** | [**Driver**](Driver.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.secret_create_request import SecretCreateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of SecretCreateRequest from a JSON string
secret_create_request_instance = SecretCreateRequest.from_json(json)
# print the JSON string representation of the object
print(SecretCreateRequest.to_json())

# convert the object into a dict
secret_create_request_dict = secret_create_request_instance.to_dict()
# create an instance of SecretCreateRequest from a dict
secret_create_request_from_dict = SecretCreateRequest.from_dict(secret_create_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


