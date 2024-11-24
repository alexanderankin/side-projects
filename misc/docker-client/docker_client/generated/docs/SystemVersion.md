# SystemVersion

Response of Engine API: GET \"/version\" 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**platform** | [**SystemVersionPlatform**](SystemVersionPlatform.md) |  | [optional] 
**components** | [**List[SystemVersionComponentsInner]**](SystemVersionComponentsInner.md) | Information about system components  | [optional] 
**version** | **str** | The version of the daemon | [optional] 
**api_version** | **str** | The default (and highest) API version that is supported by the daemon  | [optional] 
**min_api_version** | **str** | The minimum API version that is supported by the daemon  | [optional] 
**git_commit** | **str** | The Git commit of the source code that was used to build the daemon  | [optional] 
**go_version** | **str** | The version Go used to compile the daemon, and the version of the Go runtime in use.  | [optional] 
**os** | **str** | The operating system that the daemon is running on (\&quot;linux\&quot; or \&quot;windows\&quot;)  | [optional] 
**arch** | **str** | The architecture that the daemon is running on  | [optional] 
**kernel_version** | **str** | The kernel version (&#x60;uname -r&#x60;) that the daemon is running on.  This field is omitted when empty.  | [optional] 
**experimental** | **bool** | Indicates if the daemon is started with experimental features enabled.  This field is omitted when empty / false.  | [optional] 
**build_time** | **str** | The date and time that the daemon was compiled.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.system_version import SystemVersion

# TODO update the JSON string below
json = "{}"
# create an instance of SystemVersion from a JSON string
system_version_instance = SystemVersion.from_json(json)
# print the JSON string representation of the object
print(SystemVersion.to_json())

# convert the object into a dict
system_version_dict = system_version_instance.to_dict()
# create an instance of SystemVersion from a dict
system_version_from_dict = SystemVersion.from_dict(system_version_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


