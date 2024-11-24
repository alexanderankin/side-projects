# ObjectVersion

The version number of the object such as node, service, etc. This is needed to avoid conflicting writes. The client must send the version number along with the modified specification when updating these objects.  This approach ensures safe concurrency and determinism in that the change on the object may not be applied if the version number has changed from the last read. In other words, if two update requests specify the same base version, only one of the requests can succeed. As a result, two separate update requests that happen at the same time will not unintentionally overwrite each other. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**index** | **int** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.object_version import ObjectVersion

# TODO update the JSON string below
json = "{}"
# create an instance of ObjectVersion from a JSON string
object_version_instance = ObjectVersion.from_json(json)
# print the JSON string representation of the object
print(ObjectVersion.to_json())

# convert the object into a dict
object_version_dict = object_version_instance.to_dict()
# create an instance of ObjectVersion from a dict
object_version_from_dict = ObjectVersion.from_dict(object_version_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


