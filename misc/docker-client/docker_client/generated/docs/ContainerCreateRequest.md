# ContainerCreateRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**hostname** | **str** | The hostname to use for the container, as a valid RFC 1123 hostname.  | [optional] 
**domainname** | **str** | The domain name to use for the container.  | [optional] 
**user** | **str** | The user that commands are run as inside the container. | [optional] 
**attach_stdin** | **bool** | Whether to attach to &#x60;stdin&#x60;. | [optional] [default to False]
**attach_stdout** | **bool** | Whether to attach to &#x60;stdout&#x60;. | [optional] [default to True]
**attach_stderr** | **bool** | Whether to attach to &#x60;stderr&#x60;. | [optional] [default to True]
**exposed_ports** | **Dict[str, object]** | An object mapping ports to an empty object in the form:  &#x60;{\&quot;&lt;port&gt;/&lt;tcp|udp|sctp&gt;\&quot;: {}}&#x60;  | [optional] 
**tty** | **bool** | Attach standard streams to a TTY, including &#x60;stdin&#x60; if it is not closed.  | [optional] [default to False]
**open_stdin** | **bool** | Open &#x60;stdin&#x60; | [optional] [default to False]
**stdin_once** | **bool** | Close &#x60;stdin&#x60; after one attached client disconnects | [optional] [default to False]
**env** | **List[str]** | A list of environment variables to set inside the container in the form &#x60;[\&quot;VAR&#x3D;value\&quot;, ...]&#x60;. A variable without &#x60;&#x3D;&#x60; is removed from the environment, rather than to have an empty value.  | [optional] 
**cmd** | **List[str]** | Command to run specified as a string or an array of strings.  | [optional] 
**healthcheck** | [**HealthConfig**](HealthConfig.md) |  | [optional] 
**args_escaped** | **bool** | Command is already escaped (Windows only) | [optional] [default to False]
**image** | **str** | The name (or reference) of the image to use when creating the container, or which was used when the container was created.  | [optional] 
**volumes** | **Dict[str, object]** | An object mapping mount point paths inside the container to empty objects.  | [optional] 
**working_dir** | **str** | The working directory for commands to run in. | [optional] 
**entrypoint** | **List[str]** | The entry point for the container as a string or an array of strings.  If the array consists of exactly one empty string (&#x60;[\&quot;\&quot;]&#x60;) then the entry point is reset to system default (i.e., the entry point used by docker when there is no &#x60;ENTRYPOINT&#x60; instruction in the &#x60;Dockerfile&#x60;).  | [optional] 
**network_disabled** | **bool** | Disable networking for the container. | [optional] 
**mac_address** | **str** | MAC address of the container.  Deprecated: this field is deprecated in API v1.44 and up. Use EndpointSettings.MacAddress instead.  | [optional] 
**on_build** | **List[str]** | &#x60;ONBUILD&#x60; metadata that were defined in the image&#39;s &#x60;Dockerfile&#x60;.  | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**stop_signal** | **str** | Signal to stop a container as a string or unsigned integer.  | [optional] 
**stop_timeout** | **int** | Timeout to stop a container in seconds. | [optional] 
**shell** | **List[str]** | Shell for when &#x60;RUN&#x60;, &#x60;CMD&#x60;, and &#x60;ENTRYPOINT&#x60; uses a shell.  | [optional] 
**host_config** | [**HostConfig**](HostConfig.md) |  | [optional] 
**networking_config** | [**NetworkingConfig**](NetworkingConfig.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.container_create_request import ContainerCreateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerCreateRequest from a JSON string
container_create_request_instance = ContainerCreateRequest.from_json(json)
# print the JSON string representation of the object
print(ContainerCreateRequest.to_json())

# convert the object into a dict
container_create_request_dict = container_create_request_instance.to_dict()
# create an instance of ContainerCreateRequest from a dict
container_create_request_from_dict = ContainerCreateRequest.from_dict(container_create_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


