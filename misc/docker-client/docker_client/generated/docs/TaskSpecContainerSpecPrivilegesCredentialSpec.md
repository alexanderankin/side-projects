# TaskSpecContainerSpecPrivilegesCredentialSpec

CredentialSpec for managed service account (Windows only)

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**config** | **str** | Load credential spec from a Swarm Config with the given ID. The specified config must also be present in the Configs field with the Runtime property set.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;   &gt; **Note**: &#x60;CredentialSpec.File&#x60;, &#x60;CredentialSpec.Registry&#x60;, &gt; and &#x60;CredentialSpec.Config&#x60; are mutually exclusive.  | [optional] 
**file** | **str** | Load credential spec from this file. The file is read by the daemon, and must be present in the &#x60;CredentialSpecs&#x60; subdirectory in the docker data directory, which defaults to &#x60;C:\\ProgramData\\Docker\\&#x60; on Windows.  For example, specifying &#x60;spec.json&#x60; loads &#x60;C:\\ProgramData\\Docker\\CredentialSpecs\\spec.json&#x60;.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Note**: &#x60;CredentialSpec.File&#x60;, &#x60;CredentialSpec.Registry&#x60;, &gt; and &#x60;CredentialSpec.Config&#x60; are mutually exclusive.  | [optional] 
**registry** | **str** | Load credential spec from this value in the Windows registry. The specified registry value must be located in:  &#x60;HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Virtualization\\Containers\\CredentialSpecs&#x60;  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;   &gt; **Note**: &#x60;CredentialSpec.File&#x60;, &#x60;CredentialSpec.Registry&#x60;, &gt; and &#x60;CredentialSpec.Config&#x60; are mutually exclusive.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_container_spec_privileges_credential_spec import TaskSpecContainerSpecPrivilegesCredentialSpec

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecPrivilegesCredentialSpec from a JSON string
task_spec_container_spec_privileges_credential_spec_instance = TaskSpecContainerSpecPrivilegesCredentialSpec.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecPrivilegesCredentialSpec.to_json())

# convert the object into a dict
task_spec_container_spec_privileges_credential_spec_dict = task_spec_container_spec_privileges_credential_spec_instance.to_dict()
# create an instance of TaskSpecContainerSpecPrivilegesCredentialSpec from a dict
task_spec_container_spec_privileges_credential_spec_from_dict = TaskSpecContainerSpecPrivilegesCredentialSpec.from_dict(task_spec_container_spec_privileges_credential_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


