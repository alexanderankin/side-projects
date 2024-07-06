from json import loads
from pathlib import Path

from azure.storage.blob import BlobServiceClient

TF_STATE = loads((Path(__file__).parent.parent / "terraform.tfstate").read_text())
ACCOUNT_URL = TF_STATE["outputs"]["account_url"]["value"]
KEY = TF_STATE["outputs"]["account_access_key"]["value"]
CONTAINER = TF_STATE["outputs"]["account_container_name"]["value"]


def upload_blob_data(blob_service_client: BlobServiceClient, container_name: str):
    blob_client = blob_service_client.get_blob_client(container=container_name,
                                                      blob="sample-blob.txt")
    data = b"Sample data for blob"

    # Upload the blob data - default blob type is BlockBlob
    blob_client.upload_blob(data, blob_type="BlockBlob")


def download_blob_to_string(blob_service_client: BlobServiceClient, container_name):
    blob_client = blob_service_client.get_blob_client(container=container_name,
                                                      blob="sample-blob.txt")

    # encoding param is necessary for readall() to return str, otherwise it returns bytes
    downloader = blob_client.download_blob(max_concurrency=1, encoding='UTF-8')
    blob_text = downloader.readall()
    print(f"Blob contents: {blob_text}")


def upload():
    upload_blob_data(
        blob_service_client=BlobServiceClient(
            account_url=ACCOUNT_URL,
            credential=KEY
        ),
        container_name=CONTAINER
    )


def download():
    download_blob_to_string(
        blob_service_client=BlobServiceClient(
            account_url=ACCOUNT_URL,
            credential=KEY
        ),
        container_name=CONTAINER
    )


if __name__ == '__main__':
    upload()
    download()
