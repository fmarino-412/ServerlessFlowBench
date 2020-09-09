import boto3
import urllib.request as request

REKOGNITION_CLIENT = boto3.client("rekognition")


def lambda_handler(event, context):
	altUrl = "https://upload.wikimedia.org/wikipedia/en/2/2d/Front_left_of_car.jpg"
	url = None

	if event.get('queryStringParameters') is not None:
		if 'url' in event['queryStringParameters']:
			url = (event['queryStringParameters']['url'])
	elif event.get('url') is not None:
		url = event['url']
	else:
		url = altUrl

	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	r = request.Request(url, headers={'User-Agent': useragent})
	f = request.urlopen(r)
	labels = detect_object_and_scenes(f.read())

	result = ""
	for label in labels.get("Labels"):
		result = result + label.get("Name").lower() + ", "
	result = result[0: -2]

	return result


def detect_object_and_scenes(image) -> dict:
	return REKOGNITION_CLIENT.detect_labels(
		Image={
			'Bytes': image
		},
		MaxLabels=100,
		MinConfidence=70.0
	)
