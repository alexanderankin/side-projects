# TLSInfo

Information about the issuer of leaf TLS certificates and the trusted root CA certificate. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**trust_root** | **str** | The root CA certificate(s) that are used to validate leaf TLS certificates.  | [optional] 
**cert_issuer_subject** | **str** | The base64-url-safe-encoded raw subject bytes of the issuer. | [optional] 
**cert_issuer_public_key** | **str** | The base64-url-safe-encoded raw public key bytes of the issuer.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.tls_info import TLSInfo

# TODO update the JSON string below
json = "{}"
# create an instance of TLSInfo from a JSON string
tls_info_instance = TLSInfo.from_json(json)
# print the JSON string representation of the object
print(TLSInfo.to_json())

# convert the object into a dict
tls_info_dict = tls_info_instance.to_dict()
# create an instance of TLSInfo from a dict
tls_info_from_dict = TLSInfo.from_dict(tls_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


