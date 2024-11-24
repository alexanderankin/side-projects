from typing import *

from pydantic import BaseModel, Field


class Runtime(BaseModel):
    """
        None model
            Runtime describes an [OCI compliant](https://github.com/opencontainers/runtime-spec)
    runtime.

    The runtime is invoked by the daemon via the `containerd` daemon. OCI
    runtimes act as an interface to the Linux kernel namespaces, cgroups,
    and SELinux.


    """

    path: Optional[str] = Field(alias="path", default=None)

    runtimeArgs: Optional[List[str]] = Field(alias="runtimeArgs", default=None)

    status: Optional[Dict[str, Any]] = Field(alias="status", default=None)
