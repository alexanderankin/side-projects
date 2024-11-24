# docker_client.generated.ServiceApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**service_create**](ServiceApi.md#service_create) | **POST** /services/create | Create a service
[**service_delete**](ServiceApi.md#service_delete) | **DELETE** /services/{id} | Delete a service
[**service_inspect**](ServiceApi.md#service_inspect) | **GET** /services/{id} | Inspect a service
[**service_list**](ServiceApi.md#service_list) | **GET** /services | List services
[**service_logs**](ServiceApi.md#service_logs) | **GET** /services/{id}/logs | Get service logs
[**service_update**](ServiceApi.md#service_update) | **POST** /services/{id}/update | Update a service


# **service_create**
> ServiceCreateResponse service_create(body, x_registry_auth=x_registry_auth)

Create a service

### Example


```python
import docker_client.generated
from docker_client.generated.models.service_create_request import ServiceCreateRequest
from docker_client.generated.models.service_create_response import ServiceCreateResponse
from docker_client.generated.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to http://localhost/v1.45
# See configuration.py for a list of all supported configuration parameters.
configuration = docker_client.generated.Configuration(
    host = "http://localhost/v1.45"
)


# Enter a context with an instance of the API client
with docker_client.generated.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = docker_client.generated.ServiceApi(api_client)
    body = docker_client.generated.ServiceCreateRequest() # ServiceCreateRequest | 
    x_registry_auth = 'x_registry_auth_example' # str | A base64url-encoded auth configuration for pulling from private registries.  Refer to the [authentication section](#section/Authentication) for details.  (optional)

    try:
        # Create a service
        api_response = api_instance.service_create(body, x_registry_auth=x_registry_auth)
        print("The response of ServiceApi->service_create:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ServiceApi->service_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ServiceCreateRequest**](ServiceCreateRequest.md)|  | 
 **x_registry_auth** | **str**| A base64url-encoded auth configuration for pulling from private registries.  Refer to the [authentication section](#section/Authentication) for details.  | [optional] 

### Return type

[**ServiceCreateResponse**](ServiceCreateResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | no error |  -  |
**400** | bad parameter |  -  |
**403** | network is not eligible for services |  -  |
**409** | name conflicts with an existing service |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **service_delete**
> service_delete(id)

Delete a service

### Example


```python
import docker_client.generated
from docker_client.generated.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to http://localhost/v1.45
# See configuration.py for a list of all supported configuration parameters.
configuration = docker_client.generated.Configuration(
    host = "http://localhost/v1.45"
)


# Enter a context with an instance of the API client
with docker_client.generated.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = docker_client.generated.ServiceApi(api_client)
    id = 'id_example' # str | ID or name of service.

    try:
        # Delete a service
        api_instance.service_delete(id)
    except Exception as e:
        print("Exception when calling ServiceApi->service_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of service. | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such service |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **service_inspect**
> Service service_inspect(id, insert_defaults=insert_defaults)

Inspect a service

### Example


```python
import docker_client.generated
from docker_client.generated.models.service import Service
from docker_client.generated.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to http://localhost/v1.45
# See configuration.py for a list of all supported configuration parameters.
configuration = docker_client.generated.Configuration(
    host = "http://localhost/v1.45"
)


# Enter a context with an instance of the API client
with docker_client.generated.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = docker_client.generated.ServiceApi(api_client)
    id = 'id_example' # str | ID or name of service.
    insert_defaults = False # bool | Fill empty fields with default values. (optional) (default to False)

    try:
        # Inspect a service
        api_response = api_instance.service_inspect(id, insert_defaults=insert_defaults)
        print("The response of ServiceApi->service_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ServiceApi->service_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of service. | 
 **insert_defaults** | **bool**| Fill empty fields with default values. | [optional] [default to False]

### Return type

[**Service**](Service.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such service |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **service_list**
> List[Service] service_list(filters=filters, status=status)

List services

### Example


```python
import docker_client.generated
from docker_client.generated.models.service import Service
from docker_client.generated.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to http://localhost/v1.45
# See configuration.py for a list of all supported configuration parameters.
configuration = docker_client.generated.Configuration(
    host = "http://localhost/v1.45"
)


# Enter a context with an instance of the API client
with docker_client.generated.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = docker_client.generated.ServiceApi(api_client)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the services list.  Available filters:  - `id=<service id>` - `label=<service label>` - `mode=[\"replicated\"|\"global\"]` - `name=<service name>`  (optional)
    status = True # bool | Include service status, with count of running and desired tasks.  (optional)

    try:
        # List services
        api_response = api_instance.service_list(filters=filters, status=status)
        print("The response of ServiceApi->service_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ServiceApi->service_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the services list.  Available filters:  - &#x60;id&#x3D;&lt;service id&gt;&#x60; - &#x60;label&#x3D;&lt;service label&gt;&#x60; - &#x60;mode&#x3D;[\&quot;replicated\&quot;|\&quot;global\&quot;]&#x60; - &#x60;name&#x3D;&lt;service name&gt;&#x60;  | [optional] 
 **status** | **bool**| Include service status, with count of running and desired tasks.  | [optional] 

### Return type

[**List[Service]**](Service.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **service_logs**
> bytearray service_logs(id, details=details, follow=follow, stdout=stdout, stderr=stderr, since=since, timestamps=timestamps, tail=tail)

Get service logs

Get `stdout` and `stderr` logs from a service. See also [`/containers/{id}/logs`](#operation/ContainerLogs).  **Note**: This endpoint works only for services with the `local`, `json-file` or `journald` logging drivers. 

### Example


```python
import docker_client.generated
from docker_client.generated.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to http://localhost/v1.45
# See configuration.py for a list of all supported configuration parameters.
configuration = docker_client.generated.Configuration(
    host = "http://localhost/v1.45"
)


# Enter a context with an instance of the API client
with docker_client.generated.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = docker_client.generated.ServiceApi(api_client)
    id = 'id_example' # str | ID or name of the service
    details = False # bool | Show service context and extra details provided to logs. (optional) (default to False)
    follow = False # bool | Keep connection after returning logs. (optional) (default to False)
    stdout = False # bool | Return logs from `stdout` (optional) (default to False)
    stderr = False # bool | Return logs from `stderr` (optional) (default to False)
    since = 0 # int | Only return logs since this time, as a UNIX timestamp (optional) (default to 0)
    timestamps = False # bool | Add timestamps to every log line (optional) (default to False)
    tail = 'all' # str | Only return this number of log lines from the end of the logs. Specify as an integer or `all` to output all log lines.  (optional) (default to 'all')

    try:
        # Get service logs
        api_response = api_instance.service_logs(id, details=details, follow=follow, stdout=stdout, stderr=stderr, since=since, timestamps=timestamps, tail=tail)
        print("The response of ServiceApi->service_logs:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ServiceApi->service_logs: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the service | 
 **details** | **bool**| Show service context and extra details provided to logs. | [optional] [default to False]
 **follow** | **bool**| Keep connection after returning logs. | [optional] [default to False]
 **stdout** | **bool**| Return logs from &#x60;stdout&#x60; | [optional] [default to False]
 **stderr** | **bool**| Return logs from &#x60;stderr&#x60; | [optional] [default to False]
 **since** | **int**| Only return logs since this time, as a UNIX timestamp | [optional] [default to 0]
 **timestamps** | **bool**| Add timestamps to every log line | [optional] [default to False]
 **tail** | **str**| Only return this number of log lines from the end of the logs. Specify as an integer or &#x60;all&#x60; to output all log lines.  | [optional] [default to &#39;all&#39;]

### Return type

**bytearray**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/vnd.docker.raw-stream, application/vnd.docker.multiplexed-stream, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | logs returned as a stream in response body |  -  |
**404** | no such service |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **service_update**
> ServiceUpdateResponse service_update(id, version, body, registry_auth_from=registry_auth_from, rollback=rollback, x_registry_auth=x_registry_auth)

Update a service

### Example


```python
import docker_client.generated
from docker_client.generated.models.service_update_request import ServiceUpdateRequest
from docker_client.generated.models.service_update_response import ServiceUpdateResponse
from docker_client.generated.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to http://localhost/v1.45
# See configuration.py for a list of all supported configuration parameters.
configuration = docker_client.generated.Configuration(
    host = "http://localhost/v1.45"
)


# Enter a context with an instance of the API client
with docker_client.generated.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = docker_client.generated.ServiceApi(api_client)
    id = 'id_example' # str | ID or name of service.
    version = 56 # int | The version number of the service object being updated. This is required to avoid conflicting writes. This version number should be the value as currently set on the service *before* the update. You can find the current version by calling `GET /services/{id}` 
    body = docker_client.generated.ServiceUpdateRequest() # ServiceUpdateRequest | 
    registry_auth_from = spec # str | If the `X-Registry-Auth` header is not specified, this parameter indicates where to find registry authorization credentials.  (optional) (default to spec)
    rollback = 'rollback_example' # str | Set to this parameter to `previous` to cause a server-side rollback to the previous service spec. The supplied spec will be ignored in this case.  (optional)
    x_registry_auth = 'x_registry_auth_example' # str | A base64url-encoded auth configuration for pulling from private registries.  Refer to the [authentication section](#section/Authentication) for details.  (optional)

    try:
        # Update a service
        api_response = api_instance.service_update(id, version, body, registry_auth_from=registry_auth_from, rollback=rollback, x_registry_auth=x_registry_auth)
        print("The response of ServiceApi->service_update:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ServiceApi->service_update: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of service. | 
 **version** | **int**| The version number of the service object being updated. This is required to avoid conflicting writes. This version number should be the value as currently set on the service *before* the update. You can find the current version by calling &#x60;GET /services/{id}&#x60;  | 
 **body** | [**ServiceUpdateRequest**](ServiceUpdateRequest.md)|  | 
 **registry_auth_from** | **str**| If the &#x60;X-Registry-Auth&#x60; header is not specified, this parameter indicates where to find registry authorization credentials.  | [optional] [default to spec]
 **rollback** | **str**| Set to this parameter to &#x60;previous&#x60; to cause a server-side rollback to the previous service spec. The supplied spec will be ignored in this case.  | [optional] 
 **x_registry_auth** | **str**| A base64url-encoded auth configuration for pulling from private registries.  Refer to the [authentication section](#section/Authentication) for details.  | [optional] 

### Return type

[**ServiceUpdateResponse**](ServiceUpdateResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**400** | bad parameter |  -  |
**404** | no such service |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

