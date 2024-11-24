# coding: utf-8

"""
    Docker Engine API

    The Engine API is an HTTP API served by Docker Engine. It is the API the Docker client uses to communicate with the Engine, so everything the Docker client can do can be done with the API.  Most of the client's commands map directly to API endpoints (e.g. `docker ps` is `GET /containers/json`). The notable exception is running containers, which consists of several API calls.  # Errors  The API uses standard HTTP status codes to indicate the success or failure of the API call. The body of the response will be JSON in the following format:  ``` {   \"message\": \"page not found\" } ```  # Versioning  The API is usually changed in each release, so API calls are versioned to ensure that clients don't break. To lock to a specific version of the API, you prefix the URL with its version, for example, call `/v1.30/info` to use the v1.30 version of the `/info` endpoint. If the API version specified in the URL is not supported by the daemon, a HTTP `400 Bad Request` error message is returned.  If you omit the version-prefix, the current version of the API (v1.45) is used. For example, calling `/info` is the same as calling `/v1.45/info`. Using the API without a version-prefix is deprecated and will be removed in a future release.  Engine releases in the near future should support this version of the API, so your client will continue to work even if it is talking to a newer Engine.  The API uses an open schema model, which means server may add extra properties to responses. Likewise, the server will ignore any extra query parameters and request body properties. When you write clients, you need to ignore additional properties in responses to ensure they do not break when talking to newer daemons.   # Authentication  Authentication for registries is handled client side. The client has to send authentication details to various endpoints that need to communicate with registries, such as `POST /images/(name)/push`. These are sent as `X-Registry-Auth` header as a [base64url encoded](https://tools.ietf.org/html/rfc4648#section-5) (JSON) string with the following structure:  ``` {   \"username\": \"string\",   \"password\": \"string\",   \"email\": \"string\",   \"serveraddress\": \"string\" } ```  The `serveraddress` is a domain/IP without a protocol. Throughout this structure, double quotes are required.  If you have already got an identity token from the [`/auth` endpoint](#operation/SystemAuth), you can just pass this instead of credentials:  ``` {   \"identitytoken\": \"9cbaf023786cd7...\" } ``` 

    The version of the OpenAPI document: 1.45
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from docker_client.generated.models.networking_config import NetworkingConfig

class TestNetworkingConfig(unittest.TestCase):
    """NetworkingConfig unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> NetworkingConfig:
        """Test NetworkingConfig
            include_optional is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `NetworkingConfig`
        """
        model = NetworkingConfig()
        if include_optional:
            return NetworkingConfig(
                endpoints_config = {
                    'key' : docker_client.generated.models.endpoint_settings.EndpointSettings(
                        ipam_config = docker_client.generated.models.endpoint_ipam_config.EndpointIPAMConfig(
                            ipv4_address = '172.20.30.33', 
                            ipv6_address = '2001:db8:abcd::3033', 
                            link_local_ips = ["169.254.34.68","fe80::3468"], ), 
                        links = ["container_1","container_2"], 
                        mac_address = '02:42:ac:11:00:04', 
                        aliases = ["server_x","server_y"], 
                        network_id = '08754567f1f40222263eab4102e1c733ae697e8e354aa9cd6e18d7402835292a', 
                        endpoint_id = 'b88f5b905aabf2893f3cbc4ee42d1ea7980bbc0a92e2c8922b1e1795298afb0b', 
                        gateway = '172.17.0.1', 
                        ip_address = '172.17.0.4', 
                        ip_prefix_len = 16, 
                        ipv6_gateway = '2001:db8:2::100', 
                        global_ipv6_address = '2001:db8::5689', 
                        global_ipv6_prefix_len = 64, 
                        driver_opts = {"com.example.some-label":"some-value","com.example.some-other-label":"some-other-value"}, 
                        dns_names = ["foobar","server_x","server_y","my.ctr"], )
                    }
            )
        else:
            return NetworkingConfig(
        )
        """

    def testNetworkingConfig(self):
        """Test NetworkingConfig"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
