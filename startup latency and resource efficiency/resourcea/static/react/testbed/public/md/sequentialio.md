# sequential io
-----------------------
### IO performance with sequential IO

**Library:** none

+ aws : build your deployment package
```$xslt
```
+ google : requirements.txt
```$xslt
```
+ azure : requirements.txt
```$xslt
azure-functions==1.0.0b3
```
**Input:**
```aidl
{
    "file_size" : [file_size],
    "byte_size" : [byte_size_for_each_io]
}
```

**Output:** 
```aidl
{
    "disk_write_bandwidth":[disk_write_bandwidth],
    "disk_write_latency":[disk_write_latency],
    "disk_read_bandwidth":[disk_read_bandwidth],
    "disk_read_latency":[disk_read_latency]
}
```



