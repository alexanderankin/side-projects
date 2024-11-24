# Health

Health stores information about the container's healthcheck results. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **str** | Status is one of &#x60;none&#x60;, &#x60;starting&#x60;, &#x60;healthy&#x60; or &#x60;unhealthy&#x60;  - \&quot;none\&quot;      Indicates there is no healthcheck - \&quot;starting\&quot;  Starting indicates that the container is not yet ready - \&quot;healthy\&quot;   Healthy indicates that the container is running correctly - \&quot;unhealthy\&quot; Unhealthy indicates that the container has a problem  | [optional] 
**failing_streak** | **int** | FailingStreak is the number of consecutive failures | [optional] 
**log** | [**List[HealthcheckResult]**](HealthcheckResult.md) | Log contains the last few results (oldest first)  | [optional] 

## Example

```python
from docker_client.generated.models.health import Health

# TODO update the JSON string below
json = "{}"
# create an instance of Health from a JSON string
health_instance = Health.from_json(json)
# print the JSON string representation of the object
print(Health.to_json())

# convert the object into a dict
health_dict = health_instance.to_dict()
# create an instance of Health from a dict
health_from_dict = Health.from_dict(health_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


