# import sys
# reload(sys)
# sys.setdefaultencoding('utf-8')
from time import time
import gzip
import os
import json


def handler(event, context):
    file_folder = '/tmp/'
    # file_folder = './'
    file_size = event['file_size']
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
    print("execute duration: " + json.dumps(result))
    return json.dumps(result)

def test_locally():
    with open('input.json', 'r') as input_data:
        data = json.load(input_data)
        print(handler(data, None))
if __name__ == '__main__':
    test_locally()