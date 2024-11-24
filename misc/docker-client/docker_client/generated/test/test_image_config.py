# coding: utf-8

"""
    Docker Engine API

    The Engine API is an HTTP API served by Docker Engine. It is the API the Docker client uses to communicate with the Engine, so everything the Docker client can do can be done with the API.  Most of the client's commands map directly to API endpoints (e.g. `docker ps` is `GET /containers/json`). The notable exception is running containers, which consists of several API calls.  # Errors  The API uses standard HTTP status codes to indicate the success or failure of the API call. The body of the response will be JSON in the following format:  ``` {   \"message\": \"page not found\" } ```  # Versioning  The API is usually changed in each release, so API calls are versioned to ensure that clients don't break. To lock to a specific version of the API, you prefix the URL with its version, for example, call `/v1.30/info` to use the v1.30 version of the `/info` endpoint. If the API version specified in the URL is not supported by the daemon, a HTTP `400 Bad Request` error message is returned.  If you omit the version-prefix, the current version of the API (v1.45) is used. For example, calling `/info` is the same as calling `/v1.45/info`. Using the API without a version-prefix is deprecated and will be removed in a future release.  Engine releases in the near future should support this version of the API, so your client will continue to work even if it is talking to a newer Engine.  The API uses an open schema model, which means server may add extra properties to responses. Likewise, the server will ignore any extra query parameters and request body properties. When you write clients, you need to ignore additional properties in responses to ensure they do not break when talking to newer daemons.   # Authentication  Authentication for registries is handled client side. The client has to send authentication details to various endpoints that need to communicate with registries, such as `POST /images/(name)/push`. These are sent as `X-Registry-Auth` header as a [base64url encoded](https://tools.ietf.org/html/rfc4648#section-5) (JSON) string with the following structure:  ``` {   \"username\": \"string\",   \"password\": \"string\",   \"email\": \"string\",   \"serveraddress\": \"string\" } ```  The `serveraddress` is a domain/IP without a protocol. Throughout this structure, double quotes are required.  If you have already got an identity token from the [`/auth` endpoint](#operation/SystemAuth), you can just pass this instead of credentials:  ``` {   \"identitytoken\": \"9cbaf023786cd7...\" } ``` 

    The version of the OpenAPI document: 1.45
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from docker_client.generated.models.image_config import ImageConfig

class TestImageConfig(unittest.TestCase):
    """ImageConfig unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> ImageConfig:
        """Test ImageConfig
            include_optional is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `ImageConfig`
        """
        model = ImageConfig()
        if include_optional:
            return ImageConfig(
                hostname = '',
                domainname = '',
                user = 'web:web',
                attach_stdin = False,
                attach_stdout = False,
                attach_stderr = False,
                exposed_ports = {"80/tcp":{},"443/tcp":{}},
                tty = False,
                open_stdin = False,
                stdin_once = False,
                env = ["PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
                cmd = ["/bin/sh"],
                healthcheck = docker_client.generated.models.health_config.HealthConfig(
                    test = [
                        ''
                        ], 
                    interval = 56, 
                    timeout = 56, 
                    retries = 56, 
                    start_period = 56, 
                    start_interval = 56, ),
                args_escaped = False,
                image = '',
                volumes = {"/app/data":{},"/app/config":{}},
                working_dir = '/public/',
                entrypoint = [],
                network_disabled = False,
                mac_address = '',
                on_build = [],
                labels = {"com.example.some-label":"some-value","com.example.some-other-label":"some-other-value"},
                stop_signal = 'SIGTERM',
                stop_timeout = 56,
                shell = ["/bin/sh","-c"]
            )
        else:
            return ImageConfig(
        )
        """

    def testImageConfig(self):
        """Test ImageConfig"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
