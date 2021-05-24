import azure.functions as func
from azure.storage.blob import BlockBlobService
import json
import logging
from tensorflow.python.keras.preprocessing import image
from tensorflow.python.keras.applications.resnet50 import preprocess_input, decode_predictions
import numpy as np
import uuid
from time import time

from .squeezenet import SqueezeNet


tmp = "/tmp/"

def predict(img_local_path):
    start = time()
    model = SqueezeNet(weights='imagenet')
    img = image.load_img(img_local_path, target_size=(227, 227))
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    x = preprocess_input(x)
    preds = model.predict(x)
    res = decode_predictions(preds)
    latency = time() - start
    return latency, res

def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:
    acc_name = req.params.get('account_name')
    acc_key = req.params.get('account_key')
    container_name = req.params.get('container_name')
    blob_name = req.params.get('blob_name')
    model_blob_name = req.params.get('model_blob_name')

    block_blob_service = BlockBlobService(account_name=acc_name, account_key=acc_key)

    model_path = tmp + model_blob_name
    block_blob_service.get_blob_to_path(container_name, model_blob_name, model_path)

    download_path = tmp + blob_name
    block_blob_service.get_blob_to_path(container_name, blob_name, download_path)

    latency, reg = predict(download_path)
    # logging.info(reg)
    result = json.dumps({"latency":latency, "invocationid":context.invocation_id})
    logging.info("execute duration: " + result)

    return func.HttpResponse(result)