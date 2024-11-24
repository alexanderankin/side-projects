from typing import *

from pydantic import BaseModel, Field

from .HealthConfig import HealthConfig


class ContainerConfig(BaseModel):
    """
    None model
        Configuration for a container that is portable between hosts.


    """

    Hostname: Optional[str] = Field(alias="Hostname", default=None)

    Domainname: Optional[str] = Field(alias="Domainname", default=None)

    User: Optional[str] = Field(alias="User", default=None)

    AttachStdin: Optional[bool] = Field(alias="AttachStdin", default=None)

    AttachStdout: Optional[bool] = Field(alias="AttachStdout", default=None)

    AttachStderr: Optional[bool] = Field(alias="AttachStderr", default=None)

    ExposedPorts: Optional[Dict[str, Any]] = Field(alias="ExposedPorts", default=None)

    Tty: Optional[bool] = Field(alias="Tty", default=None)

    OpenStdin: Optional[bool] = Field(alias="OpenStdin", default=None)

    StdinOnce: Optional[bool] = Field(alias="StdinOnce", default=None)

    Env: Optional[List[str]] = Field(alias="Env", default=None)

    Cmd: Optional[List[str]] = Field(alias="Cmd", default=None)

    Healthcheck: Optional[HealthConfig] = Field(alias="Healthcheck", default=None)

    ArgsEscaped: Optional[bool] = Field(alias="ArgsEscaped", default=None)

    Image: Optional[str] = Field(alias="Image", default=None)

    Volumes: Optional[Dict[str, Any]] = Field(alias="Volumes", default=None)

    WorkingDir: Optional[str] = Field(alias="WorkingDir", default=None)

    Entrypoint: Optional[List[str]] = Field(alias="Entrypoint", default=None)

    NetworkDisabled: Optional[bool] = Field(alias="NetworkDisabled", default=None)

    MacAddress: Optional[str] = Field(alias="MacAddress", default=None)

    OnBuild: Optional[List[str]] = Field(alias="OnBuild", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    StopSignal: Optional[str] = Field(alias="StopSignal", default=None)

    StopTimeout: Optional[int] = Field(alias="StopTimeout", default=None)

    Shell: Optional[List[str]] = Field(alias="Shell", default=None)
