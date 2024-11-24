from enum import Enum


class NodeState(str, Enum):

    UNKNOWN = "unknown"
    DOWN = "down"
    READY = "ready"
    DISCONNECTED = "disconnected"
