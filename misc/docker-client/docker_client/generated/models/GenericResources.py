from typing import *

from pydantic import BaseModel, Field


class GenericResources(BaseModel):
    """
        None model
            User-defined resources can be either Integer resources (e.g, `SSD=3`) or
    String resources (e.g, `GPU=UUID1`).


    """
