# ClusterVolumePublishStatusInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**node_id** | **str** | The ID of the Swarm node the volume is published on.  | [optional] 
**state** | **str** | The published state of the volume. * &#x60;pending-publish&#x60; The volume should be published to this node, but the call to the controller plugin to do so has not yet been successfully completed. * &#x60;published&#x60; The volume is published successfully to the node. * &#x60;pending-node-unpublish&#x60; The volume should be unpublished from the node, and the manager is awaiting confirmation from the worker that it has done so. * &#x60;pending-controller-unpublish&#x60; The volume is successfully unpublished from the node, but has not yet been successfully unpublished on the controller.  | [optional] 
**publish_context** | **Dict[str, str]** | A map of strings to strings returned by the CSI controller plugin when a volume is published.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.cluster_volume_publish_status_inner import ClusterVolumePublishStatusInner

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolumePublishStatusInner from a JSON string
cluster_volume_publish_status_inner_instance = ClusterVolumePublishStatusInner.from_json(json)
# print the JSON string representation of the object
print(ClusterVolumePublishStatusInner.to_json())

# convert the object into a dict
cluster_volume_publish_status_inner_dict = cluster_volume_publish_status_inner_instance.to_dict()
# create an instance of ClusterVolumePublishStatusInner from a dict
cluster_volume_publish_status_inner_from_dict = ClusterVolumePublishStatusInner.from_dict(cluster_volume_publish_status_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


