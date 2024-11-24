# ServiceUpdateResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**warnings** | **List[str]** | Optional warning messages | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.service_update_response import ServiceUpdateResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceUpdateResponse from a JSON string
service_update_response_instance = ServiceUpdateResponse.from_json(json)
# print the JSON string representation of the object
print(ServiceUpdateResponse.to_json())

# convert the object into a dict
service_update_response_dict = service_update_response_instance.to_dict()
# create an instance of ServiceUpdateResponse from a dict
service_update_response_from_dict = ServiceUpdateResponse.from_dict(service_update_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


