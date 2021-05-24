
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
import subprocess




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

# execute command with added "Y"
def excuteCmd(cmd, addcmd, timeout = 1):
    #print("process delete")
    s = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, shell = True) 
    s.stdin.write(addcmd+'\n')
    out, err = s.communicate()
    if err is not None:
        return err
    
    return out

class FuncOp():
    """
    The class for function operation

    """

    def __init__(
            self,
            region,
            runtime,
            memory,
            func_name):
        self.region = region
        self.runtime = runtime
        self.memory = memory
        self.func_name = func_name

    def get_func_name(self):
        return self.func_name

    def set_func_runtime(self, runtime):
        self.runtime = runtime

    def set_func_memory(self, memory):
        self.memory = memory

    def set_func_name(self, name):
        self.func_name = name



    def del_function(self):
        try:
            #print(time.time())
            cmd = "gcloud functions delete %s --region %s" % (self.func_name, self.region)
            excuteCmd(cmd, "Y")
            #print(time.time())
            return True
        except Exception as e:
            # print str(e)
            return False

    def create_function(self, func_handler):
        """
        Create a new function

        Args:
                src_file: the DIRECTORY for the code
                all the files under the directory will be zipped
                func_handler: the name of the function entry point
        """
        try:
            #print(time.time())
            run_cmd("gcloud functions deploy %s --entry-point %s --source code/ --runtime %s --memory %sMB --timeout 300s --region %s --trigger-http --allow-unauthenticated" % (self.func_name, func_handler, self.runtime, self.memory, self.region))
            #print(time.time())
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

    def send_one_request(self, req_para):
        #time.sleep(random.randint(0,3))
        tm_st = time.time() * 1000
        
        try:
            resp = run_cmd("gcloud functions call %s --region %s --data %s" % (self.func_name, self.region, req_para))
            tm_ed = time.time() * 1000
            resp = resp.strip("\n").split("result: ")[1].strip("\\\n  \\")
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
