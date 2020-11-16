# noinspection DuplicatedCode
def ow_handler(request):

	# search for number to factorize in request
	result = None

	if request.get('result') is not None:
		result = request.get('result')
	else:
		return {
			'body': {
				'error': 'Missing argument error'
			}
		}

	# prepare and return response
	return "face" in result
