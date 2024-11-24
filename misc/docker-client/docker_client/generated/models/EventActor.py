from typing import *

from pydantic import BaseModel, Field


class EventActor(BaseModel):
    """
        None model
            Actor describes something that generates events, like a container, network,
    or a volume.


    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Attributes: Optional[Dict[str, Any]] = Field(alias="Attributes", default=None)
