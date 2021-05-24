import sys
import os
from google.cloud import storage
import json
from time import time
import _pickle as pickle
import rnn
import numpy as np
import torch
from flask import escape


tmp = "/tmp/"


def download_blob(blob, download_path):
    blob.download_to_filename(download_path)
    # print('Blob {} downloaded to {}.'.format(
    #     blob.name,
    #     download_path))

def upload_blob(bucket_name, blob, upload_path):
    blob.upload_from_filename(upload_path)
    # print('File {} uploaded to {}.'.format(
    #     blob.name,
    #     bucket_name))

def function_handler(request):
    request_json = request.get_json(silent=True)

    language = request_json['language']
    start_letters =  request_json['start_letters']

    container_name = request_json['container_name']

    model_parameter_blob_name =  request_json['model_parameter_blob_name']  # example : rnn_params.pkl
    model_blob_name =  request_json['model_blob_name']  # example : rnn_model.pth

    storage_client = storage.Client()

    # Load pre-processing parameters
    # Check if model parameters are available
    parameter_path = tmp + model_parameter_blob_name
    container = storage_client.get_bucket(container_name)
    if not os.path.isfile(parameter_path):
        model_parameter_blob = container.blob(model_parameter_blob_name)
        download_blob(model_parameter_blob, parameter_path)

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
        model_blob = container.blob(model_blob_name)
        download_blob(model_blob,model_path )

    rnn_model = rnn.RNN(n_letters, 128, n_letters, all_categories, n_categories, all_letters, n_letters)
    rnn_model.load_state_dict(torch.load(model_path))
    rnn_model.eval()

    start = time()
    output_names = list(rnn_model.samples(language, start_letters))
    latency = time() - start
    result = json.dumps({'latency': latency, 'predict': output_names,"executionId":request.headers['Function-Execution-Id']})
    print("execute duration: " + result)
    return result