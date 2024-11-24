from typing import *

from pydantic import BaseModel, Field

from .NetworkAttachmentConfig import NetworkAttachmentConfig


class TaskSpec(BaseModel):
    """
    None model
        User modifiable task configuration.

    """

    PluginSpec: Optional[Dict[str, Any]] = Field(alias="PluginSpec", default=None)

    ContainerSpec: Optional[Dict[str, Any]] = Field(alias="ContainerSpec", default=None)

    NetworkAttachmentSpec: Optional[Dict[str, Any]] = Field(alias="NetworkAttachmentSpec", default=None)

    Resources: Optional[Dict[str, Any]] = Field(alias="Resources", default=None)

    RestartPolicy: Optional[Dict[str, Any]] = Field(alias="RestartPolicy", default=None)

    Placement: Optional[Dict[str, Any]] = Field(alias="Placement", default=None)

    ForceUpdate: Optional[int] = Field(alias="ForceUpdate", default=None)

    Runtime: Optional[str] = Field(alias="Runtime", default=None)

    Networks: Optional[List[Optional[NetworkAttachmentConfig]]] = Field(alias="Networks", default=None)

    LogDriver: Optional[Dict[str, Any]] = Field(alias="LogDriver", default=None)
