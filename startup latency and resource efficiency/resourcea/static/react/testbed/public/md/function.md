AWS Lambda: system info

python 3.7
```json
{"cpuCountLogical": 2, "cpuCount": 2, "memory": 649011200, "sysVersion": ["", "", ""], "sysKernel": "4.14.165-102.205.amzn2.x86_64", "sysArch": "x86_64"}
```
python3.8
```json
{"cpuCountLogical": 2, "cpuCount": 2, "memory": 649011200, "sysVersion": ["", "", ""], "sysKernel": "4.14.165-102.205.amzn2.x86_64", "sysArch": "x86_64"}
```


Azure
python 3.7
```json
{
  "instance": true,
  "systeminfo": {
    "cpuCountLogical": 2,
    "cpuCount": 1,
    "memory": 2091905024,
    "sysVersion": [
      "debian",
      "9.12",
      ""
    ],
    "sysKernel": "4.19.104-microsoft-standard",
    "sysArch": "x86_64"
  },
  "invocationid": "09c6bd37-48d8-4d65-92bf-f904e3404af0"
}
```

Google Cloud function


128 MB
```json
{"executionId": "ddcoduo3e475", "instance": true, "systeminfo": {"cpuCountLogical": 2, "cpuCount": null, "memory": 2147483648, "memory_used": 32907264, "memory_free": 2098843648, "memory_percent": 2.3, "sysVersion": ["debian", "buster/sid", ""], "sysKernel": "4.4.0", "sysArch": "x86_64"}}
```

2048 MB
```json
{"executionId": "gjcltl5az2tq", "instance": true, "systeminfo": {"cpuCountLogical": 2, "cpuCount": null, "memory": 2147483648, "memory_used": 38744064, "memory_free": 2098659328, "memory_percent": 2.3, "sysVersion": ["debian", "buster/sid", ""], "sysKernel": "4.4.0", "sysArch": "x86_64"}}
```
