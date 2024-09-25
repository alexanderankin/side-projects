import click

CONTEXT_SETTINGS = dict(help_option_names=["-h", "--help"])


@click.group(context_settings=CONTEXT_SETTINGS)
def cli():
    pass


@cli.command
@click.option("-b", "--build-file", default="pib.yaml", show_default=True)
@click.option(
    "-t",
    "--target",
    help="""
        specify image destination

        Examples: gcr.io/project/image, registry://image-ref, docker://image,
        tar://path

        When this option is specified as a tar file, --name is required to set
        destination image tag
        """,
    required=True,
)
@click.option("--name", help="specify the image tag")
def build():
    """
    build a container image from a specification file

    uses the build file to build an image into the value of --target.

    --target is a uri, for example:

    * docker://

    --name is required when target
    """
    print("build")


@cli.group
def completion():
    pass


@completion.command
def bash():
    import os

    os.environ |= {"_PIB_COMPLETE": "bash_source"}
    cli()


def run():
    cli()
