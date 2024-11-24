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
from typing_extensions import Annotated
from typing import Optional, Set
from typing_extensions import Self

class ExecConfig(BaseModel):
    """
    ExecConfig
    """ # noqa: E501
    attach_stdin: Optional[StrictBool] = Field(default=None, description="Attach to `stdin` of the exec command.", alias="AttachStdin")
    attach_stdout: Optional[StrictBool] = Field(default=None, description="Attach to `stdout` of the exec command.", alias="AttachStdout")
    attach_stderr: Optional[StrictBool] = Field(default=None, description="Attach to `stderr` of the exec command.", alias="AttachStderr")
    console_size: Optional[Annotated[List[Annotated[int, Field(strict=True, ge=0)]], Field(min_length=2, max_length=2)]] = Field(default=None, description="Initial console size, as an `[height, width]` array.", alias="ConsoleSize")
    detach_keys: Optional[StrictStr] = Field(default=None, description="Override the key sequence for detaching a container. Format is a single character `[a-Z]` or `ctrl-<value>` where `<value>` is one of: `a-z`, `@`, `^`, `[`, `,` or `_`. ", alias="DetachKeys")
    tty: Optional[StrictBool] = Field(default=None, description="Allocate a pseudo-TTY.", alias="Tty")
    env: Optional[List[StrictStr]] = Field(default=None, description="A list of environment variables in the form `[\"VAR=value\", ...]`. ", alias="Env")
    cmd: Optional[List[StrictStr]] = Field(default=None, description="Command to run, as a string or array of strings.", alias="Cmd")
    privileged: Optional[StrictBool] = Field(default=False, description="Runs the exec process with extended privileges.", alias="Privileged")
    user: Optional[StrictStr] = Field(default=None, description="The user, and optionally, group to run the exec process inside the container. Format is one of: `user`, `user:group`, `uid`, or `uid:gid`. ", alias="User")
    working_dir: Optional[StrictStr] = Field(default=None, description="The working directory for the exec process inside the container. ", alias="WorkingDir")
    __properties: ClassVar[List[str]] = ["AttachStdin", "AttachStdout", "AttachStderr", "ConsoleSize", "DetachKeys", "Tty", "Env", "Cmd", "Privileged", "User", "WorkingDir"]

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
        """Create an instance of ExecConfig from a JSON string"""
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
        # set to None if console_size (nullable) is None
        # and model_fields_set contains the field
        if self.console_size is None and "console_size" in self.model_fields_set:
            _dict['ConsoleSize'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of ExecConfig from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "AttachStdin": obj.get("AttachStdin"),
            "AttachStdout": obj.get("AttachStdout"),
            "AttachStderr": obj.get("AttachStderr"),
            "ConsoleSize": obj.get("ConsoleSize"),
            "DetachKeys": obj.get("DetachKeys"),
            "Tty": obj.get("Tty"),
            "Env": obj.get("Env"),
            "Cmd": obj.get("Cmd"),
            "Privileged": obj.get("Privileged") if obj.get("Privileged") is not None else False,
            "User": obj.get("User"),
            "WorkingDir": obj.get("WorkingDir")
        })
        return _obj


