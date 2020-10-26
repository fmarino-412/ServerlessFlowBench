from google.cloud import translate_v2 as translate

TRANSLATE_CLIENT = translate.Client()


# noinspection DuplicatedCode
def gc_functions_handler(request):

	# search for string in request
	sentence = None

	request_json = request.get_json(silent=True)

	if request_json and 'sentence' in request_json:
		sentence = request_json['sentence']
	else:
		return {
			'result': "Error"
		}

	# prepare and return response
	return {
		'result': "Ok",
		'sentence': sentence,
		'language': detect_dominant_language(sentence)
	}


def detect_dominant_language(text) -> str:

	# prepare and perform request
	result = TRANSLATE_CLIENT.detect_language(text)

	return result["language"]
