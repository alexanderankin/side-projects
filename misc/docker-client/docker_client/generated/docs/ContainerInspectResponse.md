# ContainerInspectResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The ID of the container | [optional] 
**created** | **str** | The time the container was created | [optional] 
**path** | **str** | The path to the command being run | [optional] 
**args** | **List[str]** | The arguments to the command being run | [optional] 
**state** | [**ContainerState**](ContainerState.md) |  | [optional] 
**image** | **str** | The container&#39;s image ID | [optional] 
**resolv_conf_path** | **str** |  | [optional] 
**hostname_path** | **str** |  | [optional] 
**hosts_path** | **str** |  | [optional] 
**log_path** | **str** |  | [optional] 
**name** | **str** |  | [optional] 
**restart_count** | **int** |  | [optional] 
**driver** | **str** |  | [optional] 
**platform** | **str** |  | [optional] 
**mount_label** | **str** |  | [optional] 
**process_label** | **str** |  | [optional] 
**app_armor_profile** | **str** |  | [optional] 
**exec_ids** | **List[str]** | IDs of exec instances that are running in the container. | [optional] 
**host_config** | [**HostConfig**](HostConfig.md) |  | [optional] 
**graph_driver** | [**GraphDriverData**](GraphDriverData.md) |  | [optional] 
**size_rw** | **int** | The size of files that have been created or changed by this container.  | [optional] 
**size_root_fs** | **int** | The total size of all the files in this container. | [optional] 
**mounts** | [**List[MountPoint]**](MountPoint.md) |  | [optional] 
**config** | [**ContainerConfig**](ContainerConfig.md) |  | [optional] 
**network_settings** | [**NetworkSettings**](NetworkSettings.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.container_inspect_response import ContainerInspectResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerInspectResponse from a JSON string
container_inspect_response_instance = ContainerInspectResponse.from_json(json)
# print the JSON string representation of the object
print(ContainerInspectResponse.to_json())

# convert the object into a dict
container_inspect_response_dict = container_inspect_response_instance.to_dict()
# create an instance of ContainerInspectResponse from a dict
container_inspect_response_from_dict = ContainerInspectResponse.from_dict(container_inspect_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


