# EventActor

Actor describes something that generates events, like a container, network, or a volume. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The ID of the object emitting the event | [optional] 
**attributes** | **Dict[str, str]** | Various key/value attributes of the object, depending on its type.  | [optional] 

## Example

```python
from docker_client.generated.models.event_actor import EventActor

# TODO update the JSON string below
json = "{}"
# create an instance of EventActor from a JSON string
event_actor_instance = EventActor.from_json(json)
# print the JSON string representation of the object
print(EventActor.to_json())

# convert the object into a dict
event_actor_dict = event_actor_instance.to_dict()
# create an instance of EventActor from a dict
event_actor_from_dict = EventActor.from_dict(event_actor_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


