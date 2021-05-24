import json
import time
from flask import escape


TESTS = 100

def fibonacci(index):
    """
      Recursive function that calculates Fibonacci sequence.
      :param index: the n-th element of Fibonacci sequence to calculate.
      :return: n-th element of Fibonacci sequence.
      """
    if index <= 1:
        return index
    return fibonacci(index - 1) + fibonacci(index - 2)

def function_handler(request):
    request_json = request.get_json(silent=True)
    num = int(request_json['index'])
    start_time = time.time()*1000
    for _ in range(TESTS):
        fibonacci(num)
    duration = (time.time()*1000 - start_time)/TESTS
    print("execute duration: " + json.dumps({"duration":duration,"executionId":request.headers['Function-Execution-Id']}))
    return json.dumps({"duration":duration,"executionId":request.headers['Function-Execution-Id']})
