# Video Processing

**Library** : opencv-python

+ aws : build your deployment package
```
opencv-python
```

+ google : requirements.txt
```
opencv-python
google-cloud-storage
```
+ azure : requirements.txt
```
azure-functions==1.0.0b3
azure_storage_blob==1.0.0
opencv-python
```

**Workload Input**: Video
```aidl
src_bucket
blob_name
dst_bucket
```
**Workload Output**: Video, latency
