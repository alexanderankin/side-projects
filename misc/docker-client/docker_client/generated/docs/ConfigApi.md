# docker_client.generated.ConfigApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**config_create**](ConfigApi.md#config_create) | **POST** /configs/create | Create a config
[**config_delete**](ConfigApi.md#config_delete) | **DELETE** /configs/{id} | Delete a config
[**config_inspect**](ConfigApi.md#config_inspect) | **GET** /configs/{id} | Inspect a config
[**config_list**](ConfigApi.md#config_list) | **GET** /configs | List configs
[**config_update**](ConfigApi.md#config_update) | **POST** /configs/{id}/update | Update a Config


# **config_create**
> IdResponse config_create(body=body)

Create a config

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.config_create_request import ConfigCreateRequest
from docker_client.generated.docker_client.generated.models.id_response import IdResponse
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
    api_instance = docker_client.generated.ConfigApi(api_client)
    body = docker_client.generated.ConfigCreateRequest() # ConfigCreateRequest |  (optional)

    try:
        # Create a config
        api_response = api_instance.config_create(body=body)
        print("The response of ConfigApi->config_create:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ConfigApi->config_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ConfigCreateRequest**](ConfigCreateRequest.md)|  | [optional] 

### Return type

[**IdResponse**](IdResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | no error |  -  |
**409** | name conflicts with an existing object |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **config_delete**
> config_delete(id)

Delete a config

### Example


```python
import docker_client.generated.docker_client.generated
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
    api_instance = docker_client.generated.ConfigApi(api_client)
    id = 'id_example' # str | ID of the config

    try:
        # Delete a config
        api_instance.config_delete(id)
    except Exception as e:
        print("Exception when calling ConfigApi->config_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID of the config | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**204** | no error |  -  |
**404** | config not found |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **config_inspect**
> Config config_inspect(id)

Inspect a config

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.config import Config
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
    api_instance = docker_client.generated.ConfigApi(api_client)
    id = 'id_example' # str | ID of the config

    try:
        # Inspect a config
        api_response = api_instance.config_inspect(id)
        print("The response of ConfigApi->config_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ConfigApi->config_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID of the config | 

### Return type

[**Config**](Config.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | config not found |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **config_list**
> List[Config] config_list(filters=filters)

List configs

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.config import Config
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
    api_instance = docker_client.generated.ConfigApi(api_client)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the configs list.  Available filters:  - `id=<config id>` - `label=<key> or label=<key>=value` - `name=<config name>` - `names=<config name>`  (optional)

    try:
        # List configs
        api_response = api_instance.config_list(filters=filters)
        print("The response of ConfigApi->config_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ConfigApi->config_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the configs list.  Available filters:  - &#x60;id&#x3D;&lt;config id&gt;&#x60; - &#x60;label&#x3D;&lt;key&gt; or label&#x3D;&lt;key&gt;&#x3D;value&#x60; - &#x60;name&#x3D;&lt;config name&gt;&#x60; - &#x60;names&#x3D;&lt;config name&gt;&#x60;  | [optional] 

### Return type

[**List[Config]**](Config.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **config_update**
> config_update(id, version, body=body)

Update a Config

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.config_spec import ConfigSpec
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
    api_instance = docker_client.generated.ConfigApi(api_client)
    id = 'id_example' # str | The ID or name of the config
    version = 56 # int | The version number of the config object being updated. This is required to avoid conflicting writes. 
    body = docker_client.generated.ConfigSpec() # ConfigSpec | The spec of the config to update. Currently, only the Labels field can be updated. All other fields must remain unchanged from the [ConfigInspect endpoint](#operation/ConfigInspect) response values.  (optional)

    try:
        # Update a Config
        api_instance.config_update(id, version, body=body)
    except Exception as e:
        print("Exception when calling ConfigApi->config_update: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| The ID or name of the config | 
 **version** | **int**| The version number of the config object being updated. This is required to avoid conflicting writes.  | 
 **body** | [**ConfigSpec**](ConfigSpec.md)| The spec of the config to update. Currently, only the Labels field can be updated. All other fields must remain unchanged from the [ConfigInspect endpoint](#operation/ConfigInspect) response values.  | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, text/plain
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**400** | bad parameter |  -  |
**404** | no such config |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

