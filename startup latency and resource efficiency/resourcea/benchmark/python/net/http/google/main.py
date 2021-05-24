from time import time
import json
from urllib.request import urlopen
from flask import escape


def function_handler(request):
    request_json = request.get_json(silent=True)
    link = request_json['link'] # https://github.com/jdorfman/awesome-json-datasets

    start = time()
    f = urlopen(link)
    data = f.read().decode("utf-8")
    network = time() - start

    start = time()
    json_data = json.loads(data)
    str_json = json.dumps(json_data, indent=4)
    latency = time() - start

    result = json.dumps({"network": network, "serialization": latency,"executionId":request.headers['Function-Execution-Id']})
    print("execute duration: " + result)
    return result

