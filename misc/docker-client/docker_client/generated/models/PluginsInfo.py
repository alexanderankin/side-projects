from typing import *

from pydantic import BaseModel, Field


class PluginsInfo(BaseModel):
    """
        None model
            Available plugins per type.

    &lt;p&gt;&lt;br /&gt;&lt;/p&gt;

    &gt; **Note**: Only unmanaged (V1) plugins are included in this list.
    &gt; V1 plugins are &#34;lazily&#34; loaded, and are not returned in this list
    &gt; if there is no resource using the plugin.


    """

    Volume: Optional[List[str]] = Field(alias="Volume", default=None)

    Network: Optional[List[str]] = Field(alias="Network", default=None)

    Authorization: Optional[List[str]] = Field(alias="Authorization", default=None)

    Log: Optional[List[str]] = Field(alias="Log", default=None)
