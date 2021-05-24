import json
import time


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

def handler(event, context):
    start_time = time.time()*1000
    for _ in range(TESTS):
        fibonacci(event['index'])
    duration = (time.time()*1000 - start_time)/TESTS
    print('execute duration:' + json.dumps({"duration":duration}))
    return json.dumps({"duration":duration})

def test_locally():
    with open('input.json', 'r') as input_data:
        data = json.load(input_data)
        print(handler(data, None))
if __name__ == '__main__':
    test_locally()