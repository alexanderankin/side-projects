# Runtime

Runtime describes an [OCI compliant](https://github.com/opencontainers/runtime-spec) runtime.  The runtime is invoked by the daemon via the `containerd` daemon. OCI runtimes act as an interface to the Linux kernel namespaces, cgroups, and SELinux. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**path** | **str** | Name and, optional, path, of the OCI executable binary.  If the path is omitted, the daemon searches the host&#39;s &#x60;$PATH&#x60; for the binary and uses the first result.  | [optional] 
**runtime_args** | **List[str]** | List of command-line arguments to pass to the runtime when invoked.  | [optional] 
**status** | **Dict[str, str]** | Information specific to the runtime.  While this API specification does not define data provided by runtimes, the following well-known properties may be provided by runtimes:  &#x60;org.opencontainers.runtime-spec.features&#x60;: features structure as defined in the [OCI Runtime Specification](https://github.com/opencontainers/runtime-spec/blob/main/features.md), in a JSON string representation.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: The information returned in this field, including the &gt; formatting of values and labels, should not be considered stable, &gt; and may change without notice.  | [optional] 

## Example

```python
from docker_client.generated.models.runtime import Runtime

# TODO update the JSON string below
json = "{}"
# create an instance of Runtime from a JSON string
runtime_instance = Runtime.from_json(json)
# print the JSON string representation of the object
print(Runtime.to_json())

# convert the object into a dict
runtime_dict = runtime_instance.to_dict()
# create an instance of Runtime from a dict
runtime_from_dict = Runtime.from_dict(runtime_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


