from typing import *

from pydantic import BaseModel, Field


class RegistryServiceConfig(BaseModel):
    """
    None model
        RegistryServiceConfig stores daemon registry services configuration.


    """

    AllowNondistributableArtifactsCIDRs: Optional[List[str]] = Field(
        alias="AllowNondistributableArtifactsCIDRs", default=None
    )

    AllowNondistributableArtifactsHostnames: Optional[List[str]] = Field(
        alias="AllowNondistributableArtifactsHostnames", default=None
    )

    InsecureRegistryCIDRs: Optional[List[str]] = Field(alias="InsecureRegistryCIDRs", default=None)

    IndexConfigs: Optional[Dict[str, Any]] = Field(alias="IndexConfigs", default=None)

    Mirrors: Optional[List[str]] = Field(alias="Mirrors", default=None)
