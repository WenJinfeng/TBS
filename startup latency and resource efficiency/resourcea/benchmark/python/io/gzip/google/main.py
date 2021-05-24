from time import time
import os
from flask import escape
import gzip



def function_handler(request):
    request_json = request.get_json(silent=True)
    file_size = request_json['file_size']
    file_write_path = '/tmp/file'

    start = time()
    with open(file_write_path, 'wb') as f:
        f.write(os.urandom(file_size * 1024 * 1024))
    disk_latency = time() - start

    with open(file_write_path) as f:
        start = time()
        with gzip.open('/tmp/result.gz', 'wb') as gz:
            gz.writelines(f)
        compress_latency = time() - start

    result = {'disk_write': disk_latency, "compress": compress_latency,"executionId":request.headers['Function-Execution-Id']}
    print("execute duration: " + json.dumps(result))
    return json.dumps(result)

