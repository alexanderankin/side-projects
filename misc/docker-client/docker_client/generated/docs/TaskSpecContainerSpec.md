# TaskSpecContainerSpec

Container spec for the service.  <p><br /></p>  > **Note**: ContainerSpec, NetworkAttachmentSpec, and PluginSpec are > mutually exclusive. PluginSpec is only used when the Runtime field > is set to `plugin`. NetworkAttachmentSpec is used when the Runtime > field is set to `attachment`. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**image** | **str** | The image name to use for the container | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value data. | [optional] 
**command** | **List[str]** | The command to be run in the image. | [optional] 
**args** | **List[str]** | Arguments to the command. | [optional] 
**hostname** | **str** | The hostname to use for the container, as a valid [RFC 1123](https://tools.ietf.org/html/rfc1123) hostname.  | [optional] 
**env** | **List[str]** | A list of environment variables in the form &#x60;VAR&#x3D;value&#x60;.  | [optional] 
**dir** | **str** | The working directory for commands to run in. | [optional] 
**user** | **str** | The user inside the container. | [optional] 
**groups** | **List[str]** | A list of additional groups that the container process will run as.  | [optional] 
**privileges** | [**TaskSpecContainerSpecPrivileges**](TaskSpecContainerSpecPrivileges.md) |  | [optional] 
**tty** | **bool** | Whether a pseudo-TTY should be allocated. | [optional] 
**open_stdin** | **bool** | Open &#x60;stdin&#x60; | [optional] 
**read_only** | **bool** | Mount the container&#39;s root filesystem as read only. | [optional] 
**mounts** | [**List[Mount]**](Mount.md) | Specification for mounts to be added to containers created as part of the service.  | [optional] 
**stop_signal** | **str** | Signal to stop the container. | [optional] 
**stop_grace_period** | **int** | Amount of time to wait for the container to terminate before forcefully killing it.  | [optional] 
**health_check** | [**HealthConfig**](HealthConfig.md) |  | [optional] 
**hosts** | **List[str]** | A list of hostname/IP mappings to add to the container&#39;s &#x60;hosts&#x60; file. The format of extra hosts is specified in the [hosts(5)](http://man7.org/linux/man-pages/man5/hosts.5.html) man page:      IP_address canonical_hostname [aliases...]  | [optional] 
**dns_config** | [**TaskSpecContainerSpecDNSConfig**](TaskSpecContainerSpecDNSConfig.md) |  | [optional] 
**secrets** | [**List[TaskSpecContainerSpecSecretsInner]**](TaskSpecContainerSpecSecretsInner.md) | Secrets contains references to zero or more secrets that will be exposed to the service.  | [optional] 
**configs** | [**List[TaskSpecContainerSpecConfigsInner]**](TaskSpecContainerSpecConfigsInner.md) | Configs contains references to zero or more configs that will be exposed to the service.  | [optional] 
**isolation** | **str** | Isolation technology of the containers running the service. (Windows only)  | [optional] 
**init** | **bool** | Run an init inside the container that forwards signals and reaps processes. This field is omitted if empty, and the default (as configured on the daemon) is used.  | [optional] 
**sysctls** | **Dict[str, str]** | Set kernel namedspaced parameters (sysctls) in the container. The Sysctls option on services accepts the same sysctls as the are supported on containers. Note that while the same sysctls are supported, no guarantees or checks are made about their suitability for a clustered environment, and it&#39;s up to the user to determine whether a given sysctl will work properly in a Service.  | [optional] 
**capability_add** | **List[str]** | A list of kernel capabilities to add to the default set for the container.  | [optional] 
**capability_drop** | **List[str]** | A list of kernel capabilities to drop from the default set for the container.  | [optional] 
**ulimits** | [**List[ResourcesUlimitsInner]**](ResourcesUlimitsInner.md) | A list of resource limits to set in the container. For example: &#x60;{\&quot;Name\&quot;: \&quot;nofile\&quot;, \&quot;Soft\&quot;: 1024, \&quot;Hard\&quot;: 2048}&#x60;\&quot;  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_container_spec import TaskSpecContainerSpec

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpec from a JSON string
task_spec_container_spec_instance = TaskSpecContainerSpec.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpec.to_json())

# convert the object into a dict
task_spec_container_spec_dict = task_spec_container_spec_instance.to_dict()
# create an instance of TaskSpecContainerSpec from a dict
task_spec_container_spec_from_dict = TaskSpecContainerSpec.from_dict(task_spec_container_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


