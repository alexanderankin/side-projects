# docker_client.generated.SystemApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**system_auth**](SystemApi.md#system_auth) | **POST** /auth | Check auth configuration
[**system_data_usage**](SystemApi.md#system_data_usage) | **GET** /system/df | Get data usage information
[**system_events**](SystemApi.md#system_events) | **GET** /events | Monitor events
[**system_info**](SystemApi.md#system_info) | **GET** /info | Get system information
[**system_ping**](SystemApi.md#system_ping) | **GET** /_ping | Ping
[**system_ping_head**](SystemApi.md#system_ping_head) | **HEAD** /_ping | Ping
[**system_version**](SystemApi.md#system_version) | **GET** /version | Get version


# **system_auth**
> SystemAuthResponse system_auth(auth_config=auth_config)

Check auth configuration

Validate credentials for a registry and, if available, get an identity token for accessing the registry without password. 

### Example


```python
import docker_client.generated
from docker_client.generated.models.auth_config import AuthConfig
from docker_client.generated.models.system_auth_response import SystemAuthResponse
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
    api_instance = docker_client.generated.SystemApi(api_client)
    auth_config = docker_client.generated.AuthConfig() # AuthConfig | Authentication to check (optional)

    try:
        # Check auth configuration
        api_response = api_instance.system_auth(auth_config=auth_config)
        print("The response of SystemApi->system_auth:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SystemApi->system_auth: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **auth_config** | [**AuthConfig**](AuthConfig.md)| Authentication to check | [optional] 

### Return type

[**SystemAuthResponse**](SystemAuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | An identity token was generated successfully. |  -  |
**204** | No error |  -  |
**401** | Auth error |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **system_data_usage**
> SystemDataUsageResponse system_data_usage(type=type)

Get data usage information

### Example


```python
import docker_client.generated
from docker_client.generated.models.system_data_usage_response import SystemDataUsageResponse
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
    api_instance = docker_client.generated.SystemApi(api_client)
    type = ['type_example'] # List[str] | Object types, for which to compute and return data.  (optional)

    try:
        # Get data usage information
        api_response = api_instance.system_data_usage(type=type)
        print("The response of SystemApi->system_data_usage:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SystemApi->system_data_usage: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **type** | [**List[str]**](str.md)| Object types, for which to compute and return data.  | [optional] 

### Return type

[**SystemDataUsageResponse**](SystemDataUsageResponse.md)

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

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **system_events**
> EventMessage system_events(since=since, until=until, filters=filters)

Monitor events

Stream real-time events from the server.  Various objects within Docker report events when something happens to them.  Containers report these events: `attach`, `commit`, `copy`, `create`, `destroy`, `detach`, `die`, `exec_create`, `exec_detach`, `exec_start`, `exec_die`, `export`, `health_status`, `kill`, `oom`, `pause`, `rename`, `resize`, `restart`, `start`, `stop`, `top`, `unpause`, `update`, and `prune`  Images report these events: `delete`, `import`, `load`, `pull`, `push`, `save`, `tag`, `untag`, and `prune`  Volumes report these events: `create`, `mount`, `unmount`, `destroy`, and `prune`  Networks report these events: `create`, `connect`, `disconnect`, `destroy`, `update`, `remove`, and `prune`  The Docker daemon reports these events: `reload`  Services report these events: `create`, `update`, and `remove`  Nodes report these events: `create`, `update`, and `remove`  Secrets report these events: `create`, `update`, and `remove`  Configs report these events: `create`, `update`, and `remove`  The Builder reports `prune` events 

### Example


```python
import docker_client.generated
from docker_client.generated.models.event_message import EventMessage
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
    api_instance = docker_client.generated.SystemApi(api_client)
    since = 'since_example' # str | Show events created since this timestamp then stream new events. (optional)
    until = 'until_example' # str | Show events created until this timestamp then stop streaming. (optional)
    filters = 'filters_example' # str | A JSON encoded value of filters (a `map[string][]string`) to process on the event list. Available filters:  - `config=<string>` config name or ID - `container=<string>` container name or ID - `daemon=<string>` daemon name or ID - `event=<string>` event type - `image=<string>` image name or ID - `label=<string>` image or container label - `network=<string>` network name or ID - `node=<string>` node ID - `plugin`=<string> plugin name or ID - `scope`=<string> local or swarm - `secret=<string>` secret name or ID - `service=<string>` service name or ID - `type=<string>` object to filter by, one of `container`, `image`, `volume`, `network`, `daemon`, `plugin`, `node`, `service`, `secret` or `config` - `volume=<string>` volume name  (optional)

    try:
        # Monitor events
        api_response = api_instance.system_events(since=since, until=until, filters=filters)
        print("The response of SystemApi->system_events:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SystemApi->system_events: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **since** | **str**| Show events created since this timestamp then stream new events. | [optional] 
 **until** | **str**| Show events created until this timestamp then stop streaming. | [optional] 
 **filters** | **str**| A JSON encoded value of filters (a &#x60;map[string][]string&#x60;) to process on the event list. Available filters:  - &#x60;config&#x3D;&lt;string&gt;&#x60; config name or ID - &#x60;container&#x3D;&lt;string&gt;&#x60; container name or ID - &#x60;daemon&#x3D;&lt;string&gt;&#x60; daemon name or ID - &#x60;event&#x3D;&lt;string&gt;&#x60; event type - &#x60;image&#x3D;&lt;string&gt;&#x60; image name or ID - &#x60;label&#x3D;&lt;string&gt;&#x60; image or container label - &#x60;network&#x3D;&lt;string&gt;&#x60; network name or ID - &#x60;node&#x3D;&lt;string&gt;&#x60; node ID - &#x60;plugin&#x60;&#x3D;&lt;string&gt; plugin name or ID - &#x60;scope&#x60;&#x3D;&lt;string&gt; local or swarm - &#x60;secret&#x3D;&lt;string&gt;&#x60; secret name or ID - &#x60;service&#x3D;&lt;string&gt;&#x60; service name or ID - &#x60;type&#x3D;&lt;string&gt;&#x60; object to filter by, one of &#x60;container&#x60;, &#x60;image&#x60;, &#x60;volume&#x60;, &#x60;network&#x60;, &#x60;daemon&#x60;, &#x60;plugin&#x60;, &#x60;node&#x60;, &#x60;service&#x60;, &#x60;secret&#x60; or &#x60;config&#x60; - &#x60;volume&#x3D;&lt;string&gt;&#x60; volume name  | [optional] 

### Return type

[**EventMessage**](EventMessage.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**400** | bad parameter |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **system_info**
> SystemInfo system_info()

Get system information

### Example


```python
import docker_client.generated
from docker_client.generated.models.system_info import SystemInfo
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
    api_instance = docker_client.generated.SystemApi(api_client)

    try:
        # Get system information
        api_response = api_instance.system_info()
        print("The response of SystemApi->system_info:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SystemApi->system_info: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

[**SystemInfo**](SystemInfo.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **system_ping**
> str system_ping()

Ping

This is a dummy endpoint you can use to test if the server is accessible.

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
    api_instance = docker_client.generated.SystemApi(api_client)

    try:
        # Ping
        api_response = api_instance.system_ping()
        print("The response of SystemApi->system_ping:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SystemApi->system_ping: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  * Swarm - Contains information about Swarm status of the daemon, and if the daemon is acting as a manager or worker node.  <br>  * Docker-Experimental - If the server is running with experimental mode enabled <br>  * Cache-Control -  <br>  * Pragma -  <br>  * API-Version - Max API Version the server supports <br>  * Builder-Version - Default version of docker image builder  The default on Linux is version \&quot;2\&quot; (BuildKit), but the daemon can be configured to recommend version \&quot;1\&quot; (classic Builder). Windows does not yet support BuildKit for native Windows images, and uses \&quot;1\&quot; (classic builder) as a default.  This value is a recommendation as advertised by the daemon, and it is up to the client to choose which builder to use.  <br>  |
**500** | server error |  * Cache-Control -  <br>  * Pragma -  <br>  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **system_ping_head**
> str system_ping_head()

Ping

This is a dummy endpoint you can use to test if the server is accessible.

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
    api_instance = docker_client.generated.SystemApi(api_client)

    try:
        # Ping
        api_response = api_instance.system_ping_head()
        print("The response of SystemApi->system_ping_head:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SystemApi->system_ping_head: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  * Swarm - Contains information about Swarm status of the daemon, and if the daemon is acting as a manager or worker node.  <br>  * Docker-Experimental - If the server is running with experimental mode enabled <br>  * Cache-Control -  <br>  * Pragma -  <br>  * API-Version - Max API Version the server supports <br>  * Builder-Version - Default version of docker image builder <br>  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **system_version**
> SystemVersion system_version()

Get version

Returns the version of Docker that is running and various information about the system that Docker is running on.

### Example


```python
import docker_client.generated
from docker_client.generated.models.system_version import SystemVersion
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
    api_instance = docker_client.generated.SystemApi(api_client)

    try:
        # Get version
        api_response = api_instance.system_version()
        print("The response of SystemApi->system_version:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SystemApi->system_version: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

[**SystemVersion**](SystemVersion.md)

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

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

