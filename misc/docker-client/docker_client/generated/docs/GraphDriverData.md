# GraphDriverData

Information about the storage driver used to store the container's and image's filesystem. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the storage driver. | 
**data** | **Dict[str, str]** | Low-level storage metadata, provided as key/value pairs.  This information is driver-specific, and depends on the storage-driver in use, and should be used for informational purposes only.  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.graph_driver_data import GraphDriverData

# TODO update the JSON string below
json = "{}"
# create an instance of GraphDriverData from a JSON string
graph_driver_data_instance = GraphDriverData.from_json(json)
# print the JSON string representation of the object
print(GraphDriverData.to_json())

# convert the object into a dict
graph_driver_data_dict = graph_driver_data_instance.to_dict()
# create an instance of GraphDriverData from a dict
graph_driver_data_from_dict = GraphDriverData.from_dict(graph_driver_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


