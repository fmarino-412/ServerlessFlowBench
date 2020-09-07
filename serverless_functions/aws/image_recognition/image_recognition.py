import boto3
import time
import urllib.request as request
import json

REKOGNITION_CLIENT = boto3.client("rekognition")


def lambda_handler(event, context):
	url = None
	human = "false"

	if event.get('queryStringParameters') is not None:
		if 'url' in event['queryStringParameters']:
			url = (event['queryStringParameters']['url'])
	else:
		url = "https://upload.wikimedia.org/wikipedia/en/2/2d/Front_left_of_car.jpg"

	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	start_time = time.time()
	r = request.Request(url, headers={'User-Agent': useragent})
	f = request.urlopen(r)
	result = detect_object_and_scenes(f.read())
	if "human" in json.dumps(result).lower():
		human = "true"
	end_time = time.time()
	execution_time = (end_time - start_time) * 1000

	return {
		'statusCode': 200,
		'headers': {
			'Content-Type': 'application/json'
		},
		'body': json.dumps({
			'success': True,
			'payload': {
				'test': 'image_recognition',
				'image': url,
				'result': result,
				'human': human,
				'milliseconds': execution_time
			}
		})
	}


def detect_object_and_scenes(image) -> dict:
	return REKOGNITION_CLIENT.detect_labels(
		Image={
			'Bytes': image
		},
		MaxLabels=100,
		MinConfidence=70.0
	)
