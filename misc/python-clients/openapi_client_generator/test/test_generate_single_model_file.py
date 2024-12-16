from openapi_client_generator.generate_single_model_file import generate_single_model_file
from openapi_client_generator.ast_conversions import ast_to_source, dict_to_ast
from test.fixtures import docker_converted_openapi

def test_generate_single_model_file():
    mf = generate_single_model_file(docker_converted_openapi()[1])
    source = ast_to_source(dict_to_ast(mf.as_ast_dict()))
    print(source)
