# docker_client.generated.NetworkApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**network_connect**](NetworkApi.md#network_connect) | **POST** /networks/{id}/connect | Connect a container to a network
[**network_create**](NetworkApi.md#network_create) | **POST** /networks/create | Create a network
[**network_delete**](NetworkApi.md#network_delete) | **DELETE** /networks/{id} | Remove a network
[**network_disconnect**](NetworkApi.md#network_disconnect) | **POST** /networks/{id}/disconnect | Disconnect a container from a network
[**network_inspect**](NetworkApi.md#network_inspect) | **GET** /networks/{id} | Inspect a network
[**network_list**](NetworkApi.md#network_list) | **GET** /networks | List networks
[**network_prune**](NetworkApi.md#network_prune) | **POST** /networks/prune | Delete unused networks


# **network_connect**
> network_connect(id, container)

Connect a container to a network

The network must be either a local-scoped network or a swarm-scoped network with the `attachable` option set. A network cannot be re-attached to a running container

### Example


```python
import docker_client.generated
from docker_client.generated.models.network_connect_request import NetworkConnectRequest
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
    api_instance = docker_client.generated.NetworkApi(api_client)
    id = 'id_example' # str | Network ID or name
    container = docker_client.generated.NetworkConnectRequest() # NetworkConnectRequest | 

    try:
        # Connect a container to a network
        api_instance.network_connect(id, container)
    except Exception as e:
        print("Exception when calling NetworkApi->network_connect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| Network ID or name | 
 **container** | [**NetworkConnectRequest**](NetworkConnectRequest.md)|  | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**400** | bad parameter |  -  |
**403** | Operation forbidden |  -  |
**404** | Network or container not found |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **network_create**
> NetworkCreateResponse network_create(network_config)

Create a network

### Example


```python
import docker_client.generated
from docker_client.generated.models.network_create_request import NetworkCreateRequest
from docker_client.generated.models.network_create_response import NetworkCreateResponse
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
    api_instance = docker_client.generated.NetworkApi(api_client)
    network_config = docker_client.generated.NetworkCreateRequest() # NetworkCreateRequest | Network configuration

    try:
        # Create a network
        api_response = api_instance.network_create(network_config)
        print("The response of NetworkApi->network_create:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling NetworkApi->network_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **network_config** | [**NetworkCreateRequest**](NetworkCreateRequest.md)| Network configuration | 

### Return type

[**NetworkCreateResponse**](NetworkCreateResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | No error |  -  |
**400** | bad parameter |  -  |
**403** | Forbidden operation. This happens when trying to create a network named after a pre-defined network, or when trying to create an overlay network on a daemon which is not part of a Swarm cluster.  |  -  |
**404** | plugin not found |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **network_delete**
> network_delete(id)

Remove a network

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
    api_instance = docker_client.generated.NetworkApi(api_client)
    id = 'id_example' # str | Network ID or name

    try:
        # Remove a network
        api_instance.network_delete(id)
    except Exception as e:
        print("Exception when calling NetworkApi->network_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| Network ID or name | 

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
**204** | No error |  -  |
**403** | operation not supported for pre-defined networks |  -  |
**404** | no such network |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **network_disconnect**
> network_disconnect(id, container)

Disconnect a container from a network

### Example


```python
import docker_client.generated
from docker_client.generated.models.network_disconnect_request import NetworkDisconnectRequest
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
    api_instance = docker_client.generated.NetworkApi(api_client)
    id = 'id_example' # str | Network ID or name
    container = docker_client.generated.NetworkDisconnectRequest() # NetworkDisconnectRequest | 

    try:
        # Disconnect a container from a network
        api_instance.network_disconnect(id, container)
    except Exception as e:
        print("Exception when calling NetworkApi->network_disconnect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| Network ID or name | 
 **container** | [**NetworkDisconnectRequest**](NetworkDisconnectRequest.md)|  | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**403** | Operation not supported for swarm scoped networks |  -  |
**404** | Network or container not found |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **network_inspect**
> Network network_inspect(id, verbose=verbose, scope=scope)

Inspect a network

### Example


```python
import docker_client.generated
from docker_client.generated.models.network import Network
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
    api_instance = docker_client.generated.NetworkApi(api_client)
    id = 'id_example' # str | Network ID or name
    verbose = False # bool | Detailed inspect output for troubleshooting (optional) (default to False)
    scope = 'scope_example' # str | Filter the network by scope (swarm, global, or local) (optional)

    try:
        # Inspect a network
        api_response = api_instance.network_inspect(id, verbose=verbose, scope=scope)
        print("The response of NetworkApi->network_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling NetworkApi->network_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| Network ID or name | 
 **verbose** | **bool**| Detailed inspect output for troubleshooting | [optional] [default to False]
 **scope** | **str**| Filter the network by scope (swarm, global, or local) | [optional] 

### Return type

[**Network**](Network.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**404** | Network not found |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **network_list**
> List[Network] network_list(filters=filters)

List networks

Returns a list of networks. For details on the format, see the [network inspect endpoint](#operation/NetworkInspect).  Note that it uses a different, smaller representation of a network than inspecting a single network. For example, the list of containers attached to the network is not propagated in API versions 1.28 and up. 

### Example


```python
import docker_client.generated
from docker_client.generated.models.network import Network
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
    api_instance = docker_client.generated.NetworkApi(api_client)
    filters = 'filters_example' # str | JSON encoded value of the filters (a `map[string][]string`) to process on the networks list.  Available filters:  - `dangling=<boolean>` When set to `true` (or `1`), returns all    networks that are not in use by a container. When set to `false`    (or `0`), only networks that are in use by one or more    containers are returned. - `driver=<driver-name>` Matches a network's driver. - `id=<network-id>` Matches all or part of a network ID. - `label=<key>` or `label=<key>=<value>` of a network label. - `name=<network-name>` Matches all or part of a network name. - `scope=[\"swarm\"|\"global\"|\"local\"]` Filters networks by scope (`swarm`, `global`, or `local`). - `type=[\"custom\"|\"builtin\"]` Filters networks by type. The `custom` keyword returns all user-defined networks.  (optional)

    try:
        # List networks
        api_response = api_instance.network_list(filters=filters)
        print("The response of NetworkApi->network_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling NetworkApi->network_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the networks list.  Available filters:  - &#x60;dangling&#x3D;&lt;boolean&gt;&#x60; When set to &#x60;true&#x60; (or &#x60;1&#x60;), returns all    networks that are not in use by a container. When set to &#x60;false&#x60;    (or &#x60;0&#x60;), only networks that are in use by one or more    containers are returned. - &#x60;driver&#x3D;&lt;driver-name&gt;&#x60; Matches a network&#39;s driver. - &#x60;id&#x3D;&lt;network-id&gt;&#x60; Matches all or part of a network ID. - &#x60;label&#x3D;&lt;key&gt;&#x60; or &#x60;label&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60; of a network label. - &#x60;name&#x3D;&lt;network-name&gt;&#x60; Matches all or part of a network name. - &#x60;scope&#x3D;[\&quot;swarm\&quot;|\&quot;global\&quot;|\&quot;local\&quot;]&#x60; Filters networks by scope (&#x60;swarm&#x60;, &#x60;global&#x60;, or &#x60;local&#x60;). - &#x60;type&#x3D;[\&quot;custom\&quot;|\&quot;builtin\&quot;]&#x60; Filters networks by type. The &#x60;custom&#x60; keyword returns all user-defined networks.  | [optional] 

### Return type

[**List[Network]**](Network.md)

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

# **network_prune**
> NetworkPruneResponse network_prune(filters=filters)

Delete unused networks

### Example


```python
import docker_client.generated
from docker_client.generated.models.network_prune_response import NetworkPruneResponse
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
    api_instance = docker_client.generated.NetworkApi(api_client)
    filters = 'filters_example' # str | Filters to process on the prune list, encoded as JSON (a `map[string][]string`).  Available filters: - `until=<timestamp>` Prune networks created before this timestamp. The `<timestamp>` can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. `10m`, `1h30m`) computed relative to the daemon machine’s time. - `label` (`label=<key>`, `label=<key>=<value>`, `label!=<key>`, or `label!=<key>=<value>`) Prune networks with (or without, in case `label!=...` is used) the specified labels.  (optional)

    try:
        # Delete unused networks
        api_response = api_instance.network_prune(filters=filters)
        print("The response of NetworkApi->network_prune:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling NetworkApi->network_prune: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| Filters to process on the prune list, encoded as JSON (a &#x60;map[string][]string&#x60;).  Available filters: - &#x60;until&#x3D;&lt;timestamp&gt;&#x60; Prune networks created before this timestamp. The &#x60;&lt;timestamp&gt;&#x60; can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. &#x60;10m&#x60;, &#x60;1h30m&#x60;) computed relative to the daemon machine’s time. - &#x60;label&#x60; (&#x60;label&#x3D;&lt;key&gt;&#x60;, &#x60;label&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;, &#x60;label!&#x3D;&lt;key&gt;&#x60;, or &#x60;label!&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;) Prune networks with (or without, in case &#x60;label!&#x3D;...&#x60; is used) the specified labels.  | [optional] 

### Return type

[**NetworkPruneResponse**](NetworkPruneResponse.md)

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

