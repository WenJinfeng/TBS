import azure.functions as func
from azure.storage.blob import BlockBlobService
import logging
from time import time

import numpy
import tensorflow
from tensorflow.keras.layers import Dense, Flatten, Conv2D
from tensorflow.keras import Model
from PIL import Image
import json


tmp = "/tmp/"

class CustomModel(Model):
    def __init__(self):
        super(CustomModel, self).__init__()
        self.conv1 = Conv2D(32, 3, activation='relu')
        self.flatten = Flatten()
        self.d1 = Dense(128, activation='relu')
        self.d2 = Dense(10, activation='softmax')

    def call(self, x):
        x = self.conv1(x)
        x = self.flatten(x)
        x = self.d1(x)
        return self.d2(x)

def download_blob(block_blob_service, bucket_name, source_blob_name, destination_file_name):
    """Downloads a blob from the bucket."""
    block_blob_service.get_blob_to_path(bucket_name, source_blob_name, destination_file_name)


def main(req: func.HttpRequest, context: func.Context) -> func.HttpResponse:
    acc_name = req.params.get('account_name')
    acc_key = req.params.get('account_key')
    block_blob_service = BlockBlobService(account_name=acc_name, account_key=acc_key)
    model = None




    bucket = req.params.get['bucket']
    class_names = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat',
                   'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']
    # Model load which only happens during cold starts
    if model is None:
        download_blob(block_blob_service, bucket, 'fashion_mnist_weights.index', '/tmp/fashion_mnist_weights.index')
        download_blob(block_blob_service, bucket, 'fashion_mnist_weights.data-00000-of-00001', '/tmp/fashion_mnist_weights.data-00000-of-00001')
        model = CustomModel()
        model.load_weights('/tmp/fashion_mnist_weights')

    download_blob(block_blob_service, bucket, 'test.png', '/tmp/test.png')
    start = time()
    image = numpy.array(Image.open('/tmp/test.png'))
    input_np = (numpy.array(Image.open('/tmp/test.png'))/255)[numpy.newaxis,:,:,numpy.newaxis]
    predictions = model.call(input_np)
    latency = time() - start
    result = json.dumps({"latency":latency, "predict": class_names[numpy.argmax(predictions)], "invocationid":context.invocation_id})
    logging.info("execute duration: " + result)

    return func.HttpResponse(result)