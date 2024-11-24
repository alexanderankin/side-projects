# IPAM


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**driver** | **str** | Name of the IPAM driver to use. | [optional] [default to 'default']
**config** | [**List[IPAMConfig]**](IPAMConfig.md) | List of IPAM configuration options, specified as a map:  &#x60;&#x60;&#x60; {\&quot;Subnet\&quot;: &lt;CIDR&gt;, \&quot;IPRange\&quot;: &lt;CIDR&gt;, \&quot;Gateway\&quot;: &lt;IP address&gt;, \&quot;AuxAddress\&quot;: &lt;device_name:IP address&gt;} &#x60;&#x60;&#x60;  | [optional] 
**options** | **Dict[str, str]** | Driver-specific options, specified as a map. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.ipam import IPAM

# TODO update the JSON string below
json = "{}"
# create an instance of IPAM from a JSON string
ipam_instance = IPAM.from_json(json)
# print the JSON string representation of the object
print(IPAM.to_json())

# convert the object into a dict
ipam_dict = ipam_instance.to_dict()
# create an instance of IPAM from a dict
ipam_from_dict = IPAM.from_dict(ipam_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


