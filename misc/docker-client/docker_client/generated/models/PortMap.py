from typing import *

from pydantic import BaseModel, Field


class PortMap(BaseModel):
    """
        None model
            PortMap describes the mapping of container ports to host ports, using the
    container&#39;s port-number and protocol as key in the format `&lt;port&gt;/&lt;protocol&gt;`,
    for example, `80/udp`.

    If a container&#39;s port is mapped for multiple protocols, separate entries
    are added to the mapping table.


    """
