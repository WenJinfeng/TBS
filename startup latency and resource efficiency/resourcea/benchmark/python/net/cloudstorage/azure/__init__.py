import azure.functions as func
from azure.storage.blob import BlockBlobService
import json

import logging
from time import time

tmp = "/tmp/"

def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:
    acc_name = req.params.get('account_name')
    acc_key = req.params.get('account_key')
    src_container_name = req.params.get('src_container_name')
    dst_container_name = req.params.get('dst_container_name')
    blob_name = req.params.get('blob_name')

    block_blob_service = BlockBlobService(account_name=acc_name, account_key=acc_key)
    download_path = tmp + blob_name

    start = time()
    block_blob_service.get_blob_to_path(src_container_name, blob_name, download_path)
    download_time = time() - start

    start = time()
    upload_path = download_path
    block_blob_service.create_blob_from_path(dst_container_name, upload_path.split("/")[2], upload_path)
    upload_time = time() - start

    result = json.dumps({"download_time": download_time, "upload_time": upload_time, "invocationid":context.invocation_id})
    logging.info("execute duration: " + result)
    return func.HttpResponse(result)