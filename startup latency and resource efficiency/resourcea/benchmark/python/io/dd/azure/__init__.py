import azure.functions as func
import subprocess
import json
import logging

tmp = '/tmp/'


def main(req: func.HttpRequest) -> func.HttpResponse:
    bs = 'bs='+req.params.get('bs')
    count = 'count='+req.params.get('count')
    out_fd = open(tmp + 'io_write_logs', 'w')
    dd = subprocess.Popen(['dd', 'if=/dev/zero', 'of=/tmp/out', bs, count], stderr=out_fd)
    dd.communicate()
    subprocess.check_output(['ls', '-alh', tmp])
    with open(tmp + 'io_write_logs') as logs:
        result = str(logs.readlines()[2]).replace('\n', '')
    logging.info("execute duration: " + json.dumps({"ddop":result}))
    return func.HttpResponse(result)
