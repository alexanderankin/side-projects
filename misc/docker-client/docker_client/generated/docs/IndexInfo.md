# IndexInfo

IndexInfo contains information about a registry.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the registry, such as \&quot;docker.io\&quot;.  | [optional] 
**mirrors** | **List[str]** | List of mirrors, expressed as URIs.  | [optional] 
**secure** | **bool** | Indicates if the registry is part of the list of insecure registries.  If &#x60;false&#x60;, the registry is insecure. Insecure registries accept un-encrypted (HTTP) and/or untrusted (HTTPS with certificates from unknown CAs) communication.  &gt; **Warning**: Insecure registries can be useful when running a local &gt; registry. However, because its use creates security vulnerabilities &gt; it should ONLY be enabled for testing purposes. For increased &gt; security, users should add their CA to their system&#39;s list of &gt; trusted CAs instead of enabling this option.  | [optional] 
**official** | **bool** | Indicates whether this is an official registry (i.e., Docker Hub / docker.io)  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.index_info import IndexInfo

# TODO update the JSON string below
json = "{}"
# create an instance of IndexInfo from a JSON string
index_info_instance = IndexInfo.from_json(json)
# print the JSON string representation of the object
print(IndexInfo.to_json())

# convert the object into a dict
index_info_dict = index_info_instance.to_dict()
# create an instance of IndexInfo from a dict
index_info_from_dict = IndexInfo.from_dict(index_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


