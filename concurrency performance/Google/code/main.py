import json
import time
import os
import traceback

from stats import *

def handler(request):

    tm_st = time.time() * 1000
    request_json = request.get_json()

    wait_util = int((time.time() + int(request_json["sleep"])) * 1000)

    try:
        res = stat_basic(argv=1)
    except BaseException:
        res = 'find error! ' + traceback.format_exc()
            #res = None
        # collect all results
    basic_info = str(res)


    while time.time() * 1000 < wait_util:
        continue
        
    tm_ed = time.time() * 1000

    # record coldstart time
    timing_info = "{}#{}#{}".format(fstr(tm_st), fstr(tm_ed), fstr(tm_ed - tm_st))

    # wjf = os.environ
    res = "{}#{}".format( basic_info, timing_info)

    return res
