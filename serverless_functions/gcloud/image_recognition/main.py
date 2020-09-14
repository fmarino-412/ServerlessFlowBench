import time
import json
import urllib.request as urlrequest
from google.cloud import vision
from google.cloud.vision import types


# noinspection DuplicatedCode
def gc_functions_handler(request):
	url = None
	human = "false"

	if request.args.get('url') is not None:
		url = request.args.get('url')
	else:
		url = "https://upload.wikimedia.org/wikipedia/en/2/2d/Front_left_of_car.jpg"

	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	start_time = time.time()
	r = urlrequest.Request(url, headers={'User-Agent': useragent})
	f = urlrequest.urlopen(r)
	result = detect_object_and_scenes(f.read())
	if "human" in result.lower():
		human = "true"
	end_time = time.time()
	execution_time = (end_time - start_time) * 1000

	headers = {
		'Content-Type': 'application/json'
	}

	return (json.dumps({
		'success': True,
		'payload': {
			'test': 'image_recognition',
			'image': url,
			'result': result,
			'human': human,
			'milliseconds': execution_time
		}
	}), 200, headers)


def detect_object_and_scenes(image) -> str:
	client = vision.ImageAnnotatorClient()
	image = types.Image(content=image)

	response = client.label_detection(image=image)
	result = ""
	for label in response.label_annotations:
		result = result + label.description + "\t(" + str(label.score*100.) + ")\n"
	return result
