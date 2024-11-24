# docker_client.generated.VolumeApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**volume_create**](VolumeApi.md#volume_create) | **POST** /volumes/create | Create a volume
[**volume_delete**](VolumeApi.md#volume_delete) | **DELETE** /volumes/{name} | Remove a volume
[**volume_inspect**](VolumeApi.md#volume_inspect) | **GET** /volumes/{name} | Inspect a volume
[**volume_list**](VolumeApi.md#volume_list) | **GET** /volumes | List volumes
[**volume_prune**](VolumeApi.md#volume_prune) | **POST** /volumes/prune | Delete unused volumes
[**volume_update**](VolumeApi.md#volume_update) | **PUT** /volumes/{name} | \&quot;Update a volume. Valid only for Swarm cluster volumes\&quot; 


# **volume_create**
> Volume volume_create(volume_config)

Create a volume

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.volume import Volume
from docker_client.generated.docker_client.generated.models.volume_create_options import VolumeCreateOptions
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
    api_instance = docker_client.generated.VolumeApi(api_client)
    volume_config = docker_client.generated.VolumeCreateOptions() # VolumeCreateOptions | Volume configuration

    try:
        # Create a volume
        api_response = api_instance.volume_create(volume_config)
        print("The response of VolumeApi->volume_create:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling VolumeApi->volume_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **volume_config** | [**VolumeCreateOptions**](VolumeCreateOptions.md)| Volume configuration | 

### Return type

[**Volume**](Volume.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | The volume was created successfully |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **volume_delete**
> volume_delete(name, force=force)

Remove a volume

Instruct the driver to remove the volume.

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
    api_instance = docker_client.generated.VolumeApi(api_client)
    name = 'name_example' # str | Volume name or ID
    force = False # bool | Force the removal of the volume (optional) (default to False)

    try:
        # Remove a volume
        api_instance.volume_delete(name, force=force)
    except Exception as e:
        print("Exception when calling VolumeApi->volume_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Volume name or ID | 
 **force** | **bool**| Force the removal of the volume | [optional] [default to False]

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
**204** | The volume was removed |  -  |
**404** | No such volume or volume driver |  -  |
**409** | Volume is in use and cannot be removed |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **volume_inspect**
> Volume volume_inspect(name)

Inspect a volume

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.volume import Volume
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
    api_instance = docker_client.generated.VolumeApi(api_client)
    name = 'name_example' # str | Volume name or ID

    try:
        # Inspect a volume
        api_response = api_instance.volume_inspect(name)
        print("The response of VolumeApi->volume_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling VolumeApi->volume_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Volume name or ID | 

### Return type

[**Volume**](Volume.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**404** | No such volume |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **volume_list**
> VolumeListResponse volume_list(filters=filters)

List volumes

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.volume_list_response import VolumeListResponse
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
    api_instance = docker_client.generated.VolumeApi(api_client)
    filters = 'filters_example' # str | JSON encoded value of the filters (a `map[string][]string`) to process on the volumes list. Available filters:  - `dangling=<boolean>` When set to `true` (or `1`), returns all    volumes that are not in use by a container. When set to `false`    (or `0`), only volumes that are in use by one or more    containers are returned. - `driver=<volume-driver-name>` Matches volumes based on their driver. - `label=<key>` or `label=<key>:<value>` Matches volumes based on    the presence of a `label` alone or a `label` and a value. - `name=<volume-name>` Matches all or part of a volume name.  (optional)

    try:
        # List volumes
        api_response = api_instance.volume_list(filters=filters)
        print("The response of VolumeApi->volume_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling VolumeApi->volume_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the volumes list. Available filters:  - &#x60;dangling&#x3D;&lt;boolean&gt;&#x60; When set to &#x60;true&#x60; (or &#x60;1&#x60;), returns all    volumes that are not in use by a container. When set to &#x60;false&#x60;    (or &#x60;0&#x60;), only volumes that are in use by one or more    containers are returned. - &#x60;driver&#x3D;&lt;volume-driver-name&gt;&#x60; Matches volumes based on their driver. - &#x60;label&#x3D;&lt;key&gt;&#x60; or &#x60;label&#x3D;&lt;key&gt;:&lt;value&gt;&#x60; Matches volumes based on    the presence of a &#x60;label&#x60; alone or a &#x60;label&#x60; and a value. - &#x60;name&#x3D;&lt;volume-name&gt;&#x60; Matches all or part of a volume name.  | [optional] 

### Return type

[**VolumeListResponse**](VolumeListResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Summary volume data that matches the query |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **volume_prune**
> VolumePruneResponse volume_prune(filters=filters)

Delete unused volumes

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.volume_prune_response import VolumePruneResponse
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
    api_instance = docker_client.generated.VolumeApi(api_client)
    filters = 'filters_example' # str | Filters to process on the prune list, encoded as JSON (a `map[string][]string`).  Available filters: - `label` (`label=<key>`, `label=<key>=<value>`, `label!=<key>`, or `label!=<key>=<value>`) Prune volumes with (or without, in case `label!=...` is used) the specified labels. - `all` (`all=true`) - Consider all (local) volumes for pruning and not just anonymous volumes.  (optional)

    try:
        # Delete unused volumes
        api_response = api_instance.volume_prune(filters=filters)
        print("The response of VolumeApi->volume_prune:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling VolumeApi->volume_prune: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| Filters to process on the prune list, encoded as JSON (a &#x60;map[string][]string&#x60;).  Available filters: - &#x60;label&#x60; (&#x60;label&#x3D;&lt;key&gt;&#x60;, &#x60;label&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;, &#x60;label!&#x3D;&lt;key&gt;&#x60;, or &#x60;label!&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;) Prune volumes with (or without, in case &#x60;label!&#x3D;...&#x60; is used) the specified labels. - &#x60;all&#x60; (&#x60;all&#x3D;true&#x60;) - Consider all (local) volumes for pruning and not just anonymous volumes.  | [optional] 

### Return type

[**VolumePruneResponse**](VolumePruneResponse.md)

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

# **volume_update**
> volume_update(name, version, body=body)

\"Update a volume. Valid only for Swarm cluster volumes\" 

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.volume_update_request import VolumeUpdateRequest
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
    api_instance = docker_client.generated.VolumeApi(api_client)
    name = 'name_example' # str | The name or ID of the volume
    version = 56 # int | The version number of the volume being updated. This is required to avoid conflicting writes. Found in the volume's `ClusterVolume` field. 
    body = docker_client.generated.VolumeUpdateRequest() # VolumeUpdateRequest | The spec of the volume to update. Currently, only Availability may change. All other fields must remain unchanged.  (optional)

    try:
        # \"Update a volume. Valid only for Swarm cluster volumes\" 
        api_instance.volume_update(name, version, body=body)
    except Exception as e:
        print("Exception when calling VolumeApi->volume_update: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name or ID of the volume | 
 **version** | **int**| The version number of the volume being updated. This is required to avoid conflicting writes. Found in the volume&#39;s &#x60;ClusterVolume&#x60; field.  | 
 **body** | [**VolumeUpdateRequest**](VolumeUpdateRequest.md)| The spec of the volume to update. Currently, only Availability may change. All other fields must remain unchanged.  | [optional] 

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
**400** | bad parameter |  -  |
**404** | no such volume |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

