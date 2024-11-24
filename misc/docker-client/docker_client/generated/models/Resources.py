from typing import *

from pydantic import BaseModel, Field

from .DeviceMapping import DeviceMapping
from .DeviceRequest import DeviceRequest
from .ThrottleDevice import ThrottleDevice


class Resources(BaseModel):
    """
    None model
        A container&#39;s resources (cgroups config, ulimits, etc)

    """

    CpuShares: Optional[int] = Field(alias="CpuShares", default=None)

    Memory: Optional[int] = Field(alias="Memory", default=None)

    CgroupParent: Optional[str] = Field(alias="CgroupParent", default=None)

    BlkioWeight: Optional[int] = Field(alias="BlkioWeight", default=None)

    BlkioWeightDevice: Optional[List[Dict[str, Any]]] = Field(alias="BlkioWeightDevice", default=None)

    BlkioDeviceReadBps: Optional[List[Optional[ThrottleDevice]]] = Field(alias="BlkioDeviceReadBps", default=None)

    BlkioDeviceWriteBps: Optional[List[Optional[ThrottleDevice]]] = Field(alias="BlkioDeviceWriteBps", default=None)

    BlkioDeviceReadIOps: Optional[List[Optional[ThrottleDevice]]] = Field(alias="BlkioDeviceReadIOps", default=None)

    BlkioDeviceWriteIOps: Optional[List[Optional[ThrottleDevice]]] = Field(alias="BlkioDeviceWriteIOps", default=None)

    CpuPeriod: Optional[int] = Field(alias="CpuPeriod", default=None)

    CpuQuota: Optional[int] = Field(alias="CpuQuota", default=None)

    CpuRealtimePeriod: Optional[int] = Field(alias="CpuRealtimePeriod", default=None)

    CpuRealtimeRuntime: Optional[int] = Field(alias="CpuRealtimeRuntime", default=None)

    CpusetCpus: Optional[str] = Field(alias="CpusetCpus", default=None)

    CpusetMems: Optional[str] = Field(alias="CpusetMems", default=None)

    Devices: Optional[List[Optional[DeviceMapping]]] = Field(alias="Devices", default=None)

    DeviceCgroupRules: Optional[List[str]] = Field(alias="DeviceCgroupRules", default=None)

    DeviceRequests: Optional[List[Optional[DeviceRequest]]] = Field(alias="DeviceRequests", default=None)

    KernelMemoryTCP: Optional[int] = Field(alias="KernelMemoryTCP", default=None)

    MemoryReservation: Optional[int] = Field(alias="MemoryReservation", default=None)

    MemorySwap: Optional[int] = Field(alias="MemorySwap", default=None)

    MemorySwappiness: Optional[int] = Field(alias="MemorySwappiness", default=None)

    NanoCpus: Optional[int] = Field(alias="NanoCpus", default=None)

    OomKillDisable: Optional[bool] = Field(alias="OomKillDisable", default=None)

    Init: Optional[bool] = Field(alias="Init", default=None)

    PidsLimit: Optional[int] = Field(alias="PidsLimit", default=None)

    Ulimits: Optional[List[Dict[str, Any]]] = Field(alias="Ulimits", default=None)

    CpuCount: Optional[int] = Field(alias="CpuCount", default=None)

    CpuPercent: Optional[int] = Field(alias="CpuPercent", default=None)

    IOMaximumIOps: Optional[int] = Field(alias="IOMaximumIOps", default=None)

    IOMaximumBandwidth: Optional[int] = Field(alias="IOMaximumBandwidth", default=None)
