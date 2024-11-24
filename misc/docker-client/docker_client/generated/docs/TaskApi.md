# docker_client.generated.TaskApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**task_inspect**](TaskApi.md#task_inspect) | **GET** /tasks/{id} | Inspect a task
[**task_list**](TaskApi.md#task_list) | **GET** /tasks | List tasks
[**task_logs**](TaskApi.md#task_logs) | **GET** /tasks/{id}/logs | Get task logs


# **task_inspect**
> Task task_inspect(id)

Inspect a task

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.task import Task
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
    api_instance = docker_client.generated.TaskApi(api_client)
    id = 'id_example' # str | ID of the task

    try:
        # Inspect a task
        api_response = api_instance.task_inspect(id)
        print("The response of TaskApi->task_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TaskApi->task_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID of the task | 

### Return type

[**Task**](Task.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such task |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **task_list**
> List[Task] task_list(filters=filters)

List tasks

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.task import Task
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
    api_instance = docker_client.generated.TaskApi(api_client)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the tasks list.  Available filters:  - `desired-state=(running | shutdown | accepted)` - `id=<task id>` - `label=key` or `label=\"key=value\"` - `name=<task name>` - `node=<node id or name>` - `service=<service name>`  (optional)

    try:
        # List tasks
        api_response = api_instance.task_list(filters=filters)
        print("The response of TaskApi->task_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TaskApi->task_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the tasks list.  Available filters:  - &#x60;desired-state&#x3D;(running | shutdown | accepted)&#x60; - &#x60;id&#x3D;&lt;task id&gt;&#x60; - &#x60;label&#x3D;key&#x60; or &#x60;label&#x3D;\&quot;key&#x3D;value\&quot;&#x60; - &#x60;name&#x3D;&lt;task name&gt;&#x60; - &#x60;node&#x3D;&lt;node id or name&gt;&#x60; - &#x60;service&#x3D;&lt;service name&gt;&#x60;  | [optional] 

### Return type

[**List[Task]**](Task.md)

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

# **task_logs**
> bytearray task_logs(id, details=details, follow=follow, stdout=stdout, stderr=stderr, since=since, timestamps=timestamps, tail=tail)

Get task logs

Get `stdout` and `stderr` logs from a task. See also [`/containers/{id}/logs`](#operation/ContainerLogs).  **Note**: This endpoint works only for services with the `local`, `json-file` or `journald` logging drivers. 

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
    api_instance = docker_client.generated.TaskApi(api_client)
    id = 'id_example' # str | ID of the task
    details = False # bool | Show task context and extra details provided to logs. (optional) (default to False)
    follow = False # bool | Keep connection after returning logs. (optional) (default to False)
    stdout = False # bool | Return logs from `stdout` (optional) (default to False)
    stderr = False # bool | Return logs from `stderr` (optional) (default to False)
    since = 0 # int | Only return logs since this time, as a UNIX timestamp (optional) (default to 0)
    timestamps = False # bool | Add timestamps to every log line (optional) (default to False)
    tail = 'all' # str | Only return this number of log lines from the end of the logs. Specify as an integer or `all` to output all log lines.  (optional) (default to 'all')

    try:
        # Get task logs
        api_response = api_instance.task_logs(id, details=details, follow=follow, stdout=stdout, stderr=stderr, since=since, timestamps=timestamps, tail=tail)
        print("The response of TaskApi->task_logs:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TaskApi->task_logs: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID of the task | 
 **details** | **bool**| Show task context and extra details provided to logs. | [optional] [default to False]
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
**404** | no such task |  -  |
**500** | server error |  -  |
**503** | node is not part of a swarm |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

