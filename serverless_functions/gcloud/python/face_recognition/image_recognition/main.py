import urllib.request as urlrequest
from google.cloud import vision
from google.cloud.vision import types


# noinspection DuplicatedCode
def gc_functions_handler(request):
	url = None

	if request.args.get('url') is not None:
		url = request.args.get('url')
	else:
		return {
			'result': {"Error": "no url"}
		}

	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	r = urlrequest.Request(url, headers={'User-Agent': useragent})
	f = urlrequest.urlopen(r)
	image = f.read()
	result = detect_object_and_scenes(image)

	return {
		'image': url,
		'result': result
	}


def detect_object_and_scenes(image) -> dict:
	client = vision.ImageAnnotatorClient()
	image = types.Image(content=image)

	response = client.label_detection(image=image)
	result = {}
	for label in response.label_annotations:
		result[(label.description).lower()] = str(label.score*100.)

	return result
