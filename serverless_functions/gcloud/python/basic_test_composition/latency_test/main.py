import json


# noinspection DuplicatedCode
def gc_functions_handler(request):

	# test invocation and response timing

	# response creation
	headers = {
		'Content-Type': 'application/json'
	}

	return (json.dumps({
		'success': True,
		'payload': {
			'message': 'latency_test'
		}
	}), 200, headers)
