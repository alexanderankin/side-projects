# coding: utf-8

"""
    Docker Engine API

    The Engine API is an HTTP API served by Docker Engine. It is the API the Docker client uses to communicate with the Engine, so everything the Docker client can do can be done with the API.  Most of the client's commands map directly to API endpoints (e.g. `docker ps` is `GET /containers/json`). The notable exception is running containers, which consists of several API calls.  # Errors  The API uses standard HTTP status codes to indicate the success or failure of the API call. The body of the response will be JSON in the following format:  ``` {   \"message\": \"page not found\" } ```  # Versioning  The API is usually changed in each release, so API calls are versioned to ensure that clients don't break. To lock to a specific version of the API, you prefix the URL with its version, for example, call `/v1.30/info` to use the v1.30 version of the `/info` endpoint. If the API version specified in the URL is not supported by the daemon, a HTTP `400 Bad Request` error message is returned.  If you omit the version-prefix, the current version of the API (v1.45) is used. For example, calling `/info` is the same as calling `/v1.45/info`. Using the API without a version-prefix is deprecated and will be removed in a future release.  Engine releases in the near future should support this version of the API, so your client will continue to work even if it is talking to a newer Engine.  The API uses an open schema model, which means server may add extra properties to responses. Likewise, the server will ignore any extra query parameters and request body properties. When you write clients, you need to ignore additional properties in responses to ensure they do not break when talking to newer daemons.   # Authentication  Authentication for registries is handled client side. The client has to send authentication details to various endpoints that need to communicate with registries, such as `POST /images/(name)/push`. These are sent as `X-Registry-Auth` header as a [base64url encoded](https://tools.ietf.org/html/rfc4648#section-5) (JSON) string with the following structure:  ``` {   \"username\": \"string\",   \"password\": \"string\",   \"email\": \"string\",   \"serveraddress\": \"string\" } ```  The `serveraddress` is a domain/IP without a protocol. Throughout this structure, double quotes are required.  If you have already got an identity token from the [`/auth` endpoint](#operation/SystemAuth), you can just pass this instead of credentials:  ``` {   \"identitytoken\": \"9cbaf023786cd7...\" } ``` 

    The version of the OpenAPI document: 1.45
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from docker_client.generated.api.plugin_api import PluginApi


class TestPluginApi(unittest.TestCase):
    """PluginApi unit test stubs"""

    def setUp(self) -> None:
        self.api = PluginApi()

    def tearDown(self) -> None:
        pass

    def test_get_plugin_privileges(self) -> None:
        """Test case for get_plugin_privileges

        Get plugin privileges
        """
        pass

    def test_plugin_create(self) -> None:
        """Test case for plugin_create

        Create a plugin
        """
        pass

    def test_plugin_delete(self) -> None:
        """Test case for plugin_delete

        Remove a plugin
        """
        pass

    def test_plugin_disable(self) -> None:
        """Test case for plugin_disable

        Disable a plugin
        """
        pass

    def test_plugin_enable(self) -> None:
        """Test case for plugin_enable

        Enable a plugin
        """
        pass

    def test_plugin_inspect(self) -> None:
        """Test case for plugin_inspect

        Inspect a plugin
        """
        pass

    def test_plugin_list(self) -> None:
        """Test case for plugin_list

        List plugins
        """
        pass

    def test_plugin_pull(self) -> None:
        """Test case for plugin_pull

        Install a plugin
        """
        pass

    def test_plugin_push(self) -> None:
        """Test case for plugin_push

        Push a plugin
        """
        pass

    def test_plugin_set(self) -> None:
        """Test case for plugin_set

        Configure a plugin
        """
        pass

    def test_plugin_upgrade(self) -> None:
        """Test case for plugin_upgrade

        Upgrade a plugin
        """
        pass


if __name__ == '__main__':
    unittest.main()
