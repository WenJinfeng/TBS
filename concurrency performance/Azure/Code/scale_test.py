from utils import *
import os
import subprocess
import sys
import json
import time
import urllib2

def run_cmd(cmd):
    """
    The simplest way to run an external command
    """
    return os.popen(cmd).read()

# execute command with added "Y"
def excuteCmd(cmd, addcmd, timeout = 1):
        s = subprocess.Popen(cmd,stdin=subprocess.PIPE, stdout=subprocess.PIPE, shell = True) 
        s.stdin.write(addcmd+'\n')
        out, err = s.communicate()
        if err is not None:
            return err
    
        return out


def scale_test(
        rd_id,
        runtime,
        max_concurrent,
        sleep_tm,
        wait_tm,
        log_file):


    # func_prex = "scale"
    # func_name = func_prex + str(int(time.time() * 1000))[-8:]
    # cmd = "az functionapp create --resource-group resourcewjf --consumption-plan-location eastus --runtime python --runtime-version 3.6 --functions-version 3 --name %s --storage-account storagewjf --os-type linux" % (func_name)
    # run_cmd(cmd)


    # cmd = "func azure functionapp publish %s" % ("scale31981464")
    # run_cmd(cmd)



    # response = urllib2.urlopen("https://%s.azurewebsites.net/api/httpexample?name=%s" % ("scale31981464","4"))
    # print(response.read())
    # cont = response.read()
    # print(type(cont))

    # cmd = "az functionapp delete --name %s --resource-group resourcewjf" % ("appwjf")
    # run_cmd(cmd)


    #func_handler = "handler"
    func_prex = "scale"

    # #run_cmd("gcloud functions deploy %s --entry-point %s --runtime %s --memory %sMB --timeout 300s --region us-east1 --trigger-http --allow-unauthenticated" % ("wjf", func_handler, runtime, mem_size))
    # # a = 3
    # # para= json.dumps("{\"name\": %s}" % (a))
    # # print(run_cmd("gcloud functions call %s --data %s" % ("hello", para)))
    # # cmd = "gcloud functions delete %s" % ("function-3")
    # # excuteCmd(cmd, "Y")



    step = 5

    def worker_func(fop, **args):
        para = str(sleep_tm)
        res = fop.send_one_request(para)
        return res

    # # the third value of worker is tread pool number (maximum)
    exp = Worker(log_file, rd_id, max_concurrent +1 , worker_func)
    exp.init()




    for con_no in xrange(0, max_concurrent + 1, step):
        if con_no == 0:
            con_no = 1
        para_list = []
        fops = []

        func_name = func_prex + str(int(time.time() * 1000))[-8:]
        fop = FuncOp(
            "resourcescale",
            "storagescaletest",
            "eastus",
            runtime,
            func_name)

        #fop.del_function()
        fop.create_function()
    
        for i in xrange(con_no):
            para = (fop,)
            para_list.append(para)
            fops.append(fop)

        exp.add_tasks(para_list)
        exp.clear_queue()
        
        fop.del_function()

        time.sleep(wait_tm)

def main():
    # default python
    runtime_list = ['3.6']
    rd_id = 1
    # log_file = "../azure_result_1.log"
    log_file = "../result/azure_cold_5.log"
    max_concurrent =25
    sleep_tm = 10
    wait_tm = 20
    open(log_file, "a")
    for rd_id in xrange(1):
        for runtime in runtime_list:
            scale_test(
                rd_id,
                runtime,
                max_concurrent,
                sleep_tm,
                wait_tm,
                log_file)



if __name__ == '__main__':
    main()
