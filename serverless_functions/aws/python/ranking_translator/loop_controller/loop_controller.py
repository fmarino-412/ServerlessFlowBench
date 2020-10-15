def lambda_handler(event, context):

    # search for list of string and counter in request
    sentences = None
    counter = None

    if event.get('queryStringParameters') is not None:
        if 'Sentences' in event['queryStringParameters']:
            sentences = (event['queryStringParameters']['Sentences'])
    elif event.get('Sentences') is not None:
        sentences = event['Sentences']
    else:
        return "Error"

    if event.get('queryStringParameters') is not None:
        if 'NextIterationCounter' in event['queryStringParameters']:
            counter = (event['queryStringParameters']['NextIterationCounter'])
    elif event.get('NextIterationCounter') is not None:
        counter = event['NextIterationCounter']
    else:
        return "Error"

    # prepare and return response
    return {
        'Sentences': sentences,
        'CurrentSentence': sentences[counter],
        'NextIterationCounter': counter + 1,
        'EndNext': len(sentences) == counter + 1

    }