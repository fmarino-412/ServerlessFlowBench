import time
import json
import re


# noinspection DuplicatedCode
def lambda_handler(event, context):

	# search for array dimension in request
	n = None

	if event.get('queryStringParameters') is not None:
		if 'n' in event['queryStringParameters']:
			n = int(event['queryStringParameters']['n'])
	elif event.get('n') is not None:
		n = event.get('n')
	else:
		n = 1300000

	# available_start, memory_total = memory_stats(True)

	start_time = time.time()
	memory_stress(n)
	end_time = time.time()
	execution_time = (end_time - start_time) * 1000

	# available_end, _ = memory_stats(False)

	# prepare response
	return {
		'statusCode': 200,
		'headers': {
			'Content-Type': 'application/json'
		},
		'body': json.dumps({
			'success': True,
			'payload': {
				'test': 'memory_test',
				'dimension': n,
				'milliseconds': execution_time
			}  # ,
			# 'memory_info': {
			# 'initial_available': available_start,
			# 'final_available': available_end,
			# 'total': memory_total
			# }
		})
	}


# noinspection DuplicatedCode
def memory_stats(total):
	memory_info, memory_total = None, None

	f = open('/proc/meminfo', 'r')
	if f.mode == 'r':
		memory_info = f.read()
	f.close()

	# parse memory info
	if total:
		total_pattern = re.compile("(MemTotal:\s)(.+B)")
		memory_total = total_pattern.search(memory_info)[2]

	available_pattern = re.compile("(MemAvailable:\s)(.+B)")
	memory_available = available_pattern.search(memory_info)[2]

	return memory_available, memory_total


# noinspection DuplicatedCode
def memory_stress(n):
	# create and populate dynamically an array
	memory_list = []
	for i in range(0, n):
		memory_list.append(i)
