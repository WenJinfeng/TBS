import azure.functions as func
import math
from time import time
import json
import logging


def float_operations(N):
    start = time()
    for i in range(0, N):
        sin_i = math.sin(i)
        cos_i = math.cos(i)
        sqrt_i = math.sqrt(i)
    latency = time() - start
    return latency


def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:
    N = int(req.params.get('N'))
    latency = str(float_operations(N))
    result = json.dumps({'latency': latency, "invocationid":context.invocation_id})
    logging.info('execute duration:' + result)
    return func.HttpResponse(result)
