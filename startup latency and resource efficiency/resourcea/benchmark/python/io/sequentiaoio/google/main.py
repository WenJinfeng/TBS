from time import time
import subprocess
import os
import random
import json
from flask import escape



tmp='/tmp/'

def function_handler(request):
    request_json = request.get_json(silent=True)
    file_size = request_json['file_size']
    byte_size = request_json['byte_size']
    file_write_path = '/tmp/file'

    start = time()
    with open(file_write_path, 'wb', buffering=byte_size) as f:
        f.write(os.urandom(file_size * 1024 * 1024))
        f.flush()
        os.fsync(f.fileno())
    disk_write_latency = time() - start
    disk_write_bandwidth = file_size / disk_write_latency
    output = subprocess.check_output(['ls', '-alh', tmp])
    start = time()
    with open(file_write_path, 'rb', buffering=byte_size) as f:
        byte = f.read(byte_size)
        while byte != ''.encode('utf-8'):
            # without the encode is fatal, which will never stop
            byte = f.read(byte_size)

    disk_read_latency = time() - start
    disk_read_bandwidth = file_size / disk_read_latency

    rm = subprocess.Popen(['rm', '-rf', file_write_path])
    rm.communicate()

    returndata = {'disk_write_bandwidth':disk_write_bandwidth, 'disk_write_latency':disk_write_latency,
                  'disk_read_bandwidth':disk_read_bandwidth, 'disk_read_latency':disk_read_latency,"executionId":request.headers['Function-Execution-Id']}

    print("execute duration: " + json.dumps(returndata))

    return json.dumps(returndata)