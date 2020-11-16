import azureconfig
from azure.cognitiveservices.vision.computervision import ComputerVisionClient
from msrest.authentication import CognitiveServicesCredentials


# noinspection DuplicatedCode
def ow_handler(request):

	# search for number to factorize in request
	url = None

	if request.get('url') is not None:
		url = request.get('url')
	else:
		return {
			'body': {
				'error': 'Missing argument error'
			}
		}

	# perform image analysis
	result = detect_objects_and_scenes(url)

	# prepare and return response
	return {
		'body': {
			'result': result,
			'image': url
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