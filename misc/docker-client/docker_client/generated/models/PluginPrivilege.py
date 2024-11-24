from typing import *

from pydantic import BaseModel, Field


class PluginPrivilege(BaseModel):
    """
        None model
            Describes a permission the user has to accept upon installing
    the plugin.


    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Description: Optional[str] = Field(alias="Description", default=None)

    Value: Optional[List[str]] = Field(alias="Value", default=None)
