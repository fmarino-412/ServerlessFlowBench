import azureconfig
from azure.cognitiveservices.vision.computervision import ComputerVisionClient
from msrest.authentication import CognitiveServicesCredentials


# noinspection DuplicatedCode
def ow_handler(request):

	# search for image url in request
	if request.get('body').get('url') is not None:
		url = request.get('body').get('url')
	else:
		raise Exception('Missing argument in image recognition')

	# perform image analysis
	result = detect_objects_and_scenes(url)

	# prepare and return response
	return {
		'value': "person" in result,
		'body': {
			'url': url
		}
	}


def detect_objects_and_scenes(image) -> str:

	# prepare and perform request
	client = ComputerVisionClient(azureconfig.endpoint, CognitiveServicesCredentials(azureconfig.key))
	detect_objects_results_remote = client.detect_objects(image)

	# analyze result
	result = ""
	for detection in detect_objects_results_remote.objects:
		result = result + detection.object_property.lower() + ", "
	result = result[0: -2]
	return result
