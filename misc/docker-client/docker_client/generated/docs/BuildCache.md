# BuildCache

BuildCache contains information about a build cache record. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | Unique ID of the build cache record.  | [optional] 
**parent** | **str** | ID of the parent build cache record.  &gt; **Deprecated**: This field is deprecated, and omitted if empty.  | [optional] 
**parents** | **List[str]** | List of parent build cache record IDs.  | [optional] 
**type** | **str** | Cache record type.  | [optional] 
**description** | **str** | Description of the build-step that produced the build cache.  | [optional] 
**in_use** | **bool** | Indicates if the build cache is in use.  | [optional] 
**shared** | **bool** | Indicates if the build cache is shared.  | [optional] 
**size** | **int** | Amount of disk space used by the build cache (in bytes).  | [optional] 
**created_at** | **str** | Date and time at which the build cache was created in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  | [optional] 
**last_used_at** | **str** | Date and time at which the build cache was last used in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  | [optional] 
**usage_count** | **int** |  | [optional] 

## Example

```python
from docker_client.generated.models.build_cache import BuildCache

# TODO update the JSON string below
json = "{}"
# create an instance of BuildCache from a JSON string
build_cache_instance = BuildCache.from_json(json)
# print the JSON string representation of the object
print(BuildCache.to_json())

# convert the object into a dict
build_cache_dict = build_cache_instance.to_dict()
# create an instance of BuildCache from a dict
build_cache_from_dict = BuildCache.from_dict(build_cache_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


