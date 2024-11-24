# ContainerSummary


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The ID of this container | [optional] 
**names** | **List[str]** | The names that this container has been given | [optional] 
**image** | **str** | The name of the image used when creating this container | [optional] 
**image_id** | **str** | The ID of the image that this container was created from | [optional] 
**command** | **str** | Command to run when starting the container | [optional] 
**created** | **int** | When the container was created | [optional] 
**ports** | [**List[Port]**](Port.md) | The ports exposed by this container | [optional] 
**size_rw** | **int** | The size of files that have been created or changed by this container | [optional] 
**size_root_fs** | **int** | The total size of all the files in this container | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**state** | **str** | The state of this container (e.g. &#x60;Exited&#x60;) | [optional] 
**status** | **str** | Additional human-readable status of this container (e.g. &#x60;Exit 0&#x60;) | [optional] 
**host_config** | [**ContainerSummaryHostConfig**](ContainerSummaryHostConfig.md) |  | [optional] 
**network_settings** | [**ContainerSummaryNetworkSettings**](ContainerSummaryNetworkSettings.md) |  | [optional] 
**mounts** | [**List[MountPoint]**](MountPoint.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.container_summary import ContainerSummary

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerSummary from a JSON string
container_summary_instance = ContainerSummary.from_json(json)
# print the JSON string representation of the object
print(ContainerSummary.to_json())

# convert the object into a dict
container_summary_dict = container_summary_instance.to_dict()
# create an instance of ContainerSummary from a dict
container_summary_from_dict = ContainerSummary.from_dict(container_summary_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


