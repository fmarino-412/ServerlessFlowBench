import time
import math
import json
import re


# noinspection DuplicatedCode
def lambda_handler(event, context):

	# search for number to factorize in request
	n = None

	if event.get('queryStringParameters') is not None:
		if 'n' in event['queryStringParameters']:
			n = int(event['queryStringParameters']['n'])
	elif event.get('n') is not None:
		n = event.get('n')
	else:
		n = 71950288374236

	# check value
	if n <= 0:
		n = 71950288374236

	# cpu_model, cpu_cores = get_cpu_info()

	# measure computation
	start_time = time.time()
	result = factorize(n)
	end_time = time.time()
	execution_time = (end_time - start_time) * 1000

	# prepare response
	return {
		'statusCode': 200,
		'headers': {
			'Content-Type': 'application/json'
		},
		'body': json.dumps({
			'success': True,
			'payload': {
				'test': 'cpu_test',
				'number': n,
				'result': result,
				'milliseconds': execution_time
			}  # ,
			# 'cpu_info': {
			# 'model': cpu_model,
			# 'cores': cpu_cores
			# }
		})
	}


# noinspection DuplicatedCode
def factorize(n):
	# finds factors for n
	factors = []
	# optimized research
	for i in range(1, math.floor(math.sqrt(n)) + 1):
		if n % i == 0:
			factors.append(i)
			if n / i != i:
				factors.append(int(n / i))

	factors.sort()

	return factors


# noinspection DuplicatedCode
def get_cpu_info():
	cpu_info = None

	f = open('/proc/cpuinfo', 'r')
	if f.mode == 'r':
		cpu_info = f.read()
	f.close()

	# parse infos
	model_pattern = re.compile("(model name\s:\s)(.+@.*z)")
	cores_pattern = re.compile("(cpu cores\s:\s)([0-9]+)")
	cpu_model = model_pattern.search(cpu_info)[2]
	cpu_cores = cores_pattern.search(cpu_info)[2]

	return cpu_model, cpu_cores
