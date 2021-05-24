# TBS

The code is mainly the actual runtime performance experiments of quantitative analysis.

First, we quantitatively analyze their startup latency influencing the runtime performance from three key factors, i.e., programming languages, memory sizes, and package sizes. 

Second, we quantitatively measure their resource efficiency through a series of benchmarks with various resource demands. Benchmarks contain two types of microbenchmarks and macrobenchmarks. Microbenchmarks consist of a set of simple workloads focusing on specific resource consumption, such as CPU, memory, network, disk IO, etc. Macrobenchmarks consist of a set of real-world representative applications consuming various resources, e.g., multimedia data process, MapReduce, machine-learning-based serving, etc.

Finally, we quantitatively explore their concurrency performance under different concurrency numbers on these serverless computing platforms. Meanwhile, we try to find the potential causes influencing concurrency performance via analyzing their scalability strategy and load balancing in a black-box fashion. 

In our study, the result of quantitative analysis is as follows.
![image](https://user-images.githubusercontent.com/51308506/119304485-e13fb900-bc99-11eb-8e6b-43c3b6bcac26.png)

![image](https://user-images.githubusercontent.com/51308506/119304523-f1579880-bc99-11eb-9a23-12063efa51c6.png)
