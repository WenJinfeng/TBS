from utils import *
import os
import subprocess
import sys
import json



def scale_test(
        rd_id,
        runtime,
        mem_size,
        max_concurrent,
        sleep_tm,
        wait_tm,
        log_file):

    # cmd = "gcloud functions delete %s --region us-east1" % ("scale81026000")
    # excuteCmd(cmd, "Y")
    # cmd = "gcloud functions delete %s --region us-east1" % ("scale83470842")
    # excuteCmd(cmd, "Y")


    func_handler = "handler"
    func_prex = "scale"


    step = 5

    def worker_func(fop, **args):
        para = json.dumps("{\"sleep\": %s}" % (sleep_tm))
        res = fop.send_one_request(para)
        return res

    # # the third value of worker is tread pool number (maximum)
    exp = Worker(log_file, rd_id, max_concurrent +1 , worker_func)
    exp.init()

    for con_no in xrange(max_concurrent, max_concurrent + 1, step):
        if con_no == 0:
            con_no = 1
        para_list = []
        fops = []
        
        func_name = func_prex + str(int(time.time() * 1000))[-8:]
        fop = FuncOp(
        "us-east1",
        runtime,
        mem_size,
        func_name)
        #fop.del_function()
        fop.create_function(func_handler)
        

        for i in xrange(con_no):
            para = (fop,)
            para_list.append(para)
            fops.append(fop)

        exp.add_tasks(para_list)
        exp.clear_queue()
        
        fop.del_function()

        time.sleep(wait_tm)

def main():
    runtime_list = ['python37']
    mem_list = [4096]
    rd_id = 1
    log_file = "google_4096MB_2.log"
    # log_file = "google_tmp.log"
    max_concurrent = 20
    sleep_tm = 10
    wait_tm = 20
    open(log_file, "a")
    for rd_id in xrange(1):
        for runtime in runtime_list:
            for mem_size in mem_list:
                scale_test(
                    rd_id,
                    runtime,
                    mem_size,
                    max_concurrent,
                    sleep_tm,
                    wait_tm,
                    log_file)



if __name__ == '__main__':
    main()
