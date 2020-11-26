import time


# noinspection DuplicatedCode,PyUnusedLocal
def ow_handler(request):

	# search for array dimension in request
	n = None

	if request.get('n') is not None:
		n = int(request.get('n'))
	else:
		n = 1300000

	# check value
	if n <= 0:
		n = 1300000

	start_time = time.time()
	memory_stress(n)
	end_time = time.time()
	execution_time = (end_time - start_time) * 1000

	# prepare and return response
	return {
		'body': {
			'test': 'memory_test',
			'dimension': n,
			'milliseconds': execution_time
		}
	}


# noinspection DuplicatedCode
def memory_stress(n):
	# create and populate dynamically an array
	memory_list = []
	for i in range(0, n):
		memory_list.append(i)
