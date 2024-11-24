from typing import *

from pydantic import BaseModel, Field


class Commit(BaseModel):
    """
        None model
            Commit holds the Git-commit (SHA1) that a binary was built from, as
    reported in the version-string of external tools, such as `containerd`,
    or `runC`.


    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Expected: Optional[str] = Field(alias="Expected", default=None)
