# docker_client.generated.SwarmApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**swarm_init**](SwarmApi.md#swarm_init) | **POST** /swarm/init | Initialize a new swarm
[**swarm_inspect**](SwarmApi.md#swarm_inspect) | **GET** /swarm | Inspect swarm
[**swarm_join**](SwarmApi.md#swarm_join) | **POST** /swarm/join | Join an existing swarm
[**swarm_leave**](SwarmApi.md#swarm_leave) | **POST** /swarm/leave | Leave a swarm
[**swarm_unlock**](SwarmApi.md#swarm_unlock) | **POST** /swarm/unlock | Unlock a locked manager
[**swarm_unlockkey**](SwarmApi.md#swarm_unlockkey) | **GET** /swarm/unlockkey | Get the unlock key
[**swarm_update**](SwarmApi.md#swarm_update) | **POST** /swarm/update | Update a swarm


# **swarm_init**
> str swarm_init(body)

Initialize a new swarm

### Example


```python
import docker_client.generated
from docker_client.generated.models.swarm_init_request import SwarmInitRequest
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
    api_instance = docker_client.generated.SwarmApi(api_client)
    body = docker_client.generated.SwarmInitRequest() # SwarmInitRequest | 

    try:
        # Initialize a new swarm
        api_response = api_instance.swarm_init(body)
        print("The response of SwarmApi->swarm_init:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SwarmApi->swarm_init: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SwarmInitRequest**](SwarmInitRequest.md)|  | 

### Return type

**str**

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
**500** | server error |  -  |
**503** | node is already part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **swarm_inspect**
> Swarm swarm_inspect()

Inspect swarm

### Example


```python
import docker_client.generated
from docker_client.generated.models.swarm import Swarm
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
    api_instance = docker_client.generated.SwarmApi(api_client)

    try:
        # Inspect swarm
        api_response = api_instance.swarm_inspect()
        print("The response of SwarmApi->swarm_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SwarmApi->swarm_inspect: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

[**Swarm**](Swarm.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such swarm |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **swarm_join**
> swarm_join(body)

Join an existing swarm

### Example


```python
import docker_client.generated
from docker_client.generated.models.swarm_join_request import SwarmJoinRequest
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
    api_instance = docker_client.generated.SwarmApi(api_client)
    body = docker_client.generated.SwarmJoinRequest() # SwarmJoinRequest | 

    try:
        # Join an existing swarm
        api_instance.swarm_join(body)
    except Exception as e:
        print("Exception when calling SwarmApi->swarm_join: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SwarmJoinRequest**](SwarmJoinRequest.md)|  | 

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
**500** | server error |  -  |
**503** | node is already part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **swarm_leave**
> swarm_leave(force=force)

Leave a swarm

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
    api_instance = docker_client.generated.SwarmApi(api_client)
    force = False # bool | Force leave swarm, even if this is the last manager or that it will break the cluster.  (optional) (default to False)

    try:
        # Leave a swarm
        api_instance.swarm_leave(force=force)
    except Exception as e:
        print("Exception when calling SwarmApi->swarm_leave: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **force** | **bool**| Force leave swarm, even if this is the last manager or that it will break the cluster.  | [optional] [default to False]

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
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **swarm_unlock**
> swarm_unlock(body)

Unlock a locked manager

### Example


```python
import docker_client.generated
from docker_client.generated.models.swarm_unlock_request import SwarmUnlockRequest
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
    api_instance = docker_client.generated.SwarmApi(api_client)
    body = docker_client.generated.SwarmUnlockRequest() # SwarmUnlockRequest | 

    try:
        # Unlock a locked manager
        api_instance.swarm_unlock(body)
    except Exception as e:
        print("Exception when calling SwarmApi->swarm_unlock: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SwarmUnlockRequest**](SwarmUnlockRequest.md)|  | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **swarm_unlockkey**
> UnlockKeyResponse swarm_unlockkey()

Get the unlock key

### Example


```python
import docker_client.generated
from docker_client.generated.models.unlock_key_response import UnlockKeyResponse
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
    api_instance = docker_client.generated.SwarmApi(api_client)

    try:
        # Get the unlock key
        api_response = api_instance.swarm_unlockkey()
        print("The response of SwarmApi->swarm_unlockkey:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SwarmApi->swarm_unlockkey: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

[**UnlockKeyResponse**](UnlockKeyResponse.md)

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

# **swarm_update**
> swarm_update(version, body, rotate_worker_token=rotate_worker_token, rotate_manager_token=rotate_manager_token, rotate_manager_unlock_key=rotate_manager_unlock_key)

Update a swarm

### Example


```python
import docker_client.generated
from docker_client.generated.models.swarm_spec import SwarmSpec
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
    api_instance = docker_client.generated.SwarmApi(api_client)
    version = 56 # int | The version number of the swarm object being updated. This is required to avoid conflicting writes. 
    body = docker_client.generated.SwarmSpec() # SwarmSpec | 
    rotate_worker_token = False # bool | Rotate the worker join token. (optional) (default to False)
    rotate_manager_token = False # bool | Rotate the manager join token. (optional) (default to False)
    rotate_manager_unlock_key = False # bool | Rotate the manager unlock key. (optional) (default to False)

    try:
        # Update a swarm
        api_instance.swarm_update(version, body, rotate_worker_token=rotate_worker_token, rotate_manager_token=rotate_manager_token, rotate_manager_unlock_key=rotate_manager_unlock_key)
    except Exception as e:
        print("Exception when calling SwarmApi->swarm_update: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **int**| The version number of the swarm object being updated. This is required to avoid conflicting writes.  | 
 **body** | [**SwarmSpec**](SwarmSpec.md)|  | 
 **rotate_worker_token** | **bool**| Rotate the worker join token. | [optional] [default to False]
 **rotate_manager_token** | **bool**| Rotate the manager join token. | [optional] [default to False]
 **rotate_manager_unlock_key** | **bool**| Rotate the manager unlock key. | [optional] [default to False]

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
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

