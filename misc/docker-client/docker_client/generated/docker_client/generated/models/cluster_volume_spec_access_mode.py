# coding: utf-8

"""
    Docker Engine API

    The Engine API is an HTTP API served by Docker Engine. It is the API the Docker client uses to communicate with the Engine, so everything the Docker client can do can be done with the API.  Most of the client's commands map directly to API endpoints (e.g. `docker ps` is `GET /containers/json`). The notable exception is running containers, which consists of several API calls.  # Errors  The API uses standard HTTP status codes to indicate the success or failure of the API call. The body of the response will be JSON in the following format:  ``` {   \"message\": \"page not found\" } ```  # Versioning  The API is usually changed in each release, so API calls are versioned to ensure that clients don't break. To lock to a specific version of the API, you prefix the URL with its version, for example, call `/v1.30/info` to use the v1.30 version of the `/info` endpoint. If the API version specified in the URL is not supported by the daemon, a HTTP `400 Bad Request` error message is returned.  If you omit the version-prefix, the current version of the API (v1.45) is used. For example, calling `/info` is the same as calling `/v1.45/info`. Using the API without a version-prefix is deprecated and will be removed in a future release.  Engine releases in the near future should support this version of the API, so your client will continue to work even if it is talking to a newer Engine.  The API uses an open schema model, which means server may add extra properties to responses. Likewise, the server will ignore any extra query parameters and request body properties. When you write clients, you need to ignore additional properties in responses to ensure they do not break when talking to newer daemons.   # Authentication  Authentication for registries is handled client side. The client has to send authentication details to various endpoints that need to communicate with registries, such as `POST /images/(name)/push`. These are sent as `X-Registry-Auth` header as a [base64url encoded](https://tools.ietf.org/html/rfc4648#section-5) (JSON) string with the following structure:  ``` {   \"username\": \"string\",   \"password\": \"string\",   \"email\": \"string\",   \"serveraddress\": \"string\" } ```  The `serveraddress` is a domain/IP without a protocol. Throughout this structure, double quotes are required.  If you have already got an identity token from the [`/auth` endpoint](#operation/SystemAuth), you can just pass this instead of credentials:  ``` {   \"identitytoken\": \"9cbaf023786cd7...\" } ``` 

    The version of the OpenAPI document: 1.45
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import pprint
import re  # noqa: F401
import json

from pydantic import BaseModel, ConfigDict, Field, StrictStr, field_validator
from typing import Any, ClassVar, Dict, List, Optional
from docker_client.generated.docker_client.generated.models.cluster_volume_spec_access_mode_accessibility_requirements import ClusterVolumeSpecAccessModeAccessibilityRequirements
from docker_client.generated.docker_client.generated.models.cluster_volume_spec_access_mode_capacity_range import ClusterVolumeSpecAccessModeCapacityRange
from docker_client.generated.docker_client.generated.models.cluster_volume_spec_access_mode_secrets_inner import ClusterVolumeSpecAccessModeSecretsInner
from typing import Optional, Set
from typing_extensions import Self

class ClusterVolumeSpecAccessMode(BaseModel):
    """
    Defines how the volume is used by tasks. 
    """ # noqa: E501
    scope: Optional[StrictStr] = Field(default='single', description="The set of nodes this volume can be used on at one time. - `single` The volume may only be scheduled to one node at a time. - `multi` the volume may be scheduled to any supported number of nodes at a time. ", alias="Scope")
    sharing: Optional[StrictStr] = Field(default='none', description="The number and way that different tasks can use this volume at one time. - `none` The volume may only be used by one task at a time. - `readonly` The volume may be used by any number of tasks, but they all must mount the volume as readonly - `onewriter` The volume may be used by any number of tasks, but only one may mount it as read/write. - `all` The volume may have any number of readers and writers. ", alias="Sharing")
    mount_volume: Optional[Dict[str, Any]] = Field(default=None, description="Options for using this volume as a Mount-type volume.      Either MountVolume or BlockVolume, but not both, must be     present.   properties:     FsType:       type: \"string\"       description: |         Specifies the filesystem type for the mount volume.         Optional.     MountFlags:       type: \"array\"       description: |         Flags to pass when mounting the volume. Optional.       items:         type: \"string\" BlockVolume:   type: \"object\"   description: |     Options for using this volume as a Block-type volume.     Intentionally empty. ", alias="MountVolume")
    secrets: Optional[List[ClusterVolumeSpecAccessModeSecretsInner]] = Field(default=None, description="Swarm Secrets that are passed to the CSI storage plugin when operating on this volume. ", alias="Secrets")
    accessibility_requirements: Optional[ClusterVolumeSpecAccessModeAccessibilityRequirements] = Field(default=None, alias="AccessibilityRequirements")
    capacity_range: Optional[ClusterVolumeSpecAccessModeCapacityRange] = Field(default=None, alias="CapacityRange")
    availability: Optional[StrictStr] = Field(default='active', description="The availability of the volume for use in tasks. - `active` The volume is fully available for scheduling on the cluster - `pause` No new workloads should use the volume, but existing workloads are not stopped. - `drain` All workloads using this volume should be stopped and rescheduled, and no new ones should be started. ", alias="Availability")
    __properties: ClassVar[List[str]] = ["Scope", "Sharing", "MountVolume", "Secrets", "AccessibilityRequirements", "CapacityRange", "Availability"]

    @field_validator('scope')
    def scope_validate_enum(cls, value):
        """Validates the enum"""
        if value is None:
            return value

        if value not in set(['single', 'multi']):
            raise ValueError("must be one of enum values ('single', 'multi')")
        return value

    @field_validator('sharing')
    def sharing_validate_enum(cls, value):
        """Validates the enum"""
        if value is None:
            return value

        if value not in set(['none', 'readonly', 'onewriter', 'all']):
            raise ValueError("must be one of enum values ('none', 'readonly', 'onewriter', 'all')")
        return value

    @field_validator('availability')
    def availability_validate_enum(cls, value):
        """Validates the enum"""
        if value is None:
            return value

        if value not in set(['active', 'pause', 'drain']):
            raise ValueError("must be one of enum values ('active', 'pause', 'drain')")
        return value

    model_config = ConfigDict(
        populate_by_name=True,
        validate_assignment=True,
        protected_namespaces=(),
    )


    def to_str(self) -> str:
        """Returns the string representation of the model using alias"""
        return pprint.pformat(self.model_dump(by_alias=True))

    def to_json(self) -> str:
        """Returns the JSON representation of the model using alias"""
        # TODO: pydantic v2: use .model_dump_json(by_alias=True, exclude_unset=True) instead
        return json.dumps(self.to_dict())

    @classmethod
    def from_json(cls, json_str: str) -> Optional[Self]:
        """Create an instance of ClusterVolumeSpecAccessMode from a JSON string"""
        return cls.from_dict(json.loads(json_str))

    def to_dict(self) -> Dict[str, Any]:
        """Return the dictionary representation of the model using alias.

        This has the following differences from calling pydantic's
        `self.model_dump(by_alias=True)`:

        * `None` is only added to the output dict for nullable fields that
          were set at model initialization. Other fields with value `None`
          are ignored.
        """
        excluded_fields: Set[str] = set([
        ])

        _dict = self.model_dump(
            by_alias=True,
            exclude=excluded_fields,
            exclude_none=True,
        )
        # override the default output from pydantic by calling `to_dict()` of each item in secrets (list)
        _items = []
        if self.secrets:
            for _item_secrets in self.secrets:
                if _item_secrets:
                    _items.append(_item_secrets.to_dict())
            _dict['Secrets'] = _items
        # override the default output from pydantic by calling `to_dict()` of accessibility_requirements
        if self.accessibility_requirements:
            _dict['AccessibilityRequirements'] = self.accessibility_requirements.to_dict()
        # override the default output from pydantic by calling `to_dict()` of capacity_range
        if self.capacity_range:
            _dict['CapacityRange'] = self.capacity_range.to_dict()
        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of ClusterVolumeSpecAccessMode from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "Scope": obj.get("Scope") if obj.get("Scope") is not None else 'single',
            "Sharing": obj.get("Sharing") if obj.get("Sharing") is not None else 'none',
            "MountVolume": obj.get("MountVolume"),
            "Secrets": [ClusterVolumeSpecAccessModeSecretsInner.from_dict(_item) for _item in obj["Secrets"]] if obj.get("Secrets") is not None else None,
            "AccessibilityRequirements": ClusterVolumeSpecAccessModeAccessibilityRequirements.from_dict(obj["AccessibilityRequirements"]) if obj.get("AccessibilityRequirements") is not None else None,
            "CapacityRange": ClusterVolumeSpecAccessModeCapacityRange.from_dict(obj["CapacityRange"]) if obj.get("CapacityRange") is not None else None,
            "Availability": obj.get("Availability") if obj.get("Availability") is not None else 'active'
        })
        return _obj


