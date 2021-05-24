import azure.functions as func
from azure.storage.blob import BlockBlobService
import logging
import json
from functools import partial
from multiprocessing.dummy import Pool as ThreadPool
from time import time

subs = "</title><text>"
computer_language = ["JavaScript", "Java", "PHP", "Python", "C#", "C++",
                     "Ruby", "CSS", "Objective-C", "Perl",
                     "Scala", "Haskell", "MATLAB", "Clojure", "Groovy"]

tmp='/Users/vector/Desktop/poppy/azure/python/tmp/'

def main(req: func.HttpRequest) -> func.HttpResponse:

    acc_name = req.params.get('account_name')
    acc_key = req.params.get('account_key')
    dst_container_name = req.params.get('dst_container_name')

    block_blob_service = BlockBlobService(account_name=acc_name, account_key=acc_key)
    output = {}
    network = 0
    reduce = 0

    for lang in computer_language:
        output[lang] = 0

    all_keys = []
    for obj in block_blob_service.list_blobs(dst_container_name):
        all_keys.append(obj.name)

    for key in all_keys:
        start = time()
        download_path = tmp + key
        block_blob_service.get_blob_to_path(dst_container_name, key, download_path)
        with open(download_path, 'r') as f:
            contents = f.read()
            network += time() - start
            start = time()
            data = json.loads(contents)
            for key in data:
                output[key] += data[key]
            reduce += time() - start
    logging.info(output)
    metadata = {
        'output': str(output),
        'network': str(network),
        'reduce': str(reduce)
    }
    return func.HttpResponse(json.dumps(metadata))
