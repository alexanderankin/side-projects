from typing import *

from pydantic import BaseModel, Field

from .EngineDescription import EngineDescription
from .Platform import Platform
from .ResourceObject import ResourceObject
from .TLSInfo import TLSInfo


class NodeDescription(BaseModel):
    """
        None model
            NodeDescription encapsulates the properties of the Node as reported by the
    agent.


    """

    Hostname: Optional[str] = Field(alias="Hostname", default=None)

    Platform: Optional[Platform] = Field(alias="Platform", default=None)

    Resources: Optional[ResourceObject] = Field(alias="Resources", default=None)

    Engine: Optional[EngineDescription] = Field(alias="Engine", default=None)

    TLSInfo: Optional[TLSInfo] = Field(alias="TLSInfo", default=None)
