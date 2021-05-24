import azure.functions as func
from time import time
import gzip
import os
import json
import logging

tmp = '/tmp/'


def main(req: func.HttpRequest) -> func.HttpResponse:
    file_folder = tmp
    file_size = int(req.params.get('file_size'))
    file_write_path = file_folder + 'file'
    start = time()
    with open(file_write_path, 'wb') as f:
        f.write(os.urandom(file_size * 1024 * 1024))
    disk_latency = time() - start
    with open(file_write_path, "rb") as f:
        start = time()
        with gzip.open(file_folder + 'result.gz', 'wb') as gz:
            gz.writelines(f)
        compress_latency = time() - start

    result = {'disk_write': disk_latency, "compress": compress_latency}
    logging.info("execute duration: " + json.dumps(result))

    return func.HttpResponse(json.dumps(result))