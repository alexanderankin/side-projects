# ServiceServiceStatus

The status of the service's tasks. Provided only when requested as part of a ServiceList operation. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**running_tasks** | **int** | The number of tasks for the service currently in the Running state.  | [optional] 
**desired_tasks** | **int** | The number of tasks for the service desired to be running. For replicated services, this is the replica count from the service spec. For global services, this is computed by taking count of all tasks for the service with a Desired State other than Shutdown.  | [optional] 
**completed_tasks** | **int** | The number of tasks for a job that are in the Completed state. This field must be cross-referenced with the service type, as the value of 0 may mean the service is not in a job mode, or it may mean the job-mode service has no tasks yet Completed.  | [optional] 

## Example

```python
from docker_client.generated.models.service_service_status import ServiceServiceStatus

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceServiceStatus from a JSON string
service_service_status_instance = ServiceServiceStatus.from_json(json)
# print the JSON string representation of the object
print(ServiceServiceStatus.to_json())

# convert the object into a dict
service_service_status_dict = service_service_status_instance.to_dict()
# create an instance of ServiceServiceStatus from a dict
service_service_status_from_dict = ServiceServiceStatus.from_dict(service_service_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


