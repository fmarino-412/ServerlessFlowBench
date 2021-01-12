# noinspection SpellCheckingInspection
import urllib.request as urlrequest
from google.cloud import vision


# noinspection DuplicatedCode,PyUnusedLocal
def gc_functions_handler(request):
	# search for url in request
	url = None

	if request.args.get('url') is not None:
		url = request.args.get('url')
	else:
		return {
			'result': "Error"
		}

	# image download
	# noinspection SpellCheckingInspection
	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	r = urlrequest.Request(url, headers={'User-Agent': useragent})
	f = urlrequest.urlopen(r)
	image = f.read()

	# perform image analysis
	result = detect_object_and_scenes(image)
	if is_face(result):
		result = "face"
	else:
		result = "other"

	# prepare and return response
	return {
		'image': url,
		'result': result
	}


def is_face(input_result) -> bool:
	return "face" in input_result or "cheek" in input_result or "forehead" in input_result \
			or "eyebrow" in input_result or "nose" in input_result or "lip" in input_result \
			or "mouth" in input_result or "eye" in input_result or "lashes" in input_result


def detect_object_and_scenes(image) -> dict:
	# prepare request
	client = vision.ImageAnnotatorClient()
	image = vision.Image(content=image)

	# perform request and analyze result
	response = client.label_detection(image=image)
	result = ""
	for label in response.label_annotations:
		result = result + label.description.lower()
	result = result[0: -2]
	return result
