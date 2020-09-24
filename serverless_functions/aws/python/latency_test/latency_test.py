import json


# noinspection DuplicatedCode
def lambda_handler(event, context):
	return {
		'statusCode': 200,
		'headers': {
			'Content-Type': 'application/json'
		},
		'body': json.dumps({
			'success': True,
			'payload': {
				'test': 'latency_test'
			}
		})
	}
