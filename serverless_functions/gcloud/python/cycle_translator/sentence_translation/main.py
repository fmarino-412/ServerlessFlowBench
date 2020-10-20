from google.cloud import translate_v2 as translate
import six

TRANSLATE_CLIENT = translate.Client()


# noinspection DuplicatedCode
def gc_functions_handler(request):

	# search for string in request
	sentence = None
	language_code = None

	if request.args.get('sentence') is not None:
		sentence = request.args.get('sentence')
	else:
		return {
			'result': "Error"
		}

	if request.args.get('language_code') is not None:
		language_code = request.args.get('language_code')
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
	return result["translatedText"]
