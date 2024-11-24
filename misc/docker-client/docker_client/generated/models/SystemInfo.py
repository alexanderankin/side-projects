from typing import *

from pydantic import BaseModel, Field

from .Commit import Commit
from .GenericResources import GenericResources
from .PluginsInfo import PluginsInfo
from .RegistryServiceConfig import RegistryServiceConfig
from .SwarmInfo import SwarmInfo


class SystemInfo(BaseModel):
    """
    None model

    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Containers: Optional[int] = Field(alias="Containers", default=None)

    ContainersRunning: Optional[int] = Field(alias="ContainersRunning", default=None)

    ContainersPaused: Optional[int] = Field(alias="ContainersPaused", default=None)

    ContainersStopped: Optional[int] = Field(alias="ContainersStopped", default=None)

    Images: Optional[int] = Field(alias="Images", default=None)

    Driver: Optional[str] = Field(alias="Driver", default=None)

    DriverStatus: Optional[List[List[str]]] = Field(alias="DriverStatus", default=None)

    DockerRootDir: Optional[str] = Field(alias="DockerRootDir", default=None)

    Plugins: Optional[PluginsInfo] = Field(alias="Plugins", default=None)

    MemoryLimit: Optional[bool] = Field(alias="MemoryLimit", default=None)

    SwapLimit: Optional[bool] = Field(alias="SwapLimit", default=None)

    KernelMemoryTCP: Optional[bool] = Field(alias="KernelMemoryTCP", default=None)

    CpuCfsPeriod: Optional[bool] = Field(alias="CpuCfsPeriod", default=None)

    CpuCfsQuota: Optional[bool] = Field(alias="CpuCfsQuota", default=None)

    CPUShares: Optional[bool] = Field(alias="CPUShares", default=None)

    CPUSet: Optional[bool] = Field(alias="CPUSet", default=None)

    PidsLimit: Optional[bool] = Field(alias="PidsLimit", default=None)

    OomKillDisable: Optional[bool] = Field(alias="OomKillDisable", default=None)

    IPv4Forwarding: Optional[bool] = Field(alias="IPv4Forwarding", default=None)

    BridgeNfIptables: Optional[bool] = Field(alias="BridgeNfIptables", default=None)

    BridgeNfIp6tables: Optional[bool] = Field(alias="BridgeNfIp6tables", default=None)

    Debug: Optional[bool] = Field(alias="Debug", default=None)

    NFd: Optional[int] = Field(alias="NFd", default=None)

    NGoroutines: Optional[int] = Field(alias="NGoroutines", default=None)

    SystemTime: Optional[str] = Field(alias="SystemTime", default=None)

    LoggingDriver: Optional[str] = Field(alias="LoggingDriver", default=None)

    CgroupDriver: Optional[str] = Field(alias="CgroupDriver", default=None)

    CgroupVersion: Optional[str] = Field(alias="CgroupVersion", default=None)

    NEventsListener: Optional[int] = Field(alias="NEventsListener", default=None)

    KernelVersion: Optional[str] = Field(alias="KernelVersion", default=None)

    OperatingSystem: Optional[str] = Field(alias="OperatingSystem", default=None)

    OSVersion: Optional[str] = Field(alias="OSVersion", default=None)

    OSType: Optional[str] = Field(alias="OSType", default=None)

    Architecture: Optional[str] = Field(alias="Architecture", default=None)

    NCPU: Optional[int] = Field(alias="NCPU", default=None)

    MemTotal: Optional[int] = Field(alias="MemTotal", default=None)

    IndexServerAddress: Optional[str] = Field(alias="IndexServerAddress", default=None)

    RegistryConfig: Optional[RegistryServiceConfig] = Field(alias="RegistryConfig", default=None)

    GenericResources: Optional[GenericResources] = Field(alias="GenericResources", default=None)

    HttpProxy: Optional[str] = Field(alias="HttpProxy", default=None)

    HttpsProxy: Optional[str] = Field(alias="HttpsProxy", default=None)

    NoProxy: Optional[str] = Field(alias="NoProxy", default=None)

    Name: Optional[str] = Field(alias="Name", default=None)

    Labels: Optional[List[str]] = Field(alias="Labels", default=None)

    ExperimentalBuild: Optional[bool] = Field(alias="ExperimentalBuild", default=None)

    ServerVersion: Optional[str] = Field(alias="ServerVersion", default=None)

    Runtimes: Optional[Dict[str, Any]] = Field(alias="Runtimes", default=None)

    DefaultRuntime: Optional[str] = Field(alias="DefaultRuntime", default=None)

    Swarm: Optional[SwarmInfo] = Field(alias="Swarm", default=None)

    LiveRestoreEnabled: Optional[bool] = Field(alias="LiveRestoreEnabled", default=None)

    Isolation: Optional[str] = Field(alias="Isolation", default=None)

    InitBinary: Optional[str] = Field(alias="InitBinary", default=None)

    ContainerdCommit: Optional[Commit] = Field(alias="ContainerdCommit", default=None)

    RuncCommit: Optional[Commit] = Field(alias="RuncCommit", default=None)

    InitCommit: Optional[Commit] = Field(alias="InitCommit", default=None)

    SecurityOptions: Optional[List[str]] = Field(alias="SecurityOptions", default=None)

    ProductLicense: Optional[str] = Field(alias="ProductLicense", default=None)

    DefaultAddressPools: Optional[List[Dict[str, Any]]] = Field(alias="DefaultAddressPools", default=None)

    Warnings: Optional[List[str]] = Field(alias="Warnings", default=None)

    CDISpecDirs: Optional[List[str]] = Field(alias="CDISpecDirs", default=None)
