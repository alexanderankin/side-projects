# HealthcheckResult

HealthcheckResult stores information about a single run of a healthcheck probe 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**start** | **datetime** | Date and time at which this check started in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  | [optional] 
**end** | **str** | Date and time at which this check ended in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  | [optional] 
**exit_code** | **int** | ExitCode meanings:  - &#x60;0&#x60; healthy - &#x60;1&#x60; unhealthy - &#x60;2&#x60; reserved (considered unhealthy) - other values: error running probe  | [optional] 
**output** | **str** | Output from last check | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.healthcheck_result import HealthcheckResult

# TODO update the JSON string below
json = "{}"
# create an instance of HealthcheckResult from a JSON string
healthcheck_result_instance = HealthcheckResult.from_json(json)
# print the JSON string representation of the object
print(HealthcheckResult.to_json())

# convert the object into a dict
healthcheck_result_dict = healthcheck_result_instance.to_dict()
# create an instance of HealthcheckResult from a dict
healthcheck_result_from_dict = HealthcheckResult.from_dict(healthcheck_result_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


