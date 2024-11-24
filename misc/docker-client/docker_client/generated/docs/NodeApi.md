# docker_client.generated.NodeApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**node_delete**](NodeApi.md#node_delete) | **DELETE** /nodes/{id} | Delete a node
[**node_inspect**](NodeApi.md#node_inspect) | **GET** /nodes/{id} | Inspect a node
[**node_list**](NodeApi.md#node_list) | **GET** /nodes | List nodes
[**node_update**](NodeApi.md#node_update) | **POST** /nodes/{id}/update | Update a node


# **node_delete**
> node_delete(id, force=force)

Delete a node

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
    api_instance = docker_client.generated.NodeApi(api_client)
    id = 'id_example' # str | The ID or name of the node
    force = False # bool | Force remove a node from the swarm (optional) (default to False)

    try:
        # Delete a node
        api_instance.node_delete(id, force=force)
    except Exception as e:
        print("Exception when calling NodeApi->node_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| The ID or name of the node | 
 **force** | **bool**| Force remove a node from the swarm | [optional] [default to False]

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
**404** | no such node |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **node_inspect**
> Node node_inspect(id)

Inspect a node

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.node import Node
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
    api_instance = docker_client.generated.NodeApi(api_client)
    id = 'id_example' # str | The ID or name of the node

    try:
        # Inspect a node
        api_response = api_instance.node_inspect(id)
        print("The response of NodeApi->node_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling NodeApi->node_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| The ID or name of the node | 

### Return type

[**Node**](Node.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such node |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **node_list**
> List[Node] node_list(filters=filters)

List nodes

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.node import Node
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
    api_instance = docker_client.generated.NodeApi(api_client)
    filters = 'filters_example' # str | Filters to process on the nodes list, encoded as JSON (a `map[string][]string`).  Available filters: - `id=<node id>` - `label=<engine label>` - `membership=`(`accepted`|`pending`)` - `name=<node name>` - `node.label=<node label>` - `role=`(`manager`|`worker`)`  (optional)

    try:
        # List nodes
        api_response = api_instance.node_list(filters=filters)
        print("The response of NodeApi->node_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling NodeApi->node_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| Filters to process on the nodes list, encoded as JSON (a &#x60;map[string][]string&#x60;).  Available filters: - &#x60;id&#x3D;&lt;node id&gt;&#x60; - &#x60;label&#x3D;&lt;engine label&gt;&#x60; - &#x60;membership&#x3D;&#x60;(&#x60;accepted&#x60;|&#x60;pending&#x60;)&#x60; - &#x60;name&#x3D;&lt;node name&gt;&#x60; - &#x60;node.label&#x3D;&lt;node label&gt;&#x60; - &#x60;role&#x3D;&#x60;(&#x60;manager&#x60;|&#x60;worker&#x60;)&#x60;  | [optional] 

### Return type

[**List[Node]**](Node.md)

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

# **node_update**
> node_update(id, version, body=body)

Update a node

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.node_spec import NodeSpec
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
    api_instance = docker_client.generated.NodeApi(api_client)
    id = 'id_example' # str | The ID of the node
    version = 56 # int | The version number of the node object being updated. This is required to avoid conflicting writes. 
    body = docker_client.generated.NodeSpec() # NodeSpec |  (optional)

    try:
        # Update a node
        api_instance.node_update(id, version, body=body)
    except Exception as e:
        print("Exception when calling NodeApi->node_update: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| The ID of the node | 
 **version** | **int**| The version number of the node object being updated. This is required to avoid conflicting writes.  | 
 **body** | [**NodeSpec**](NodeSpec.md)|  | [optional] 

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
**404** | no such node |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

