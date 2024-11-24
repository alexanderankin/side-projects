# TaskSpecContainerSpecDNSConfig

Specification for DNS related configurations in resolver configuration file (`resolv.conf`). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**nameservers** | **List[str]** | The IP addresses of the name servers. | [optional] 
**search** | **List[str]** | A search list for host-name lookup. | [optional] 
**options** | **List[str]** | A list of internal resolver variables to be modified (e.g., &#x60;debug&#x60;, &#x60;ndots:3&#x60;, etc.).  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_container_spec_dns_config import TaskSpecContainerSpecDNSConfig

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecDNSConfig from a JSON string
task_spec_container_spec_dns_config_instance = TaskSpecContainerSpecDNSConfig.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecDNSConfig.to_json())

# convert the object into a dict
task_spec_container_spec_dns_config_dict = task_spec_container_spec_dns_config_instance.to_dict()
# create an instance of TaskSpecContainerSpecDNSConfig from a dict
task_spec_container_spec_dns_config_from_dict = TaskSpecContainerSpecDNSConfig.from_dict(task_spec_container_spec_dns_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


