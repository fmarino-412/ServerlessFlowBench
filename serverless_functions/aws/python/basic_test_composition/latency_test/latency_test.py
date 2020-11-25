import json


# noinspection DuplicatedCode,PyUnusedLocal
def lambda_handler(event, context):

	# test invocation and response timing

	# response creation
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
