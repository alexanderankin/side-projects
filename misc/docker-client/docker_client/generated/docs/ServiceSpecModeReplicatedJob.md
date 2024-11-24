# ServiceSpecModeReplicatedJob

The mode used for services with a finite number of tasks that run to a completed state. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**max_concurrent** | **int** | The maximum number of replicas to run simultaneously.  | [optional] [default to 1]
**total_completions** | **int** | The total number of replicas desired to reach the Completed state. If unset, will default to the value of &#x60;MaxConcurrent&#x60;  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.service_spec_mode_replicated_job import ServiceSpecModeReplicatedJob

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceSpecModeReplicatedJob from a JSON string
service_spec_mode_replicated_job_instance = ServiceSpecModeReplicatedJob.from_json(json)
# print the JSON string representation of the object
print(ServiceSpecModeReplicatedJob.to_json())

# convert the object into a dict
service_spec_mode_replicated_job_dict = service_spec_mode_replicated_job_instance.to_dict()
# create an instance of ServiceSpecModeReplicatedJob from a dict
service_spec_mode_replicated_job_from_dict = ServiceSpecModeReplicatedJob.from_dict(service_spec_mode_replicated_job_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


