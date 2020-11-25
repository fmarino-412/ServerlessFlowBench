import boto3
import json
import time
from datetime import datetime

# noinspection SpellCheckingInspection
STEPFUNCTIONS_CLIENT = boto3.client("stepfunctions")


# noinspection PyUnusedLocal
def lambda_handler(event, context):
	arn = None

	if event.get('queryStringParameters') is not None:
		if 'arn' in event['queryStringParameters']:
			arn = (event['queryStringParameters']['arn'])
	elif event.get('arn') is not None:
		arn = event['arn']
	else:
		return "Missing machine arn"

	date = datetime.now()

	response = STEPFUNCTIONS_CLIENT.start_execution(
		stateMachineArn=arn,
		name="execution_" + date.strftime("%d-%m-%Y_%H-%M-%S")
	)

	execution_arn = response.get("executionArn")
	busy_wait(0.5)
	execution_info = STEPFUNCTIONS_CLIENT.describe_execution(executionArn=execution_arn)

	while execution_info.get("status") == "RUNNING":
		busy_wait(0.05)
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


def busy_wait(dt):
	# to avoid scheduler decisions
	current_time = time.time()
	while time.time() < current_time + dt:
		continue
