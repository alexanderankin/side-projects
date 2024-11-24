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

from pydantic import BaseModel, ConfigDict, Field, StrictBool, StrictStr
from typing import Any, ClassVar, Dict, List, Optional
from docker_client.generated.models.config_reference import ConfigReference
from docker_client.generated.models.ipam import IPAM
from docker_client.generated.models.network_container import NetworkContainer
from docker_client.generated.models.peer_info import PeerInfo
from typing import Optional, Set
from typing_extensions import Self

class Network(BaseModel):
    """
    Network
    """ # noqa: E501
    name: Optional[StrictStr] = Field(default=None, description="Name of the network. ", alias="Name")
    id: Optional[StrictStr] = Field(default=None, description="ID that uniquely identifies a network on a single machine. ", alias="Id")
    created: Optional[StrictStr] = Field(default=None, description="Date and time at which the network was created in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds. ", alias="Created")
    scope: Optional[StrictStr] = Field(default=None, description="The level at which the network exists (e.g. `swarm` for cluster-wide or `local` for machine level) ", alias="Scope")
    driver: Optional[StrictStr] = Field(default=None, description="The name of the driver used to create the network (e.g. `bridge`, `overlay`). ", alias="Driver")
    enable_ipv6: Optional[StrictBool] = Field(default=None, description="Whether the network was created with IPv6 enabled. ", alias="EnableIPv6")
    ipam: Optional[IPAM] = Field(default=None, alias="IPAM")
    internal: Optional[StrictBool] = Field(default=False, description="Whether the network is created to only allow internal networking connectivity. ", alias="Internal")
    attachable: Optional[StrictBool] = Field(default=False, description="Whether a global / swarm scope network is manually attachable by regular containers from workers in swarm mode. ", alias="Attachable")
    ingress: Optional[StrictBool] = Field(default=False, description="Whether the network is providing the routing-mesh for the swarm cluster. ", alias="Ingress")
    config_from: Optional[ConfigReference] = Field(default=None, alias="ConfigFrom")
    config_only: Optional[StrictBool] = Field(default=False, description="Whether the network is a config-only network. Config-only networks are placeholder networks for network configurations to be used by other networks. Config-only networks cannot be used directly to run containers or services. ", alias="ConfigOnly")
    containers: Optional[Dict[str, NetworkContainer]] = Field(default=None, description="Contains endpoints attached to the network. ", alias="Containers")
    options: Optional[Dict[str, StrictStr]] = Field(default=None, description="Network-specific options uses when creating the network. ", alias="Options")
    labels: Optional[Dict[str, StrictStr]] = Field(default=None, description="User-defined key/value metadata.", alias="Labels")
    peers: Optional[List[PeerInfo]] = Field(default=None, description="List of peer nodes for an overlay network. This field is only present for overlay networks, and omitted for other network types. ", alias="Peers")
    __properties: ClassVar[List[str]] = ["Name", "Id", "Created", "Scope", "Driver", "EnableIPv6", "IPAM", "Internal", "Attachable", "Ingress", "ConfigFrom", "ConfigOnly", "Containers", "Options", "Labels", "Peers"]

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
        """Create an instance of Network from a JSON string"""
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
        # override the default output from pydantic by calling `to_dict()` of ipam
        if self.ipam:
            _dict['IPAM'] = self.ipam.to_dict()
        # override the default output from pydantic by calling `to_dict()` of config_from
        if self.config_from:
            _dict['ConfigFrom'] = self.config_from.to_dict()
        # override the default output from pydantic by calling `to_dict()` of each value in containers (dict)
        _field_dict = {}
        if self.containers:
            for _key_containers in self.containers:
                if self.containers[_key_containers]:
                    _field_dict[_key_containers] = self.containers[_key_containers].to_dict()
            _dict['Containers'] = _field_dict
        # override the default output from pydantic by calling `to_dict()` of each item in peers (list)
        _items = []
        if self.peers:
            for _item_peers in self.peers:
                if _item_peers:
                    _items.append(_item_peers.to_dict())
            _dict['Peers'] = _items
        # set to None if peers (nullable) is None
        # and model_fields_set contains the field
        if self.peers is None and "peers" in self.model_fields_set:
            _dict['Peers'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of Network from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "Name": obj.get("Name"),
            "Id": obj.get("Id"),
            "Created": obj.get("Created"),
            "Scope": obj.get("Scope"),
            "Driver": obj.get("Driver"),
            "EnableIPv6": obj.get("EnableIPv6"),
            "IPAM": IPAM.from_dict(obj["IPAM"]) if obj.get("IPAM") is not None else None,
            "Internal": obj.get("Internal") if obj.get("Internal") is not None else False,
            "Attachable": obj.get("Attachable") if obj.get("Attachable") is not None else False,
            "Ingress": obj.get("Ingress") if obj.get("Ingress") is not None else False,
            "ConfigFrom": ConfigReference.from_dict(obj["ConfigFrom"]) if obj.get("ConfigFrom") is not None else None,
            "ConfigOnly": obj.get("ConfigOnly") if obj.get("ConfigOnly") is not None else False,
            "Containers": dict(
                (_k, NetworkContainer.from_dict(_v))
                for _k, _v in obj["Containers"].items()
            )
            if obj.get("Containers") is not None
            else None,
            "Options": obj.get("Options"),
            "Labels": obj.get("Labels"),
            "Peers": [PeerInfo.from_dict(_item) for _item in obj["Peers"]] if obj.get("Peers") is not None else None
        })
        return _obj


