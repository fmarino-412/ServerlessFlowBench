from google.cloud import translate_v2 as translate
import six

TRANSLATE_CLIENT = translate.Client()


# noinspection DuplicatedCode,PyUnusedLocal
def gc_functions_handler(request):

	# search for string and language code in request
	sentence = None
	language_code = None

	request_json = request.get_json(silent=True)

	if request_json and 'sentence' in request_json:
		sentence = request_json['sentence']
	else:
		return {
			'result': "Error"
		}

	if request_json and 'language_code' in request_json:
		language_code = request_json['language_code']
	else:
		return {
			'result': "Error"
		}

	# prepare and return response
	return {
		'result': "Ok",
		'original_sentence': sentence,
		'sentence': translate_text(sentence, language_code)
	}


def translate_text(text, source_language_code) -> str:

	# prepare and perform request
	if isinstance(text, six.binary_type):
		text = text.decode("utf-8")

	result = TRANSLATE_CLIENT.translate(text, source_language=source_language_code, target_language="en")
	# noinspection PyTypeChecker
	return result["translatedText"]
