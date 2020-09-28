# Documentation on project progresses, results, findings and decisions.
Francesco Marino

Rome University of "Tor Vergata", Computer Engineering 

A.Y. 2019/2020

## Serverless composition workflow ideas:
1. <s>Video conversion</s>:
	1. **needs external executables** for functionality perform,
	2. **not suitable** for a serverless environment.
2. <s>Text to speech conversion</s>: 
	1. needs to store data on S3 or Google Cloud Sotrage
	2. not suitable for benchmarking execution,
	3. **hard to place it in a workflow**.
3. Translator:
	1. Language **recognition** step
	2. [If different from english] translation step
	3. Common english words **ranking** using **NoSQL database**:
		1. Amazon Dynamo DB atomic counters
		2. Google Bigtable row-wide atomicity

## Findings and Decisions:
1. Since both AWS Step Functions and Google Cloud Workflows [BETA] support only **asynchronous invocations** a serverless Python handler has been used (**double billing constraint**).
2. **Google** authentication **token** function (needed for Google Cloud Workflows [BETA] execution), auth in benchmark application, pass token as HTTP argument just for testing scopes.
3. **AWS Api Gateway 30 seconds limit** for any tipe of integration!
4. Image detection functions with **512MB RAM** for timing decreasings.
5. Light changes in Java Image Detection workflow in **Google Cloud Workflows** [BETA] implementation due to impossibility for **Java** to create **dictionaries**!