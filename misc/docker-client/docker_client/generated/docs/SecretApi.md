# docker_client.generated.SecretApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**secret_create**](SecretApi.md#secret_create) | **POST** /secrets/create | Create a secret
[**secret_delete**](SecretApi.md#secret_delete) | **DELETE** /secrets/{id} | Delete a secret
[**secret_inspect**](SecretApi.md#secret_inspect) | **GET** /secrets/{id} | Inspect a secret
[**secret_list**](SecretApi.md#secret_list) | **GET** /secrets | List secrets
[**secret_update**](SecretApi.md#secret_update) | **POST** /secrets/{id}/update | Update a Secret


# **secret_create**
> IdResponse secret_create(body=body)

Create a secret

### Example


```python
import docker_client.generated
from docker_client.generated.models.id_response import IdResponse
from docker_client.generated.models.secret_create_request import SecretCreateRequest
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
    api_instance = docker_client.generated.SecretApi(api_client)
    body = docker_client.generated.SecretCreateRequest() # SecretCreateRequest |  (optional)

    try:
        # Create a secret
        api_response = api_instance.secret_create(body=body)
        print("The response of SecretApi->secret_create:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SecretApi->secret_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SecretCreateRequest**](SecretCreateRequest.md)|  | [optional] 

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

# **secret_delete**
> secret_delete(id)

Delete a secret

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
    api_instance = docker_client.generated.SecretApi(api_client)
    id = 'id_example' # str | ID of the secret

    try:
        # Delete a secret
        api_instance.secret_delete(id)
    except Exception as e:
        print("Exception when calling SecretApi->secret_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID of the secret | 

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
**404** | secret not found |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **secret_inspect**
> Secret secret_inspect(id)

Inspect a secret

### Example


```python
import docker_client.generated
from docker_client.generated.models.secret import Secret
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
    api_instance = docker_client.generated.SecretApi(api_client)
    id = 'id_example' # str | ID of the secret

    try:
        # Inspect a secret
        api_response = api_instance.secret_inspect(id)
        print("The response of SecretApi->secret_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SecretApi->secret_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID of the secret | 

### Return type

[**Secret**](Secret.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | secret not found |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **secret_list**
> List[Secret] secret_list(filters=filters)

List secrets

### Example


```python
import docker_client.generated
from docker_client.generated.models.secret import Secret
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
    api_instance = docker_client.generated.SecretApi(api_client)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the secrets list.  Available filters:  - `id=<secret id>` - `label=<key> or label=<key>=value` - `name=<secret name>` - `names=<secret name>`  (optional)

    try:
        # List secrets
        api_response = api_instance.secret_list(filters=filters)
        print("The response of SecretApi->secret_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling SecretApi->secret_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the secrets list.  Available filters:  - &#x60;id&#x3D;&lt;secret id&gt;&#x60; - &#x60;label&#x3D;&lt;key&gt; or label&#x3D;&lt;key&gt;&#x3D;value&#x60; - &#x60;name&#x3D;&lt;secret name&gt;&#x60; - &#x60;names&#x3D;&lt;secret name&gt;&#x60;  | [optional] 

### Return type

[**List[Secret]**](Secret.md)

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

# **secret_update**
> secret_update(id, version, body=body)

Update a Secret

### Example


```python
import docker_client.generated
from docker_client.generated.models.secret_spec import SecretSpec
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
    api_instance = docker_client.generated.SecretApi(api_client)
    id = 'id_example' # str | The ID or name of the secret
    version = 56 # int | The version number of the secret object being updated. This is required to avoid conflicting writes. 
    body = docker_client.generated.SecretSpec() # SecretSpec | The spec of the secret to update. Currently, only the Labels field can be updated. All other fields must remain unchanged from the [SecretInspect endpoint](#operation/SecretInspect) response values.  (optional)

    try:
        # Update a Secret
        api_instance.secret_update(id, version, body=body)
    except Exception as e:
        print("Exception when calling SecretApi->secret_update: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| The ID or name of the secret | 
 **version** | **int**| The version number of the secret object being updated. This is required to avoid conflicting writes.  | 
 **body** | [**SecretSpec**](SecretSpec.md)| The spec of the secret to update. Currently, only the Labels field can be updated. All other fields must remain unchanged from the [SecretInspect endpoint](#operation/SecretInspect) response values.  | [optional] 

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
**404** | no such secret |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

