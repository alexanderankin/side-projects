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

from pydantic import BaseModel, ConfigDict, Field, StrictBool
from typing import Any, ClassVar, Dict, List, Optional
from docker_client.generated.models.task_spec_container_spec_privileges_app_armor import TaskSpecContainerSpecPrivilegesAppArmor
from docker_client.generated.models.task_spec_container_spec_privileges_credential_spec import TaskSpecContainerSpecPrivilegesCredentialSpec
from docker_client.generated.models.task_spec_container_spec_privileges_se_linux_context import TaskSpecContainerSpecPrivilegesSELinuxContext
from docker_client.generated.models.task_spec_container_spec_privileges_seccomp import TaskSpecContainerSpecPrivilegesSeccomp
from typing import Optional, Set
from typing_extensions import Self

class TaskSpecContainerSpecPrivileges(BaseModel):
    """
    Security options for the container
    """ # noqa: E501
    credential_spec: Optional[TaskSpecContainerSpecPrivilegesCredentialSpec] = Field(default=None, alias="CredentialSpec")
    se_linux_context: Optional[TaskSpecContainerSpecPrivilegesSELinuxContext] = Field(default=None, alias="SELinuxContext")
    seccomp: Optional[TaskSpecContainerSpecPrivilegesSeccomp] = Field(default=None, alias="Seccomp")
    app_armor: Optional[TaskSpecContainerSpecPrivilegesAppArmor] = Field(default=None, alias="AppArmor")
    no_new_privileges: Optional[StrictBool] = Field(default=None, description="Configuration of the no_new_privs bit in the container", alias="NoNewPrivileges")
    __properties: ClassVar[List[str]] = ["CredentialSpec", "SELinuxContext", "Seccomp", "AppArmor", "NoNewPrivileges"]

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
        """Create an instance of TaskSpecContainerSpecPrivileges from a JSON string"""
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
        # override the default output from pydantic by calling `to_dict()` of credential_spec
        if self.credential_spec:
            _dict['CredentialSpec'] = self.credential_spec.to_dict()
        # override the default output from pydantic by calling `to_dict()` of se_linux_context
        if self.se_linux_context:
            _dict['SELinuxContext'] = self.se_linux_context.to_dict()
        # override the default output from pydantic by calling `to_dict()` of seccomp
        if self.seccomp:
            _dict['Seccomp'] = self.seccomp.to_dict()
        # override the default output from pydantic by calling `to_dict()` of app_armor
        if self.app_armor:
            _dict['AppArmor'] = self.app_armor.to_dict()
        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of TaskSpecContainerSpecPrivileges from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "CredentialSpec": TaskSpecContainerSpecPrivilegesCredentialSpec.from_dict(obj["CredentialSpec"]) if obj.get("CredentialSpec") is not None else None,
            "SELinuxContext": TaskSpecContainerSpecPrivilegesSELinuxContext.from_dict(obj["SELinuxContext"]) if obj.get("SELinuxContext") is not None else None,
            "Seccomp": TaskSpecContainerSpecPrivilegesSeccomp.from_dict(obj["Seccomp"]) if obj.get("Seccomp") is not None else None,
            "AppArmor": TaskSpecContainerSpecPrivilegesAppArmor.from_dict(obj["AppArmor"]) if obj.get("AppArmor") is not None else None,
            "NoNewPrivileges": obj.get("NoNewPrivileges")
        })
        return _obj


