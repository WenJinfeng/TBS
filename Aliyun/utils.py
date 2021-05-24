
import fc2
import datetime
import json
from zipfile import ZipFile
from collections import OrderedDict
import uuid
import time
from urlparse import urlparse
from threading import Thread
import httplib
from Queue import Queue
import uuid
import json

import random
import os
import sys
import copy
import decimal
from conf import *


def fstr(f):
    """
    Convert a float number to string
    """
    ctx = decimal.Context()
    ctx.prec = 20
    d1 = ctx.create_decimal(repr(f))
    return format(d1, 'f')

def get_config_basic():
    """
    Get the credentials and basic setting from the config file
    """
    endpoint = CONGIF["creds"]["endpoint"]
    aliyun_id = CONGIF["creds"]["aliyun_id"]
    aliyun_key = CONGIF["creds"]["aliyun_key"]
    region = CONGIF["func"]["region"]
    roles = [CONGIF["func"]["role_1"], CONGIF["func"]["role_2"]]

    return endpoint, aliyun_id, aliyun_key, region, roles


def get_default_req(sleep_tm):
    """
    Construct the basic request
    By default all the parameters in the request are set to False to skip
    the tests, except sleep_tm and stat

    Args:
            sleep_tm: the function start tasks after 
            current time + sleep_tm 
            
    """
    d = copy.deepcopy(PARA_TEMP)
    for k in d:
        d[k] = False
    d["stat"] = dict(argv=1)
    # add sleep time at the current time
    # d["sleep"] = (time.time() + sleep_tm) * 1000
    d["sleep"] = sleep_tm
    #d["cpu"] = dict(n=1000)
    return {"cmds": d}


def zip_code(zip_name, code_path):
    """
    Zip the source function files to a deployment package
    """
    with ZipFile(zip_name, 'w') as lambda_zip:
        if not os.path.isdir(code_path):
            lambda_zip.write(code_path)
        else:
            for root, dirs, fs in os.walk(code_path):
                for f in fs:
                    abs_path = os.path.join(root, f)
                    lambda_zip.write(abs_path, f)




class FuncOp():
    """
    The class for function operation

    """

    def __init__(
            self,
            endpoint,
            aliyun_id,
            aliyun_key,
            region,
            role,
            runtime,
            memory,
            func_name):
        self.endpoint = endpoint
        self.aliyun_id = aliyun_id
        self.aliyun_key = aliyun_key
        self.region = region
        self.role = role
        self.runtime = runtime
        self.memory = memory
        self.func_name = func_name

    def get_func_name(self):
        return self.func_name

    def set_func_role(self, role):
        self.role = role

    def set_func_runtime(self, runtime):
        self.runtime = runtime

    def set_func_memory(self, memory):
        self.memory = memory

    def set_func_name(self, name):
        self.func_name = name




    def get_client(self):
        """
        run this everytime to get a new connection
        should not use a persistent connection
        """
        # config = fc2.config(connection_timeout=300, read_timeout=300)
        client = fc2.Client(endpoint = self.endpoint, accessKeyID = self.aliyun_id, accessKeySecret = self.aliyun_key)
        # client.read_timeout = 300
        # client.connection_timeout = 300
        return client
    


    def del_function(self):
        try:
            client = self.get_client()
            client.delete_function(serviceName = 'aliyun_measurestudy', functionName = self.func_name)
            return True
        except Exception as e:
            # print str(e)
            return False


 

    def create_function(self, src_file, func_handler):

        try:
            client = self.get_client()
            response = client.create_function(
                serviceName = 'aliyun_measurestudy', 
                functionName = self.func_name,
                description = '',
                runtime = self.runtime,
                memorySize = self.memory,
                timeout = 300,
                handler = func_handler,
                codeZipFile = src_file)
            return True
        except Exception as e:
            print str(e)
            return False



    def dump_meta(self):
        """
        The basic information to record
        """
        return "{}#{}#{}#{}".format(
            self.region,
            self.runtime,
            self.memory,
            self.func_name)

    def send_one_request(self, req_para={}):
        client = self.get_client()
        tm_st = time.time() * 1000
        resp = client.invoke_function(
            serviceName = 'aliyun_measurestudy',
            functionName = self.func_name, 
            payload = json.dumps(req_para)
        )
        
        tm_ed = time.time() * 1000

        try:
            resp = resp.data.decode('utf-8')
        except Exception as e:
            print str(e), resp
        if not resp:
            resp = "ERROR"
        out = "{}#{}#{}#{}".format(
            resp, fstr(tm_st), fstr(tm_ed), fstr(
                tm_ed - tm_st))
        out = "{}#{}".format(self.dump_meta(), out)
        return out




class Worker():
    """
    A queue-based multiple threading framework for sending
    parallel requests
    """

    def __init__(self, fout, rd_id, work_no, func):
        self.fout = fout
        self.work_no = work_no
        self.rd_id = rd_id
        self.func = func
        self.subrd_id = 0
        self.q = Queue(10000)
        self.task_no = 0

    def set_rdid(self, _id):
        self.rd_id = _id

    def set_subrdid(self, _id):
        self.subrd_id = _id

    def clear_queue(self):
        with self.q.mutex:
            self.q.queue.clear()

    def run_task(self, task):
        while True:
            work_id, para = self.q.get()

            res = task(para)
            _entry = "{}#{}#{}#{}#{}\n".format(
                self.rd_id, self.subrd_id, self.task_no, work_id, res)
            open(self.fout, "a").write(_entry)
            # print res
            self.q.task_done()

    def init(self):
        for i in range(self.work_no):
            t = Thread(target=self.run_task, args=(self.task,))
            t.daemon = True
            t.start()

    def add_tasks(self, para_list):
        self.task_no = len(para_list)
        self.subrd_id += 1
        try:
            for i in xrange(self.task_no):
                para = para_list[i]
                work_id = i
                self.q.put((work_id, para))
            self.q.join()
        except KeyboardInterrupt:
            sys.exit(1)

    def task(self, para):
        """
        Customized your task here
        """
        res = self.func(*para)
        return res

