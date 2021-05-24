from utils import *
import fc2


def scale_test(
        rd_id,
        runtime,
        mem_size,
        max_concurrent,
        sleep_tm,
        wait_tm,
        log_file):
    func_prex = "scale"
    endpoint, aliyun_id, aliyun_key, region, roles = get_config_basic()
    role = roles[0]
    zipped_code_path = os.path.join(os.getcwd(), "tmp.zip")
    func_handler = "index.handler"
    step = 5


    def worker_func(fop, **args):
        para = get_default_req(sleep_tm)
        res = fop.send_one_request(para)
        return res

    # the third value of worker is tread pool number (maximum)
    exp = Worker(log_file, rd_id, max_concurrent + 5, worker_func)
    exp.init()

    src_code_path = os.path.join(os.getcwd(), CODE_PATH[runtime])
    zip_code(zipped_code_path, src_code_path)


    for con_no in xrange(0, max_concurrent + 1, step):
        if con_no == 0:
            con_no = 1
        para_list = []
        fops = []
        
        func_name = func_prex + str(int(time.time() * 1000))[-8:]
        fop = FuncOp(
            endpoint,
            aliyun_id,
            aliyun_key,
            region,
            role,
            runtime,
            mem_size,
            func_name)
        #fop.del_function()
        fop.create_function(zipped_code_path, func_handler)

        for i in xrange(con_no):
            para = (fop,)
            para_list.append(para)
            fops.append(fop)

        exp.add_tasks(para_list)
        exp.clear_queue()
        
        fop.del_function()



        time.sleep(wait_tm)


def main():
    runtime_list = ['python3']
    mem_list = [1024]
    rd_id = 1
    log_file = "ali_1024MB_10.log"
    max_concurrent =100
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
