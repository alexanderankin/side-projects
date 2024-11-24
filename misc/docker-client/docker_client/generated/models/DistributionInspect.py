from typing import *

from pydantic import BaseModel, Field

from .OCIDescriptor import OCIDescriptor
from .OCIPlatform import OCIPlatform


class DistributionInspect(BaseModel):
    """
        DistributionInspectResponse model
            Describes the result obtained from contacting the registry to retrieve
    image metadata.


    """

    Descriptor: OCIDescriptor = Field(alias="Descriptor")

    Platforms: List[OCIPlatform] = Field(alias="Platforms")
