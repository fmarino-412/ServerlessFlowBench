import time
import math
import json
import re


def lambda_handler(event, context):
    cpu_info, n = None, None

    f = open('/proc/cpuinfo', 'r')
    if f.mode == 'r':
        cpu_info = f.read()
    f.close()

    model_pattern = re.compile("(model name\s:\s)(.+@.*z)")
    cores_pattern = re.compile("(cpu cores\s:\s)([0-9]+)")
    cpu_model = model_pattern.search(cpu_info)[2]
    cpu_cores = cores_pattern.search(cpu_info)[2]

    if event.get('queryStringParameters') is not None:
        if 'n' in event['queryStringParameters']:
            n = int(event['queryStringParameters']['n'])
    else:
        n = 71950288374236

    start_time = time.time()
    result = factorize(n)
    end_time = time.time()
    execution_time = (end_time - start_time) * 1000

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
            },
            'cpu_info': {
                'model': cpu_model,
                'cores': cpu_cores
            }
        })
    }


def factorize(n):
    # finds two factors for n
    factors = []
    for i in range(1, math.floor(math.sqrt(n)) + 1):
        if n % i == 0:
            factors.append(i)
            if n / i != i:
                factors.append(int(n / i))

    factors.sort()

    return factors
