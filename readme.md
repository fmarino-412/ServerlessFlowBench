# Serverless Composition Performance Project

Computer Engineering Master's Degree final project @ [University of Rome 'Tor Vergata'](https://en.uniroma2.it/)

Author: [Francesco Marino](https://github.com/francescom412)

Academic Year: 2019/2020

The **Serverless Composition Performance Project** is a framework that allows users to:

* deploy **serverless functions** to Amazon Web Services, Google Cloud Platform and Open Whisk (already defined functions are available),
* deploy **serverless function compositions** to Amazon Web Services, Google Cloud Platform and Open Whisk (as before, altready defined compositions are available) and
* perform HTTP **bechmarks** on deployed functions and compositions.

---

<h2>Execution Requirements</h2>

* [Java Developer Kit (JDK) version 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) (recommended) or newer
* [Docker Desktop](https://www.docker.com/products/docker-desktop)
* [Amazon Web Services](https://aws.amazon.com/console/) valid account that can access to the following services:
	* [AWS Lambda](https://aws.amazon.com/lambda/),
	* [Amazon API Gateway](https://aws.amazon.com/api-gateway/),
	* [AWS Step Functions](https://aws.amazon.com/step-functions/),
	* [Amazon S3](https://aws.amazon.com/it/s3/),
	* [Amazon Rekognition](https://aws.amazon.com/rekognition/) and
	* [Amazon Translate](https://aws.amazon.com/translate/).
* [Google Cloud Platform](https://cloud.google.com/) valid account with, at least, the following enabled:
	* [Build API](https://cloud.google.com/cloud-build),
	* [Workflows \[BETA\] API](https://cloud.google.com/workflows),
	* [Cloud Vision API](https://cloud.google.com/vision) and
	* [Translate API](https://cloud.google.com/translate).
* [Open Whisk](https://openwhisk.apache.org/) running deployment
* \[OPTIONAL\] [Azure](https://azure.microsoft.com/) valid and active account with, at least, the following enabled:
	* [Vision API](https://azure.microsoft.com/services/cognitive-services/computer-vision/) and
	* [Face API](https://azure.microsoft.com/services/cognitive-services/face/).

**Please note**: the framework remains usable even with just 1 or 2, out of the 3, serverless platform(s) available.

---

<h2>Project structure description</h2>

<h2>Java Project structure description</h2>

The entire project part was developed using [JetBrains' IntelliJ IDEA](https://www.jetbrains.com/idea/) so it is recommended to open it using this IDE for better code navigation.

In the main folder is located the class [`ServerlessBenchmarkToolMain.java`](src/main/java/ServerlessBenchmarkToolMain.java), this is the application entry point that allows the user to:

1. deploy serverless functions,
2. deploy serverless compositions,
3. optionally deploy of elements needed by the previous entities to work (e.g. cloud buckets),
4. perform benchmarks on functions and compositions,
5. deploy serverless functions that collect information about their execution environment and
6. remove every entity previously deployed.

### [cmd package](src/main/java/cmd)

This package contains classes for shell commands execution grouped by functionality type.

In the main folder there are:

* [`CommandExecutor.java`](src/main/java/cmd/CommandExecutor.java), an abstract class providing common functions needed for shell command execution,
* [`CommandUtility.java`](src/main/java/cmd/CommandUtility.java), an abstract class providing common functions and elements needed for shell command building and
* [`StreamGobbler.java`](src/main/java/cmd/StreamGobbler.java) used for executing shell command output collection.

#### [cmd.benchmark\_commands package](src/main/java/cmd/benchmark_commands)

* [`BenchmarkCommandExecutor.java`](src/main/java/cmd/benchmark_commands/BenchmarkCommandExecutor.java) needed to execute load benchmarks, cold start benchmarks and collect results,
* [`BenchmarkCommandUtility.java`](src/main/java/cmd/benchmark_commands/BenchmarkCommandUtility.java) needed to build shell commands for load benchmarks execution using [wrk2](https://github.com/giltene/wrk2) and
* [output\_parsing package](src/main/java/cmd/benchmark_commands/output_parsing) containing utilities to parse benchmarks results:
	* [`BenchmarkCollector.java`](src/main/java/cmd/benchmark_commands/output_parsing/BenchmarkCollector.java) needed to parse [wrk2](https://github.com/giltene/wrk2) benchmarks results and
	* [`BenchmarkStats.java`](src/main/java/cmd/benchmark_commands/output_parsing/BenchmarkStats.java) needed to collect [wrk2](https://github.com/giltene/wrk2) benchmarks results.

#### [cmd.docker\_daemon\_utility package](src/main/java/cmd/docker_daemon_utility)

* [`DockerException.java`](src/main/java/cmd/docker_daemon_utility/DockerException.java) raised when a Docker daemon execution related error occurs and 
* [`DockerExecutor.java`](src/main/java/cmd/docker_daemon_utility/DockerExecutor.java) needed to check Docker containers correct configuration, Docker images presence and Docker composition running.

#### [cmd.functionality\_commands package](src/main/java/cmd/functionality_commands)

* [`AmazonCommandUtility.java`](src/main/java/cmd/functionality_commands/AmazonCommandUtility.java) used to create [Amazon Web Services CLI](https://aws.amazon.com/cli/) shell commands,
* [`GoogleCommandUtility.java`](src/main/java/cmd/functionality_commands/GoogleCommandUtility.java) used to create [Google CLoud Platform CLI](https://cloud.google.com/sdk/gcloud) shell commands,
* [`OpenWhiskCommandUtility.java`](src/main/java/cmd/functionality_commands/OpenWhiskCommandUtility.java) used to create [Open Whisk CLI](https://github.com/apache/openwhisk-cli) shell commands,
* [`BucketsCommandExecutor.java `](src/main/java/cmd/functionality_commands/BucketsCommandExecutor.java) used to execute cloud buckets related commands,
* [`CompositionCommandExecutor.java `](src/main/java/cmd/functionality_commands/CompositionCommandExecutor.java) used to execute serverless compositions related commands,
* [`FunctionCommandExecutor.java `](src/main/java/cmd/functionality_commands/FunctionCommandExecutor.java) used to execute serverless functions related commands,
* [`TablesCommandExecutor.java `](src/main/java/cmd/functionality_commands/TablesCommandExecutor.java) used to execute cloud NoSQL storage related commands,
* [`IllegalNameException.java `](src/main/java/cmd/functionality_commands/IllegalNameException.java) raised when a malformed name is attempted to be assigned to a resource,
* [output\_parsing package](src/main/java/cmd/functionality_commands/output_parsing) containing utilities to parse command outputs:
	* [`ReplyCollector.java `](src/main/java/cmd/functionality_commands/output_parsing/ReplyCollector.java) used to collect console command execution output and
	* [`URLFinder.java `](src/main/java/cmd/functionality_commands/output_parsing/URLFinder.java) used to collect deployment url from console command execution output and
* [security package](src/main/java/cmd/functionality_commands/security) containing security utilities:
	* [`GoogleAuthClient.java `](src/main/java/cmd/functionality_commands/output_parsing/GoogleAuthClient.java) used to authenticate [Google Cloud Workflows \[BETA\]](https://cloud.google.com/workflows) executions urls.

### [databases package](src/main/java/databases)

This package contains classes needed for external databases interaction.

#### [cmd.influx package](src/main/java/databases/influx)

* [`InfluxClient.java `](src/main/java/databases/influx/InfluxClient.java) used to export benchmark results to the time series database [InfluxDB](https://www.influxdata.com/products/influxdb/).

#### [cmd.mysql package](src/main/java/databases/mysql)

* [`CloudEntityData.java `](src/main/java/databases/mysql/CloudEntityData) used to collect functions, compositions, bucket and NoSQL table information,
* [`DAO.java `](src/main/java/databases/mysql/DAO.java), an abstract class providing common information and methods needed by database access objects,
* [`FunctionalityURL.java `](src/main/java/databases/mysql/FunctionalityURL.java) used to collect resource deployment url,
* [`MySQLConnect.java `](src/main/java/databases/mysql/MySQLConnect.java) used to connect and disconnect [MySQL database](https://www.mysql.com/),
* [daos package](src/main/java/databases/mysql/daos) containing database access objects implementations:

### [utility package](src/main/java/utility)

---

<h2>User specific required files</h2>

Authentication files related to user's active services required to run the application (the ones used in the development process were excluded using `.gitignore` file for privacy related reasons).

### Amazon Web Services
A file named `credentials` is required [`serverless_functions/aws/.aws`](serverless_functions/aws/.aws), it should contain AWS account access key and secret. This file has the following structure:

```
[default]
aws_access_key_id=xxxxxxxxxx
aws_secret_access_key=xxxxxxxxxx
```
It can be downloaded from AWS Console &#8594; My Security Credentials (in the account menu) &#8594; Access Keys &#8594; New Access Key.

### Google Cloud Platform
A file named `credentials.json` is required in [`serverless_functions/gcloud/.credentials`](serverless_functions/gcloud/.credentials), it should contain a Google Cloud Platform service account related info. This file has the following structure:

```json
{
  "type": "service_account",
  "project_id": "id of the Google Cloud Platform project",
  "private_key_id": "xxxxxxxxxxxxxxx",
  "private_key": "-----BEGIN PRIVATE KEY-----\nxxxxxxxxxxxxxxxxxx\n-----END PRIVATE KEY-----\n",
  "client_email": "xxxxxxxxxx@xxxxx.xxx",
  "client_id": "xxxxxxxxxxxxxxx",
  "auth_uri": "https://xxxxxxxxxxxxx",
  "token_uri": "https://xxxxxxxxxx",
  "auth_provider_x509_cert_url": "https://xxxxxxxxxx",
  "client_x509_cert_url": "https://xxxxxxxxxxx"
}
```
It can be downloaded from Google Cloud Platform Console &#8594; API and services (in the side menu) &#8594; Credentials &#8594; Service accounts (selecting the one with desired authorizations) &#8594; New key.

### Azure API in Open Whisk \[optional\]
These files are needed only if the user needs to execute **benchmarks** on **Open Whisk** for the originally defined **anger detection workflows**. Being every file specific for each function, several versions of this information are needed. Strings needed to fill these files can be found from Azure Console &#8594; Resources (in the side menu) &#8594; Choose the specific Cognitive Service resource &#8594; Keys and endpoints.

<h4>Java:</h4> 

In [`serverless_functions/openwhisk/java/face_recognition/anger_detection/src/main/java/anger_detection`](serverless_functions/openwhisk/java/face_recognition/anger_detection/src/main/java/anger_detection) and [`serverless_functions/openwhisk/java/face_recognition/image_recognition/src/main/java/image_recognition`](serverless_functions/openwhisk/java/face_recognition/image_recognition/src/main/java/image_recognition) a file named `AzureConfig.java` with the following structure:

```java
public class AzureConfig {
	protected static String endpoint = "xxxxxxxxxx";
	protected static String key = "xxxxxxxxxx";
}
```

<h4>Node.js:</h4>

In [`serverless_functions/openwhisk/node/face_recognition/anger_detection`](serverless_functions/openwhisk/node/face_recognition/anger_detection) and [`serverless_functions/openwhisk/node/face_recognition/image_recognition`](serverless_functions/openwhisk/node/face_recognition/image_recognition) a file named `azureconfig.js` with the following structure:

```javascript
module.exports = {
    endpoint: "xxxxxxxxxx",
    key: "xxxxxxxxxx"
};
```

<h4>Python:</h4>

In [`serverless_functions/openwhisk/python/face_recognition/anger_detection`](serverless_functions/openwhisk/python/face_recognition/anger_detection) and [`serverless_functions/openwhisk/python/face_recognition/image_recognition`](serverless_functions/openwhisk/python/face_recognition/image_recognition) a file named `azureconfig.py` with the following structure:

```python
endpoint = "xxxxxxxxxx"
key = "xxxxxxxxxx"
```

### config.properties
A file named `config.properties` [in the project root](https://github.com/francescom412/serverless_composition_performance_project) with the following structure (filled with valid current information):

```properties
docker_compose_dir=absolute_path_to:docker_env

mysql_ip=localhost
mysql_port=3306
mysql_user=xxxxxxx
mysql_password=xxxxxxx
mysql_dbname=xxxxxxx

influx_ip=localhost
influx_port=8086
influx_user=xxxxxxx
influx_password=xxxxxxx
influx_dbname=xxxxxxx

google_cloud_auth_json_path=absolute_path_to:credentials.json
google_cloud_cli_container_name=gcloud-cli
google_cloud_stage_bucket=name_of_stage_bucket_in_Google_Cloud_Platform

aws_auth_folder_path=absolute_path_to:credentials
aws_lambda_execution_role=arn:xxxxxxx
aws_step_functions_execution_role=arn:xxxxxxx

openwhisk_host=xxx.xxx.xxx.xxx
openwhisk_auth=xxxxxxx
openwhisk_ignore_ssl=True [or False if Open Whisk is deployed on a SSL certified endpoint]

google_handler_function_path=absolute_path_to:serverless_functions/gcloud/orchestration_handler
aws_handler_function_path=absolute_path_to:serverless_functions/aws/orchestration_handler
```

---

<h2>Serverless functions packages creation</h2>
