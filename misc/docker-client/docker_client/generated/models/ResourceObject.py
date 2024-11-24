from typing import *

from pydantic import BaseModel, Field

from .GenericResources import GenericResources


class ResourceObject(BaseModel):
    """
        None model
            An object describing the resources which can be advertised by a node and
    requested by a task.


    """

    NanoCPUs: Optional[int] = Field(alias="NanoCPUs", default=None)

    MemoryBytes: Optional[int] = Field(alias="MemoryBytes", default=None)

    GenericResources: Optional[GenericResources] = Field(alias="GenericResources", default=None)
