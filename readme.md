# ServerlessFlowBench

Computer Engineering Master's Degree final project @ [University of Rome 'Tor Vergata'](https://en.uniroma2.it/)

Author: [Francesco Marino](https://github.com/francescom412)

Academic Year: 2019/2020

The **Serverless Composition Performance Project** is a framework that allows users to:

* deploy **serverless functions** to Amazon Web Services, Google Cloud Platform and OpenWhisk (already defined functions are available),
* deploy **serverless function compositions** to Amazon Web Services, Google Cloud Platform and OpenWhisk (as before, altready defined compositions are available) and
* perform HTTP **bechmarks** on deployed functions and compositions.

---

<h2>Execution Requirements</h2>

* [Java Developer Kit (JDK) version 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) (recommended) or newer
* [Docker Desktop](https://www.docker.com/products/docker-desktop) with the following Docker images installed:
	* [`amazon/aws-cli`](https://hub.docker.com/r/amazon/aws-cli) with tag `2.0.60`
	* [`google/cloud-sdk`](https://hub.docker.com/r/google/cloud-sdk) with tag `316.0.0`
	* [`francescom412/ow-utils-complete`](https://hub.docker.com/r/francescom412/ow-utils-complete) with tag `63a5498`
	* [`influxdb`](https://hub.docker.com/_/influxdb) with tag `1.8.2`
	* [`grafana/grafana`](https://hub.docker.com/r/grafana/grafana) with tag `6.5.0`
	* [`mysql`](https://hub.docker.com/_/mysql) with tag `8.0.17`
	* [`bschitter/alpine-with-wrk2`](https://hub.docker.com/r/bschitter/alpine-with-wrk2) with tag `0.1`
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
* [OpenWhisk](https://openwhisk.apache.org/) running deployment
* \[OPTIONAL\] [Azure](https://azure.microsoft.com/) valid and active account with, at least, the following enabled:
	* [Vision API](https://azure.microsoft.com/services/cognitive-services/computer-vision/) and
	* [Face API](https://azure.microsoft.com/services/cognitive-services/face/).

**Please note**: the framework remains usable even with just 1 or 2, out of the 3, serverless platform(s) available.

---

<h2>Project structure description</h2>

### [docker\_env](docker_env)

Folder containing files needed for a container based execution of the project architecture.

#### Content:

* [`docker-compose.yml`](docker_env/docker-compose.yml) used to describe and deploy the project support architecture,
* [`grafana_storage`](docker_env/grafana_storage) folder, **to be added if not present**, used to store [Grafana](https://grafana.com/) container content and implement persistence,
* [`influx_storage`](docker_env/influx_storage) folder, **to be added if not present**, used to store [InfluxDB](https://www.influxdata.com/products/influxdb/) container content and implement persistence,
* [`mysql_storage`](docker_env/mysql_storage) folder, **to be added if not present**, used to store [MySQL](https://www.mysql.com/) container content and implement persistence and
* [`grafana_dashboards`](docker_env/grafana_dashboards) folder used to store [Grafana](https://grafana.com/) dashboards needed to show benchmarks results.

#### Notes:

##### Composition containers' description:

* [MySQL](https://www.mysql.com/): relational database used to keep track of every entity deployed to the cloud in order to be able to reach and, eventually, delete each of them.
* [InfluxDB](https://www.influxdata.com/products/influxdb/): time series database used to keep track of benchmarks' results, each of them with the right test performance date and time.
* [Grafana](https://grafana.com/): visualization tool used to show benchmarks' result stored in InfluxDB in clear and explicative dashboards.

##### Credentials:

In [`docker-compose.yml`](docker_env/docker-compose.yml) file are listed credentials needed to access service containers:

* MySQL:
	* username: `root`,
	* password: `password`.
* InfluxDB:
	* username: `root`,
	* password: `password`.
* Grafana:
	* username: `root`,
	* password: `password`.

##### First start:

In order to import in Grafana, after having the Docker compose environment up, the dashboards saved in [`grafana_dashboards`](docker_env/grafana_dashboards):

1. connect to `http://localhost:3000`,
2. login using Grafana username and password,
3. select the "_setting_" panel,
4. choose "_datasources_" and add a new datasource,
5. choose influxDB as datasource, set `http://influx-db:8086` (or replace "influx-db" with your InfluxDB Docker container name) as url, select your database \(name can be set using the `config.properties` file located [in the project root](https://github.com/francescom412/ServerlessFlowBench)\) and insert InfluxDB credentials,
6. select the "_+_" tab,
7. choose "_import_" option,
8. select every dashboard inside the [`grafana_dashboards`](docker_env/grafana_dashboards) directory.

### [serverless\_functions](serverless_functions)

Folder containing examples of serverless functions and compositions created and benchmarked by the author.

#### Functions:

Here is the list of the functionalities realized:

* `basic_composition`: composition realized just calling two different functions.
	* `latency_test`: JSON response generator.
	* `cpu_test`: big number factorization.
* `memory_test`: dynamic array allocation and filling.
* `face_recognition`: detection of face and anger in an image.
	* `image_recognition`: detection of faces.
	* `anger_detection`: detection of anger if face found.
* `cycle_translator`: translation of sentences from any language to english (OpenWhisk version not realized).
	* `loop_controller`: utility to manage more sentence translation at a time.
	* `language_detection`: detection of the sentence language.
	* `sentence_translation`: translation to English language.
	* `translation_logger`: translation logging in a cloud bucket.

Each of them has been realized for [Python](https://www.python.org/), [Java](https://docs.oracle.com/javase/tutorial/index.html) and [Node.js \(Javascript\)](https://nodejs.org/en/docs/) in different versions, one for each tested provider.

#### Content:

* [`aws`](serverless_functions/aws) folder containing functionalities meant to be deployed to Amazon Web Services:
	* [`java`](serverless_functions/aws/java) containing Java AWS version of the functionalities,
	* [`node`](serverless_functions/aws/node) containing Node.js AWS version of the functionalities,
	* [`python`](serverless_functions/aws/python) containing Python AWS version of the functionalities and
	* [`orchestration_handler`](serverless_functions/aws/orchestration_handler) folder containing a Python handler to execute and return result of compositions.
* [`gcloud`](serverless_functions/gcloud) folder containing functionalities meant to be deployed to Google Cloud Platform:
	* [`java`](serverless_functions/gcloud/java) containing Java Google Cloud version of the functionalities,
	* [`node`](serverless_functions/gcloud/node) containing Node.js Google Cloud version of the functionalities,
	* [`python`](serverless_functions/gcloud/python) containing Python Google Cloud version of the functionalities and
	* [`orchestration_handler`](serverless_functions/gcloud/orchestration_handler) folder containing a Python handler to execute and return result of compositions.
* [`openwhisk`](serverless_functions/openwhisk) folder containing functionalities meant to be deployed to OpenWhisk:
	* [`java`](serverless_functions/openwhisk/java) containing Java OpenWhisk version of the functionalities,
	* [`node`](serverless_functions/openwhisk/node) containing Node.js OpenWhisk version of the functionalities and
	* [`python`](serverless_functions/openwhisk/python) containing Python OpenWhisk version of the functionalities.

### [src](src)

This directory contains, in its subdirectories, Java code for Serverless Composition Performance Project execution, further details are provided in the next section.

<h2>Java Project structure description</h2>

The entire project part was developed using [JetBrains' IntelliJ IDEA](https://www.jetbrains.com/idea/) so it is recommended to open it using this IDE for better code navigation.

In the main folder is located the class [`ServerlessFlowBenchMain.java`](src/main/java/ServerlessBenchmarkToolMain.java), this is the application entry point that allows the user to:

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
* [`OpenWhiskCommandUtility.java`](src/main/java/cmd/functionality_commands/OpenWhiskCommandUtility.java) used to create [OpenWhisk CLI](https://github.com/apache/openwhisk-cli) shell commands,
* [`BucketsCommandExecutor.java`](src/main/java/cmd/functionality_commands/BucketsCommandExecutor.java) used to execute cloud buckets related commands,
* [`CompositionCommandExecutor.java`](src/main/java/cmd/functionality_commands/CompositionCommandExecutor.java) used to execute serverless compositions related commands,
* [`FunctionCommandExecutor.java`](src/main/java/cmd/functionality_commands/FunctionCommandExecutor.java) used to execute serverless functions related commands,
* [`TablesCommandExecutor.java`](src/main/java/cmd/functionality_commands/TablesCommandExecutor.java) used to execute cloud NoSQL storage related commands,
* [`IllegalNameException.java`](src/main/java/cmd/functionality_commands/IllegalNameException.java) raised when a malformed name is attempted to be assigned to a resource,
* [output\_parsing package](src/main/java/cmd/functionality_commands/output_parsing) containing utilities to parse command outputs:
	* [`ReplyCollector.java`](src/main/java/cmd/functionality_commands/output_parsing/ReplyCollector.java) used to collect console command execution output and
	* [`URLFinder.java`](src/main/java/cmd/functionality_commands/output_parsing/URLFinder.java) used to collect deployment url from console command execution output and
* [security package](src/main/java/cmd/functionality_commands/security) containing security utilities:
	* [`GoogleAuthClient.java`](src/main/java/cmd/functionality_commands/security/GoogleAuthClient.java) used to authenticate [Google Cloud Workflows \[BETA\]](https://cloud.google.com/workflows) executions urls.

### [databases package](src/main/java/databases)

This package contains classes needed for external databases interaction.

#### [cmd.influx package](src/main/java/databases/influx)

* [`InfluxClient.java`](src/main/java/databases/influx/InfluxClient.java) used to export benchmark results to the time series database [InfluxDB](https://www.influxdata.com/products/influxdb/).

#### [cmd.mysql package](src/main/java/databases/mysql)

* [`CloudEntityData.java`](src/main/java/databases/mysql/CloudEntityData.java) used to collect functions, compositions, bucket and NoSQL table information,
* [`DAO.java`](src/main/java/databases/mysql/DAO.java), an abstract class providing common information and methods needed by database access objects,
* [`FunctionalityURL.java`](src/main/java/databases/mysql/FunctionalityURL.java) used to collect resource deployment url,
* [`MySQLConnect.java`](src/main/java/databases/mysql/MySQLConnect.java) used to connect and disconnect [MySQL database](https://www.mysql.com/),
* [daos package](src/main/java/databases/mysql/daos) containing database access objects implementations:
	* [`BucketsRepositoryDAO.java`](src/main/java/databases/mysql/daos/BucketsRepositoryDAO.java) needed for cloud buckets perstistence management,
	* [`CompositionsRepositoryDAO.java`](src/main/java/databases/mysql/daos/CompositionsRepositoryDAO.java) needed for serverless compositions perstistence management,
	* [`FunctionsRepositoryDAO.java`](src/main/java/databases/mysql/daos/FunctionsRepositoryDAO.java) needed for serverless functions perstistence management ans
	* [`TablesRepositoryDAO.java`](src/main/java/databases/mysql/daos/TablesRepositoryDAO.java) needed for cloud NoSQL tables perstistence management.

### [utility package](src/main/java/utility)

This package contains classes needed for configuration purposes.

* [`ComposeManager.java`](src/main/java/utility/ComposeManager.java) used to obtain automatically Docker images used inside the [`docker-compose.yml`](docker_env/docker-compose.yml) and
* [`PropertiesManager.java`](src/main/java/utility/PropertiesManager.java) used to get configuration parameters from `config.properties` file stored [in the project root](https://github.com/francescom412/ServerlessFlowBench) (further details provided in following sections).

---

<h2>User specific required files</h2>

Authentication files related to user's active services required to run the application (the ones used in the development process were excluded using `.gitignore` file for privacy related reasons).

### Amazon Web Services
A file named `credentials` is required [`serverless_functions/aws/.aws`](serverless_functions/aws/.aws), it should contain AWS account access key and secret. This file has the following structure:

```text
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

### Azure API in OpenWhisk \[optional\]
These files are needed only if the user needs to execute **benchmarks** on **OpenWhisk** for the originally defined **anger detection workflows**. Being every file specific for each function, several versions of this information are needed. Strings needed to fill these files can be found from Azure Console &#8594; Resources (in the side menu) &#8594; Choose the specific Cognitive Service resource &#8594; Keys and endpoints.

#### Java:

In [`serverless_functions/openwhisk/java/face_recognition/anger_detection/src/main/java/anger_detection`](serverless_functions/openwhisk/java/face_recognition/anger_detection/src/main/java/anger_detection) and [`serverless_functions/openwhisk/java/face_recognition/image_recognition/src/main/java/image_recognition`](serverless_functions/openwhisk/java/face_recognition/image_recognition/src/main/java/image_recognition) a file named `AzureConfig.java` with the following structure:

```java
public class AzureConfig {
	protected static String endpoint = "xxxxxxxxxx";
	protected static String key = "xxxxxxxxxx";
}
```

#### Node.js:

In [`serverless_functions/openwhisk/node/face_recognition/anger_detection`](serverless_functions/openwhisk/node/face_recognition/anger_detection) and [`serverless_functions/openwhisk/node/face_recognition/image_recognition`](serverless_functions/openwhisk/node/face_recognition/image_recognition) a file named `azureconfig.js` with the following structure:

```javascript
module.exports = {
    endpoint: "xxxxxxxxxx",
    key: "xxxxxxxxxx"
};
```

#### Python:

In [`serverless_functions/openwhisk/python/face_recognition/anger_detection`](serverless_functions/openwhisk/python/face_recognition/anger_detection) and [`serverless_functions/openwhisk/python/face_recognition/image_recognition`](serverless_functions/openwhisk/python/face_recognition/image_recognition) a file named `azureconfig.py` with the following structure:

```python
endpoint = "xxxxxxxxxx"
key = "xxxxxxxxxx"
```

### config.properties
A file named `config.properties` [in the project root](https://github.com/francescom412/ServerlessFlowBench) with the following structure (filled with valid current information):

```properties
docker_compose_dir=absolute_path_to:docker_env

mysql_ip=localhost ['localhost' to use Docker compose MySQL instance]
mysql_port=3306
mysql_user=xxxxxxx
mysql_password=xxxxxxx
mysql_dbname=xxxxxxx

influx_ip=localhost ['localhost' to use Docker compose InfluxDB instance]
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
openwhisk_ignore_ssl=True [or False if OpenWhisk is deployed on a SSL certified endpoint]

google_handler_function_path=absolute_path_to:serverless_functions/gcloud/orchestration_handler
aws_handler_function_path=absolute_path_to:serverless_functions/aws/orchestration_handler
```

---

<h2>Serverless functions packages creation</h2>

This section's purpose is to explain how to create packages ready for deployment to the different service providers.

### Amazon Web Services

#### Java:

The `.jar` file to deploy can be easily created using the project management tool [Maven](https://maven.apache.org/).

Here an example of the `pom.xml` file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>GROUP_ID</groupId>
    <artifactId>PROJECT_NAME</artifactId>
    <version>VERSION</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-core</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-events</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-log4j2</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>javax.json</groupId>
            <artifactId>javax.json-api</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>javax.json.bind</groupId>
            <artifactId>javax.json.bind-api</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>javax.json</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>x.x.x</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>x.x.x</version>
                <configuration>
                    <createDependencyReducedPom>false</createDependencyReducedPom>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>x.x.x</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Node.js:

In order to create a Node.js zipped package:

1. define the `package.json` file with every needed dependency (an example can be found at the end of this subsection),
2. install every needed dependency using [`npm`](https://www.npmjs.com/) inside a folder named `node_modules` placed in the Node.js project root,
3. put `package.json` file, `node_modules` folder and `.js` code files inside a `.zip` archive ready to be deployed.

Here an example of the `package.json` file.

```json
{
  "name": "PROJECT_NAME",
  "version": "VERSION",
  "description": "PROJECT_DESCRIPTION",
  "main": "index.js",
  "author": "PROJECT_AUTHOR",
  "license": "ISC",
  "dependencies": {
    "dependency_name": "^x.x.x"
  }
}
```

**Please note**: package creation for AWS Node.js example functions can be automatically performed running the [`generate_archives.sh`](serverless_functions/aws/node/generate_archives.sh) script.

#### Python:

In order to create a Python zipped package:

1. install every needed dependency using [`pip`](https://pip.pypa.io/en/stable/) inside the Python project root,
2. put every dependency installed and the `.py` files inside `.zip` archive ready to be deployed.

**Please note**:

* In the common cases the function needs only to communicate with AWS services, a .zip archive with just .py files inside is needed.
* Package creation for AWS Python example functions can be automatically performed running the [`generate_archives.sh`](serverless_functions/aws/python/generate_archives.sh) script.

### Google Cloud Platform

For Google Cloud Platform no archive creation is needed.

#### Java:

The project to deploy can be easily created using [Maven](https://maven.apache.org/), in order to perform deployment is enough passing the project root path to the deployment utility.

Here an example of the `pom.xml` file needed for Google Cloud Functions deployment.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>GROUP_ID</groupId>
    <artifactId>PROJECT_NAME</artifactId>
    <version>VERSION</version>

    <properties>
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.source>1.8</maven.compiler.source>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.google.cloud.functions</groupId>
            <artifactId>functions-framework-api</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>javax.json</groupId>
            <artifactId>javax.json-api</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>javax.json.bind</groupId>
            <artifactId>javax.json.bind-api</artifactId>
            <version>x.x.x</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>javax.json</artifactId>
            <version>x.x.x</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>x.x.x</version>
                <configuration>
                    <excludes>
                        <exclude>.google/</exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Node.js:

In order to create a Node.js package to deploy:

1. define the `package.json` file with every needed dependency (an example can be found in the Amazon Web Services Node.js section),
3. put `package.json` file and `.js` code files inside the project root to deploy and pass its absolute path to the deployment utility.

#### Python:

In order to create a Python package to deploy:

1. put every needed `.py` file in the package root and
2. create a `requirements.txt` file in the package root with every needed dependency.

The deployment process is similar to the ones for Node.js and Java in Google Cloud Platform.

Here an example of the `requirements.txt` file needed for Google Cloud Functions deployment.

```text
dependency-name==x.x.x
dependency-name==x.x.x
dependency-name==x.x.x
...
```

### OpenWhisk

#### Java:

The `.jar` file to deploy can be created, again, using [Maven](https://maven.apache.org/).

Here an example of the `pom.xml` file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>GROUP_ID</groupId>
    <artifactId>PROJECT_NAME</artifactId>
    <version>VERSION</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>x.x.x</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>x.x.x</version>
                <configuration>
                    <createDependencyReducedPom>false</createDependencyReducedPom>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>x.x.x</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Node.js:

In order to create a Node.js zipped package:

1. define the `package.json` file with every needed dependency (an example can be found in the Amazon Web Services Node.js subsection),
2. install every needed dependency using [`npm`](https://www.npmjs.com/) inside a folder named `node_modules` placed in the Node.js project root,
3. put `package.json` file, `node_modules` folder and `.js` code files inside a `.zip` archive ready to be deployed.

**Please note**: package creation for OpenWhisk Node.js example functions can be automatically performed running the [`generate_archives.sh`](serverless_functions/openwhisk/node/generate_archives.sh) script.

#### Python:

[\[SOURCE\]](https://jamesthom.as/2017/04/python-packages-in-openwhisk/) In order to create a Python zipped package:

1. create the entry point file in the Python project root and name it as `__main__.py`,
2. create a virtual environment,
3. install every needed dependency using [`pip`](https://pip.pypa.io/en/stable/) inside the Python project root,
4. put the `virtualenv` directory and the `.py` files inside `.zip` archive ready to be deployed.

In order to create a virtual environment execute the following command starting from the Python project root:

```shell script
$ virtualenv virtualenv
```
In order to install dependencies execute the following commands starting from the Python project root:

```shell script
$ source virtualenv/bin/activate
(virtualenv) $ pip install dependency-name
(virtualenv) $ pip install dependency-name
...
```

**Please note**: package creation for OpenWhisk Python example functions can be automatically performed running the [`generate_archives.sh`](serverless_functions/openwhisk/python/generate_archives.sh) script.
