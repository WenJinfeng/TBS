import azure.functions as func
from azure.storage.file import FileService

import cv2
import logging
from time import time
import json


FILE_PATH_INDEX = 2

TMP = "/tmp/"

def video_processing(file_name, video_path):
    result_file_path = TMP+file_name+'-output.avi'

    video = cv2.VideoCapture(video_path)

    width = int(video.get(3))
    height = int(video.get(4))

    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    out = cv2.VideoWriter(result_file_path, fourcc, 20.0, (width, height))

    start = time()
    while(video.isOpened()):
        ret, frame = video.read()

        if ret:
            gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            im = cv2.imwrite(TMP + 'tmp.jpg', gray_frame)
            gray_frame = cv2.imread(TMP + 'tmp.jpg')
            out.write(gray_frame)
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

    file_service = FileService(account_name=acc_name, account_key=acc_key)

    download_path = TMP + blob_name
    file_service.get_file_to_path(container_name, None, blob_name, download_path)

    logging.info("Downloading blob to " + download_path)

    latency, upload_path = video_processing(blob_name, download_path)
    file_service.create_file_from_path(container_name, None, upload_path.split("/")[FILE_PATH_INDEX], upload_path)
    result = json.dumps({'latency': latency, "invocationid":context.invocation_id})
    logging.info('execute duration:' + result)
    return func.HttpResponse(result)