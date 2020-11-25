import time
import json


# noinspection DuplicatedCode,PyUnusedLocal
def gc_functions_handler(request):

	# search for array dimension in request
	n = None

	if request.args.get('n') is not None:
		n = int(request.args.get('n'))
	else:
		n = 1300000

	# check value
	if n <= 0:
		n = 1300000

	start_time = time.time()
	memory_stress(n)
	end_time = time.time()
	execution_time = (end_time - start_time) * 1000

	# prepare response
	headers = {
		'Content-Type': 'application/json'
	}

	return (json.dumps({
		'success': True,
		'payload': {
			'test': 'memory_test',
			'dimension': n,
			'milliseconds': execution_time
		}
	}), 200, headers)


# noinspection DuplicatedCode
def memory_stress(n):
	# create and populate dynamically an array
	memory_list = []
	for i in range(0, n):
		memory_list.append(i)
