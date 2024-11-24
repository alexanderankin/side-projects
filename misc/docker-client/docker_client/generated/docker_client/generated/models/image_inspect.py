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

from pydantic import BaseModel, ConfigDict, Field, StrictInt, StrictStr
from typing import Any, ClassVar, Dict, List, Optional
from docker_client.generated.models.graph_driver_data import GraphDriverData
from docker_client.generated.models.image_config import ImageConfig
from docker_client.generated.models.image_inspect_metadata import ImageInspectMetadata
from docker_client.generated.models.image_inspect_root_fs import ImageInspectRootFS
from typing import Optional, Set
from typing_extensions import Self

class ImageInspect(BaseModel):
    """
    Information about an image in the local image cache. 
    """ # noqa: E501
    id: Optional[StrictStr] = Field(default=None, description="ID is the content-addressable ID of an image.  This identifier is a content-addressable digest calculated from the image's configuration (which includes the digests of layers used by the image).  Note that this digest differs from the `RepoDigests` below, which holds digests of image manifests that reference the image. ", alias="Id")
    repo_tags: Optional[List[StrictStr]] = Field(default=None, description="List of image names/tags in the local image cache that reference this image.  Multiple image tags can refer to the same image, and this list may be empty if no tags reference the image, in which case the image is \"untagged\", in which case it can still be referenced by its ID. ", alias="RepoTags")
    repo_digests: Optional[List[StrictStr]] = Field(default=None, description="List of content-addressable digests of locally available image manifests that the image is referenced from. Multiple manifests can refer to the same image.  These digests are usually only available if the image was either pulled from a registry, or if the image was pushed to a registry, which is when the manifest is generated and its digest calculated. ", alias="RepoDigests")
    parent: Optional[StrictStr] = Field(default=None, description="ID of the parent image.  Depending on how the image was created, this field may be empty and is only set for images that were built/created locally. This field is empty if the image was pulled from an image registry. ", alias="Parent")
    comment: Optional[StrictStr] = Field(default=None, description="Optional message that was set when committing or importing the image. ", alias="Comment")
    created: Optional[StrictStr] = Field(default=None, description="Date and time at which the image was created, formatted in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  This information is only available if present in the image, and omitted otherwise. ", alias="Created")
    docker_version: Optional[StrictStr] = Field(default=None, description="The version of Docker that was used to build the image.  Depending on how the image was created, this field may be empty. ", alias="DockerVersion")
    author: Optional[StrictStr] = Field(default=None, description="Name of the author that was specified when committing the image, or as specified through MAINTAINER (deprecated) in the Dockerfile. ", alias="Author")
    config: Optional[ImageConfig] = Field(default=None, alias="Config")
    architecture: Optional[StrictStr] = Field(default=None, description="Hardware CPU architecture that the image runs on. ", alias="Architecture")
    variant: Optional[StrictStr] = Field(default=None, description="CPU architecture variant (presently ARM-only). ", alias="Variant")
    os: Optional[StrictStr] = Field(default=None, description="Operating System the image is built to run on. ", alias="Os")
    os_version: Optional[StrictStr] = Field(default=None, description="Operating System version the image is built to run on (especially for Windows). ", alias="OsVersion")
    size: Optional[StrictInt] = Field(default=None, description="Total size of the image including all layers it is composed of. ", alias="Size")
    virtual_size: Optional[StrictInt] = Field(default=None, description="Total size of the image including all layers it is composed of.  Deprecated: this field is omitted in API v1.44, but kept for backward compatibility. Use Size instead. ", alias="VirtualSize")
    graph_driver: Optional[GraphDriverData] = Field(default=None, alias="GraphDriver")
    root_fs: Optional[ImageInspectRootFS] = Field(default=None, alias="RootFS")
    metadata: Optional[ImageInspectMetadata] = Field(default=None, alias="Metadata")
    __properties: ClassVar[List[str]] = ["Id", "RepoTags", "RepoDigests", "Parent", "Comment", "Created", "DockerVersion", "Author", "Config", "Architecture", "Variant", "Os", "OsVersion", "Size", "VirtualSize", "GraphDriver", "RootFS", "Metadata"]

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
        """Create an instance of ImageInspect from a JSON string"""
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
        # override the default output from pydantic by calling `to_dict()` of config
        if self.config:
            _dict['Config'] = self.config.to_dict()
        # override the default output from pydantic by calling `to_dict()` of graph_driver
        if self.graph_driver:
            _dict['GraphDriver'] = self.graph_driver.to_dict()
        # override the default output from pydantic by calling `to_dict()` of root_fs
        if self.root_fs:
            _dict['RootFS'] = self.root_fs.to_dict()
        # override the default output from pydantic by calling `to_dict()` of metadata
        if self.metadata:
            _dict['Metadata'] = self.metadata.to_dict()
        # set to None if created (nullable) is None
        # and model_fields_set contains the field
        if self.created is None and "created" in self.model_fields_set:
            _dict['Created'] = None

        # set to None if variant (nullable) is None
        # and model_fields_set contains the field
        if self.variant is None and "variant" in self.model_fields_set:
            _dict['Variant'] = None

        # set to None if os_version (nullable) is None
        # and model_fields_set contains the field
        if self.os_version is None and "os_version" in self.model_fields_set:
            _dict['OsVersion'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of ImageInspect from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "Id": obj.get("Id"),
            "RepoTags": obj.get("RepoTags"),
            "RepoDigests": obj.get("RepoDigests"),
            "Parent": obj.get("Parent"),
            "Comment": obj.get("Comment"),
            "Created": obj.get("Created"),
            "DockerVersion": obj.get("DockerVersion"),
            "Author": obj.get("Author"),
            "Config": ImageConfig.from_dict(obj["Config"]) if obj.get("Config") is not None else None,
            "Architecture": obj.get("Architecture"),
            "Variant": obj.get("Variant"),
            "Os": obj.get("Os"),
            "OsVersion": obj.get("OsVersion"),
            "Size": obj.get("Size"),
            "VirtualSize": obj.get("VirtualSize"),
            "GraphDriver": GraphDriverData.from_dict(obj["GraphDriver"]) if obj.get("GraphDriver") is not None else None,
            "RootFS": ImageInspectRootFS.from_dict(obj["RootFS"]) if obj.get("RootFS") is not None else None,
            "Metadata": ImageInspectMetadata.from_dict(obj["Metadata"]) if obj.get("Metadata") is not None else None
        })
        return _obj


