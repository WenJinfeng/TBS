import azure.functions as func
from azure.storage.blob import BlockBlobService
from azure.storage.queue import QueueService
import logging
import json
from functools import partial
from multiprocessing.dummy import Pool as ThreadPool
import time
import requests
from urllib.parse import urlparse


tmp='/Users/vector/Desktop/poppy/azure/python/tmp/'

total_map = 0
total_network_mapper = 0
total_reduce = 0
total_reduce_network = 0

def map_invoke_lambda(url, acc_name, acc_key, dst_container_name, src_container_name, all_keys, batch_size, mapper_id):


    # invoke the function with queue, but fail to get response
    # queue_service = QueueService(account_name=acc_name, account_key=acc_key)
    # queue_service.put_message("queuetestforly",json.dumps(payload))

    keys = all_keys[mapper_id * batch_size: (mapper_id + 1) * batch_size]
    payload = {"account_name":acc_name,"account_key":acc_key,"src_container_name":src_container_name,"dst_container_name":dst_container_name,"mapper_id":mapper_id,"keys":keys}
    # targetUrl = f"{url.scheme}://{url.netloc}/api/mapper"
    targetUrl = "http://localhost:7072/api/mapper"
    logging.info("=====targetUrl======")
    logging.info(targetUrl)
    response = requests.post(url=targetUrl, json=payload,headers={'Content-Type':'application/json'})
    output = response.text
    logging.info("mapper output : " + output)

    json_data = json.loads(output)

    global total_map, total_network_mapper
    total_map += float(json_data['map'])
    total_network_mapper += float(json_data['network'])


def reduce_invoke_lambda(url, acc_name, acc_key,dst_container_name):
    # invoke with http
    # targetUrl = f"{url.scheme}://{url.netloc}/api/reducer"
    targetUrl = "http://localhost:7073/api/reducer"
    logging.info(targetUrl)
    response = requests.get(url=targetUrl, params={"account_name":acc_name, "account_key":acc_key,"dst_container_name":dst_container_name})
    output = response.text
    json_data = json.loads(output)
    global total_reduce, total_reduce_network
    logging.info("reducer output : " + output)
    total_reduce += float(json_data['reduce'])
    total_reduce_network += float(json_data['network'])
    

def main(req: func.HttpRequest) -> func.HttpResponse:
    acc_name = req.params.get('account_name')
    acc_key = req.params.get('account_key')
    src_container_name = req.params.get('src_container_name')
    dst_container_name = req.params.get('dst_container_name')
    n_mapper = int(req.params.get('n_mapper'))
    url = urlparse(req.url)
    logging.info(url)

    block_blob_service = BlockBlobService(account_name=acc_name, account_key=acc_key)
    # Fetch all the keys
    all_keys = []
    for obj in block_blob_service.list_blobs(src_container_name):
        all_keys.append(obj.name)

    logging.info("dataset file : " + str(len(all_keys)))
    logging.info("key name : " + str(all_keys))
    logging.info("# of Mappers " +  str(n_mapper))
    total_size = len(all_keys)
    batch_size = 0

    if total_size % n_mapper == 0:
        batch_size = total_size // n_mapper
    else:
        batch_size = total_size // n_mapper + 1

    for idx in range(n_mapper):
        logging.info("mapper-" + str(idx) + ":" + str(all_keys[idx * batch_size: (idx + 1) * batch_size]))

    pool = ThreadPool(n_mapper)
    invoke_lambda_partial = partial(map_invoke_lambda, url, acc_name, acc_key, dst_container_name, src_container_name, all_keys, batch_size)
    pool.map(invoke_lambda_partial, range(n_mapper))
    pool.close()
    pool.join()

    while True:
        job_keys = block_blob_service.list_blobs(dst_container_name)
        jobdone = 0
        for obj in job_keys:
            jobdone = jobdone + 1
        logging.info("Wait Mapper Jobs ..." + str(jobdone))
        time.sleep(5)
        if jobdone == n_mapper:
            logging.info("[*] Map Done : mapper " + str(jobdone) + " finished.")
            break

    logging.info("[*] Map Done - map : " + str(total_map) + " network : " + str(total_network_mapper))

    # Reducer
    reduce_invoke_lambda(url, acc_name, acc_key, dst_container_name)
    logging.info("[*] Reduce Done : reducer finished.")

    result = json.dumps({"total_reduce_network":total_network_mapper, "total_reduce":total_reduce, "total_map":total_map, "total_network_mapper":total_network_mapper})
    logging.info("execute duration: " + result)
    return func.HttpResponse(result)