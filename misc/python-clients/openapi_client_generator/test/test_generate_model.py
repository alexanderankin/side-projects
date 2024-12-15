from jpype import getDefaultJVMPath, java, startJVM

from openapi_client_generator import generate


def test():
    generate()


def test_jpype():
    startJVM("-Xms128m", "-Xmx512m", classpath="", jvmpath=getDefaultJVMPath())
    result = java.lang.Math.max(2, 3)
    print(result)
