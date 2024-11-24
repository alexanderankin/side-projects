# Commit

Commit holds the Git-commit (SHA1) that a binary was built from, as reported in the version-string of external tools, such as `containerd`, or `runC`. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | Actual commit ID of external tool. | [optional] 
**expected** | **str** | Commit ID of external tool expected by dockerd as set at build time.  | [optional] 

## Example

```python
from docker_client.generated.models.commit import Commit

# TODO update the JSON string below
json = "{}"
# create an instance of Commit from a JSON string
commit_instance = Commit.from_json(json)
# print the JSON string representation of the object
print(Commit.to_json())

# convert the object into a dict
commit_dict = commit_instance.to_dict()
# create an instance of Commit from a dict
commit_from_dict = Commit.from_dict(commit_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


