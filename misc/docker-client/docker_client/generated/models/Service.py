from typing import *

from pydantic import BaseModel, Field

from .ObjectVersion import ObjectVersion
from .ServiceSpec import ServiceSpec


class Service(BaseModel):
    """
    None model

    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Version: Optional[ObjectVersion] = Field(alias="Version", default=None)

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    UpdatedAt: Optional[str] = Field(alias="UpdatedAt", default=None)

    Spec: Optional[ServiceSpec] = Field(alias="Spec", default=None)

    Endpoint: Optional[Dict[str, Any]] = Field(alias="Endpoint", default=None)

    UpdateStatus: Optional[Dict[str, Any]] = Field(alias="UpdateStatus", default=None)

    ServiceStatus: Optional[Dict[str, Any]] = Field(alias="ServiceStatus", default=None)

    JobStatus: Optional[Dict[str, Any]] = Field(alias="JobStatus", default=None)
