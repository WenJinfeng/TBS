import azure.functions as func
import json
import time
import logging

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

def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:
    num = int(req.params.get('index'))
    start_time = time.time()*1000
    for _ in range(TESTS):
        fibonacci(num)
    duration = (time.time()*1000 - start_time)/TESTS
    result = json.dumps({"duration":duration, "invocationid":context.invocation_id})
    logging.info("execute duration: " + result)
    return func.HttpResponse(result)