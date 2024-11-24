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

from pydantic import BaseModel, ConfigDict, Field, StrictStr
from typing import Any, ClassVar, Dict, List, Optional
from docker_client.generated.docker_client.generated.models.endpoint_spec import EndpointSpec
from docker_client.generated.docker_client.generated.models.network_attachment_config import NetworkAttachmentConfig
from docker_client.generated.docker_client.generated.models.service_spec_mode import ServiceSpecMode
from docker_client.generated.docker_client.generated.models.service_spec_rollback_config import ServiceSpecRollbackConfig
from docker_client.generated.docker_client.generated.models.service_spec_update_config import ServiceSpecUpdateConfig
from docker_client.generated.docker_client.generated.models.task_spec import TaskSpec
from typing import Optional, Set
from typing_extensions import Self

class ServiceUpdateRequest(BaseModel):
    """
    ServiceUpdateRequest
    """ # noqa: E501
    name: Optional[StrictStr] = Field(default=None, description="Name of the service.", alias="Name")
    labels: Optional[Dict[str, StrictStr]] = Field(default=None, description="User-defined key/value metadata.", alias="Labels")
    task_template: Optional[TaskSpec] = Field(default=None, alias="TaskTemplate")
    mode: Optional[ServiceSpecMode] = Field(default=None, alias="Mode")
    update_config: Optional[ServiceSpecUpdateConfig] = Field(default=None, alias="UpdateConfig")
    rollback_config: Optional[ServiceSpecRollbackConfig] = Field(default=None, alias="RollbackConfig")
    networks: Optional[List[NetworkAttachmentConfig]] = Field(default=None, description="Specifies which networks the service should attach to.  Deprecated: This field is deprecated since v1.44. The Networks field in TaskSpec should be used instead. ", alias="Networks")
    endpoint_spec: Optional[EndpointSpec] = Field(default=None, alias="EndpointSpec")
    __properties: ClassVar[List[str]] = ["Name", "Labels", "TaskTemplate", "Mode", "UpdateConfig", "RollbackConfig", "Networks", "EndpointSpec"]

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
        """Create an instance of ServiceUpdateRequest from a JSON string"""
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
        # override the default output from pydantic by calling `to_dict()` of task_template
        if self.task_template:
            _dict['TaskTemplate'] = self.task_template.to_dict()
        # override the default output from pydantic by calling `to_dict()` of mode
        if self.mode:
            _dict['Mode'] = self.mode.to_dict()
        # override the default output from pydantic by calling `to_dict()` of update_config
        if self.update_config:
            _dict['UpdateConfig'] = self.update_config.to_dict()
        # override the default output from pydantic by calling `to_dict()` of rollback_config
        if self.rollback_config:
            _dict['RollbackConfig'] = self.rollback_config.to_dict()
        # override the default output from pydantic by calling `to_dict()` of each item in networks (list)
        _items = []
        if self.networks:
            for _item_networks in self.networks:
                if _item_networks:
                    _items.append(_item_networks.to_dict())
            _dict['Networks'] = _items
        # override the default output from pydantic by calling `to_dict()` of endpoint_spec
        if self.endpoint_spec:
            _dict['EndpointSpec'] = self.endpoint_spec.to_dict()
        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of ServiceUpdateRequest from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "Name": obj.get("Name"),
            "Labels": obj.get("Labels"),
            "TaskTemplate": TaskSpec.from_dict(obj["TaskTemplate"]) if obj.get("TaskTemplate") is not None else None,
            "Mode": ServiceSpecMode.from_dict(obj["Mode"]) if obj.get("Mode") is not None else None,
            "UpdateConfig": ServiceSpecUpdateConfig.from_dict(obj["UpdateConfig"]) if obj.get("UpdateConfig") is not None else None,
            "RollbackConfig": ServiceSpecRollbackConfig.from_dict(obj["RollbackConfig"]) if obj.get("RollbackConfig") is not None else None,
            "Networks": [NetworkAttachmentConfig.from_dict(_item) for _item in obj["Networks"]] if obj.get("Networks") is not None else None,
            "EndpointSpec": EndpointSpec.from_dict(obj["EndpointSpec"]) if obj.get("EndpointSpec") is not None else None
        })
        return _obj


