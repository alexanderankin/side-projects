# ContainerSummaryNetworkSettings

A summary of the container's network settings

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**networks** | [**Dict[str, EndpointSettings]**](EndpointSettings.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.container_summary_network_settings import ContainerSummaryNetworkSettings

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerSummaryNetworkSettings from a JSON string
container_summary_network_settings_instance = ContainerSummaryNetworkSettings.from_json(json)
# print the JSON string representation of the object
print(ContainerSummaryNetworkSettings.to_json())

# convert the object into a dict
container_summary_network_settings_dict = container_summary_network_settings_instance.to_dict()
# create an instance of ContainerSummaryNetworkSettings from a dict
container_summary_network_settings_from_dict = ContainerSummaryNetworkSettings.from_dict(container_summary_network_settings_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


