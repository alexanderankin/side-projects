# IPAMConfig


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**subnet** | **str** |  | [optional] 
**ip_range** | **str** |  | [optional] 
**gateway** | **str** |  | [optional] 
**auxiliary_addresses** | **Dict[str, str]** |  | [optional] 

## Example

```python
from docker_client.generated.models.ipam_config import IPAMConfig

# TODO update the JSON string below
json = "{}"
# create an instance of IPAMConfig from a JSON string
ipam_config_instance = IPAMConfig.from_json(json)
# print the JSON string representation of the object
print(IPAMConfig.to_json())

# convert the object into a dict
ipam_config_dict = ipam_config_instance.to_dict()
# create an instance of IPAMConfig from a dict
ipam_config_from_dict = IPAMConfig.from_dict(ipam_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


