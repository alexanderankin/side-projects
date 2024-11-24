# ServiceUpdateStatus

The status of a service update.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**state** | **str** |  | [optional] 
**started_at** | **str** |  | [optional] 
**completed_at** | **str** |  | [optional] 
**message** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.models.service_update_status import ServiceUpdateStatus

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceUpdateStatus from a JSON string
service_update_status_instance = ServiceUpdateStatus.from_json(json)
# print the JSON string representation of the object
print(ServiceUpdateStatus.to_json())

# convert the object into a dict
service_update_status_dict = service_update_status_instance.to_dict()
# create an instance of ServiceUpdateStatus from a dict
service_update_status_from_dict = ServiceUpdateStatus.from_dict(service_update_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


