# Build python deployment package
-----------------------
It is recommended to install dependent packages with requirements.txt, which describes package name and version.

For example, we can specify the **pillow** and **google-cloud-storage** package as shown below.
```aidl
pillow
google-cloud-storage
```


Each platform supports updating function code with a zip file, which contains function code and all dependent packages. We can create the deployment package as shown below:

## Build deployment package

1. Installed the dependent packages 

Install a specified package [xxx]
```js
 pip install xx --upgrade --ignore-installed --no-cache-dir -t ./target_folder package_name
```
Install all dependent packages with requirements.txt file
```aidl
pip install -r requirements.txt --upgrade --ignore-installed --no-cache-dir -t ./target_folder package_name
```

2. Remove unnecessary file

*Some packages may be broken, it is better to check if the trimmed packages can run correctly*
```bash
rm -rf setuptools* wheel* pip* pkg*
find . -type d -name "tests" -exec rm -rf {} +
find -name "*.so" | xargs strip
find -name "*.so.*" | xargs strip
find . -name \*.pyc -delete
```
3. Zip installed packages and function code
```bash
zip -r9 ../[LIBRARY].zip
zip -g [LIBRARY].zip lambda_function.py
```
4. Upload to cloud storage (s3) for further installation
```bash
aws s3 cp [LIBRARY].zip s3://bucket_name/
```

-----------------------


## Build with Oryx

build deployment package with [Oryx](https://github.com/Microsoft/Oryx)


## Publish Azure function and Google cloud function directly

Some platforms provide users with the ability to build deployment package with remote support, which will automatically generate the deployment package in remote server.

For example, we can use following command to publish an function app within the directory that contains function code and requirements.txt (describe the dependencies)

```aidl
func azure functionapp publish funname
```
-----------------------


Deploy google cloud function with following command:

```aidl
gcloud functions deploy
```


[azure-functions-python-samples](https://github.com/yokawasa/azure-functions-python-samples/blob/master/v2functions/requirements.txt)