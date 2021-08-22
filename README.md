# Characterizing Commodity Serverless Computing Platforms

### 1. In qualitative analysis, we explore and summarize the various characteristics described in the official documentation of these serverless computing platforms. These characteristics specify the inherent restrictions of different aspects involving developing, deploying, and executing functions, which may result in fatal failures if developers do not comply with these restrictions. To this end, we construct a taxonomy with respect to the need-to-consider information from three aspects,i.e., development, deployment, and runtime. Such a taxonomy can help developers better understand the supported characteristics of serverless computing platforms to facilitate further development practice.

![image](https://user-images.githubusercontent.com/51308506/130357170-5deb10c0-1aff-4ecb-9c82-7e214dd1a97d.png)

![image](https://user-images.githubusercontent.com/51308506/130357224-e3f01a96-666e-4232-8db8-7188429883ed.png)


### 2. In quantitative analysis, we explore the actual runtime performance of these serverless computing platforms from multiple dimensions, in order to help developers select an appropriate platform based on their application features and improve applications' performance with tuned configurations. Indeed, the overall perceived performance of a serverless application may mainly be influenced by three kinds of latency, i.e., the startup latency of initiating the function instance, the execution latency of running the function, and the scheduling latency of waiting for serving by available instances when the number of requests dramatically increases.

2.1 First, we quantitatively analyze how can programming languages, memory sizes, and package sizes influence the startup latency on different serverless platforms. Startup latency severely affects the responsiveness of serverless applications and may limit the adoption of serverless computing under various applications.

Python, Node.js, Java functions with different memory sizes
![image](https://user-images.githubusercontent.com/51308506/130357279-50fcd528-68a1-4cf1-b72c-50e7b7b6c199.png)

Python functions with 128 MB of memory under the various number of third-party packages (Pillow, Numpy, OpenCV)
![image](https://user-images.githubusercontent.com/51308506/130357352-22ca1c79-d590-40ca-a414-2a931ddbe6f7.png)



2.2 Second, we quantitatively measure the applications' actual runtime performance to compare the underlying resource efficiency of different serverless platforms with a set of well-designed benchmarks. We categorize these benchmarks into two types,i.e., microbenchmarks and macrobenchmarks. Microbenchmarks consist of a set of simple workloads focusing on specific resource consumption, such as CPU, memory, network, disk IO, etc. Macrobenchmarks consist of a set of real-world representative applications, e.g., multimedia data process, MapReduce, machine-learning-based serving, which need to utilize various system resources.

performance for microbenchmarks, e.g., CPU-bound workloads, memory-bound workloads, and diskIO-bound workloads
![image](https://user-images.githubusercontent.com/51308506/130357850-38b245bc-36e8-4911-93f2-9186e2ee7c10.png)

![image](https://user-images.githubusercontent.com/51308506/130357512-2e5cb715-f3b9-4970-b601-d0db45d62dd4.png)


performance for macrobenchmarks, e.g., video processing, machine learning serving
![image](https://user-images.githubusercontent.com/51308506/130357527-b74c79c3-8f4f-4e30-a9e0-e6e9d6848e0d.png)



2.3 Finally, we quantitatively compare the concurrency performance of different serverless computing platforms, i.e., how they perform when dealing with multiple requests due to different auto-scaling features and inherent concurrency limits. A coming request may be throttled if no available function instances can handle it, which results in non-negligible latency. Meanwhile, we try to analyze the potential causes influencing concurrency performance by analyzing and inferring their scalability strategy and load balancing from a black-box perspective. 


We create 20 python-based serverless functions with the same configuration and code but different function names [f1, f2, ..., f20] and invoke each fi with 10xi concurrent requests. Meanwhile, we also explore concurrency performance under various memory sizes.
![image](https://user-images.githubusercontent.com/51308506/130357581-45f30092-05f8-4212-8f3f-a6bc3bd727d6.png)

We use the runtime information (e.g., VMs and function instances) collected by our collection function in TBS, in order to analyze potential causes. Finally, we suppose request scheduling strategies for these serverless computing platforms. More details can be found in our paper.

