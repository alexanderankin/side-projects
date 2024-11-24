# docker_client.generated.ImageApi

All URIs are relative to *http://localhost/v1.45*

Method | HTTP request | Description
------------- | ------------- | -------------
[**build_prune**](ImageApi.md#build_prune) | **POST** /build/prune | Delete builder cache
[**image_build**](ImageApi.md#image_build) | **POST** /build | Build an image
[**image_commit**](ImageApi.md#image_commit) | **POST** /commit | Create a new image from a container
[**image_create**](ImageApi.md#image_create) | **POST** /images/create | Create an image
[**image_delete**](ImageApi.md#image_delete) | **DELETE** /images/{name} | Remove an image
[**image_get**](ImageApi.md#image_get) | **GET** /images/{name}/get | Export an image
[**image_get_all**](ImageApi.md#image_get_all) | **GET** /images/get | Export several images
[**image_history**](ImageApi.md#image_history) | **GET** /images/{name}/history | Get the history of an image
[**image_inspect**](ImageApi.md#image_inspect) | **GET** /images/{name}/json | Inspect an image
[**image_list**](ImageApi.md#image_list) | **GET** /images/json | List Images
[**image_load**](ImageApi.md#image_load) | **POST** /images/load | Import images
[**image_prune**](ImageApi.md#image_prune) | **POST** /images/prune | Delete unused images
[**image_push**](ImageApi.md#image_push) | **POST** /images/{name}/push | Push an image
[**image_search**](ImageApi.md#image_search) | **GET** /images/search | Search images
[**image_tag**](ImageApi.md#image_tag) | **POST** /images/{name}/tag | Tag an image


# **build_prune**
> BuildPruneResponse build_prune(keep_storage=keep_storage, all=all, filters=filters)

Delete builder cache

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.build_prune_response import BuildPruneResponse
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
    api_instance = docker_client.generated.ImageApi(api_client)
    keep_storage = 56 # int | Amount of disk space in bytes to keep for cache (optional)
    all = True # bool | Remove all types of build cache (optional)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the list of build cache objects.  Available filters:  - `until=<timestamp>` remove cache older than `<timestamp>`. The `<timestamp>` can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. `10m`, `1h30m`) computed relative to the daemon's local time. - `id=<id>` - `parent=<id>` - `type=<string>` - `description=<string>` - `inuse` - `shared` - `private`  (optional)

    try:
        # Delete builder cache
        api_response = api_instance.build_prune(keep_storage=keep_storage, all=all, filters=filters)
        print("The response of ImageApi->build_prune:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->build_prune: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **keep_storage** | **int**| Amount of disk space in bytes to keep for cache | [optional] 
 **all** | **bool**| Remove all types of build cache | [optional] 
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the list of build cache objects.  Available filters:  - &#x60;until&#x3D;&lt;timestamp&gt;&#x60; remove cache older than &#x60;&lt;timestamp&gt;&#x60;. The &#x60;&lt;timestamp&gt;&#x60; can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. &#x60;10m&#x60;, &#x60;1h30m&#x60;) computed relative to the daemon&#39;s local time. - &#x60;id&#x3D;&lt;id&gt;&#x60; - &#x60;parent&#x3D;&lt;id&gt;&#x60; - &#x60;type&#x3D;&lt;string&gt;&#x60; - &#x60;description&#x3D;&lt;string&gt;&#x60; - &#x60;inuse&#x60; - &#x60;shared&#x60; - &#x60;private&#x60;  | [optional] 

### Return type

[**BuildPruneResponse**](BuildPruneResponse.md)

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

# **image_build**
> image_build(dockerfile=dockerfile, t=t, extrahosts=extrahosts, remote=remote, q=q, nocache=nocache, cachefrom=cachefrom, pull=pull, rm=rm, forcerm=forcerm, memory=memory, memswap=memswap, cpushares=cpushares, cpusetcpus=cpusetcpus, cpuperiod=cpuperiod, cpuquota=cpuquota, buildargs=buildargs, shmsize=shmsize, squash=squash, labels=labels, networkmode=networkmode, content_type=content_type, x_registry_config=x_registry_config, platform=platform, target=target, outputs=outputs, version=version, input_stream=input_stream)

Build an image

Build an image from a tar archive with a `Dockerfile` in it.  The `Dockerfile` specifies how the image is built from the tar archive. It is typically in the archive's root, but can be at a different path or have a different name by specifying the `dockerfile` parameter. [See the `Dockerfile` reference for more information](https://docs.docker.com/engine/reference/builder/).  The Docker daemon performs a preliminary validation of the `Dockerfile` before starting the build, and returns an error if the syntax is incorrect. After that, each instruction is run one-by-one until the ID of the new image is output.  The build is canceled if the client drops the connection by quitting or being killed. 

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
    api_instance = docker_client.generated.ImageApi(api_client)
    dockerfile = 'Dockerfile' # str | Path within the build context to the `Dockerfile`. This is ignored if `remote` is specified and points to an external `Dockerfile`. (optional) (default to 'Dockerfile')
    t = 't_example' # str | A name and optional tag to apply to the image in the `name:tag` format. If you omit the tag the default `latest` value is assumed. You can provide several `t` parameters. (optional)
    extrahosts = 'extrahosts_example' # str | Extra hosts to add to /etc/hosts (optional)
    remote = 'remote_example' # str | A Git repository URI or HTTP/HTTPS context URI. If the URI points to a single text file, the file’s contents are placed into a file called `Dockerfile` and the image is built from that file. If the URI points to a tarball, the file is downloaded by the daemon and the contents therein used as the context for the build. If the URI points to a tarball and the `dockerfile` parameter is also specified, there must be a file with the corresponding path inside the tarball. (optional)
    q = False # bool | Suppress verbose build output. (optional) (default to False)
    nocache = False # bool | Do not use the cache when building the image. (optional) (default to False)
    cachefrom = 'cachefrom_example' # str | JSON array of images used for build cache resolution. (optional)
    pull = 'pull_example' # str | Attempt to pull the image even if an older image exists locally. (optional)
    rm = True # bool | Remove intermediate containers after a successful build. (optional) (default to True)
    forcerm = False # bool | Always remove intermediate containers, even upon failure. (optional) (default to False)
    memory = 56 # int | Set memory limit for build. (optional)
    memswap = 56 # int | Total memory (memory + swap). Set as `-1` to disable swap. (optional)
    cpushares = 56 # int | CPU shares (relative weight). (optional)
    cpusetcpus = 'cpusetcpus_example' # str | CPUs in which to allow execution (e.g., `0-3`, `0,1`). (optional)
    cpuperiod = 56 # int | The length of a CPU period in microseconds. (optional)
    cpuquota = 56 # int | Microseconds of CPU time that the container can get in a CPU period. (optional)
    buildargs = 'buildargs_example' # str | JSON map of string pairs for build-time variables. Users pass these values at build-time. Docker uses the buildargs as the environment context for commands run via the `Dockerfile` RUN instruction, or for variable expansion in other `Dockerfile` instructions. This is not meant for passing secret values.  For example, the build arg `FOO=bar` would become `{\"FOO\":\"bar\"}` in JSON. This would result in the query parameter `buildargs={\"FOO\":\"bar\"}`. Note that `{\"FOO\":\"bar\"}` should be URI component encoded.  [Read more about the buildargs instruction.](https://docs.docker.com/engine/reference/builder/#arg)  (optional)
    shmsize = 56 # int | Size of `/dev/shm` in bytes. The size must be greater than 0. If omitted the system uses 64MB. (optional)
    squash = True # bool | Squash the resulting images layers into a single layer. *(Experimental release only.)* (optional)
    labels = 'labels_example' # str | Arbitrary key/value labels to set on the image, as a JSON map of string pairs. (optional)
    networkmode = 'networkmode_example' # str | Sets the networking mode for the run commands during build. Supported standard values are: `bridge`, `host`, `none`, and `container:<name|id>`. Any other value is taken as a custom network's name or ID to which this container should connect to.  (optional)
    content_type = application/x-tar # str |  (optional) (default to application/x-tar)
    x_registry_config = 'x_registry_config_example' # str | This is a base64-encoded JSON object with auth configurations for multiple registries that a build may refer to.  The key is a registry URL, and the value is an auth configuration object, [as described in the authentication section](#section/Authentication). For example:  ``` {   \"docker.example.com\": {     \"username\": \"janedoe\",     \"password\": \"hunter2\"   },   \"https://index.docker.io/v1/\": {     \"username\": \"mobydock\",     \"password\": \"conta1n3rize14\"   } } ```  Only the registry domain name (and port if not the default 443) are required. However, for legacy reasons, the Docker Hub registry must be specified with both a `https://` prefix and a `/v1/` suffix even though Docker will prefer to use the v2 registry API.  (optional)
    platform = 'platform_example' # str | Platform in the format os[/arch[/variant]] (optional)
    target = 'target_example' # str | Target build stage (optional)
    outputs = 'outputs_example' # str | BuildKit output configuration (optional)
    version = 1 # str | Version of the builder backend to use.  - `1` is the first generation classic (deprecated) builder in the Docker daemon (default) - `2` is [BuildKit](https://github.com/moby/buildkit)  (optional) (default to 1)
    input_stream = None # bytearray | A tar archive compressed with one of the following algorithms: identity (no compression), gzip, bzip2, xz. (optional)

    try:
        # Build an image
        api_instance.image_build(dockerfile=dockerfile, t=t, extrahosts=extrahosts, remote=remote, q=q, nocache=nocache, cachefrom=cachefrom, pull=pull, rm=rm, forcerm=forcerm, memory=memory, memswap=memswap, cpushares=cpushares, cpusetcpus=cpusetcpus, cpuperiod=cpuperiod, cpuquota=cpuquota, buildargs=buildargs, shmsize=shmsize, squash=squash, labels=labels, networkmode=networkmode, content_type=content_type, x_registry_config=x_registry_config, platform=platform, target=target, outputs=outputs, version=version, input_stream=input_stream)
    except Exception as e:
        print("Exception when calling ImageApi->image_build: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **dockerfile** | **str**| Path within the build context to the &#x60;Dockerfile&#x60;. This is ignored if &#x60;remote&#x60; is specified and points to an external &#x60;Dockerfile&#x60;. | [optional] [default to &#39;Dockerfile&#39;]
 **t** | **str**| A name and optional tag to apply to the image in the &#x60;name:tag&#x60; format. If you omit the tag the default &#x60;latest&#x60; value is assumed. You can provide several &#x60;t&#x60; parameters. | [optional] 
 **extrahosts** | **str**| Extra hosts to add to /etc/hosts | [optional] 
 **remote** | **str**| A Git repository URI or HTTP/HTTPS context URI. If the URI points to a single text file, the file’s contents are placed into a file called &#x60;Dockerfile&#x60; and the image is built from that file. If the URI points to a tarball, the file is downloaded by the daemon and the contents therein used as the context for the build. If the URI points to a tarball and the &#x60;dockerfile&#x60; parameter is also specified, there must be a file with the corresponding path inside the tarball. | [optional] 
 **q** | **bool**| Suppress verbose build output. | [optional] [default to False]
 **nocache** | **bool**| Do not use the cache when building the image. | [optional] [default to False]
 **cachefrom** | **str**| JSON array of images used for build cache resolution. | [optional] 
 **pull** | **str**| Attempt to pull the image even if an older image exists locally. | [optional] 
 **rm** | **bool**| Remove intermediate containers after a successful build. | [optional] [default to True]
 **forcerm** | **bool**| Always remove intermediate containers, even upon failure. | [optional] [default to False]
 **memory** | **int**| Set memory limit for build. | [optional] 
 **memswap** | **int**| Total memory (memory + swap). Set as &#x60;-1&#x60; to disable swap. | [optional] 
 **cpushares** | **int**| CPU shares (relative weight). | [optional] 
 **cpusetcpus** | **str**| CPUs in which to allow execution (e.g., &#x60;0-3&#x60;, &#x60;0,1&#x60;). | [optional] 
 **cpuperiod** | **int**| The length of a CPU period in microseconds. | [optional] 
 **cpuquota** | **int**| Microseconds of CPU time that the container can get in a CPU period. | [optional] 
 **buildargs** | **str**| JSON map of string pairs for build-time variables. Users pass these values at build-time. Docker uses the buildargs as the environment context for commands run via the &#x60;Dockerfile&#x60; RUN instruction, or for variable expansion in other &#x60;Dockerfile&#x60; instructions. This is not meant for passing secret values.  For example, the build arg &#x60;FOO&#x3D;bar&#x60; would become &#x60;{\&quot;FOO\&quot;:\&quot;bar\&quot;}&#x60; in JSON. This would result in the query parameter &#x60;buildargs&#x3D;{\&quot;FOO\&quot;:\&quot;bar\&quot;}&#x60;. Note that &#x60;{\&quot;FOO\&quot;:\&quot;bar\&quot;}&#x60; should be URI component encoded.  [Read more about the buildargs instruction.](https://docs.docker.com/engine/reference/builder/#arg)  | [optional] 
 **shmsize** | **int**| Size of &#x60;/dev/shm&#x60; in bytes. The size must be greater than 0. If omitted the system uses 64MB. | [optional] 
 **squash** | **bool**| Squash the resulting images layers into a single layer. *(Experimental release only.)* | [optional] 
 **labels** | **str**| Arbitrary key/value labels to set on the image, as a JSON map of string pairs. | [optional] 
 **networkmode** | **str**| Sets the networking mode for the run commands during build. Supported standard values are: &#x60;bridge&#x60;, &#x60;host&#x60;, &#x60;none&#x60;, and &#x60;container:&lt;name|id&gt;&#x60;. Any other value is taken as a custom network&#39;s name or ID to which this container should connect to.  | [optional] 
 **content_type** | **str**|  | [optional] [default to application/x-tar]
 **x_registry_config** | **str**| This is a base64-encoded JSON object with auth configurations for multiple registries that a build may refer to.  The key is a registry URL, and the value is an auth configuration object, [as described in the authentication section](#section/Authentication). For example:  &#x60;&#x60;&#x60; {   \&quot;docker.example.com\&quot;: {     \&quot;username\&quot;: \&quot;janedoe\&quot;,     \&quot;password\&quot;: \&quot;hunter2\&quot;   },   \&quot;https://index.docker.io/v1/\&quot;: {     \&quot;username\&quot;: \&quot;mobydock\&quot;,     \&quot;password\&quot;: \&quot;conta1n3rize14\&quot;   } } &#x60;&#x60;&#x60;  Only the registry domain name (and port if not the default 443) are required. However, for legacy reasons, the Docker Hub registry must be specified with both a &#x60;https://&#x60; prefix and a &#x60;/v1/&#x60; suffix even though Docker will prefer to use the v2 registry API.  | [optional] 
 **platform** | **str**| Platform in the format os[/arch[/variant]] | [optional] 
 **target** | **str**| Target build stage | [optional] 
 **outputs** | **str**| BuildKit output configuration | [optional] 
 **version** | **str**| Version of the builder backend to use.  - &#x60;1&#x60; is the first generation classic (deprecated) builder in the Docker daemon (default) - &#x60;2&#x60; is [BuildKit](https://github.com/moby/buildkit)  | [optional] [default to 1]
 **input_stream** | **bytearray**| A tar archive compressed with one of the following algorithms: identity (no compression), gzip, bzip2, xz. | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/octet-stream
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**400** | Bad parameter |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_commit**
> IdResponse image_commit(container=container, repo=repo, tag=tag, comment=comment, author=author, pause=pause, changes=changes, container_config=container_config)

Create a new image from a container

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.container_config import ContainerConfig
from docker_client.generated.docker_client.generated.models.id_response import IdResponse
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
    api_instance = docker_client.generated.ImageApi(api_client)
    container = 'container_example' # str | The ID or name of the container to commit (optional)
    repo = 'repo_example' # str | Repository name for the created image (optional)
    tag = 'tag_example' # str | Tag name for the create image (optional)
    comment = 'comment_example' # str | Commit message (optional)
    author = 'author_example' # str | Author of the image (e.g., `John Hannibal Smith <hannibal@a-team.com>`) (optional)
    pause = True # bool | Whether to pause the container before committing (optional) (default to True)
    changes = 'changes_example' # str | `Dockerfile` instructions to apply while committing (optional)
    container_config = docker_client.generated.ContainerConfig() # ContainerConfig | The container configuration (optional)

    try:
        # Create a new image from a container
        api_response = api_instance.image_commit(container=container, repo=repo, tag=tag, comment=comment, author=author, pause=pause, changes=changes, container_config=container_config)
        print("The response of ImageApi->image_commit:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_commit: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **container** | **str**| The ID or name of the container to commit | [optional] 
 **repo** | **str**| Repository name for the created image | [optional] 
 **tag** | **str**| Tag name for the create image | [optional] 
 **comment** | **str**| Commit message | [optional] 
 **author** | **str**| Author of the image (e.g., &#x60;John Hannibal Smith &lt;hannibal@a-team.com&gt;&#x60;) | [optional] 
 **pause** | **bool**| Whether to pause the container before committing | [optional] [default to True]
 **changes** | **str**| &#x60;Dockerfile&#x60; instructions to apply while committing | [optional] 
 **container_config** | [**ContainerConfig**](ContainerConfig.md)| The container configuration | [optional] 

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
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_create**
> image_create(from_image=from_image, from_src=from_src, repo=repo, tag=tag, message=message, x_registry_auth=x_registry_auth, changes=changes, platform=platform, input_image=input_image)

Create an image

Pull or import an image.

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
    api_instance = docker_client.generated.ImageApi(api_client)
    from_image = 'from_image_example' # str | Name of the image to pull. The name may include a tag or digest. This parameter may only be used when pulling an image. The pull is cancelled if the HTTP connection is closed. (optional)
    from_src = 'from_src_example' # str | Source to import. The value may be a URL from which the image can be retrieved or `-` to read the image from the request body. This parameter may only be used when importing an image. (optional)
    repo = 'repo_example' # str | Repository name given to an image when it is imported. The repo may include a tag. This parameter may only be used when importing an image. (optional)
    tag = 'tag_example' # str | Tag or digest. If empty when pulling an image, this causes all tags for the given image to be pulled. (optional)
    message = 'message_example' # str | Set commit message for imported image. (optional)
    x_registry_auth = 'x_registry_auth_example' # str | A base64url-encoded auth configuration.  Refer to the [authentication section](#section/Authentication) for details.  (optional)
    changes = ['changes_example'] # List[str] | Apply `Dockerfile` instructions to the image that is created, for example: `changes=ENV DEBUG=true`. Note that `ENV DEBUG=true` should be URI component encoded.  Supported `Dockerfile` instructions: `CMD`|`ENTRYPOINT`|`ENV`|`EXPOSE`|`ONBUILD`|`USER`|`VOLUME`|`WORKDIR`  (optional)
    platform = 'platform_example' # str | Platform in the format os[/arch[/variant]].  When used in combination with the `fromImage` option, the daemon checks if the given image is present in the local image cache with the given OS and Architecture, and otherwise attempts to pull the image. If the option is not set, the host's native OS and Architecture are used. If the given image does not exist in the local image cache, the daemon attempts to pull the image with the host's native OS and Architecture. If the given image does exists in the local image cache, but its OS or architecture does not match, a warning is produced.  When used with the `fromSrc` option to import an image from an archive, this option sets the platform information for the imported image. If the option is not set, the host's native OS and Architecture are used for the imported image.  (optional)
    input_image = 'input_image_example' # str | Image content if the value `-` has been specified in fromSrc query parameter (optional)

    try:
        # Create an image
        api_instance.image_create(from_image=from_image, from_src=from_src, repo=repo, tag=tag, message=message, x_registry_auth=x_registry_auth, changes=changes, platform=platform, input_image=input_image)
    except Exception as e:
        print("Exception when calling ImageApi->image_create: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **from_image** | **str**| Name of the image to pull. The name may include a tag or digest. This parameter may only be used when pulling an image. The pull is cancelled if the HTTP connection is closed. | [optional] 
 **from_src** | **str**| Source to import. The value may be a URL from which the image can be retrieved or &#x60;-&#x60; to read the image from the request body. This parameter may only be used when importing an image. | [optional] 
 **repo** | **str**| Repository name given to an image when it is imported. The repo may include a tag. This parameter may only be used when importing an image. | [optional] 
 **tag** | **str**| Tag or digest. If empty when pulling an image, this causes all tags for the given image to be pulled. | [optional] 
 **message** | **str**| Set commit message for imported image. | [optional] 
 **x_registry_auth** | **str**| A base64url-encoded auth configuration.  Refer to the [authentication section](#section/Authentication) for details.  | [optional] 
 **changes** | [**List[str]**](str.md)| Apply &#x60;Dockerfile&#x60; instructions to the image that is created, for example: &#x60;changes&#x3D;ENV DEBUG&#x3D;true&#x60;. Note that &#x60;ENV DEBUG&#x3D;true&#x60; should be URI component encoded.  Supported &#x60;Dockerfile&#x60; instructions: &#x60;CMD&#x60;|&#x60;ENTRYPOINT&#x60;|&#x60;ENV&#x60;|&#x60;EXPOSE&#x60;|&#x60;ONBUILD&#x60;|&#x60;USER&#x60;|&#x60;VOLUME&#x60;|&#x60;WORKDIR&#x60;  | [optional] 
 **platform** | **str**| Platform in the format os[/arch[/variant]].  When used in combination with the &#x60;fromImage&#x60; option, the daemon checks if the given image is present in the local image cache with the given OS and Architecture, and otherwise attempts to pull the image. If the option is not set, the host&#39;s native OS and Architecture are used. If the given image does not exist in the local image cache, the daemon attempts to pull the image with the host&#39;s native OS and Architecture. If the given image does exists in the local image cache, but its OS or architecture does not match, a warning is produced.  When used with the &#x60;fromSrc&#x60; option to import an image from an archive, this option sets the platform information for the imported image. If the option is not set, the host&#39;s native OS and Architecture are used for the imported image.  | [optional] 
 **input_image** | **str**| Image content if the value &#x60;-&#x60; has been specified in fromSrc query parameter | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: text/plain, application/octet-stream
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**404** | repository does not exist or no read access |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_delete**
> List[ImageDeleteResponseItem] image_delete(name, force=force, noprune=noprune)

Remove an image

Remove an image, along with any untagged parent images that were referenced by that image.  Images can't be removed if they have descendant images, are being used by a running container or are being used by a build. 

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.image_delete_response_item import ImageDeleteResponseItem
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
    api_instance = docker_client.generated.ImageApi(api_client)
    name = 'name_example' # str | Image name or ID
    force = False # bool | Remove the image even if it is being used by stopped containers or has other tags (optional) (default to False)
    noprune = False # bool | Do not delete untagged parent images (optional) (default to False)

    try:
        # Remove an image
        api_response = api_instance.image_delete(name, force=force, noprune=noprune)
        print("The response of ImageApi->image_delete:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Image name or ID | 
 **force** | **bool**| Remove the image even if it is being used by stopped containers or has other tags | [optional] [default to False]
 **noprune** | **bool**| Do not delete untagged parent images | [optional] [default to False]

### Return type

[**List[ImageDeleteResponseItem]**](ImageDeleteResponseItem.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The image was deleted successfully |  -  |
**404** | No such image |  -  |
**409** | Conflict |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_get**
> bytearray image_get(name)

Export an image

Get a tarball containing all images and metadata for a repository.  If `name` is a specific name and tag (e.g. `ubuntu:latest`), then only that image (and its parents) are returned. If `name` is an image ID, similarly only that image (and its parents) are returned, but with the exclusion of the `repositories` file in the tarball, as there were no image names referenced.  ### Image tarball format  An image tarball contains one directory per image layer (named using its long ID), each containing these files:  - `VERSION`: currently `1.0` - the file format version - `json`: detailed layer information, similar to `docker inspect layer_id` - `layer.tar`: A tarfile containing the filesystem changes in this layer  The `layer.tar` file contains `aufs` style `.wh..wh.aufs` files and directories for storing attribute changes and deletions.  If the tarball defines a repository, the tarball should also include a `repositories` file at the root that contains a list of repository and tag names mapped to layer IDs.  ```json {   \"hello-world\": {     \"latest\": \"565a9d68a73f6706862bfe8409a7f659776d4d60a8d096eb4a3cbce6999cc2a1\"   } } ``` 

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
    api_instance = docker_client.generated.ImageApi(api_client)
    name = 'name_example' # str | Image name or ID

    try:
        # Export an image
        api_response = api_instance.image_get(name)
        print("The response of ImageApi->image_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Image name or ID | 

### Return type

**bytearray**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-tar

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_get_all**
> bytearray image_get_all(names=names)

Export several images

Get a tarball containing all images and metadata for several image repositories.  For each value of the `names` parameter: if it is a specific name and tag (e.g. `ubuntu:latest`), then only that image (and its parents) are returned; if it is an image ID, similarly only that image (and its parents) are returned and there would be no names referenced in the 'repositories' file for this image ID.  For details on the format, see the [export image endpoint](#operation/ImageGet). 

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
    api_instance = docker_client.generated.ImageApi(api_client)
    names = ['names_example'] # List[str] | Image names to filter by (optional)

    try:
        # Export several images
        api_response = api_instance.image_get_all(names=names)
        print("The response of ImageApi->image_get_all:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_get_all: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **names** | [**List[str]**](str.md)| Image names to filter by | [optional] 

### Return type

**bytearray**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-tar

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_history**
> List[HistoryResponseItem] image_history(name)

Get the history of an image

Return parent layers of an image.

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.history_response_item import HistoryResponseItem
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
    api_instance = docker_client.generated.ImageApi(api_client)
    name = 'name_example' # str | Image name or ID

    try:
        # Get the history of an image
        api_response = api_instance.image_history(name)
        print("The response of ImageApi->image_history:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_history: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Image name or ID | 

### Return type

[**List[HistoryResponseItem]**](HistoryResponseItem.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | List of image layers |  -  |
**404** | No such image |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_inspect**
> ImageInspect image_inspect(name)

Inspect an image

Return low-level information about an image.

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.image_inspect import ImageInspect
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
    api_instance = docker_client.generated.ImageApi(api_client)
    name = 'name_example' # str | Image name or id

    try:
        # Inspect an image
        api_response = api_instance.image_inspect(name)
        print("The response of ImageApi->image_inspect:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_inspect: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Image name or id | 

### Return type

[**ImageInspect**](ImageInspect.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | No error |  -  |
**404** | No such image |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_list**
> List[ImageSummary] image_list(all=all, filters=filters, shared_size=shared_size, digests=digests)

List Images

Returns a list of images on the server. Note that it uses a different, smaller representation of an image than inspecting a single image.

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.image_summary import ImageSummary
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
    api_instance = docker_client.generated.ImageApi(api_client)
    all = False # bool | Show all images. Only images from a final layer (no children) are shown by default. (optional) (default to False)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the images list.  Available filters:  - `before`=(`<image-name>[:<tag>]`,  `<image id>` or `<image@digest>`) - `dangling=true` - `label=key` or `label=\"key=value\"` of an image label - `reference`=(`<image-name>[:<tag>]`) - `since`=(`<image-name>[:<tag>]`,  `<image id>` or `<image@digest>`) - `until=<timestamp>`  (optional)
    shared_size = False # bool | Compute and show shared size as a `SharedSize` field on each image. (optional) (default to False)
    digests = False # bool | Show digest information as a `RepoDigests` field on each image. (optional) (default to False)

    try:
        # List Images
        api_response = api_instance.image_list(all=all, filters=filters, shared_size=shared_size, digests=digests)
        print("The response of ImageApi->image_list:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_list: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **all** | **bool**| Show all images. Only images from a final layer (no children) are shown by default. | [optional] [default to False]
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the images list.  Available filters:  - &#x60;before&#x60;&#x3D;(&#x60;&lt;image-name&gt;[:&lt;tag&gt;]&#x60;,  &#x60;&lt;image id&gt;&#x60; or &#x60;&lt;image@digest&gt;&#x60;) - &#x60;dangling&#x3D;true&#x60; - &#x60;label&#x3D;key&#x60; or &#x60;label&#x3D;\&quot;key&#x3D;value\&quot;&#x60; of an image label - &#x60;reference&#x60;&#x3D;(&#x60;&lt;image-name&gt;[:&lt;tag&gt;]&#x60;) - &#x60;since&#x60;&#x3D;(&#x60;&lt;image-name&gt;[:&lt;tag&gt;]&#x60;,  &#x60;&lt;image id&gt;&#x60; or &#x60;&lt;image@digest&gt;&#x60;) - &#x60;until&#x3D;&lt;timestamp&gt;&#x60;  | [optional] 
 **shared_size** | **bool**| Compute and show shared size as a &#x60;SharedSize&#x60; field on each image. | [optional] [default to False]
 **digests** | **bool**| Show digest information as a &#x60;RepoDigests&#x60; field on each image. | [optional] [default to False]

### Return type

[**List[ImageSummary]**](ImageSummary.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Summary image data for the images matching the query |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_load**
> image_load(quiet=quiet, images_tarball=images_tarball)

Import images

Load a set of images and tags into a repository.  For details on the format, see the [export image endpoint](#operation/ImageGet). 

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
    api_instance = docker_client.generated.ImageApi(api_client)
    quiet = False # bool | Suppress progress details during load. (optional) (default to False)
    images_tarball = None # bytearray | Tar archive containing images (optional)

    try:
        # Import images
        api_instance.image_load(quiet=quiet, images_tarball=images_tarball)
    except Exception as e:
        print("Exception when calling ImageApi->image_load: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **quiet** | **bool**| Suppress progress details during load. | [optional] [default to False]
 **images_tarball** | **bytearray**| Tar archive containing images | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/x-tar
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | no error |  -  |
**500** | server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_prune**
> ImagePruneResponse image_prune(filters=filters)

Delete unused images

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.image_prune_response import ImagePruneResponse
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
    api_instance = docker_client.generated.ImageApi(api_client)
    filters = 'filters_example' # str | Filters to process on the prune list, encoded as JSON (a `map[string][]string`). Available filters:  - `dangling=<boolean>` When set to `true` (or `1`), prune only    unused *and* untagged images. When set to `false`    (or `0`), all unused images are pruned. - `until=<string>` Prune images created before this timestamp. The `<timestamp>` can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. `10m`, `1h30m`) computed relative to the daemon machine’s time. - `label` (`label=<key>`, `label=<key>=<value>`, `label!=<key>`, or `label!=<key>=<value>`) Prune images with (or without, in case `label!=...` is used) the specified labels.  (optional)

    try:
        # Delete unused images
        api_response = api_instance.image_prune(filters=filters)
        print("The response of ImageApi->image_prune:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_prune: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filters** | **str**| Filters to process on the prune list, encoded as JSON (a &#x60;map[string][]string&#x60;). Available filters:  - &#x60;dangling&#x3D;&lt;boolean&gt;&#x60; When set to &#x60;true&#x60; (or &#x60;1&#x60;), prune only    unused *and* untagged images. When set to &#x60;false&#x60;    (or &#x60;0&#x60;), all unused images are pruned. - &#x60;until&#x3D;&lt;string&gt;&#x60; Prune images created before this timestamp. The &#x60;&lt;timestamp&gt;&#x60; can be Unix timestamps, date formatted timestamps, or Go duration strings (e.g. &#x60;10m&#x60;, &#x60;1h30m&#x60;) computed relative to the daemon machine’s time. - &#x60;label&#x60; (&#x60;label&#x3D;&lt;key&gt;&#x60;, &#x60;label&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;, &#x60;label!&#x3D;&lt;key&gt;&#x60;, or &#x60;label!&#x3D;&lt;key&gt;&#x3D;&lt;value&gt;&#x60;) Prune images with (or without, in case &#x60;label!&#x3D;...&#x60; is used) the specified labels.  | [optional] 

### Return type

[**ImagePruneResponse**](ImagePruneResponse.md)

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

# **image_push**
> image_push(name, x_registry_auth, tag=tag)

Push an image

Push an image to a registry.  If you wish to push an image on to a private registry, that image must already have a tag which references the registry. For example, `registry.example.com/myimage:latest`.  The push is cancelled if the HTTP connection is closed. 

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
    api_instance = docker_client.generated.ImageApi(api_client)
    name = 'name_example' # str | Name of the image to push. For example, `registry.example.com/myimage`. The image must be present in the local image store with the same name.  The name should be provided without tag; if a tag is provided, it is ignored. For example, `registry.example.com/myimage:latest` is considered equivalent to `registry.example.com/myimage`.  Use the `tag` parameter to specify the tag to push. 
    x_registry_auth = 'x_registry_auth_example' # str | A base64url-encoded auth configuration.  Refer to the [authentication section](#section/Authentication) for details. 
    tag = 'tag_example' # str | Tag of the image to push. For example, `latest`. If no tag is provided, all tags of the given image that are present in the local image store are pushed.  (optional)

    try:
        # Push an image
        api_instance.image_push(name, x_registry_auth, tag=tag)
    except Exception as e:
        print("Exception when calling ImageApi->image_push: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Name of the image to push. For example, &#x60;registry.example.com/myimage&#x60;. The image must be present in the local image store with the same name.  The name should be provided without tag; if a tag is provided, it is ignored. For example, &#x60;registry.example.com/myimage:latest&#x60; is considered equivalent to &#x60;registry.example.com/myimage&#x60;.  Use the &#x60;tag&#x60; parameter to specify the tag to push.  | 
 **x_registry_auth** | **str**| A base64url-encoded auth configuration.  Refer to the [authentication section](#section/Authentication) for details.  | 
 **tag** | **str**| Tag of the image to push. For example, &#x60;latest&#x60;. If no tag is provided, all tags of the given image that are present in the local image store are pushed.  | [optional] 

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
**404** | No such image |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **image_search**
> List[ImageSearchResponseItem] image_search(term, limit=limit, filters=filters)

Search images

Search for an image on Docker Hub.

### Example


```python
import docker_client.generated.docker_client.generated
from docker_client.generated.docker_client.generated.models.image_search_response_item import ImageSearchResponseItem
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
    api_instance = docker_client.generated.ImageApi(api_client)
    term = 'term_example' # str | Term to search
    limit = 56 # int | Maximum number of results to return (optional)
    filters = 'filters_example' # str | A JSON encoded value of the filters (a `map[string][]string`) to process on the images list. Available filters:  - `is-official=(true|false)` - `stars=<number>` Matches images that has at least 'number' stars.  (optional)

    try:
        # Search images
        api_response = api_instance.image_search(term, limit=limit, filters=filters)
        print("The response of ImageApi->image_search:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ImageApi->image_search: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **term** | **str**| Term to search | 
 **limit** | **int**| Maximum number of results to return | [optional] 
 **filters** | **str**| A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the images list. Available filters:  - &#x60;is-official&#x3D;(true|false)&#x60; - &#x60;stars&#x3D;&lt;number&gt;&#x60; Matches images that has at least &#39;number&#39; stars.  | [optional] 

### Return type

[**List[ImageSearchResponseItem]**](ImageSearchResponseItem.md)

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

# **image_tag**
> image_tag(name, repo=repo, tag=tag)

Tag an image

Tag an image so that it becomes part of a repository.

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
    api_instance = docker_client.generated.ImageApi(api_client)
    name = 'name_example' # str | Image name or ID to tag.
    repo = 'repo_example' # str | The repository to tag in. For example, `someuser/someimage`. (optional)
    tag = 'tag_example' # str | The name of the new tag. (optional)

    try:
        # Tag an image
        api_instance.image_tag(name, repo=repo, tag=tag)
    except Exception as e:
        print("Exception when calling ImageApi->image_tag: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| Image name or ID to tag. | 
 **repo** | **str**| The repository to tag in. For example, &#x60;someuser/someimage&#x60;. | [optional] 
 **tag** | **str**| The name of the new tag. | [optional] 

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
**201** | No error |  -  |
**400** | Bad parameter |  -  |
**404** | No such image |  -  |
**409** | Conflict |  -  |
**500** | Server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

