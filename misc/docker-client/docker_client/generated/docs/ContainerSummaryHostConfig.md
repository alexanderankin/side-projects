# ContainerSummaryHostConfig


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**network_mode** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.container_summary_host_config import ContainerSummaryHostConfig

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerSummaryHostConfig from a JSON string
container_summary_host_config_instance = ContainerSummaryHostConfig.from_json(json)
# print the JSON string representation of the object
print(ContainerSummaryHostConfig.to_json())

# convert the object into a dict
container_summary_host_config_dict = container_summary_host_config_instance.to_dict()
# create an instance of ContainerSummaryHostConfig from a dict
container_summary_host_config_from_dict = ContainerSummaryHostConfig.from_dict(container_summary_host_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


