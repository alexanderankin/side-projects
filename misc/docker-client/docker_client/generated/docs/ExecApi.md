# docker_client.generated.ExecApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**container_exec**](ExecApi.md#container_exec) | **POST** /containers/{id}/exec | Create an exec instance
[**exec_inspect**](ExecApi.md#exec_inspect) | **GET** /exec/{id}/json | Inspect an exec instance
[**exec_resize**](ExecApi.md#exec_resize) | **POST** /exec/{id}/resize | Resize an exec instance
[**exec_start**](ExecApi.md#exec_start) | **POST** /exec/{id}/start | Start an exec instance


# **container_exec**
> IdResponse container_exec(id, exec_config)

Create an exec instance

Run a command inside a running container.

### Example


```python
import docker_client.generated
from docker_client.generated.models.exec_config import ExecConfig
from docker_client.generated.models.id_response import IdResponse
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
    api_instance = docker_client.generated.ExecApi(api_client)
    id = 'id_example' # str | ID or name of container
    exec_config = docker_client.generated.ExecConfig() # ExecConfig | Exec configuration

    try:
        # Create an exec instance
        api_response = api_instance.container_exec(id, exec_config)
        print("The response of ExecApi->container_exec:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ExecApi->container_exec: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of container | 
 **exec_config** | [**ExecConfig**](ExecConfig.md)| Exec configuration | 

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
**404** | no such container |  -  |
**409** | container is paused |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **exec_inspect**
> ExecInspectResponse exec_inspect(id)

Inspect an exec instance

Return low-level information about an exec instance.

### Example


```python
import docker_client.generated
from docker_client.generated.models.exec_inspect_response import ExecInspectResponse
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
    api_instance = docker_client.generated.ExecApi(api_client)
    id = 'id_example' # str | Exec instance ID

    try:
        # Inspect an exec instance
        api_response = api_instance.exec_inspect(id)
        print("The response of ExecApi->exec_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ExecApi->exec_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| Exec instance ID | 

### Return type

[**ExecInspectResponse**](ExecInspectResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**404** | No such exec instance |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **exec_resize**
> exec_resize(id, h=h, w=w)

Resize an exec instance

Resize the TTY session used by an exec instance. This endpoint only works if `tty` was specified as part of creating and starting the exec instance. 

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
    api_instance = docker_client.generated.ExecApi(api_client)
    id = 'id_example' # str | Exec instance ID
    h = 56 # int | Height of the TTY session in characters (optional)
    w = 56 # int | Width of the TTY session in characters (optional)

    try:
        # Resize an exec instance
        api_instance.exec_resize(id, h=h, w=w)
    except Exception as e:
        print("Exception when calling ExecApi->exec_resize: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| Exec instance ID | 
 **h** | **int**| Height of the TTY session in characters | [optional] 
 **w** | **int**| Width of the TTY session in characters | [optional] 

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
**200** | No error |  -  |
**400** | bad parameter |  -  |
**404** | No such exec instance |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **exec_start**
> exec_start(id, exec_start_config=exec_start_config)

Start an exec instance

Starts a previously set up exec instance. If detach is true, this endpoint returns immediately after starting the command. Otherwise, it sets up an interactive session with the command. 

### Example


```python
import docker_client.generated
from docker_client.generated.models.exec_start_config import ExecStartConfig
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
    api_instance = docker_client.generated.ExecApi(api_client)
    id = 'id_example' # str | Exec instance ID
    exec_start_config = docker_client.generated.ExecStartConfig() # ExecStartConfig |  (optional)

    try:
        # Start an exec instance
        api_instance.exec_start(id, exec_start_config=exec_start_config)
    except Exception as e:
        print("Exception when calling ExecApi->exec_start: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| Exec instance ID | 
 **exec_start_config** | [**ExecStartConfig**](ExecStartConfig.md)|  | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.docker.raw-stream, application/vnd.docker.multiplexed-stream

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**404** | No such exec instance |  -  |
**409** | Container is stopped or paused |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

