# noinspection DuplicatedCode
def gc_functions_handler(request):

	# search for list of string and counter in request
	sentences = None
	counter = None

	request_json = request.get_json(silent=True)

	if request_json and 'Sentences' in request_json:
		sentences = request_json['Sentences']
	else:
		return {
			'result': "Error"
		}

	if request_json and 'NextIterationCounter' in request_json:
		counter = request_json['NextIterationCounter']
	else:
		return {
			'result': "Error"
		}

	# prepare and return response
	return {
		'Sentences': sentences,
		'CurrentSentence': sentences[counter],
		'NextIterationCounter': counter + 1,
		'EndNext': len(sentences) == counter + 1,
		'result': "Ok"
	}
