from typing import *

from pydantic import BaseModel, Field


class OCIPlatform(BaseModel):
    """
        None model
            Describes the platform which the image in the manifest runs on, as defined
    in the [OCI Image Index Specification](https://github.com/opencontainers/image-spec/blob/v1.0.1/image-index.md).


    """

    architecture: Optional[str] = Field(alias="architecture", default=None)

    os: Optional[str] = Field(alias="os", default=None)

    os.version: Optional[str] = Field(alias="os.version", default=None)

    os.features: Optional[List[str]] = Field(alias="os.features", default=None)

    variant: Optional[str] = Field(alias="variant", default=None)
