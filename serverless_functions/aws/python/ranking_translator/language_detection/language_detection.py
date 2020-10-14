import boto3
import os

REGION = os.environ['AWS_REGION']
COMPREHEND_CLIENT = boto3.client("comprehend", region_name=REGION)


def lambda_handler(event, context):
    # search for string in request
    sentence = None

    if event.get('queryStringParameters') is not None:
        if 'sentence' in event['queryStringParameters']:
            sentence = (event['queryStringParameters']['sentence'])
    elif event.get('sentence') is not None:
        sentence = event['sentence']
    else:
        return "Error"

    # prepare and return response
    return {
        'sentence': sentence,
        'language': detect_dominant_language(sentence)
    }


def detect_dominant_language(text) -> str:

    # prepare and perform request
    languages = COMPREHEND_CLIENT.detect_dominant_language(Text=text)['Languages']

    # analyze result
    max_index = 0
    max_score = 0
    for language in languages:
        if language['Score'] > max_score:
            max_score = language['Score']
            max_index = languages.index(language)

    return languages[max_index]['LanguageCode'].lower()
