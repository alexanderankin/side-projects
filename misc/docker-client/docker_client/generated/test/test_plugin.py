# coding: utf-8

"""
    Docker Engine API

    The Engine API is an HTTP API served by Docker Engine. It is the API the Docker client uses to communicate with the Engine, so everything the Docker client can do can be done with the API.  Most of the client's commands map directly to API endpoints (e.g. `docker ps` is `GET /containers/json`). The notable exception is running containers, which consists of several API calls.  # Errors  The API uses standard HTTP status codes to indicate the success or failure of the API call. The body of the response will be JSON in the following format:  ``` {   \"message\": \"page not found\" } ```  # Versioning  The API is usually changed in each release, so API calls are versioned to ensure that clients don't break. To lock to a specific version of the API, you prefix the URL with its version, for example, call `/v1.30/info` to use the v1.30 version of the `/info` endpoint. If the API version specified in the URL is not supported by the daemon, a HTTP `400 Bad Request` error message is returned.  If you omit the version-prefix, the current version of the API (v1.45) is used. For example, calling `/info` is the same as calling `/v1.45/info`. Using the API without a version-prefix is deprecated and will be removed in a future release.  Engine releases in the near future should support this version of the API, so your client will continue to work even if it is talking to a newer Engine.  The API uses an open schema model, which means server may add extra properties to responses. Likewise, the server will ignore any extra query parameters and request body properties. When you write clients, you need to ignore additional properties in responses to ensure they do not break when talking to newer daemons.   # Authentication  Authentication for registries is handled client side. The client has to send authentication details to various endpoints that need to communicate with registries, such as `POST /images/(name)/push`. These are sent as `X-Registry-Auth` header as a [base64url encoded](https://tools.ietf.org/html/rfc4648#section-5) (JSON) string with the following structure:  ``` {   \"username\": \"string\",   \"password\": \"string\",   \"email\": \"string\",   \"serveraddress\": \"string\" } ```  The `serveraddress` is a domain/IP without a protocol. Throughout this structure, double quotes are required.  If you have already got an identity token from the [`/auth` endpoint](#operation/SystemAuth), you can just pass this instead of credentials:  ``` {   \"identitytoken\": \"9cbaf023786cd7...\" } ``` 

    The version of the OpenAPI document: 1.45
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from docker_client.generated.docker_client.generated.models.plugin import Plugin

class TestPlugin(unittest.TestCase):
    """Plugin unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> Plugin:
        """Test Plugin
            include_optional is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `Plugin`
        """
        model = Plugin()
        if include_optional:
            return Plugin(
                id = '5724e2c8652da337ab2eedd19fc6fc0ec908e4bd907c7421bf6a8dfc70c4c078',
                name = 'tiborvass/sample-volume-plugin',
                enabled = True,
                settings = docker_client.generated.models.plugin_settings.Plugin_Settings(
                    mounts = [
                        docker_client.generated.models.plugin_mount.PluginMount(
                            name = 'some-mount', 
                            description = 'This is a mount that's used by the plugin.', 
                            settable = [
                                ''
                                ], 
                            source = '/var/lib/docker/plugins/', 
                            destination = '/mnt/state', 
                            type = 'bind', 
                            options = ["rbind","rw"], )
                        ], 
                    env = ["DEBUG=0"], 
                    args = [
                        ''
                        ], 
                    devices = [
                        docker_client.generated.models.plugin_device.PluginDevice(
                            name = '', 
                            description = '', 
                            settable = [
                                ''
                                ], 
                            path = '/dev/fuse', )
                        ], ),
                plugin_reference = 'localhost:5000/tiborvass/sample-volume-plugin:latest',
                config = docker_client.generated.models.plugin_config.Plugin_Config(
                    docker_version = '17.06.0-ce', 
                    description = 'A sample volume plugin for Docker', 
                    documentation = 'https://docs.docker.com/engine/extend/plugins/', 
                    interface = docker_client.generated.models.plugin_config_interface.Plugin_Config_Interface(
                        types = ["docker.volumedriver/1.0"], 
                        socket = 'plugins.sock', 
                        protocol_scheme = 'some.protocol/v1.0', ), 
                    entrypoint = ["/usr/bin/sample-volume-plugin","/data"], 
                    work_dir = '/bin/', 
                    user = docker_client.generated.models.plugin_config_user.Plugin_Config_User(
                        uid = 1000, 
                        gid = 1000, ), 
                    network = docker_client.generated.models.plugin_config_network.Plugin_Config_Network(
                        type = 'host', ), 
                    linux = docker_client.generated.models.plugin_config_linux.Plugin_Config_Linux(
                        capabilities = ["CAP_SYS_ADMIN","CAP_SYSLOG"], 
                        allow_all_devices = False, 
                        devices = [
                            docker_client.generated.models.plugin_device.PluginDevice(
                                name = '', 
                                description = '', 
                                settable = [
                                    ''
                                    ], 
                                path = '/dev/fuse', )
                            ], ), 
                    propagated_mount = '/mnt/volumes', 
                    ipc_host = False, 
                    pid_host = False, 
                    mounts = [
                        docker_client.generated.models.plugin_mount.PluginMount(
                            name = 'some-mount', 
                            description = 'This is a mount that's used by the plugin.', 
                            settable = [
                                ''
                                ], 
                            source = '/var/lib/docker/plugins/', 
                            destination = '/mnt/state', 
                            type = 'bind', 
                            options = ["rbind","rw"], )
                        ], 
                    env = [{"Name":"DEBUG","Description":"If set, prints debug messages","Value":"0"}], 
                    args = docker_client.generated.models.plugin_config_args.Plugin_Config_Args(
                        name = 'args', 
                        description = 'command line arguments', 
                        settable = , 
                        value = [
                            ''
                            ], ), 
                    rootfs = docker_client.generated.models.plugin_config_rootfs.Plugin_Config_rootfs(
                        type = 'layers', 
                        diff_ids = ["sha256:675532206fbf3030b8458f88d6e26d4eb1577688a25efec97154c94e8b6b4887","sha256:e216a057b1cb1efc11f8a268f37ef62083e70b1b38323ba252e25ac88904a7e8"], ), )
            )
        else:
            return Plugin(
                name = 'tiborvass/sample-volume-plugin',
                enabled = True,
                settings = docker_client.generated.models.plugin_settings.Plugin_Settings(
                    mounts = [
                        docker_client.generated.models.plugin_mount.PluginMount(
                            name = 'some-mount', 
                            description = 'This is a mount that's used by the plugin.', 
                            settable = [
                                ''
                                ], 
                            source = '/var/lib/docker/plugins/', 
                            destination = '/mnt/state', 
                            type = 'bind', 
                            options = ["rbind","rw"], )
                        ], 
                    env = ["DEBUG=0"], 
                    args = [
                        ''
                        ], 
                    devices = [
                        docker_client.generated.models.plugin_device.PluginDevice(
                            name = '', 
                            description = '', 
                            settable = [
                                ''
                                ], 
                            path = '/dev/fuse', )
                        ], ),
                config = docker_client.generated.models.plugin_config.Plugin_Config(
                    docker_version = '17.06.0-ce', 
                    description = 'A sample volume plugin for Docker', 
                    documentation = 'https://docs.docker.com/engine/extend/plugins/', 
                    interface = docker_client.generated.models.plugin_config_interface.Plugin_Config_Interface(
                        types = ["docker.volumedriver/1.0"], 
                        socket = 'plugins.sock', 
                        protocol_scheme = 'some.protocol/v1.0', ), 
                    entrypoint = ["/usr/bin/sample-volume-plugin","/data"], 
                    work_dir = '/bin/', 
                    user = docker_client.generated.models.plugin_config_user.Plugin_Config_User(
                        uid = 1000, 
                        gid = 1000, ), 
                    network = docker_client.generated.models.plugin_config_network.Plugin_Config_Network(
                        type = 'host', ), 
                    linux = docker_client.generated.models.plugin_config_linux.Plugin_Config_Linux(
                        capabilities = ["CAP_SYS_ADMIN","CAP_SYSLOG"], 
                        allow_all_devices = False, 
                        devices = [
                            docker_client.generated.models.plugin_device.PluginDevice(
                                name = '', 
                                description = '', 
                                settable = [
                                    ''
                                    ], 
                                path = '/dev/fuse', )
                            ], ), 
                    propagated_mount = '/mnt/volumes', 
                    ipc_host = False, 
                    pid_host = False, 
                    mounts = [
                        docker_client.generated.models.plugin_mount.PluginMount(
                            name = 'some-mount', 
                            description = 'This is a mount that's used by the plugin.', 
                            settable = [
                                ''
                                ], 
                            source = '/var/lib/docker/plugins/', 
                            destination = '/mnt/state', 
                            type = 'bind', 
                            options = ["rbind","rw"], )
                        ], 
                    env = [{"Name":"DEBUG","Description":"If set, prints debug messages","Value":"0"}], 
                    args = docker_client.generated.models.plugin_config_args.Plugin_Config_Args(
                        name = 'args', 
                        description = 'command line arguments', 
                        settable = , 
                        value = [
                            ''
                            ], ), 
                    rootfs = docker_client.generated.models.plugin_config_rootfs.Plugin_Config_rootfs(
                        type = 'layers', 
                        diff_ids = ["sha256:675532206fbf3030b8458f88d6e26d4eb1577688a25efec97154c94e8b6b4887","sha256:e216a057b1cb1efc11f8a268f37ef62083e70b1b38323ba252e25ac88904a7e8"], ), ),
        )
        """

    def testPlugin(self):
        """Test Plugin"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
