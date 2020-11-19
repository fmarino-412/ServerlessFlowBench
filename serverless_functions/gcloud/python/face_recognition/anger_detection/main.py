import urllib.request as urlrequest
from google.cloud import vision


# noinspection DuplicatedCode
def gc_functions_handler(request):

	# search for url in request
	url = None

	if request.args.get('url') is not None:
		url = request.args.get('url')
	else:
		return "Error"

	# image download
	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	r = urlrequest.Request(url, headers={'User-Agent': useragent})
	f = urlrequest.urlopen(r)
	image = f.read()

	# perform image analysis
	return detect_anger(image)


def detect_anger(image) -> str:

	# prepare request
	client = vision.ImageAnnotatorClient()
	image = vision.Image(content=image)

	# perform request and analyze result
	response = client.face_detection(image=image)
	for face in response.face_annotations:
		if face.anger_likelihood == "LIKELY" or face.anger_likelihood == "VERY_LIKELY":
			return str(True)
	return str(False)
