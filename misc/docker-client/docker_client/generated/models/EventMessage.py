from typing import *

from pydantic import BaseModel, Field

from .EventActor import EventActor


class EventMessage(BaseModel):
    """
    SystemEventsResponse model
        EventMessage represents the information an event contains.


    """

    Type: Optional[str] = Field(alias="Type", default=None)

    Action: Optional[str] = Field(alias="Action", default=None)

    Actor: Optional[EventActor] = Field(alias="Actor", default=None)

    scope: Optional[str] = Field(alias="scope", default=None)

    time: Optional[int] = Field(alias="time", default=None)

    timeNano: Optional[int] = Field(alias="timeNano", default=None)
