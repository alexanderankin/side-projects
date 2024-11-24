from typing import *

from pydantic import BaseModel, Field

from .EndpointSpec import EndpointSpec
from .NetworkAttachmentConfig import NetworkAttachmentConfig
from .TaskSpec import TaskSpec


class ServiceSpec(BaseModel):
    """
    None model
        User modifiable configuration for a service.

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    TaskTemplate: Optional[TaskSpec] = Field(alias="TaskTemplate", default=None)

    Mode: Optional[Dict[str, Any]] = Field(alias="Mode", default=None)

    UpdateConfig: Optional[Dict[str, Any]] = Field(alias="UpdateConfig", default=None)

    RollbackConfig: Optional[Dict[str, Any]] = Field(alias="RollbackConfig", default=None)

    Networks: Optional[List[Optional[NetworkAttachmentConfig]]] = Field(alias="Networks", default=None)

    EndpointSpec: Optional[EndpointSpec] = Field(alias="EndpointSpec", default=None)
