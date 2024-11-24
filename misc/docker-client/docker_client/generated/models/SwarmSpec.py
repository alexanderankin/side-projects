from typing import *

from pydantic import BaseModel, Field


class SwarmSpec(BaseModel):
    """
    None model
        User modifiable swarm configuration.

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    Orchestration: Optional[Dict[str, Any]] = Field(alias="Orchestration", default=None)

    Raft: Optional[Dict[str, Any]] = Field(alias="Raft", default=None)

    Dispatcher: Optional[Dict[str, Any]] = Field(alias="Dispatcher", default=None)

    CAConfig: Optional[Dict[str, Any]] = Field(alias="CAConfig", default=None)

    EncryptionConfig: Optional[Dict[str, Any]] = Field(alias="EncryptionConfig", default=None)

    TaskDefaults: Optional[Dict[str, Any]] = Field(alias="TaskDefaults", default=None)
