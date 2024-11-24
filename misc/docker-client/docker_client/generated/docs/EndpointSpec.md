# EndpointSpec

Properties that can be configured to access and load balance a service.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**mode** | **str** | The mode of resolution to use for internal load balancing between tasks.  | [optional] [default to 'vip']
**ports** | [**List[EndpointPortConfig]**](EndpointPortConfig.md) | List of exposed ports that this service is accessible on from the outside. Ports can only be provided if &#x60;vip&#x60; resolution mode is used.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.endpoint_spec import EndpointSpec

# TODO update the JSON string below
json = "{}"
# create an instance of EndpointSpec from a JSON string
endpoint_spec_instance = EndpointSpec.from_json(json)
# print the JSON string representation of the object
print(EndpointSpec.to_json())

# convert the object into a dict
endpoint_spec_dict = endpoint_spec_instance.to_dict()
# create an instance of EndpointSpec from a dict
endpoint_spec_from_dict = EndpointSpec.from_dict(endpoint_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


