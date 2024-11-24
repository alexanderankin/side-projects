from typing import *

from pydantic import BaseModel, Field


class OCIDescriptor(BaseModel):
    """
        None model
            A descriptor struct containing digest, media type, and size, as defined in
    the [OCI Content Descriptors Specification](https://github.com/opencontainers/image-spec/blob/v1.0.1/descriptor.md).


    """

    mediaType: Optional[str] = Field(alias="mediaType", default=None)

    digest: Optional[str] = Field(alias="digest", default=None)

    size: Optional[int] = Field(alias="size", default=None)
