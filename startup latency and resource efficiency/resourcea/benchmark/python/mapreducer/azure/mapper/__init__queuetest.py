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

def main(myitem: func.QueueMessage) -> None:
    # {"account_name":"slstorageinaz","account_key":"7kv7/1FMT6OlmeMpFcX7D6PYThdDcOWifG8vFJCa7L4XQkVizLWRpgPEAkzC2GvDSAvTqxEtQtNJf7cTlu6wQA==","src_container_name":"mapper","dst_container_name":"reducer","mapper_id":1,"keys":["Talk~U;Nee_2325.html","Template_talk~Vw1_7d44.html"]}
    result = json.dumps({
        'id': myitem.id,
        'body': myitem.get_body().decode('utf-8'),
        'expiration_time': (myitem.expiration_time.isoformat()
                            if myitem.expiration_time else None),
        'insertion_time': (myitem.insertion_time.isoformat()
                           if myitem.insertion_time else None),
        'time_next_visible': (myitem.time_next_visible.isoformat()
                              if myitem.time_next_visible else None),
        'pop_receipt': myitem.pop_receipt,
        'dequeue_count': myitem.dequeue_count
    })

    logging.info(result)

    req = json.loads(myitem.get_body().decode('utf-8'))

    acc_name = req['account_name']
    acc_key = req['account_key']
    src_container_name = req['src_container_name']
    dst_container_name = req['dst_container_name']
    mapper_id = req['mapper_id']
    src_keys = req['keys']

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
        print(key)
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
    print(output)
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
    return None