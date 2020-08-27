import json


def gc_functions_handler(request):
    headers = {
        'Content-Type': 'application/json'
    }

    return (json.dumps({
        'success': True,
        'payload': {
            'message': 'latency_test function'
        }
    }), 200, headers)
