import azure.functions as func
from numpy import matrix, linalg, random, amax
from time import time
import json
import logging



def linpack(N):
    eps = 2.22e-16

    ops = (2.0*N)*N*N/3.0+(2.0*N)*N

    # Create AxA array of random numbers -0.5 to 0.5
    A = random.random_sample((N, N))-0.5
    B = A.sum(axis=1)

    # Convert to matrices
    A = matrix(A)

    B = matrix(B.reshape((N, 1)))

    start = time()
    X = linalg.solve(A, B)
    latency = time() - start

    mflops = (ops*1e-6/latency)

    result = {
        'mflops': mflops,
        'latency': latency
    }

    return result


def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:
    N = int(req.params.get('N'))
    resulttmp = linpack(N)
    resulttmp["invocationid"] = context.invocation_id
    result = json.dumps(resulttmp)
    logging.info('execute duration:' + result)
    return func.HttpResponse(result)
