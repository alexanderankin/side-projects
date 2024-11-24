# HealthConfig

A test to perform to check that the container is healthy.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**test** | **List[str]** | The test to perform. Possible values are:  - &#x60;[]&#x60; inherit healthcheck from image or parent image - &#x60;[\&quot;NONE\&quot;]&#x60; disable healthcheck - &#x60;[\&quot;CMD\&quot;, args...]&#x60; exec arguments directly - &#x60;[\&quot;CMD-SHELL\&quot;, command]&#x60; run command with system&#39;s default shell  | [optional] 
**interval** | **int** | The time to wait between checks in nanoseconds. It should be 0 or at least 1000000 (1 ms). 0 means inherit.  | [optional] 
**timeout** | **int** | The time to wait before considering the check to have hung. It should be 0 or at least 1000000 (1 ms). 0 means inherit.  | [optional] 
**retries** | **int** | The number of consecutive failures needed to consider a container as unhealthy. 0 means inherit.  | [optional] 
**start_period** | **int** | Start period for the container to initialize before starting health-retries countdown in nanoseconds. It should be 0 or at least 1000000 (1 ms). 0 means inherit.  | [optional] 
**start_interval** | **int** | The time to wait between checks in nanoseconds during the start period. It should be 0 or at least 1000000 (1 ms). 0 means inherit.  | [optional] 

## Example

```python
from docker_client.generated.models.health_config import HealthConfig

# TODO update the JSON string below
json = "{}"
# create an instance of HealthConfig from a JSON string
health_config_instance = HealthConfig.from_json(json)
# print the JSON string representation of the object
print(HealthConfig.to_json())

# convert the object into a dict
health_config_dict = health_config_instance.to_dict()
# create an instance of HealthConfig from a dict
health_config_from_dict = HealthConfig.from_dict(health_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


