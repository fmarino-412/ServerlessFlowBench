# Serverless Composition Performance Project

Computer Engineering Master's Degree final project @ [University of Rome 'Tor Vergata'](https://en.uniroma2.it/)

Author: [Francesco Marino](https://github.com/francescom412)

The **Serverless Composition Performance Project** is a framework that allows users to:

* deploy **serverless functions** to Amazon Web Services, Google Cloud Platform and Open Whisk (already defined functions are available),
* deploy **serverless function compositions** to Amazon Web Services, Google Cloud Platform and Open Whisk (as before, altready defined compositions are available) and
* perform HTTP **bechmarks** on deployed functions and compositions.

<h2>Execution Requirements</h2>

* [Java Developer Kit (JDK) 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
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
* \[OPTIONAL\] [Open Whisk](https://openwhisk.apache.org/) running deployment
* \[OPTIONAL\] [Azure](https://azure.microsoft.com/) valid and active account with, at least, the following enabled:
	* [Vision API](https://azure.microsoft.com/services/cognitive-services/computer-vision/) and
	* [Face API](https://azure.microsoft.com/services/cognitive-services/face/).

---

<h2>Project structure description</h2>

<h2>Java Project structure description</h2>

The entire project part was developed using [JetBrains' IntelliJ IDEA](https://www.jetbrains.com/idea/) so it is recommended to open it using this IDE for better code navigation. 

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

### Azure API in Open Whisk (optional)
These files are needed only if the user needs to execute benchmarks on Open Whisk for the originally defined anger detection workflows. Being every file specific for each function, several versions of this information are needed. Strings needed to fill these files can be found from Azure Console &#8594; Resources (in the side menu) &#8594; Choose the specific Cognitive Service resource &#8594; Keys and endpoints.

<h4>Java:</h4> 

In [`serverless_functions/openwhisk/java/face_recognition/anger_detection/src/main/java/anger_detection`](serverless_functions/openwhisk/java/face_recognition/anger_detection/src/main/java/anger_detection) and [`serverless_functions/openwhisk/java/face_recognition/image_recognition/src/main/java/image_recognition`](serverless_functions/openwhisk/java/face_recognition/image_recognition/src/main/java/image_recognition) a file named `AzureConfig.java` with the following structure:

```java
public class AzureConfig {
	protected static String endpoint = "xxxxxxxxxx";
	protected static String key = "xxxxxxxxxx";
}
```

<h4>Node.js:</h4>
<TODO: write>

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
