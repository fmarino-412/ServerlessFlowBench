import azureconfig
from azure.cognitiveservices.vision.face import FaceClient
from msrest.authentication import CognitiveServicesCredentials


# noinspection DuplicatedCode
def ow_handler(request):

	# search for number to factorize in request
	url = None

	if request.get('image') is not None:
		url = request.get('image')
	else:
		return {
			'body': {
				'error': 'Missing argument error'
			}
		}

	# perform image analysis and return response
	return {
		'body': detect_anger(url)
	}


def detect_anger(image) -> bool:

	# prepare and perform request
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
