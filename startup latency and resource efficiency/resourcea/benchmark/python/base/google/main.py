from time import time
from time import sleep
import json
import psutil
import platform
from flask import escape


instance_var = True


def getInstanceInfo():
    result = {}
    sys_version=platform.linux_distribution()
    sys_kernel=platform.uname()[2] #kernel version
    sys_arch=platform.uname()[4]   #eg:x86_64 amd64 win32
    mem=psutil.virtual_memory()
    disk=psutil.disk_partitions()
    partition = psutil.disk_usage('/')
    total=mem.total
    result['cpuCountLogical'] = psutil.cpu_count()
    result['cpuCount'] = psutil.cpu_count(logical = False)
    result['memory'] = total
    result['memory_used']= mem.used
    result['memory_free']= mem.free
    result['memory_percent']= mem.percent
    result['disk_info']= disk
    result['disk'] = partition.total
    result['sysVersion'] = sys_version
    result['sysKernel'] = sys_kernel
    result['sysArch'] = sys_arch
    return result

def function_handler(request):
    request_json = request.get_json(silent=True)
    global instance_var
    systeminfo = getInstanceInfo()
    result = {"executionId":request.headers['Function-Execution-Id'],"instance":instance_var, "systeminfo":systeminfo}
    print('execute duration:' + json.dumps(result))
    instance_var = False
    sleep(5)
    return json.dumps(result)