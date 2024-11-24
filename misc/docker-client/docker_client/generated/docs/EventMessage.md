# EventMessage

EventMessage represents the information an event contains. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | **str** | The type of object emitting the event | [optional] 
**action** | **str** | The type of event | [optional] 
**actor** | [**EventActor**](EventActor.md) |  | [optional] 
**scope** | **str** | Scope of the event. Engine events are &#x60;local&#x60; scope. Cluster (Swarm) events are &#x60;swarm&#x60; scope.  | [optional] 
**time** | **int** | Timestamp of event | [optional] 
**time_nano** | **int** | Timestamp of event, with nanosecond accuracy | [optional] 

## Example

```python
from docker_client.generated.models.event_message import EventMessage

# TODO update the JSON string below
json = "{}"
# create an instance of EventMessage from a JSON string
event_message_instance = EventMessage.from_json(json)
# print the JSON string representation of the object
print(EventMessage.to_json())

# convert the object into a dict
event_message_dict = event_message_instance.to_dict()
# create an instance of EventMessage from a dict
event_message_from_dict = EventMessage.from_dict(event_message_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


