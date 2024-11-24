# docker_client.generated.PluginApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**get_plugin_privileges**](PluginApi.md#get_plugin_privileges) | **GET** /plugins/privileges | Get plugin privileges
[**plugin_create**](PluginApi.md#plugin_create) | **POST** /plugins/create | Create a plugin
[**plugin_delete**](PluginApi.md#plugin_delete) | **DELETE** /plugins/{name} | Remove a plugin
[**plugin_disable**](PluginApi.md#plugin_disable) | **POST** /plugins/{name}/disable | Disable a plugin
[**plugin_enable**](PluginApi.md#plugin_enable) | **POST** /plugins/{name}/enable | Enable a plugin
[**plugin_inspect**](PluginApi.md#plugin_inspect) | **GET** /plugins/{name}/json | Inspect a plugin
[**plugin_list**](PluginApi.md#plugin_list) | **GET** /plugins | List plugins
[**plugin_pull**](PluginApi.md#plugin_pull) | **POST** /plugins/pull | Install a plugin
[**plugin_push**](PluginApi.md#plugin_push) | **POST** /plugins/{name}/push | Push a plugin
[**plugin_set**](PluginApi.md#plugin_set) | **POST** /plugins/{name}/set | Configure a plugin
[**plugin_upgrade**](PluginApi.md#plugin_upgrade) | **POST** /plugins/{name}/upgrade | Upgrade a plugin


# **get_plugin_privileges**
> List[PluginPrivilege] get_plugin_privileges(remote)

Get plugin privileges

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.plugin_privilege import PluginPrivilege
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
    api_instance = docker_client.generated.PluginApi(api_client)
    remote = 'remote_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 

    try:
        # Get plugin privileges
        api_response = api_instance.get_plugin_privileges(remote)
        print("The response of PluginApi->get_plugin_privileges:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling PluginApi->get_plugin_privileges: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **remote** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 

### Return type

[**List[PluginPrivilege]**](PluginPrivilege.md)

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

# **plugin_create**
> plugin_create(name, tar_context=tar_context)

Create a plugin

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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 
    tar_context = None # bytearray | Path to tar containing plugin rootfs and manifest (optional)

    try:
        # Create a plugin
        api_instance.plugin_create(name, tar_context=tar_context)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 
 **tar_context** | **bytearray**| Path to tar containing plugin rootfs and manifest | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/x-tar
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**204** | no error |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_delete**
> Plugin plugin_delete(name, force=force)

Remove a plugin

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.plugin import Plugin
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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 
    force = False # bool | Disable the plugin before removing. This may result in issues if the plugin is in use by a container.  (optional) (default to False)

    try:
        # Remove a plugin
        api_response = api_instance.plugin_delete(name, force=force)
        print("The response of PluginApi->plugin_delete:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 
 **force** | **bool**| Disable the plugin before removing. This may result in issues if the plugin is in use by a container.  | [optional] [default to False]

### Return type

[**Plugin**](Plugin.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | plugin is not installed |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_disable**
> plugin_disable(name, force=force)

Disable a plugin

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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 
    force = True # bool | Force disable a plugin even if still in use.  (optional)

    try:
        # Disable a plugin
        api_instance.plugin_disable(name, force=force)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_disable: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 
 **force** | **bool**| Force disable a plugin even if still in use.  | [optional] 

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
**404** | plugin is not installed |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_enable**
> plugin_enable(name, timeout=timeout)

Enable a plugin

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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 
    timeout = 0 # int | Set the HTTP client timeout (in seconds) (optional) (default to 0)

    try:
        # Enable a plugin
        api_instance.plugin_enable(name, timeout=timeout)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_enable: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 
 **timeout** | **int**| Set the HTTP client timeout (in seconds) | [optional] [default to 0]

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
**404** | plugin is not installed |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_inspect**
> Plugin plugin_inspect(name)

Inspect a plugin

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.plugin import Plugin
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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 

    try:
        # Inspect a plugin
        api_response = api_instance.plugin_inspect(name)
        print("The response of PluginApi->plugin_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 

### Return type

[**Plugin**](Plugin.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | plugin is not installed |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_list**
> List[Plugin] plugin_list(filters=filters)

List plugins

Returns information about installed plugins.

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.plugin import Plugin
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
    api_instance = docker_client.generated.PluginApi(api_client)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the plugin list.  Available filters:  - `capability=<capability name>` - `enable=<true>|<false>`  (optional)

    try:
        # List plugins
        api_response = api_instance.plugin_list(filters=filters)
        print("The response of PluginApi->plugin_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the plugin list.  Available filters:  - &#x60;capability&#x3D;&lt;capability name&gt;&#x60; - &#x60;enable&#x3D;&lt;true&gt;|&lt;false&gt;&#x60;  | [optional] 

### Return type

[**List[Plugin]**](Plugin.md)

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

# **plugin_pull**
> plugin_pull(remote, name=name, x_registry_auth=x_registry_auth, body=body)

Install a plugin

Pulls and installs a plugin. After the plugin is installed, it can be enabled using the [`POST /plugins/{name}/enable` endpoint](#operation/PostPluginsEnable). 

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.plugin_privilege import PluginPrivilege
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
    api_instance = docker_client.generated.PluginApi(api_client)
    remote = 'remote_example' # str | Remote reference for plugin to install.  The `:latest` tag is optional, and is used as the default if omitted. 
    name = 'name_example' # str | Local name for the pulled plugin.  The `:latest` tag is optional, and is used as the default if omitted.  (optional)
    x_registry_auth = 'x_registry_auth_example' # str | A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  (optional)
    body = [docker_client.generated.PluginPrivilege()] # List[PluginPrivilege] |  (optional)

    try:
        # Install a plugin
        api_instance.plugin_pull(remote, name=name, x_registry_auth=x_registry_auth, body=body)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_pull: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **remote** | **str**| Remote reference for plugin to install.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.  | 
 **name** | **str**| Local name for the pulled plugin.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.  | [optional] 
 **x_registry_auth** | **str**| A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  | [optional] 
 **body** | [**List[PluginPrivilege]**](PluginPrivilege.md)|  | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, text/plain
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**204** | no error |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_push**
> plugin_push(name)

Push a plugin

Push a plugin to the registry. 

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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 

    try:
        # Push a plugin
        api_instance.plugin_push(name)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_push: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 

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
**404** | plugin not installed |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_set**
> plugin_set(name, body=body)

Configure a plugin

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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 
    body = ['body_example'] # List[str] |  (optional)

    try:
        # Configure a plugin
        api_instance.plugin_set(name, body=body)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_set: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 
 **body** | [**List[str]**](str.md)|  | [optional] 

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
**204** | No error |  -  |
**404** | Plugin not installed |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **plugin_upgrade**
> plugin_upgrade(name, remote, x_registry_auth=x_registry_auth, body=body)

Upgrade a plugin

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.plugin_privilege import PluginPrivilege
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
    api_instance = docker_client.generated.PluginApi(api_client)
    name = 'name_example' # str | The name of the plugin. The `:latest` tag is optional, and is the default if omitted. 
    remote = 'remote_example' # str | Remote reference to upgrade to.  The `:latest` tag is optional, and is used as the default if omitted. 
    x_registry_auth = 'x_registry_auth_example' # str | A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  (optional)
    body = [docker_client.generated.PluginPrivilege()] # List[PluginPrivilege] |  (optional)

    try:
        # Upgrade a plugin
        api_instance.plugin_upgrade(name, remote, x_registry_auth=x_registry_auth, body=body)
    except Exception as e:
        print("Exception when calling PluginApi->plugin_upgrade: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.  | 
 **remote** | **str**| Remote reference to upgrade to.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.  | 
 **x_registry_auth** | **str**| A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  | [optional] 
 **body** | [**List[PluginPrivilege]**](PluginPrivilege.md)|  | [optional] 

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
**204** | no error |  -  |
**404** | plugin not installed |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

