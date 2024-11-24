from enum import Enum


class TaskState(str, Enum):

    NEW = "new"
    ALLOCATED = "allocated"
    PENDING = "pending"
    ASSIGNED = "assigned"
    ACCEPTED = "accepted"
    PREPARING = "preparing"
    READY = "ready"
    STARTING = "starting"
    RUNNING = "running"
    COMPLETE = "complete"
    SHUTDOWN = "shutdown"
    FAILED = "failed"
    REJECTED = "rejected"
    REMOVE = "remove"
    ORPHANED = "orphaned"
