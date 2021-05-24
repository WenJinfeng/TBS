import azure.functions as func
import subprocess
import json
import logging

def network_test(server_ip, server_port, test_time, reverse):
    reverse_option = ""
    if reverse:
        reverse_option = "R"

    sp = subprocess.Popen(["./iperf3",
                           "-c",
                           server_ip,
                           "-p",
                           str(server_port),
                           reverse_option,
                           "-t",
                           test_time,
                           "-Z",
                           "-J"
                           ],
                          stdout=subprocess.PIPE,
                          stderr=subprocess.PIPE)

    out, err = sp.communicate()

    end = json.loads(out)["end"]

    sender = end["sum_sent"]
    receiver = end["sum_received"]

    send_mbit_s = sender["bits_per_second"] / kilo / kilo / byte
    recv_mbit_s = receiver["bits_per_second"] / kilo / kilo / byte

    return send_mbit_s, recv_mbit_s

def str2bool(v):
    return v.lower() in ("yes","true","t","1")

def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:
    server_ip = req.params.get('server_ip')
    server_port = int(req.params.get('server_port'))
    test_time = req.params.get('test_time')
    reverse = str2bool(req.params.get('reverse'))

    send_mbit_s, recv_mbit_s = network_test(server_ip, server_port, test_time, reverse)

    result = json.dumps({'send_mbit_s': send_mbit_s, 'recv_mbit_s': recv_mbit_s, "invocationid":context.invocation_id})
    logging.info('execute duration:' + result)
    
    return func.HttpResponse(result)