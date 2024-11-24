# ServiceJobStatus

The status of the service when it is in one of ReplicatedJob or GlobalJob modes. Absent on Replicated and Global mode services. The JobIteration is an ObjectVersion, but unlike the Service's version, does not need to be sent with an update request. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**job_iteration** | [**ObjectVersion**](ObjectVersion.md) |  | [optional] 
**last_execution** | **str** | The last time, as observed by the server, that this job was started.  | [optional] 

## Example

```python
from docker_client.generated.models.service_job_status import ServiceJobStatus

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceJobStatus from a JSON string
service_job_status_instance = ServiceJobStatus.from_json(json)
# print the JSON string representation of the object
print(ServiceJobStatus.to_json())

# convert the object into a dict
service_job_status_dict = service_job_status_instance.to_dict()
# create an instance of ServiceJobStatus from a dict
service_job_status_from_dict = ServiceJobStatus.from_dict(service_job_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


