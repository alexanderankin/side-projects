# coding: utf-8

"""
    Docker Engine API

    The Engine API is an HTTP API served by Docker Engine. It is the API the Docker client uses to communicate with the Engine, so everything the Docker client can do can be done with the API.  Most of the client's commands map directly to API endpoints (e.g. `docker ps` is `GET /containers/json`). The notable exception is running containers, which consists of several API calls.  # Errors  The API uses standard HTTP status codes to indicate the success or failure of the API call. The body of the response will be JSON in the following format:  ``` {   \"message\": \"page not found\" } ```  # Versioning  The API is usually changed in each release, so API calls are versioned to ensure that clients don't break. To lock to a specific version of the API, you prefix the URL with its version, for example, call `/v1.30/info` to use the v1.30 version of the `/info` endpoint. If the API version specified in the URL is not supported by the daemon, a HTTP `400 Bad Request` error message is returned.  If you omit the version-prefix, the current version of the API (v1.45) is used. For example, calling `/info` is the same as calling `/v1.45/info`. Using the API without a version-prefix is deprecated and will be removed in a future release.  Engine releases in the near future should support this version of the API, so your client will continue to work even if it is talking to a newer Engine.  The API uses an open schema model, which means server may add extra properties to responses. Likewise, the server will ignore any extra query parameters and request body properties. When you write clients, you need to ignore additional properties in responses to ensure they do not break when talking to newer daemons.   # Authentication  Authentication for registries is handled client side. The client has to send authentication details to various endpoints that need to communicate with registries, such as `POST /images/(name)/push`. These are sent as `X-Registry-Auth` header as a [base64url encoded](https://tools.ietf.org/html/rfc4648#section-5) (JSON) string with the following structure:  ``` {   \"username\": \"string\",   \"password\": \"string\",   \"email\": \"string\",   \"serveraddress\": \"string\" } ```  The `serveraddress` is a domain/IP without a protocol. Throughout this structure, double quotes are required.  If you have already got an identity token from the [`/auth` endpoint](#operation/SystemAuth), you can just pass this instead of credentials:  ``` {   \"identitytoken\": \"9cbaf023786cd7...\" } ``` 

    The version of the OpenAPI document: 1.45
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import pprint
import re  # noqa: F401
import json

from pydantic import BaseModel, ConfigDict, Field, StrictBool, StrictInt, StrictStr
from typing import Any, ClassVar, Dict, List, Optional
from typing_extensions import Annotated
from docker_client.generated.models.device_mapping import DeviceMapping
from docker_client.generated.models.device_request import DeviceRequest
from docker_client.generated.models.resources_blkio_weight_device_inner import ResourcesBlkioWeightDeviceInner
from docker_client.generated.models.resources_ulimits_inner import ResourcesUlimitsInner
from docker_client.generated.models.restart_policy import RestartPolicy
from docker_client.generated.models.throttle_device import ThrottleDevice
from typing import Optional, Set
from typing_extensions import Self

class ContainerUpdateRequest(BaseModel):
    """
    ContainerUpdateRequest
    """ # noqa: E501
    cpu_shares: Optional[StrictInt] = Field(default=None, description="An integer value representing this container's relative CPU weight versus other containers. ", alias="CpuShares")
    memory: Optional[StrictInt] = Field(default=0, description="Memory limit in bytes.", alias="Memory")
    cgroup_parent: Optional[StrictStr] = Field(default=None, description="Path to `cgroups` under which the container's `cgroup` is created. If the path is not absolute, the path is considered to be relative to the `cgroups` path of the init process. Cgroups are created if they do not already exist. ", alias="CgroupParent")
    blkio_weight: Optional[Annotated[int, Field(le=1000, strict=True, ge=0)]] = Field(default=None, description="Block IO weight (relative weight).", alias="BlkioWeight")
    blkio_weight_device: Optional[List[ResourcesBlkioWeightDeviceInner]] = Field(default=None, description="Block IO weight (relative device weight) in the form:  ``` [{\"Path\": \"device_path\", \"Weight\": weight}] ``` ", alias="BlkioWeightDevice")
    blkio_device_read_bps: Optional[List[ThrottleDevice]] = Field(default=None, description="Limit read rate (bytes per second) from a device, in the form:  ``` [{\"Path\": \"device_path\", \"Rate\": rate}] ``` ", alias="BlkioDeviceReadBps")
    blkio_device_write_bps: Optional[List[ThrottleDevice]] = Field(default=None, description="Limit write rate (bytes per second) to a device, in the form:  ``` [{\"Path\": \"device_path\", \"Rate\": rate}] ``` ", alias="BlkioDeviceWriteBps")
    blkio_device_read_i_ops: Optional[List[ThrottleDevice]] = Field(default=None, description="Limit read rate (IO per second) from a device, in the form:  ``` [{\"Path\": \"device_path\", \"Rate\": rate}] ``` ", alias="BlkioDeviceReadIOps")
    blkio_device_write_i_ops: Optional[List[ThrottleDevice]] = Field(default=None, description="Limit write rate (IO per second) to a device, in the form:  ``` [{\"Path\": \"device_path\", \"Rate\": rate}] ``` ", alias="BlkioDeviceWriteIOps")
    cpu_period: Optional[StrictInt] = Field(default=None, description="The length of a CPU period in microseconds.", alias="CpuPeriod")
    cpu_quota: Optional[StrictInt] = Field(default=None, description="Microseconds of CPU time that the container can get in a CPU period. ", alias="CpuQuota")
    cpu_realtime_period: Optional[StrictInt] = Field(default=None, description="The length of a CPU real-time period in microseconds. Set to 0 to allocate no time allocated to real-time tasks. ", alias="CpuRealtimePeriod")
    cpu_realtime_runtime: Optional[StrictInt] = Field(default=None, description="The length of a CPU real-time runtime in microseconds. Set to 0 to allocate no time allocated to real-time tasks. ", alias="CpuRealtimeRuntime")
    cpuset_cpus: Optional[StrictStr] = Field(default=None, description="CPUs in which to allow execution (e.g., `0-3`, `0,1`). ", alias="CpusetCpus")
    cpuset_mems: Optional[StrictStr] = Field(default=None, description="Memory nodes (MEMs) in which to allow execution (0-3, 0,1). Only effective on NUMA systems. ", alias="CpusetMems")
    devices: Optional[List[DeviceMapping]] = Field(default=None, description="A list of devices to add to the container.", alias="Devices")
    device_cgroup_rules: Optional[List[StrictStr]] = Field(default=None, description="a list of cgroup rules to apply to the container", alias="DeviceCgroupRules")
    device_requests: Optional[List[DeviceRequest]] = Field(default=None, description="A list of requests for devices to be sent to device drivers. ", alias="DeviceRequests")
    kernel_memory_tcp: Optional[StrictInt] = Field(default=None, description="Hard limit for kernel TCP buffer memory (in bytes). Depending on the OCI runtime in use, this option may be ignored. It is no longer supported by the default (runc) runtime.  This field is omitted when empty. ", alias="KernelMemoryTCP")
    memory_reservation: Optional[StrictInt] = Field(default=None, description="Memory soft limit in bytes.", alias="MemoryReservation")
    memory_swap: Optional[StrictInt] = Field(default=None, description="Total memory limit (memory + swap). Set as `-1` to enable unlimited swap. ", alias="MemorySwap")
    memory_swappiness: Optional[Annotated[int, Field(le=100, strict=True, ge=0)]] = Field(default=None, description="Tune a container's memory swappiness behavior. Accepts an integer between 0 and 100. ", alias="MemorySwappiness")
    nano_cpus: Optional[StrictInt] = Field(default=None, description="CPU quota in units of 10<sup>-9</sup> CPUs.", alias="NanoCpus")
    oom_kill_disable: Optional[StrictBool] = Field(default=None, description="Disable OOM Killer for the container.", alias="OomKillDisable")
    init: Optional[StrictBool] = Field(default=None, description="Run an init inside the container that forwards signals and reaps processes. This field is omitted if empty, and the default (as configured on the daemon) is used. ", alias="Init")
    pids_limit: Optional[StrictInt] = Field(default=None, description="Tune a container's PIDs limit. Set `0` or `-1` for unlimited, or `null` to not change. ", alias="PidsLimit")
    ulimits: Optional[List[ResourcesUlimitsInner]] = Field(default=None, description="A list of resource limits to set in the container. For example:  ``` {\"Name\": \"nofile\", \"Soft\": 1024, \"Hard\": 2048} ``` ", alias="Ulimits")
    cpu_count: Optional[StrictInt] = Field(default=None, description="The number of usable CPUs (Windows only).  On Windows Server containers, the processor resource controls are mutually exclusive. The order of precedence is `CPUCount` first, then `CPUShares`, and `CPUPercent` last. ", alias="CpuCount")
    cpu_percent: Optional[StrictInt] = Field(default=None, description="The usable percentage of the available CPUs (Windows only).  On Windows Server containers, the processor resource controls are mutually exclusive. The order of precedence is `CPUCount` first, then `CPUShares`, and `CPUPercent` last. ", alias="CpuPercent")
    io_maximum_i_ops: Optional[StrictInt] = Field(default=None, description="Maximum IOps for the container system drive (Windows only)", alias="IOMaximumIOps")
    io_maximum_bandwidth: Optional[StrictInt] = Field(default=None, description="Maximum IO in bytes per second for the container system drive (Windows only). ", alias="IOMaximumBandwidth")
    restart_policy: Optional[RestartPolicy] = Field(default=None, alias="RestartPolicy")
    __properties: ClassVar[List[str]] = ["CpuShares", "Memory", "CgroupParent", "BlkioWeight", "BlkioWeightDevice", "BlkioDeviceReadBps", "BlkioDeviceWriteBps", "BlkioDeviceReadIOps", "BlkioDeviceWriteIOps", "CpuPeriod", "CpuQuota", "CpuRealtimePeriod", "CpuRealtimeRuntime", "CpusetCpus", "CpusetMems", "Devices", "DeviceCgroupRules", "DeviceRequests", "KernelMemoryTCP", "MemoryReservation", "MemorySwap", "MemorySwappiness", "NanoCpus", "OomKillDisable", "Init", "PidsLimit", "Ulimits", "CpuCount", "CpuPercent", "IOMaximumIOps", "IOMaximumBandwidth", "RestartPolicy"]

    model_config = ConfigDict(
        populate_by_name=True,
        validate_assignment=True,
        protected_namespaces=(),
    )


    def to_str(self) -> str:
        """Returns the string representation of the model using alias"""
        return pprint.pformat(self.model_dump(by_alias=True))

    def to_json(self) -> str:
        """Returns the JSON representation of the model using alias"""
        # TODO: pydantic v2: use .model_dump_json(by_alias=True, exclude_unset=True) instead
        return json.dumps(self.to_dict())

    @classmethod
    def from_json(cls, json_str: str) -> Optional[Self]:
        """Create an instance of ContainerUpdateRequest from a JSON string"""
        return cls.from_dict(json.loads(json_str))

    def to_dict(self) -> Dict[str, Any]:
        """Return the dictionary representation of the model using alias.

        This has the following differences from calling pydantic's
        `self.model_dump(by_alias=True)`:

        * `None` is only added to the output dict for nullable fields that
          were set at model initialization. Other fields with value `None`
          are ignored.
        """
        excluded_fields: Set[str] = set([
        ])

        _dict = self.model_dump(
            by_alias=True,
            exclude=excluded_fields,
            exclude_none=True,
        )
        # override the default output from pydantic by calling `to_dict()` of each item in blkio_weight_device (list)
        _items = []
        if self.blkio_weight_device:
            for _item_blkio_weight_device in self.blkio_weight_device:
                if _item_blkio_weight_device:
                    _items.append(_item_blkio_weight_device.to_dict())
            _dict['BlkioWeightDevice'] = _items
        # override the default output from pydantic by calling `to_dict()` of each item in blkio_device_read_bps (list)
        _items = []
        if self.blkio_device_read_bps:
            for _item_blkio_device_read_bps in self.blkio_device_read_bps:
                if _item_blkio_device_read_bps:
                    _items.append(_item_blkio_device_read_bps.to_dict())
            _dict['BlkioDeviceReadBps'] = _items
        # override the default output from pydantic by calling `to_dict()` of each item in blkio_device_write_bps (list)
        _items = []
        if self.blkio_device_write_bps:
            for _item_blkio_device_write_bps in self.blkio_device_write_bps:
                if _item_blkio_device_write_bps:
                    _items.append(_item_blkio_device_write_bps.to_dict())
            _dict['BlkioDeviceWriteBps'] = _items
        # override the default output from pydantic by calling `to_dict()` of each item in blkio_device_read_i_ops (list)
        _items = []
        if self.blkio_device_read_i_ops:
            for _item_blkio_device_read_i_ops in self.blkio_device_read_i_ops:
                if _item_blkio_device_read_i_ops:
                    _items.append(_item_blkio_device_read_i_ops.to_dict())
            _dict['BlkioDeviceReadIOps'] = _items
        # override the default output from pydantic by calling `to_dict()` of each item in blkio_device_write_i_ops (list)
        _items = []
        if self.blkio_device_write_i_ops:
            for _item_blkio_device_write_i_ops in self.blkio_device_write_i_ops:
                if _item_blkio_device_write_i_ops:
                    _items.append(_item_blkio_device_write_i_ops.to_dict())
            _dict['BlkioDeviceWriteIOps'] = _items
        # override the default output from pydantic by calling `to_dict()` of each item in devices (list)
        _items = []
        if self.devices:
            for _item_devices in self.devices:
                if _item_devices:
                    _items.append(_item_devices.to_dict())
            _dict['Devices'] = _items
        # override the default output from pydantic by calling `to_dict()` of each item in device_requests (list)
        _items = []
        if self.device_requests:
            for _item_device_requests in self.device_requests:
                if _item_device_requests:
                    _items.append(_item_device_requests.to_dict())
            _dict['DeviceRequests'] = _items
        # override the default output from pydantic by calling `to_dict()` of each item in ulimits (list)
        _items = []
        if self.ulimits:
            for _item_ulimits in self.ulimits:
                if _item_ulimits:
                    _items.append(_item_ulimits.to_dict())
            _dict['Ulimits'] = _items
        # override the default output from pydantic by calling `to_dict()` of restart_policy
        if self.restart_policy:
            _dict['RestartPolicy'] = self.restart_policy.to_dict()
        # set to None if init (nullable) is None
        # and model_fields_set contains the field
        if self.init is None and "init" in self.model_fields_set:
            _dict['Init'] = None

        # set to None if pids_limit (nullable) is None
        # and model_fields_set contains the field
        if self.pids_limit is None and "pids_limit" in self.model_fields_set:
            _dict['PidsLimit'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of ContainerUpdateRequest from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "CpuShares": obj.get("CpuShares"),
            "Memory": obj.get("Memory") if obj.get("Memory") is not None else 0,
            "CgroupParent": obj.get("CgroupParent"),
            "BlkioWeight": obj.get("BlkioWeight"),
            "BlkioWeightDevice": [ResourcesBlkioWeightDeviceInner.from_dict(_item) for _item in obj["BlkioWeightDevice"]] if obj.get("BlkioWeightDevice") is not None else None,
            "BlkioDeviceReadBps": [ThrottleDevice.from_dict(_item) for _item in obj["BlkioDeviceReadBps"]] if obj.get("BlkioDeviceReadBps") is not None else None,
            "BlkioDeviceWriteBps": [ThrottleDevice.from_dict(_item) for _item in obj["BlkioDeviceWriteBps"]] if obj.get("BlkioDeviceWriteBps") is not None else None,
            "BlkioDeviceReadIOps": [ThrottleDevice.from_dict(_item) for _item in obj["BlkioDeviceReadIOps"]] if obj.get("BlkioDeviceReadIOps") is not None else None,
            "BlkioDeviceWriteIOps": [ThrottleDevice.from_dict(_item) for _item in obj["BlkioDeviceWriteIOps"]] if obj.get("BlkioDeviceWriteIOps") is not None else None,
            "CpuPeriod": obj.get("CpuPeriod"),
            "CpuQuota": obj.get("CpuQuota"),
            "CpuRealtimePeriod": obj.get("CpuRealtimePeriod"),
            "CpuRealtimeRuntime": obj.get("CpuRealtimeRuntime"),
            "CpusetCpus": obj.get("CpusetCpus"),
            "CpusetMems": obj.get("CpusetMems"),
            "Devices": [DeviceMapping.from_dict(_item) for _item in obj["Devices"]] if obj.get("Devices") is not None else None,
            "DeviceCgroupRules": obj.get("DeviceCgroupRules"),
            "DeviceRequests": [DeviceRequest.from_dict(_item) for _item in obj["DeviceRequests"]] if obj.get("DeviceRequests") is not None else None,
            "KernelMemoryTCP": obj.get("KernelMemoryTCP"),
            "MemoryReservation": obj.get("MemoryReservation"),
            "MemorySwap": obj.get("MemorySwap"),
            "MemorySwappiness": obj.get("MemorySwappiness"),
            "NanoCpus": obj.get("NanoCpus"),
            "OomKillDisable": obj.get("OomKillDisable"),
            "Init": obj.get("Init"),
            "PidsLimit": obj.get("PidsLimit"),
            "Ulimits": [ResourcesUlimitsInner.from_dict(_item) for _item in obj["Ulimits"]] if obj.get("Ulimits") is not None else None,
            "CpuCount": obj.get("CpuCount"),
            "CpuPercent": obj.get("CpuPercent"),
            "IOMaximumIOps": obj.get("IOMaximumIOps"),
            "IOMaximumBandwidth": obj.get("IOMaximumBandwidth"),
            "RestartPolicy": RestartPolicy.from_dict(obj["RestartPolicy"]) if obj.get("RestartPolicy") is not None else None
        })
        return _obj


