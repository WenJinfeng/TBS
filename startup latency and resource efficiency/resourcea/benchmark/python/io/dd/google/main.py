import os
import subprocess
import json
from flask import escape


def function_handler(request):
    request_json = request.get_json(silent=True)
    bs = 'bs='+request_json['bs']
    count = 'count='+request_json['count']
    out_fd = open('/tmp/io_write_logs','w')
    a = subprocess.Popen(['dd', 'if=/dev/zero', 'of=/tmp/out', bs, count], stderr=out_fd)
    a.communicate()
    
    output = subprocess.check_output(['ls', '-alh', '/tmp/'])

    output = subprocess.check_output(['du', '-sh', '/tmp/'])

    with open('/tmp/io_write_logs') as logs:
        result = str(logs.readlines()[2]).replace('\n', '')
    print("execute duration: " + json.dumps({"ddop":result,"executionId":request.headers['Function-Execution-Id']}))
    return json.dumps({"ddop":result,"executionId":request.headers['Function-Execution-Id']})