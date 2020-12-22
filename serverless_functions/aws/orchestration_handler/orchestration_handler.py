import boto3
import json
import time
import random
from datetime import datetime

# noinspection SpellCheckingInspection
STEPFUNCTIONS_CLIENT = boto3.client("stepfunctions")


# noinspection PyUnusedLocal,PyBroadException
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
		name="execution_" + date.strftime("%d-%m-%Y_%H-%M-%S-%f")
	)

	execution_arn = response.get("executionArn")
	busy_wait(0.5)
	try:
		execution_info = STEPFUNCTIONS_CLIENT.describe_execution(executionArn=execution_arn)
	except:
		execution_info = {
			'status': 'RUNNING'
		}

	while execution_info.get("status") == "RUNNING":
		busy_wait(0.05)
		try:
			execution_info = STEPFUNCTIONS_CLIENT.describe_execution(executionArn=execution_arn)
		except:
			execution_info = {
				'status': 'RUNNING'
			}
			# to more likely avoid another ThrottleException
			busy_wait(random.uniform(0.05, 0.95))

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
