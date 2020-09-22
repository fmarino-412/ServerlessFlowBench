import boto3
import urllib.request as request

REKOGNITION_CLIENT = boto3.client("rekognition")


def lambda_handler(event, context):
	url = None

	if event.get('queryStringParameters') is not None:
		if 'url' in event['queryStringParameters']:
			url = (event['queryStringParameters']['url'])
	elif event.get('url') is not None:
		url = event['url']

	if url is None:
		return "Error"

	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	r = request.Request(url, headers={'User-Agent': useragent})
	f = request.urlopen(r)
	image = f.read()
	return detect_anger(image)


def detect_anger(image) -> bool:
	response = REKOGNITION_CLIENT.detect_faces(
		Image={
			'Bytes': image
		},
		Attributes=['ALL']
	)

	for faceDetail in response['FaceDetails']:
		for emotion in faceDetail['Emotions']:
			if emotion['Type'] == "ANGRY" and float(emotion['Confidence']) >= 60:
				return True

	return False
