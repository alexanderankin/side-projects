# ServiceSpec

User modifiable configuration for a service.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the service. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**task_template** | [**TaskSpec**](TaskSpec.md) |  | [optional] 
**mode** | [**ServiceSpecMode**](ServiceSpecMode.md) |  | [optional] 
**update_config** | [**ServiceSpecUpdateConfig**](ServiceSpecUpdateConfig.md) |  | [optional] 
**rollback_config** | [**ServiceSpecRollbackConfig**](ServiceSpecRollbackConfig.md) |  | [optional] 
**networks** | [**List[NetworkAttachmentConfig]**](NetworkAttachmentConfig.md) | Specifies which networks the service should attach to.  Deprecated: This field is deprecated since v1.44. The Networks field in TaskSpec should be used instead.  | [optional] 
**endpoint_spec** | [**EndpointSpec**](EndpointSpec.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.service_spec import ServiceSpec

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceSpec from a JSON string
service_spec_instance = ServiceSpec.from_json(json)
# print the JSON string representation of the object
print(ServiceSpec.to_json())

# convert the object into a dict
service_spec_dict = service_spec_instance.to_dict()
# create an instance of ServiceSpec from a dict
service_spec_from_dict = ServiceSpec.from_dict(service_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


