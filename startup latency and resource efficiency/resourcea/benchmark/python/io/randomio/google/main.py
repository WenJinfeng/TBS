from time import time
import subprocess
import os
import random
import json
from flask import escape


# {"file_size":1, "byte_size":1000}
def function_handler(request):
    request_json = request.get_json(silent=True)
    file_size = request_json['file_size']
    byte_size = request_json['byte_size']
    file_write_path = '/tmp/file'

    block = os.urandom(byte_size)
    total_file_bytes = file_size * 1024 * 1024 - byte_size

    start = time()
    with open(file_write_path, 'wb') as f:
        for _ in range(total_file_bytes//byte_size):
            f.seek(random.randrange(total_file_bytes))
            f.write(block)
        f.flush()
        os.fsync(f.fileno())
    disk_write_latency = time() - start
    disk_write_bandwidth = file_size / disk_write_latency

    output = subprocess.check_output(['ls', '-alh', '/tmp/'])

    start = time()
    with open(file_write_path, 'rb') as f:
        for _ in range(total_file_bytes//byte_size):
            f.seek(random.randrange(total_file_bytes))
            f.read(byte_size)
    disk_read_latency = time() - start
    disk_read_bandwidth = file_size / disk_read_latency

    rm = subprocess.Popen(['rm', '-rf', file_write_path])
    rm.communicate()

    print("execute duration: " + json.dumps({'disk_write_bandwidth':disk_write_bandwidth,'disk_write_latency':disk_write_latency,'disk_read_bandwidth':disk_read_bandwidth,'disk_read_latency':disk_read_latency,"executionId":request.headers['Function-Execution-Id']}))

    return  json.dumps({
        'disk_write_bandwidth':disk_write_bandwidth,
        'disk_write_latency':disk_write_latency,
        'disk_read_bandwidth':disk_read_bandwidth,
        'disk_read_latency':disk_read_latency,
        "executionId":request.headers['Function-Execution-Id']
    })