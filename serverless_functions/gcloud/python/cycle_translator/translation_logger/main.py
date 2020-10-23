from google.cloud import storage
import datetime
import string
import random


# noinspection DuplicatedCode
def gc_functions_handler(request):
	# search for strings, original language code and logging bucket in request
	original_sentence = None
	original_language_code = None
	translated_sentence = None
	logging_bucket_name = None

	if request.args.get('original_sentence') is not None:
		original_sentence = request.args.get('original_sentence')
	else:
		return "Error"

	if request.args.get('original_language_code') is not None:
		original_language_code = request.args.get('original_language_code')
	else:
		return "Error"

	if request.args.get('translated_sentence') is not None:
		translated_sentence = request.args.get('translated_sentence')
	else:
		return "Error"

	if request.args.get('logging_bucket_name') is not None:
		logging_bucket_name = request.args.get('logging_bucket_name')
	else:
		return "Error"

	log_translation(original_sentence, original_language_code, translated_sentence, "en", logging_bucket_name)

	# prepare and return response
	return "Logged"


def log_translation(original_sentence, original_language_code, translated_sentence, destination_language_code,
					logging_bucket_name):
	# timestamp
	timestamp = datetime.datetime.utcnow()

	# create filename
	filename = "Translation " + str(timestamp.isoformat(sep=' ', timespec='milliseconds')) + make_id() + ".log"
	filename = filename.replace(" ", "_")

	# create body
	body = "Translation info:" + "\n\n" + "original sentence: " + original_sentence + "\n" + "original language: " + \
		original_language_code + "\n" + "translated sentence: " + translated_sentence + "\n" + \
		"destination language: " + destination_language_code + "\n" + "log date: " + str(timestamp.date()) + "\n" + \
		"log time: " + str(timestamp.time())

	# connect Google Cloud Storage
	client = storage.Client()
	bucket = client.get_bucket(logging_bucket_name)

	# create file
	file = bucket.blob(filename)
	# write on file
	file.upload_from_string(body, content_type="text/plain")
	# make file private
	file.make_private()


def make_id() -> str:
	letters = string.ascii_letters
	fingerprint = ''.join(random.choice(letters) for _ in range(8))
	return "[PythonRuntime_" + fingerprint + "]"
