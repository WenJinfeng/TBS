# Image Processing

**Library** : Pillow

+ aws : build your deployment package
```
pillow
```

+ google : requirements.txt
```
pillow
google-cloud-storage
```
+ azure : requirements.txt
```
azure-functions==1.0.0b3
azure_storage_blob==1.0.0
pillow
```

**Workload Input**: Image
**Workload Output**: Image

**Lambda Payload**(test-event) example:
```
{
    "input_bucket": [INPUT_BUCKET_NAME],
    "object_key": [IMAGE_FILE_NAME],
    "output_bucket": [OUTPUT_BUCKET_NAME],
}
```

**Lambda Output** : latency