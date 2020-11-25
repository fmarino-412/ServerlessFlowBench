import boto3
import os

REGION = os.environ['AWS_REGION']
TRANSLATE_CLIENT = boto3.client("translate", region_name=REGION, use_ssl=True)


# noinspection DuplicatedCode,PyUnusedLocal
def lambda_handler(event, context):
    # search for string and language in request
    sentence = None
    language_code = None

    if event.get('queryStringParameters') is not None:
        if 'sentence' in event['queryStringParameters']:
            sentence = (event['queryStringParameters']['sentence'])
    elif event.get('sentence') is not None:
        sentence = event['sentence']
    else:
        return "Error"

    if event.get('queryStringParameters') is not None:
        if 'language_code' in event['queryStringParameters']:
            language_code = (event['queryStringParameters']['language_code'])
    elif event.get('language_code') is not None:
        language_code = event['language_code']
    else:
        return "Error"

    # prepare and return response
    return {
        'original_sentence': sentence,
        'sentence': translate_text(sentence, language_code)
    }


def translate_text(text, source_language_code) -> str:

    # prepare and perform request
    return TRANSLATE_CLIENT.translate_text(Text=text, SourceLanguageCode=source_language_code, TargetLanguageCode="en")[
        "TranslatedText"]
