from typing import *

from pydantic import BaseModel, Field


class SystemVersion(BaseModel):
    """
    None model
        Response of Engine API: GET &#34;/version&#34;


    """

    Platform: Optional[Dict[str, Any]] = Field(alias="Platform", default=None)

    Components: Optional[List[Dict[str, Any]]] = Field(alias="Components", default=None)

    Version: Optional[str] = Field(alias="Version", default=None)

    ApiVersion: Optional[str] = Field(alias="ApiVersion", default=None)

    MinAPIVersion: Optional[str] = Field(alias="MinAPIVersion", default=None)

    GitCommit: Optional[str] = Field(alias="GitCommit", default=None)

    GoVersion: Optional[str] = Field(alias="GoVersion", default=None)

    Os: Optional[str] = Field(alias="Os", default=None)

    Arch: Optional[str] = Field(alias="Arch", default=None)

    KernelVersion: Optional[str] = Field(alias="KernelVersion", default=None)

    Experimental: Optional[bool] = Field(alias="Experimental", default=None)

    BuildTime: Optional[str] = Field(alias="BuildTime", default=None)
