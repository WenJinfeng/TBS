## Platforms

----
| **Platform**              |  AWS Lambda  | Azure Function  | Google Cloud Function  | Aliyun Cloud Function |
|-----------------------|--------|--------|--------|--------|


## Memory

- **AWS Lambda**: Allowed values from 128MB to 3008MB. By default, a new function is limited to 128MB of memory. 
- **Google cloud function**: Allowed values are: 128MB, 256MB, 512MB, 1024MB, and 2048MB. By default, a new function
 is limited to 256MB of memory. When deploying an update to an existing function, the function will keep its old
  memory limit unless you specify this flag.
- **Azure**: In contrast to AWS and Google cloud service, users cannot set memory size in the Microsoft cloud service
; hence, we show the latency of Microsoft service only at the pre-configured 2GB memory bar.


## Language and Runtime

Python 3.7, Nodejs

- **AWS Lambda**: Nodejs, Python, Java
- **Google cloud function**: nodejs8: Node.js 8, nodejs10: Node.js 10, python37: Python 3.7, python38: Python 3.8
, go111: Go 1.11, go113: Go 1.13
- **Azure**: Nodejs, python, Java, C#

## Timeout

- **AWS Lambda**: no more than 900s
- **Google cloud function**: no more than 540s
- **Azure**: no more than 300s


## Idle instance lifetime


After that request is processed, the instance stays alive to be reused for subsequent requests.

The strategy for reuse differs very between the cloud vendors:

|Platform|Idle instance lifetime|
|-----------------------|--------|
|AWS Lambda	|10 minutes|
|Azure Functions	|Mostly 20 minutes|
|Platform|Idle instance lifetime|
|Google Cloud Functions	|Anywhere between 3 minutes and 3+ hours|
|Aliyun Cloud Function	||

AWS and Azure have the policies to recycle an idle instance after a fixed period, 10 and 20 minutes respectively. GCP employ some other strategies to determine the threshold, potentially based on the current demand-supply balance of their resource pools.


## Trigger type

- **AWS Lambda**: bucket, http...
- **Google cloud function**: bucket, http...
- **Azure**: bucket, http...

## Concurrency/Max instance/Scale

- **AWS Lambda**: In AWS Lambda, each parallel request is handled by its own instance.
- **Google cloud function**: Sets the maximum number of instances for the function with flag --max-instances=MAX_INSTANCES. A function execution that
 would exceed max-instances times out.  The environment running a function instance is typically resilient and reused by subsequent function invocations, unless the number of instances is being scaled down (due to lack of ongoing traffic), or your function crashes. This means that when one function execution ends, another function invocation can be handled by the same function instance. Therefore, it is recommended to cache state across invocations in global scope where possible. Your function should be still prepared to work without this cache available as there is no guarantee that the next invocation will reach the same function instance
- **Azure**: In Azure, an instance can and will process multiple requests at the same time. Of course, at some amount of parallel requests, a new instance will be required, but they can provision a new instance while handling all current requests by the existing instance(s). 

[ref](https://blog.binaris.com/from-0-to-1000-instances/)

## Package Size

Larger packages cause a significant slowdown of the cold start. Adding dependencies and thus increasing the deployed package size will further increase the cold start durations.


## Cold start
For the platform, a coldstart may involve launching a new container, setting up the runtime environment, and
 deploying a function, which will take more time to handle a request than reusing an existing function instance (warmstart). Thus, coldstarts can significantly affect application responsiveness and, in turn, user experience.



## deploy time

size/dependencies


## 函数运行资源限制


### aliyun

|限制项	| 资源上限 |
|临时磁盘空间（/tmp空间）|	512 MB|
|文件描述符	| 1024|
|进程和线程总数	| 1024|
|函数最大申请内存 |	3 GB|
|函数最大运行时间 |	600s|
|Initializer最大运行时间 |300s|
|函数同步调用响应正文有效负载大小 |	6 MB|
|函数异步调用请求正文有效负载大小 |	128 KB|
|代码部署包大小（压缩为ZIP或JAR文件）|	50 MB|
|原始代码大小	|500 MB|


[Quotas for google cloud function](https://cloud.google.com/functions/quotas)

[Code start for google cloud function](https://mikhail.io/serverless/coldstarts/gcp/)

[Code start optimization for google cloud function](https://medium.com/@duhroach/improving-cloud-function-cold-start-time-2eb6f5700f6)

[Code start analysis for popular serverless platform](https://mikhail.io/serverless/coldstarts/big3/)

[阿里云资源使用限制](https://help.aliyun.com/document_detail/51907.html?spm=a2c4g.11186623.6.751.1bd75503YTkcSl)