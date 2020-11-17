import azureconfig
from azure.cognitiveservices.vision.face import FaceClient
from msrest.authentication import CognitiveServicesCredentials


# noinspection DuplicatedCode
def ow_handler(request):

	# search for image url in request
	if request.get('body').get('url') is not None:
		url = request.get('body').get('url')
	else:
		raise Exception('Missing argument in anger detection')

	# perform image analysis and return response
	result = detect_anger(url)
	return {
		'value': bool(result)
	}


def detect_anger(image) -> bool:

	# prepare request
	client = FaceClient(azureconfig.endpoint, CognitiveServicesCredentials(azureconfig.key))
	attributes = ["emotion"]
	include_id = False
	include_landmarks = False

	# perform request and analyze result
	response = client.face.detect_with_url(image, include_id, include_landmarks, attributes, raw=False)
	for result in response:
		if result.face_attributes.emotion.anger is not None and result.face_attributes.emotion.anger >= 0.6:
			return True
	return False
