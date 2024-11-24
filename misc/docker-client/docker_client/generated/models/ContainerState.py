from typing import *

from pydantic import BaseModel, Field

from .Health import Health


class ContainerState(BaseModel):
    """
        None model
            ContainerState stores container&#39;s running state. It&#39;s part of ContainerJSONBase
    and will be returned by the &#34;inspect&#34; command.


    """

    Status: Optional[str] = Field(alias="Status", default=None)

    Running: Optional[bool] = Field(alias="Running", default=None)

    Paused: Optional[bool] = Field(alias="Paused", default=None)

    Restarting: Optional[bool] = Field(alias="Restarting", default=None)

    OOMKilled: Optional[bool] = Field(alias="OOMKilled", default=None)

    Dead: Optional[bool] = Field(alias="Dead", default=None)

    Pid: Optional[int] = Field(alias="Pid", default=None)

    ExitCode: Optional[int] = Field(alias="ExitCode", default=None)

    Error: Optional[str] = Field(alias="Error", default=None)

    StartedAt: Optional[str] = Field(alias="StartedAt", default=None)

    FinishedAt: Optional[str] = Field(alias="FinishedAt", default=None)

    Health: Optional[Health] = Field(alias="Health", default=None)
