# ProgressDetail


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**current** | **int** |  | [optional] 
**total** | **int** |  | [optional] 

## Example

```python
from docker_client.generated.models.progress_detail import ProgressDetail

# TODO update the JSON string below
json = "{}"
# create an instance of ProgressDetail from a JSON string
progress_detail_instance = ProgressDetail.from_json(json)
# print the JSON string representation of the object
print(ProgressDetail.to_json())

# convert the object into a dict
progress_detail_dict = progress_detail_instance.to_dict()
# create an instance of ProgressDetail from a dict
progress_detail_from_dict = ProgressDetail.from_dict(progress_detail_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


