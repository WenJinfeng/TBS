import azure.functions as func
from azure.storage.blob import BlockBlobService
import json
import logging
from time import time
import os
import _pickle as pickle
import numpy as np
import torch

from . import rnn


tmp = "/tmp/"


def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:

    language = req.params.get('language')
    start_letters =  req.params.get('start_letters')

    acc_name = req.params.get('account_name')
    acc_key = req.params.get('account_key')
    container_name = req.params.get('container_name')

    model_parameter_blob_name =  req.params.get('model_parameter_blob_name')  # example : rnn_params.pkl
    model_blob_name =  req.params.get('model_blob_name')  # example : rnn_model.pth

    block_blob_service = BlockBlobService(account_name=acc_name, account_key=acc_key)

    # Load pre-processing parameters
    # Check if model parameters are available
    parameter_path = tmp + model_parameter_blob_name
    if not os.path.isfile(parameter_path):
        block_blob_service.get_blob_to_path(container_name, model_parameter_blob_name,parameter_path)

    with open(parameter_path, 'rb') as pkl:
        params = pickle.load(pkl)

    all_categories = params['all_categories']
    n_categories = params['n_categories']
    all_letters = params['all_letters']
    n_letters = params['n_letters']

    # Check if models are available
    # Download model from S3 if model is not already present
    model_path = tmp + model_blob_name
    if not os.path.isfile(model_path):
        block_blob_service.get_blob_to_path(container_name, model_blob_name, model_path)

    rnn_model = rnn.RNN(n_letters, 128, n_letters, all_categories, n_categories, all_letters, n_letters)
    rnn_model.load_state_dict(torch.load(model_path))
    rnn_model.eval()

    start = time()
    output_names = list(rnn_model.samples(language, start_letters))
    latency = time() - start
    result = json.dumps({'latency': latency, 'predict': output_names, "invocationid":context.invocation_id})
    logging.info("execute duration: " + result)
    return func.HttpResponse(result)