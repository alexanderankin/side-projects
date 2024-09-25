from python_image_builder.builder import ContainerImage


def test_parse():
    assert ContainerImage.parse("alpine") == ContainerImage("alpine", "latest")
    assert ContainerImage.parse("alpine:3.18") == ContainerImage("alpine", "3.18")
    assert ContainerImage.parse("quay.io/alpine/alpine:3.18") == ContainerImage(
        "alpine", "3.18", "quay.io/alpine"
    )
    assert ContainerImage.parse("localhost:5000/alpine/alpine:3.18") == ContainerImage(
        "alpine", "3.18", "localhost:5000/alpine"
    )
