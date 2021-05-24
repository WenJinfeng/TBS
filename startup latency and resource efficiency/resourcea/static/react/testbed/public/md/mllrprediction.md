# ml_lr_prediction
-----------------------
### Logistic regression prediction

**Library:** storage,scikit-learn,pandas,numpy

+ aws : build your deployment package
```$xslt
pandas
sklearn
```
+ google : requirements.txt
```$xslt
google-cloud-storage
gcsfs
scikit-learn
pandas
numpy
```
+ azure : requirements.txt
```$xslt
azure-functions==1.0.0b3
azure_storage_blob==1.0.0
azure-storage-file==1.0.0
numpy
pandas
scikit-learn
```
**Input:** Text
```aidl
input
dataset_bucket
dataset_blob_name
model_bucket
model_blob_name
```

**Output:** latency, lr model



