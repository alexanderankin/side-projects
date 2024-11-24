from enum import Enum


class Reachability(str, Enum):

    UNKNOWN = "unknown"
    UNREACHABLE = "unreachable"
    REACHABLE = "reachable"
