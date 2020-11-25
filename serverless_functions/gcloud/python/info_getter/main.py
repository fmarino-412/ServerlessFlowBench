import json


# noinspection DuplicatedCode,PyUnusedLocal
def gc_functions_handler(request):

    cpu_info = get_cpu_info()
    mem_info = get_memory_info()

    # prepare response
    headers = {
        'Content-Type': 'application/json'
    }

    # prepare response
    return (json.dumps({
            'success': True,
            'payload': {
                'execution': 'info getter',
                'cpu info': cpu_info,
                'memory info': mem_info
            }
            }), 200, headers)


# noinspection DuplicatedCode
def get_cpu_info():
    cpu_info = None
    # noinspection SpellCheckingInspection
    f = open('/proc/cpuinfo', 'r')
    if f.mode == 'r':
        cpu_info = f.read()
    f.close()
    cpu_info.replace("\t", "")
    cpu_info = cpu_info.split("\n")
    result = []
    for info in cpu_info:
        if ":" in info:
            result.append({info.split(":")[0]: info.split(":")[1]})
        elif info != "":
            result.append({"generic_info": info})
    return result


# noinspection DuplicatedCode
def get_memory_info():
    memory_info = None
    # noinspection SpellCheckingInspection
    f = open('/proc/meminfo', 'r')
    if f.mode == 'r':
        memory_info = f.read()
    f.close()
    memory_info.replace("\t", "")
    memory_info = memory_info.split("\n")
    result = []
    for info in memory_info:
        if ":" in info:
            result.append({info.split(":")[0]: info.split(":")[1]})
        elif info != "":
            result.append({"generic_info": info})
    return result
