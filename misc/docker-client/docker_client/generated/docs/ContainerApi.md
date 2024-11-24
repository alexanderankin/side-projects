# docker_client.generated.ContainerApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**container_archive**](ContainerApi.md#container_archive) | **GET** /containers/{id}/archive | Get an archive of a filesystem resource in a container
[**container_archive_info**](ContainerApi.md#container_archive_info) | **HEAD** /containers/{id}/archive | Get information about files in a container
[**container_attach**](ContainerApi.md#container_attach) | **POST** /containers/{id}/attach | Attach to a container
[**container_attach_websocket**](ContainerApi.md#container_attach_websocket) | **GET** /containers/{id}/attach/ws | Attach to a container via a websocket
[**container_changes**](ContainerApi.md#container_changes) | **GET** /containers/{id}/changes | Get changes on a container’s filesystem
[**container_create**](ContainerApi.md#container_create) | **POST** /containers/create | Create a container
[**container_delete**](ContainerApi.md#container_delete) | **DELETE** /containers/{id} | Remove a container
[**container_export**](ContainerApi.md#container_export) | **GET** /containers/{id}/export | Export a container
[**container_inspect**](ContainerApi.md#container_inspect) | **GET** /containers/{id}/json | Inspect a container
[**container_kill**](ContainerApi.md#container_kill) | **POST** /containers/{id}/kill | Kill a container
[**container_list**](ContainerApi.md#container_list) | **GET** /containers/json | List containers
[**container_logs**](ContainerApi.md#container_logs) | **GET** /containers/{id}/logs | Get container logs
[**container_pause**](ContainerApi.md#container_pause) | **POST** /containers/{id}/pause | Pause a container
[**container_prune**](ContainerApi.md#container_prune) | **POST** /containers/prune | Delete stopped containers
[**container_rename**](ContainerApi.md#container_rename) | **POST** /containers/{id}/rename | Rename a container
[**container_resize**](ContainerApi.md#container_resize) | **POST** /containers/{id}/resize | Resize a container TTY
[**container_restart**](ContainerApi.md#container_restart) | **POST** /containers/{id}/restart | Restart a container
[**container_start**](ContainerApi.md#container_start) | **POST** /containers/{id}/start | Start a container
[**container_stats**](ContainerApi.md#container_stats) | **GET** /containers/{id}/stats | Get container stats based on resource usage
[**container_stop**](ContainerApi.md#container_stop) | **POST** /containers/{id}/stop | Stop a container
[**container_top**](ContainerApi.md#container_top) | **GET** /containers/{id}/top | List processes running inside a container
[**container_unpause**](ContainerApi.md#container_unpause) | **POST** /containers/{id}/unpause | Unpause a container
[**container_update**](ContainerApi.md#container_update) | **POST** /containers/{id}/update | Update a container
[**container_wait**](ContainerApi.md#container_wait) | **POST** /containers/{id}/wait | Wait for a container
[**put_container_archive**](ContainerApi.md#put_container_archive) | **PUT** /containers/{id}/archive | Extract an archive of files or folders to a directory in a container


# **container_archive**
> container_archive(id, path)

Get an archive of a filesystem resource in a container

Get a tar archive of a resource in the filesystem of container id.

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    path = 'path_example' # str | Resource in the container’s filesystem to archive.

    try:
        # Get an archive of a filesystem resource in a container
        api_instance.container_archive(id, path)
    except Exception as e:
        print("Exception when calling ContainerApi->container_archive: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **path** | **str**| Resource in the container’s filesystem to archive. | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-tar, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**400** | Bad parameter |  -  |
**404** | Container or path does not exist |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_archive_info**
> container_archive_info(id, path)

Get information about files in a container

A response header `X-Docker-Container-Path-Stat` is returned, containing a base64 - encoded JSON object with some filesystem header information about the path. 

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    path = 'path_example' # str | Resource in the container’s filesystem to archive.

    try:
        # Get information about files in a container
        api_instance.container_archive_info(id, path)
    except Exception as e:
        print("Exception when calling ContainerApi->container_archive_info: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **path** | **str**| Resource in the container’s filesystem to archive. | 

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
**200** | no error |  * X-Docker-Container-Path-Stat - A base64 - encoded JSON object with some filesystem header information about the path  <br>  |
**400** | Bad parameter |  -  |
**404** | Container or path does not exist |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_attach**
> container_attach(id, detach_keys=detach_keys, logs=logs, stream=stream, stdin=stdin, stdout=stdout, stderr=stderr)

Attach to a container

Attach to a container to read its output or send it input. You can attach to the same container multiple times and you can reattach to containers that have been detached.  Either the `stream` or `logs` parameter must be `true` for this endpoint to do anything.  See the [documentation for the `docker attach` command](https://docs.docker.com/engine/reference/commandline/attach/) for more details.  ### Hijacking  This endpoint hijacks the HTTP connection to transport `stdin`, `stdout`, and `stderr` on the same socket.  This is the response from the daemon for an attach request:  ``` HTTP/1.1 200 OK Content-Type: application/vnd.docker.raw-stream  [STREAM] ```  After the headers and two new lines, the TCP connection can now be used for raw, bidirectional communication between the client and server.  To hint potential proxies about connection hijacking, the Docker client can also optionally send connection upgrade headers.  For example, the client sends this request to upgrade the connection:  ``` POST /containers/16253994b7c4/attach?stream=1&stdout=1 HTTP/1.1 Upgrade: tcp Connection: Upgrade ```  The Docker daemon will respond with a `101 UPGRADED` response, and will similarly follow with the raw stream:  ``` HTTP/1.1 101 UPGRADED Content-Type: application/vnd.docker.raw-stream Connection: Upgrade Upgrade: tcp  [STREAM] ```  ### Stream format  When the TTY setting is disabled in [`POST /containers/create`](#operation/ContainerCreate), the HTTP Content-Type header is set to application/vnd.docker.multiplexed-stream and the stream over the hijacked connected is multiplexed to separate out `stdout` and `stderr`. The stream consists of a series of frames, each containing a header and a payload.  The header contains the information which the stream writes (`stdout` or `stderr`). It also contains the size of the associated frame encoded in the last four bytes (`uint32`).  It is encoded on the first eight bytes like this:  ```go header := [8]byte{STREAM_TYPE, 0, 0, 0, SIZE1, SIZE2, SIZE3, SIZE4} ```  `STREAM_TYPE` can be:  - 0: `stdin` (is written on `stdout`) - 1: `stdout` - 2: `stderr`  `SIZE1, SIZE2, SIZE3, SIZE4` are the four bytes of the `uint32` size encoded as big endian.  Following the header is the payload, which is the specified number of bytes of `STREAM_TYPE`.  The simplest way to implement this protocol is the following:  1. Read 8 bytes. 2. Choose `stdout` or `stderr` depending on the first byte. 3. Extract the frame size from the last four bytes. 4. Read the extracted size and output it on the correct output. 5. Goto 1.  ### Stream format when using a TTY  When the TTY setting is enabled in [`POST /containers/create`](#operation/ContainerCreate), the stream is not multiplexed. The data exchanged over the hijacked connection is simply the raw data from the process PTY and client's `stdin`. 

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    detach_keys = 'detach_keys_example' # str | Override the key sequence for detaching a container.Format is a single character `[a-Z]` or `ctrl-<value>` where `<value>` is one of: `a-z`, `@`, `^`, `[`, `,` or `_`.  (optional)
    logs = False # bool | Replay previous logs from the container.  This is useful for attaching to a container that has started and you want to output everything since the container started.  If `stream` is also enabled, once all the previous output has been returned, it will seamlessly transition into streaming current output.  (optional) (default to False)
    stream = False # bool | Stream attached streams from the time the request was made onwards.  (optional) (default to False)
    stdin = False # bool | Attach to `stdin` (optional) (default to False)
    stdout = False # bool | Attach to `stdout` (optional) (default to False)
    stderr = False # bool | Attach to `stderr` (optional) (default to False)

    try:
        # Attach to a container
        api_instance.container_attach(id, detach_keys=detach_keys, logs=logs, stream=stream, stdin=stdin, stdout=stdout, stderr=stderr)
    except Exception as e:
        print("Exception when calling ContainerApi->container_attach: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **detach_keys** | **str**| Override the key sequence for detaching a container.Format is a single character &#x60;[a-Z]&#x60; or &#x60;ctrl-&lt;value&gt;&#x60; where &#x60;&lt;value&gt;&#x60; is one of: &#x60;a-z&#x60;, &#x60;@&#x60;, &#x60;^&#x60;, &#x60;[&#x60;, &#x60;,&#x60; or &#x60;_&#x60;.  | [optional] 
 **logs** | **bool**| Replay previous logs from the container.  This is useful for attaching to a container that has started and you want to output everything since the container started.  If &#x60;stream&#x60; is also enabled, once all the previous output has been returned, it will seamlessly transition into streaming current output.  | [optional] [default to False]
 **stream** | **bool**| Stream attached streams from the time the request was made onwards.  | [optional] [default to False]
 **stdin** | **bool**| Attach to &#x60;stdin&#x60; | [optional] [default to False]
 **stdout** | **bool**| Attach to &#x60;stdout&#x60; | [optional] [default to False]
 **stderr** | **bool**| Attach to &#x60;stderr&#x60; | [optional] [default to False]

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/vnd.docker.raw-stream, application/vnd.docker.multiplexed-stream, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**101** | no error, hints proxy about hijacking |  -  |
**200** | no error, no upgrade header found |  -  |
**400** | bad parameter |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_attach_websocket**
> container_attach_websocket(id, detach_keys=detach_keys, logs=logs, stream=stream, stdin=stdin, stdout=stdout, stderr=stderr)

Attach to a container via a websocket

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    detach_keys = 'detach_keys_example' # str | Override the key sequence for detaching a container.Format is a single character `[a-Z]` or `ctrl-<value>` where `<value>` is one of: `a-z`, `@`, `^`, `[`, `,`, or `_`.  (optional)
    logs = False # bool | Return logs (optional) (default to False)
    stream = False # bool | Return stream (optional) (default to False)
    stdin = False # bool | Attach to `stdin` (optional) (default to False)
    stdout = False # bool | Attach to `stdout` (optional) (default to False)
    stderr = False # bool | Attach to `stderr` (optional) (default to False)

    try:
        # Attach to a container via a websocket
        api_instance.container_attach_websocket(id, detach_keys=detach_keys, logs=logs, stream=stream, stdin=stdin, stdout=stdout, stderr=stderr)
    except Exception as e:
        print("Exception when calling ContainerApi->container_attach_websocket: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **detach_keys** | **str**| Override the key sequence for detaching a container.Format is a single character &#x60;[a-Z]&#x60; or &#x60;ctrl-&lt;value&gt;&#x60; where &#x60;&lt;value&gt;&#x60; is one of: &#x60;a-z&#x60;, &#x60;@&#x60;, &#x60;^&#x60;, &#x60;[&#x60;, &#x60;,&#x60;, or &#x60;_&#x60;.  | [optional] 
 **logs** | **bool**| Return logs | [optional] [default to False]
 **stream** | **bool**| Return stream | [optional] [default to False]
 **stdin** | **bool**| Attach to &#x60;stdin&#x60; | [optional] [default to False]
 **stdout** | **bool**| Attach to &#x60;stdout&#x60; | [optional] [default to False]
 **stderr** | **bool**| Attach to &#x60;stderr&#x60; | [optional] [default to False]

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
**101** | no error, hints proxy about hijacking |  -  |
**200** | no error, no upgrade header found |  -  |
**400** | bad parameter |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_changes**
> List[FilesystemChange] container_changes(id)

Get changes on a container’s filesystem

Returns which files in a container's filesystem have been added, deleted, or modified. The `Kind` of modification can be one of:  - `0`: Modified (\"C\") - `1`: Added (\"A\") - `2`: Deleted (\"D\") 

### Example


```python
import docker_client.generated
from docker_client.generated.models.filesystem_change import FilesystemChange
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container

    try:
        # Get changes on a container’s filesystem
        api_response = api_instance.container_changes(id)
        print("The response of ContainerApi->container_changes:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_changes: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 

### Return type

[**List[FilesystemChange]**](FilesystemChange.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The list of changes |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_create**
> ContainerCreateResponse container_create(body, name=name, platform=platform)

Create a container

### Example


```python
import docker_client.generated
from docker_client.generated.models.container_create_request import ContainerCreateRequest
from docker_client.generated.models.container_create_response import ContainerCreateResponse
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    body = docker_client.generated.ContainerCreateRequest() # ContainerCreateRequest | Container to create
    name = 'name_example' # str | Assign the specified name to the container. Must match `/?[a-zA-Z0-9][a-zA-Z0-9_.-]+`.  (optional)
    platform = 'platform_example' # str | Platform in the format `os[/arch[/variant]]` used for image lookup.  When specified, the daemon checks if the requested image is present in the local image cache with the given OS and Architecture, and otherwise returns a `404` status.  If the option is not set, the host's native OS and Architecture are used to look up the image in the image cache. However, if no platform is passed and the given image does exist in the local image cache, but its OS or architecture does not match, the container is created with the available image, and a warning is added to the `Warnings` field in the response, for example;      WARNING: The requested image's platform (linux/arm64/v8) does not              match the detected host platform (linux/amd64) and no              specific platform was requested  (optional)

    try:
        # Create a container
        api_response = api_instance.container_create(body, name=name, platform=platform)
        print("The response of ContainerApi->container_create:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ContainerCreateRequest**](ContainerCreateRequest.md)| Container to create | 
 **name** | **str**| Assign the specified name to the container. Must match &#x60;/?[a-zA-Z0-9][a-zA-Z0-9_.-]+&#x60;.  | [optional] 
 **platform** | **str**| Platform in the format &#x60;os[/arch[/variant]]&#x60; used for image lookup.  When specified, the daemon checks if the requested image is present in the local image cache with the given OS and Architecture, and otherwise returns a &#x60;404&#x60; status.  If the option is not set, the host&#39;s native OS and Architecture are used to look up the image in the image cache. However, if no platform is passed and the given image does exist in the local image cache, but its OS or architecture does not match, the container is created with the available image, and a warning is added to the &#x60;Warnings&#x60; field in the response, for example;      WARNING: The requested image&#39;s platform (linux/arm64/v8) does not              match the detected host platform (linux/amd64) and no              specific platform was requested  | [optional] 

### Return type

[**ContainerCreateResponse**](ContainerCreateResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/octet-stream
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | Container created successfully |  -  |
**400** | bad parameter |  -  |
**404** | no such image |  -  |
**409** | conflict |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_delete**
> container_delete(id, v=v, force=force, link=link)

Remove a container

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    v = False # bool | Remove anonymous volumes associated with the container. (optional) (default to False)
    force = False # bool | If the container is running, kill it before removing it. (optional) (default to False)
    link = False # bool | Remove the specified link associated with the container. (optional) (default to False)

    try:
        # Remove a container
        api_instance.container_delete(id, v=v, force=force, link=link)
    except Exception as e:
        print("Exception when calling ContainerApi->container_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **v** | **bool**| Remove anonymous volumes associated with the container. | [optional] [default to False]
 **force** | **bool**| If the container is running, kill it before removing it. | [optional] [default to False]
 **link** | **bool**| Remove the specified link associated with the container. | [optional] [default to False]

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
**204** | no error |  -  |
**400** | bad parameter |  -  |
**404** | no such container |  -  |
**409** | conflict |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_export**
> container_export(id)

Export a container

Export the contents of a container as a tarball.

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container

    try:
        # Export a container
        api_instance.container_export(id)
    except Exception as e:
        print("Exception when calling ContainerApi->container_export: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_inspect**
> ContainerInspectResponse container_inspect(id, size=size)

Inspect a container

Return low-level information about a container.

### Example


```python
import docker_client.generated
from docker_client.generated.models.container_inspect_response import ContainerInspectResponse
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    size = False # bool | Return the size of container as fields `SizeRw` and `SizeRootFs` (optional) (default to False)

    try:
        # Inspect a container
        api_response = api_instance.container_inspect(id, size=size)
        print("The response of ContainerApi->container_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **size** | **bool**| Return the size of container as fields &#x60;SizeRw&#x60; and &#x60;SizeRootFs&#x60; | [optional] [default to False]

### Return type

[**ContainerInspectResponse**](ContainerInspectResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_kill**
> container_kill(id, signal=signal)

Kill a container

Send a POSIX signal to a container, defaulting to killing to the container. 

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    signal = 'SIGKILL' # str | Signal to send to the container as an integer or string (e.g. `SIGINT`).  (optional) (default to 'SIGKILL')

    try:
        # Kill a container
        api_instance.container_kill(id, signal=signal)
    except Exception as e:
        print("Exception when calling ContainerApi->container_kill: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **signal** | **str**| Signal to send to the container as an integer or string (e.g. &#x60;SIGINT&#x60;).  | [optional] [default to &#39;SIGKILL&#39;]

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
**204** | no error |  -  |
**404** | no such container |  -  |
**409** | container is not running |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_list**
> List[ContainerSummary] container_list(all=all, limit=limit, size=size, filters=filters)

List containers

Returns a list of containers. For details on the format, see the [inspect endpoint](#operation/ContainerInspect).  Note that it uses a different, smaller representation of a container than inspecting a single container. For example, the list of linked containers is not propagated . 

### Example


```python
import docker_client.generated
from docker_client.generated.models.container_summary import ContainerSummary
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    all = False # bool | Return all containers. By default, only running containers are shown.  (optional) (default to False)
    limit = 56 # int | Return this number of most recently created containers, including non-running ones.  (optional)
    size = False # bool | Return the size of container as fields `SizeRw` and `SizeRootFs`.  (optional) (default to False)
    filters = 'filters_example' # str | Filters to process on the container list, encoded as JSON (a `map[string][]string`). For example, `{\"status\": [\"paused\"]}` will only return paused containers.  Available filters:  - `ancestor`=(`<image-name>[:<tag>]`, `<image id>`, or `<image@digest>`) - `before`=(`<container id>` or `<container name>`) - `expose`=(`<port>[/<proto>]`|`<startport-endport>/[<proto>]`) - `exited=<int>` containers with exit code of `<int>` - `health`=(`starting`|`healthy`|`unhealthy`|`none`) - `id=<ID>` a container's ID - `isolation=`(`default`|`process`|`hyperv`) (Windows daemon only) - `is-task=`(`true`|`false`) - `label=key` or `label=\"key=value\"` of a container label - `name=<name>` a container's name - `network`=(`<network id>` or `<network name>`) - `publish`=(`<port>[/<proto>]`|`<startport-endport>/[<proto>]`) - `since`=(`<container id>` or `<container name>`) - `status=`(`created`|`restarting`|`running`|`removing`|`paused`|`exited`|`dead`) - `volume`=(`<volume name>` or `<mount point destination>`)  (optional)

    try:
        # List containers
        api_response = api_instance.container_list(all=all, limit=limit, size=size, filters=filters)
        print("The response of ContainerApi->container_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **all** | **bool**| Return all containers. By default, only running containers are shown.  | [optional] [default to False]
 **limit** | **int**| Return this number of most recently created containers, including non-running ones.  | [optional] 
 **size** | **bool**| Return the size of container as fields &#x60;SizeRw&#x60; and &#x60;SizeRootFs&#x60;.  | [optional] [default to False]
 **filters** | **str**| Filters to process on the container list, encoded as JSON (a &#x60;map[string][]string&#x60;). For example, &#x60;{\&quot;status\&quot;: [\&quot;paused\&quot;]}&#x60; will only return paused containers.  Available filters:  - &#x60;ancestor&#x60;&#x3D;(&#x60;&lt;image-name&gt;[:&lt;tag&gt;]&#x60;, &#x60;&lt;image id&gt;&#x60;, or &#x60;&lt;image@digest&gt;&#x60;) - &#x60;before&#x60;&#x3D;(&#x60;&lt;container id&gt;&#x60; or &#x60;&lt;container name&gt;&#x60;) - &#x60;expose&#x60;&#x3D;(&#x60;&lt;port&gt;[/&lt;proto&gt;]&#x60;|&#x60;&lt;startport-endport&gt;/[&lt;proto&gt;]&#x60;) - &#x60;exited&#x3D;&lt;int&gt;&#x60; containers with exit code of &#x60;&lt;int&gt;&#x60; - &#x60;health&#x60;&#x3D;(&#x60;starting&#x60;|&#x60;healthy&#x60;|&#x60;unhealthy&#x60;|&#x60;none&#x60;) - &#x60;id&#x3D;&lt;ID&gt;&#x60; a container&#39;s ID - &#x60;isolation&#x3D;&#x60;(&#x60;default&#x60;|&#x60;process&#x60;|&#x60;hyperv&#x60;) (Windows daemon only) - &#x60;is-task&#x3D;&#x60;(&#x60;true&#x60;|&#x60;false&#x60;) - &#x60;label&#x3D;key&#x60; or &#x60;label&#x3D;\&quot;key&#x3D;value\&quot;&#x60; of a container label - &#x60;name&#x3D;&lt;name&gt;&#x60; a container&#39;s name - &#x60;network&#x60;&#x3D;(&#x60;&lt;network id&gt;&#x60; or &#x60;&lt;network name&gt;&#x60;) - &#x60;publish&#x60;&#x3D;(&#x60;&lt;port&gt;[/&lt;proto&gt;]&#x60;|&#x60;&lt;startport-endport&gt;/[&lt;proto&gt;]&#x60;) - &#x60;since&#x60;&#x3D;(&#x60;&lt;container id&gt;&#x60; or &#x60;&lt;container name&gt;&#x60;) - &#x60;status&#x3D;&#x60;(&#x60;created&#x60;|&#x60;restarting&#x60;|&#x60;running&#x60;|&#x60;removing&#x60;|&#x60;paused&#x60;|&#x60;exited&#x60;|&#x60;dead&#x60;) - &#x60;volume&#x60;&#x3D;(&#x60;&lt;volume name&gt;&#x60; or &#x60;&lt;mount point destination&gt;&#x60;)  | [optional] 

### Return type

[**List[ContainerSummary]**](ContainerSummary.md)

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

# **container_logs**
> bytearray container_logs(id, follow=follow, stdout=stdout, stderr=stderr, since=since, until=until, timestamps=timestamps, tail=tail)

Get container logs

Get `stdout` and `stderr` logs from a container.  Note: This endpoint works only for containers with the `json-file` or `journald` logging driver. 

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    follow = False # bool | Keep connection after returning logs. (optional) (default to False)
    stdout = False # bool | Return logs from `stdout` (optional) (default to False)
    stderr = False # bool | Return logs from `stderr` (optional) (default to False)
    since = 0 # int | Only return logs since this time, as a UNIX timestamp (optional) (default to 0)
    until = 0 # int | Only return logs before this time, as a UNIX timestamp (optional) (default to 0)
    timestamps = False # bool | Add timestamps to every log line (optional) (default to False)
    tail = 'all' # str | Only return this number of log lines from the end of the logs. Specify as an integer or `all` to output all log lines.  (optional) (default to 'all')

    try:
        # Get container logs
        api_response = api_instance.container_logs(id, follow=follow, stdout=stdout, stderr=stderr, since=since, until=until, timestamps=timestamps, tail=tail)
        print("The response of ContainerApi->container_logs:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_logs: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **follow** | **bool**| Keep connection after returning logs. | [optional] [default to False]
 **stdout** | **bool**| Return logs from &#x60;stdout&#x60; | [optional] [default to False]
 **stderr** | **bool**| Return logs from &#x60;stderr&#x60; | [optional] [default to False]
 **since** | **int**| Only return logs since this time, as a UNIX timestamp | [optional] [default to 0]
 **until** | **int**| Only return logs before this time, as a UNIX timestamp | [optional] [default to 0]
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
**200** | logs returned as a stream in response body. For the stream format, [see the documentation for the attach endpoint](#operation/ContainerAttach). Note that unlike the attach endpoint, the logs endpoint does not upgrade the connection and does not set Content-Type.  |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_pause**
> container_pause(id)

Pause a container

Use the freezer cgroup to suspend all processes in a container.  Traditionally, when suspending a process the `SIGSTOP` signal is used, which is observable by the process being suspended. With the freezer cgroup the process is unaware, and unable to capture, that it is being suspended, and subsequently resumed. 

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container

    try:
        # Pause a container
        api_instance.container_pause(id)
    except Exception as e:
        print("Exception when calling ContainerApi->container_pause: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 

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
**204** | no error |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_prune**
> ContainerPruneResponse container_prune(filters=filters)

Delete stopped containers

### Example


```python
import docker_client.generated
from docker_client.generated.models.container_prune_response import ContainerPruneResponse
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    filters = 'filters_example' # str | Filters to process on the prune list, encoded as JSON (a `map[string][]string`).  Available filters: - `until=<timestamp>` Prune containers created before this timestamp. The `<timestamp>` can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. `10m`, `1h30m`) computed relative to the daemon machine’s time. - `label` (`label=<key>`, `label=<key>=<value>`, `label!=<key>`, or `label!=<key>=<value>`) Prune containers with (or without, in case `label!=...` is used) the specified labels.  (optional)

    try:
        # Delete stopped containers
        api_response = api_instance.container_prune(filters=filters)
        print("The response of ContainerApi->container_prune:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_prune: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| Filters to process on the prune list, encoded as JSON (a &#x60;map[string][]string&#x60;).  Available filters: - &#x60;until&#x3D;&lt;timestamp&gt;&#x60; Prune containers created before this timestamp. The &#x60;&lt;timestamp&gt;&#x60; can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. &#x60;10m&#x60;, &#x60;1h30m&#x60;) computed relative to the daemon machine’s time. - &#x60;label&#x60; (&#x60;label&#x3D;&lt;key&gt;&#x60;, &#x60;label&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;, &#x60;label!&#x3D;&lt;key&gt;&#x60;, or &#x60;label!&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;) Prune containers with (or without, in case &#x60;label!&#x3D;...&#x60; is used) the specified labels.  | [optional] 

### Return type

[**ContainerPruneResponse**](ContainerPruneResponse.md)

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

# **container_rename**
> container_rename(id, name)

Rename a container

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    name = 'name_example' # str | New name for the container

    try:
        # Rename a container
        api_instance.container_rename(id, name)
    except Exception as e:
        print("Exception when calling ContainerApi->container_rename: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **name** | **str**| New name for the container | 

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
**204** | no error |  -  |
**404** | no such container |  -  |
**409** | name already in use |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_resize**
> container_resize(id, h=h, w=w)

Resize a container TTY

Resize the TTY for a container.

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    h = 56 # int | Height of the TTY session in characters (optional)
    w = 56 # int | Width of the TTY session in characters (optional)

    try:
        # Resize a container TTY
        api_instance.container_resize(id, h=h, w=w)
    except Exception as e:
        print("Exception when calling ContainerApi->container_resize: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **h** | **int**| Height of the TTY session in characters | [optional] 
 **w** | **int**| Width of the TTY session in characters | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such container |  -  |
**500** | cannot resize container |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_restart**
> container_restart(id, signal=signal, t=t)

Restart a container

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    signal = 'signal_example' # str | Signal to send to the container as an integer or string (e.g. `SIGINT`).  (optional)
    t = 56 # int | Number of seconds to wait before killing the container (optional)

    try:
        # Restart a container
        api_instance.container_restart(id, signal=signal, t=t)
    except Exception as e:
        print("Exception when calling ContainerApi->container_restart: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **signal** | **str**| Signal to send to the container as an integer or string (e.g. &#x60;SIGINT&#x60;).  | [optional] 
 **t** | **int**| Number of seconds to wait before killing the container | [optional] 

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
**204** | no error |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_start**
> container_start(id, detach_keys=detach_keys)

Start a container

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    detach_keys = 'detach_keys_example' # str | Override the key sequence for detaching a container. Format is a single character `[a-Z]` or `ctrl-<value>` where `<value>` is one of: `a-z`, `@`, `^`, `[`, `,` or `_`.  (optional)

    try:
        # Start a container
        api_instance.container_start(id, detach_keys=detach_keys)
    except Exception as e:
        print("Exception when calling ContainerApi->container_start: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **detach_keys** | **str**| Override the key sequence for detaching a container. Format is a single character &#x60;[a-Z]&#x60; or &#x60;ctrl-&lt;value&gt;&#x60; where &#x60;&lt;value&gt;&#x60; is one of: &#x60;a-z&#x60;, &#x60;@&#x60;, &#x60;^&#x60;, &#x60;[&#x60;, &#x60;,&#x60; or &#x60;_&#x60;.  | [optional] 

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
**204** | no error |  -  |
**304** | container already started |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_stats**
> object container_stats(id, stream=stream, one_shot=one_shot)

Get container stats based on resource usage

This endpoint returns a live stream of a container’s resource usage statistics.  The `precpu_stats` is the CPU statistic of the *previous* read, and is used to calculate the CPU usage percentage. It is not an exact copy of the `cpu_stats` field.  If either `precpu_stats.online_cpus` or `cpu_stats.online_cpus` is nil then for compatibility with older daemons the length of the corresponding `cpu_usage.percpu_usage` array should be used.  On a cgroup v2 host, the following fields are not set * `blkio_stats`: all fields other than `io_service_bytes_recursive` * `cpu_stats`: `cpu_usage.percpu_usage` * `memory_stats`: `max_usage` and `failcnt` Also, `memory_stats.stats` fields are incompatible with cgroup v1.  To calculate the values shown by the `stats` command of the docker cli tool the following formulas can be used: * used_memory = `memory_stats.usage - memory_stats.stats.cache` * available_memory = `memory_stats.limit` * Memory usage % = `(used_memory / available_memory) * 100.0` * cpu_delta = `cpu_stats.cpu_usage.total_usage - precpu_stats.cpu_usage.total_usage` * system_cpu_delta = `cpu_stats.system_cpu_usage - precpu_stats.system_cpu_usage` * number_cpus = `length(cpu_stats.cpu_usage.percpu_usage)` or `cpu_stats.online_cpus` * CPU usage % = `(cpu_delta / system_cpu_delta) * number_cpus * 100.0` 

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    stream = True # bool | Stream the output. If false, the stats will be output once and then it will disconnect.  (optional) (default to True)
    one_shot = False # bool | Only get a single stat instead of waiting for 2 cycles. Must be used with `stream=false`.  (optional) (default to False)

    try:
        # Get container stats based on resource usage
        api_response = api_instance.container_stats(id, stream=stream, one_shot=one_shot)
        print("The response of ContainerApi->container_stats:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_stats: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **stream** | **bool**| Stream the output. If false, the stats will be output once and then it will disconnect.  | [optional] [default to True]
 **one_shot** | **bool**| Only get a single stat instead of waiting for 2 cycles. Must be used with &#x60;stream&#x3D;false&#x60;.  | [optional] [default to False]

### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_stop**
> container_stop(id, signal=signal, t=t)

Stop a container

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    signal = 'signal_example' # str | Signal to send to the container as an integer or string (e.g. `SIGINT`).  (optional)
    t = 56 # int | Number of seconds to wait before killing the container (optional)

    try:
        # Stop a container
        api_instance.container_stop(id, signal=signal, t=t)
    except Exception as e:
        print("Exception when calling ContainerApi->container_stop: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **signal** | **str**| Signal to send to the container as an integer or string (e.g. &#x60;SIGINT&#x60;).  | [optional] 
 **t** | **int**| Number of seconds to wait before killing the container | [optional] 

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
**204** | no error |  -  |
**304** | container already stopped |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_top**
> ContainerTopResponse container_top(id, ps_args=ps_args)

List processes running inside a container

On Unix systems, this is done by running the `ps` command. This endpoint is not supported on Windows. 

### Example


```python
import docker_client.generated
from docker_client.generated.models.container_top_response import ContainerTopResponse
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    ps_args = '-ef' # str | The arguments to pass to `ps`. For example, `aux` (optional) (default to '-ef')

    try:
        # List processes running inside a container
        api_response = api_instance.container_top(id, ps_args=ps_args)
        print("The response of ContainerApi->container_top:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_top: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **ps_args** | **str**| The arguments to pass to &#x60;ps&#x60;. For example, &#x60;aux&#x60; | [optional] [default to &#39;-ef&#39;]

### Return type

[**ContainerTopResponse**](ContainerTopResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_unpause**
> container_unpause(id)

Unpause a container

Resume a container which has been paused.

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container

    try:
        # Unpause a container
        api_instance.container_unpause(id)
    except Exception as e:
        print("Exception when calling ContainerApi->container_unpause: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 

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
**204** | no error |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_update**
> ContainerUpdateResponse container_update(id, update)

Update a container

Change various configuration options of a container without having to recreate it. 

### Example


```python
import docker_client.generated
from docker_client.generated.models.container_update_request import ContainerUpdateRequest
from docker_client.generated.models.container_update_response import ContainerUpdateResponse
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    update = docker_client.generated.ContainerUpdateRequest() # ContainerUpdateRequest | 

    try:
        # Update a container
        api_response = api_instance.container_update(id, update)
        print("The response of ContainerApi->container_update:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_update: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **update** | [**ContainerUpdateRequest**](ContainerUpdateRequest.md)|  | 

### Return type

[**ContainerUpdateResponse**](ContainerUpdateResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The container has been updated. |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **container_wait**
> ContainerWaitResponse container_wait(id, condition=condition)

Wait for a container

Block until a container stops, then returns the exit code.

### Example


```python
import docker_client.generated
from docker_client.generated.models.container_wait_response import ContainerWaitResponse
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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    condition = not-running # str | Wait until a container state reaches the given condition.  Defaults to `not-running` if omitted or empty.  (optional) (default to not-running)

    try:
        # Wait for a container
        api_response = api_instance.container_wait(id, condition=condition)
        print("The response of ContainerApi->container_wait:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContainerApi->container_wait: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **condition** | **str**| Wait until a container state reaches the given condition.  Defaults to &#x60;not-running&#x60; if omitted or empty.  | [optional] [default to not-running]

### Return type

[**ContainerWaitResponse**](ContainerWaitResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The container has exit. |  -  |
**400** | bad parameter |  -  |
**404** | no such container |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **put_container_archive**
> put_container_archive(id, path, input_stream, no_overwrite_dir_non_dir=no_overwrite_dir_non_dir, copy_uidgid=copy_uidgid)

Extract an archive of files or folders to a directory in a container

Upload a tar archive to be extracted to a path in the filesystem of container id. `path` parameter is asserted to be a directory. If it exists as a file, 400 error will be returned with message \"not a directory\". 

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
    api_instance = docker_client.generated.ContainerApi(api_client)
    id = 'id_example' # str | ID or name of the container
    path = 'path_example' # str | Path to a directory in the container to extract the archive’s contents into. 
    input_stream = None # bytearray | The input stream must be a tar archive compressed with one of the following algorithms: `identity` (no compression), `gzip`, `bzip2`, or `xz`. 
    no_overwrite_dir_non_dir = 'no_overwrite_dir_non_dir_example' # str | If `1`, `true`, or `True` then it will be an error if unpacking the given content would cause an existing directory to be replaced with a non-directory and vice versa.  (optional)
    copy_uidgid = 'copy_uidgid_example' # str | If `1`, `true`, then it will copy UID/GID maps to the dest file or dir  (optional)

    try:
        # Extract an archive of files or folders to a directory in a container
        api_instance.put_container_archive(id, path, input_stream, no_overwrite_dir_non_dir=no_overwrite_dir_non_dir, copy_uidgid=copy_uidgid)
    except Exception as e:
        print("Exception when calling ContainerApi->put_container_archive: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **str**| ID or name of the container | 
 **path** | **str**| Path to a directory in the container to extract the archive’s contents into.  | 
 **input_stream** | **bytearray**| The input stream must be a tar archive compressed with one of the following algorithms: &#x60;identity&#x60; (no compression), &#x60;gzip&#x60;, &#x60;bzip2&#x60;, or &#x60;xz&#x60;.  | 
 **no_overwrite_dir_non_dir** | **str**| If &#x60;1&#x60;, &#x60;true&#x60;, or &#x60;True&#x60; then it will be an error if unpacking the given content would cause an existing directory to be replaced with a non-directory and vice versa.  | [optional] 
 **copy_uidgid** | **str**| If &#x60;1&#x60;, &#x60;true&#x60;, then it will copy UID/GID maps to the dest file or dir  | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/x-tar, application/octet-stream
 - **Accept**: application/json, text/plain

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The content was extracted successfully |  -  |
**400** | Bad parameter |  -  |
**403** | Permission denied, the volume or container rootfs is marked as read-only. |  -  |
**404** | No such container or path does not exist inside the container |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

