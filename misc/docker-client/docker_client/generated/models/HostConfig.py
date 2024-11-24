from typing import *

from pydantic import BaseModel, Field


class HostConfig(BaseModel):
    """
    None model
        Container configuration that depends on the host we are running on

    """
