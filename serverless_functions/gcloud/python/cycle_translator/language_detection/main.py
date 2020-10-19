from google.cloud import translate_v2 as translate

TRANSLATE_CLIENT = translate.Client()


# noinspection DuplicatedCode
def gc_functions_handler(request):

	# search for string in request - %3f for ? character
	sentence = None

	if request.args.get('sentence') is not None:
		sentence = request.args.get('sentence')
	else:
		return {
			'result': {"Error": "sentence error"}
		}

	# prepare and return response
	return {
		'result': {"Ok": "language detected"},
		'sentence': sentence,
		'language': detect_dominant_language(sentence)
	}


def detect_dominant_language(text) -> str:

	# prepare and perform request
	result = TRANSLATE_CLIENT.detect_language(text)

	return result["language"]
