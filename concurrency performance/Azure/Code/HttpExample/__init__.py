
import json
import time
import os
import socket
import sys
import uuid
import subprocess

try:
    import urllib2
    from urllib2 import urlopen
except BaseException:
    from urllib.request import urlopen

import decimal

# from stats import *

import azure.functions as func

# Set it to your own servers
INST_PRIV_IP_DST = "8.8.8.8"
VM_PUB_ID_DST = "http://ip.42.pl/raw"

def fstr(f):
    """
    Convert a float number to string
    """
    ctx = decimal.Context()
    ctx.prec = 20
    d1 = ctx.create_decimal(repr(f))
    return format(d1, 'f')

def get_inst_priv_ip():
    """ Get inst private IP """
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect((INST_PRIV_IP_DST, 80))
    ip = s.getsockname()[0]
    s.close()
    return ip


def get_vm_priv_ip():
    """ Get VM private IP """
    ip = socket.gethostbyname(socket.getfqdn())
    return ip


def get_vm_pub_ip():
    """ Get VM public IP by querying external server """
    ip = "None"
    try:
        ip = str(urlopen(VM_PUB_ID_DST).read())
    except BaseException:
        pass
    return ip

def get_btime():
    """ Get VM uptime """
    for line in open('/proc/stat'):
        if 'btime' in line:
            btime = line.strip('\n')

    return btime

def get_inst_id():
    """ Get the inst ID """
    log_file = '/tmp/inst_id.txt'
    new_id = str(uuid.uuid4())
    try:
        exist_id = open(log_file).read().strip('\n')
    except BaseException:
        open(log_file, 'w').write(new_id)
        exist_id = new_id
    return exist_id, new_id


def get_cpuinfo_short():
    """ Get CPU version information """
    buf = "".join(open("/proc/cpuinfo").readlines())
    cpuinfo = buf.replace("\n", ";").replace("\t", "")
    a1 = cpuinfo.count("processor")
    a2 = cpuinfo.split(";")[4].split(":")[1].strip()
    return "%s,%s" % (a1, a2)

def get_meminfo():
    """
    Get and format the content of /proc/meminfo
    """
    buf = open('/proc/meminfo').read()
    buf = ','.join([v.replace(' ', '') for v in
                    buf.split('\n') if v])

    return buf

def get_vm_id():
    """ Get VM ID from /proc/self/cgroup """
    buf = open('/proc/self/cgroup').read()
    #buf = open('/proc/self/cgroup').read().split('\n')[-3].split('/')
  #  vm_id, inst_id = buf[1], buf[2]
  #  return vm_id, inst_id
    buf = buf.split('\n')
    
    # for s in bufs:
    #     if s.split(':')[1] == 'cpu,cpuacct':
    #         sandboxvalue = s.split(':')[2]
    return ''.join(buf), 'null'

def get_uptime():
    """ Get VM uptime """
    uptime = ','.join(open('/proc/uptime').read().strip('\n').split(' '))
    return uptime

def main(req: func.HttpRequest) -> func.HttpResponse:
    tm_st = time.time() * 1000
    name = req.params.get('name')
    wait_util = int((time.time() + int(name)) * 1000)
    
    exist_id, new_id = get_inst_id()
    vm_id, inst_id = get_vm_id()
    uptime = get_uptime()
    vm_priv_ip = get_vm_priv_ip()
    vm_pub_ip = get_vm_pub_ip()
    inst_priv_ip = get_inst_priv_ip()
    cpu_info = get_cpuinfo_short()
    memory = get_meminfo()
    btime = get_btime()
    
    # wjfhost = os.environ.get('HOSTNAME')
    # wjfinstance = os.environ.get('CONTAINER_NAME')
    # wjf = os.getenv('WEBSITE_INSTANCE_ID')



    basic_info = [
        btime,
        exist_id,
        new_id,
        vm_id,
        vm_priv_ip,
        vm_pub_ip,
        inst_priv_ip,
        uptime,
        cpu_info,
        memory]

    basic_info = "#".join([str(v) for v in basic_info])
    
    # basic_info = stat_basic(argv=1)
    
    while time.time() * 1000 < wait_util:
        continue
    
    tm_ed = time.time() * 1000

    timing_info = "{}#{}#{}".format(fstr(tm_st), fstr(tm_ed), fstr(tm_ed - tm_st))

    res = "{}#{}".format(basic_info, timing_info)

    if name:
        return func.HttpResponse(f"{res}")
    else:
        return func.HttpResponse(
             "else",
             status_code=200
        )
