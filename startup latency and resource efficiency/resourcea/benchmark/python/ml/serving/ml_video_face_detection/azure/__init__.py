import azure.functions as func
from azure.storage.blob import BlockBlobService

import cv2
import json
import logging
from time import time


tmp = "/tmp/"
FILE_NAME_INDEX = 0
FILE_PATH_INDEX = 2


def video_processing(object_key, video_path, model_path):
    file_name = object_key.split(".")[FILE_NAME_INDEX]
    result_file_path = tmp+file_name+'-detection.avi'

    video = cv2.VideoCapture(video_path)

    width = int(video.get(3))
    height = int(video.get(4))
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    out = cv2.VideoWriter(result_file_path, fourcc, 20.0, (width, height))
    face_cascade = cv2.CascadeClassifier(model_path)
    start = time()
    while video.isOpened():
        ret, frame = video.read()
        if ret:
            gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = face_cascade.detectMultiScale(gray_frame, 1.3, 5)
            # print("Found {0} faces!".format(len(faces)))
            for (x, y, w, h) in faces:
                cv2.rectangle(frame, (x, y), (x+w, y+h), (255, 0, 0), 2)
            out.write(frame)
        else:
            break
    latency = time() - start
    video.release()
    out.release()

    return latency, result_file_path

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

    latency, upload_path = video_processing(blob_name, download_path, model_path)

    result = json.dumps({"latency":latency, "invocationid":context.invocation_id})
    #TODO upload the processed video?
    logging.info("execute duration: " + result)

    return func.HttpResponse(result)
