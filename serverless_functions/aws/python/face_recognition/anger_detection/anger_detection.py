import boto3
import urllib.request as request

REKOGNITION_CLIENT = boto3.client("rekognition")


# noinspection PyUnusedLocal
def lambda_handler(event, context):

	# search for url in request
	url = None

	if event.get('queryStringParameters') is not None:
		if 'url' in event['queryStringParameters']:
			url = (event['queryStringParameters']['url'])
	elif event.get('url') is not None:
		url = event['url']
	else:
		return "Error"

	# image download
	# noinspection SpellCheckingInspection
	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
		'Mobile/10A5355d Safari/8536.25 '

	r = request.Request(url, headers={'User-Agent': useragent})
	f = request.urlopen(r)
	image = f.read()

	# perform image analysis
	return detect_anger(image)


def detect_anger(image) -> str:

	# prepare and perform request
	response = REKOGNITION_CLIENT.detect_faces(
		Image={
			'Bytes': image
		},
		Attributes=['ALL']
	)

	# analyze result
	for face_detail in response['FaceDetails']:
		for emotion in face_detail['Emotions']:
			if emotion['Type'] == "ANGRY" and float(emotion['Confidence']) >= 60:
				return str(True)

	return str(False)
