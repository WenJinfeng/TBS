import boto3
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
import boto3
import json

import random
import os
import sys
import copy
import decimal
import subprocess
import urllib2
import socket




def fstr(f):
    """
    Convert a float number to string
    """
    ctx = decimal.Context()
    ctx.prec = 20
    d1 = ctx.create_decimal(repr(f))
    return format(d1, 'f')

def run_cmd(cmd):
    """
    The simplest way to run an external command
    """
    return os.popen(cmd).read()


class FuncOp():
    """
    The class for function operation

    """

    def __init__(
            self,
            resourcegroup,
            storagename,
            region,
            runtime,
            func_name):
        self.resourcegroup = resourcegroup
        self.storagename = storagename
        self.region = region
        self.runtime = runtime
        self.func_name = func_name

    def get_func_name(self):
        return self.func_name

    def set_func_runtime(self, runtime):
        self.runtime = runtime

    def set_func_name(self, name):
        self.func_name = name



    def del_function(self):
        try:
            #print(time.time())
            cmd = "az functionapp delete --name %s --resource-group %s" % (self.func_name, self.resourcegroup)
            run_cmd(cmd)
            print("delete app: {}".format(self.func_name))
            return True
        except Exception as e:
            # print str(e)
            return False

    def create_function(self):

        try:
            #print(time.time())
            print(run_cmd("az functionapp create --resource-group %s --consumption-plan-location %s --runtime python --runtime-version %s --functions-version 3 --name %s --storage-account %s --os-type linux" % (self.resourcegroup, self.region, self.runtime, self.func_name, self.storagename)))
            time.sleep(20)
            print(run_cmd("func azure functionapp publish %s" % (self.func_name)))
            print(run_cmd("az functionapp update --set clientCertEnabled=false --name %s --resource-group %s" % (self.func_name, self.resourcegroup)))
            print("create app: {}".format(self.func_name))
            return True
        except Exception as e:
            print str(e)
            return False


    def dump_meta(self):
        """
        The basic information to record
        """
        return "{}#python{}#{}".format(
            self.region,
            self.runtime,
            self.func_name)

    def send_one_request(self, req_para):
        #time.sleep(random.randint(0,3))
        tm_st = time.time() * 1000
        resp = "no_change"
        try:
            socket.setdefaulttimeout(600)
            url = "https://%s.azurewebsites.net/api/httpexample?name=%s" % (self.func_name,req_para)
            hdr = {'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36'}
            req = urllib2.Request(url, headers=hdr)
            resp = urllib2.urlopen(req, None, 600)
            resp =resp.read()

            # cmd = "curl -s \'https://%s.azurewebsites.net/api/httpexample?name=%s\'" % (self.func_name,req_para)
            # resp = run_cmd(cmd)
            #resp = resp.strip("\n").split("result: ")[1].strip("\\\n  \\")
        except Exception as e:
            print str(e)
            # return "retry"
        if not resp:
            resp = "ERROR"
        tm_ed = time.time() * 1000
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
        # if res == "retry":
        #     res = self.func(*para)
        return res
