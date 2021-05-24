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
    #get TODO
    acc_name = req.params.get('account_name')
    #post
    if not acc_name:
        try:
            req_body = req.get_json()
            logging.info(req_body)
        except ValueError:
            pass
        else:
            acc_name = req_body['account_name']
            acc_key = req_body['account_key']
            src_container_name = req_body['src_container_name']
            dst_container_name = req_body['dst_container_name']
            mapper_id = req_body['mapper_id']
            src_keys = req_body['keys']

    block_blob_service = BlockBlobService(account_name=acc_name, account_key=acc_key)
    output = {}

    for lang in computer_language:
        output[lang] = 0

    network = 0
    map = 0
    # keys = src_keys.split('/')
    keys = src_keys

    # Download and process all keys
    for key in keys:
        logging.info(key)
        start = time()
        download_path = tmp + key
        block_blob_service.get_blob_to_path(src_container_name, key, download_path)
        with open(download_path, 'r') as f:
            contents = f.read()
            network += time() - start
            start = time()
            for line in contents.split('\n')[:-1]:
                idx = line.find(subs)
                text = line[idx + len(subs): len(line) - 16]
                for lang in computer_language:
                    if lang in text:
                        output[lang] += 1
            map += time() - start
    logging.info(output)
    metadata = {
        'output': str(output),
        'network': str(network),
        'map': str(map)
    }
    start = time()
    tmpSavePath = tmp + str(mapper_id)
    with open(tmpSavePath, 'w') as f:
        f.write(json.dumps(output))
    block_blob_service.create_blob_from_path(dst_container_name, str(mapper_id), tmpSavePath)
    network += time() - start
    return func.HttpResponse(json.dumps(metadata))