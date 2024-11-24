# docker_client.generated.DistributionApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**distribution_inspect**](DistributionApi.md#distribution_inspect) | **GET** /distribution/{name}/json | Get image information from the registry


# **distribution_inspect**
> DistributionInspect distribution_inspect(name)

Get image information from the registry

Return image digest and platform information by contacting the registry. 

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.distribution_inspect import DistributionInspect
from docker_client.generated.docker_client.generated.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to http://localhost/v1.45
# See configuration.py for a list of all supported configuration parameters.
configuration = docker_client.generated.Configuration(
    host = "http://localhost/v1.45"
)


# Enter a context with an instance of the API client
with docker_client.generated.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = docker_client.generated.DistributionApi(api_client)
    name = 'name_example' # str | Image name or id

    try:
        # Get image information from the registry
        api_response = api_instance.distribution_inspect(name)
        print("The response of DistributionApi->distribution_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DistributionApi->distribution_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Image name or id | 

### Return type

[**DistributionInspect**](DistributionInspect.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | descriptor and platform information |  -  |
**401** | Failed authentication or no image found |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

