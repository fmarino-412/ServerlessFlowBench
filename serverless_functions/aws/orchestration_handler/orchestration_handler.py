import boto3
import json
import time
from datetime import datetime

STEPFUNCTIONS_CLIENT = boto3.client("stepfunctions")


def lambda_handler(event, context):

	alt_url = "https://upload.wikimedia.org/wikipedia/en/2/2d/Front_left_of_car.jpg"
	url = None
	arn = None

	if event.get('queryStringParameters') is not None:
		if 'url' in event['queryStringParameters']:
			url = (event['queryStringParameters']['url'])
	elif event.get('url') is not None:
		url = event['url']
	else:
		url = alt_url

	if event.get('queryStringParameters') is not None:
		if 'arn' in event['queryStringParameters']:
			arn = (event['queryStringParameters']['arn'])
	elif event.get('arn') is not None:
		arn = event['arn']

	date = datetime.now()

	response = STEPFUNCTIONS_CLIENT.start_execution(
		stateMachineArn=arn,
		name="execution_" + date.strftime("%d-%m-%Y_%H-%M-%S"),
		input=json.dumps({
			'url': url
		})
	)

	execution_arn = response.get("executionArn")
	execution_info = STEPFUNCTIONS_CLIENT.describe_execution(executionArn=execution_arn)

	while execution_info.get("status") == "RUNNING":
		time.sleep(0.05)
		execution_info = STEPFUNCTIONS_CLIENT.describe_execution(executionArn=execution_arn)

	success = execution_info.get("status") == "SUCCEEDED"
	name = execution_info.get("name")
	output = execution_info.get("output").replace('"', '')

	return {
		'statusCode': 200,
		'headers': {
			'Content-Type': 'application/json'
		},
		'body': json.dumps({
			'success': success,
			'payload': {
				'test': name + ' execution',
				'result': output

			}
		})
	}




