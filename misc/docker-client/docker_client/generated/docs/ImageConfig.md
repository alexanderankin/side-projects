# ImageConfig

Configuration of the image. These fields are used as defaults when starting a container from the image. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**hostname** | **str** | The hostname to use for the container, as a valid RFC 1123 hostname.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always empty and must not be used.  | [optional] 
**domainname** | **str** | The domain name to use for the container.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always empty and must not be used.  | [optional] 
**user** | **str** | The user that commands are run as inside the container. | [optional] 
**attach_stdin** | **bool** | Whether to attach to &#x60;stdin&#x60;.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always false and must not be used.  | [optional] [default to False]
**attach_stdout** | **bool** | Whether to attach to &#x60;stdout&#x60;.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always false and must not be used.  | [optional] [default to False]
**attach_stderr** | **bool** | Whether to attach to &#x60;stderr&#x60;.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always false and must not be used.  | [optional] [default to False]
**exposed_ports** | **Dict[str, object]** | An object mapping ports to an empty object in the form:  &#x60;{\&quot;&lt;port&gt;/&lt;tcp|udp|sctp&gt;\&quot;: {}}&#x60;  | [optional] 
**tty** | **bool** | Attach standard streams to a TTY, including &#x60;stdin&#x60; if it is not closed.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always false and must not be used.  | [optional] [default to False]
**open_stdin** | **bool** | Open &#x60;stdin&#x60;  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always false and must not be used.  | [optional] [default to False]
**stdin_once** | **bool** | Close &#x60;stdin&#x60; after one attached client disconnects.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always false and must not be used.  | [optional] [default to False]
**env** | **List[str]** | A list of environment variables to set inside the container in the form &#x60;[\&quot;VAR&#x3D;value\&quot;, ...]&#x60;. A variable without &#x60;&#x3D;&#x60; is removed from the environment, rather than to have an empty value.  | [optional] 
**cmd** | **List[str]** | Command to run specified as a string or an array of strings.  | [optional] 
**healthcheck** | [**HealthConfig**](HealthConfig.md) |  | [optional] 
**args_escaped** | **bool** | Command is already escaped (Windows only) | [optional] [default to False]
**image** | **str** | The name (or reference) of the image to use when creating the container, or which was used when the container was created.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always empty and must not be used.  | [optional] [default to '']
**volumes** | **Dict[str, object]** | An object mapping mount point paths inside the container to empty objects.  | [optional] 
**working_dir** | **str** | The working directory for commands to run in. | [optional] 
**entrypoint** | **List[str]** | The entry point for the container as a string or an array of strings.  If the array consists of exactly one empty string (&#x60;[\&quot;\&quot;]&#x60;) then the entry point is reset to system default (i.e., the entry point used by docker when there is no &#x60;ENTRYPOINT&#x60; instruction in the &#x60;Dockerfile&#x60;).  | [optional] 
**network_disabled** | **bool** | Disable networking for the container.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always omitted and must not be used.  | [optional] [default to False]
**mac_address** | **str** | MAC address of the container.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: this field is deprecated in API v1.44 and up. It is always omitted.  | [optional] [default to '']
**on_build** | **List[str]** | &#x60;ONBUILD&#x60; metadata that were defined in the image&#39;s &#x60;Dockerfile&#x60;.  | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**stop_signal** | **str** | Signal to stop a container as a string or unsigned integer.  | [optional] 
**stop_timeout** | **int** | Timeout to stop a container in seconds.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: this field is always omitted and must not be used.  | [optional] 
**shell** | **List[str]** | Shell for when &#x60;RUN&#x60;, &#x60;CMD&#x60;, and &#x60;ENTRYPOINT&#x60; uses a shell.  | [optional] 

## Example

```python
from docker_client.generated.models.image_config import ImageConfig

# TODO update the JSON string below
json = "{}"
# create an instance of ImageConfig from a JSON string
image_config_instance = ImageConfig.from_json(json)
# print the JSON string representation of the object
print(ImageConfig.to_json())

# convert the object into a dict
image_config_dict = image_config_instance.to_dict()
# create an instance of ImageConfig from a dict
image_config_from_dict = ImageConfig.from_dict(image_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


