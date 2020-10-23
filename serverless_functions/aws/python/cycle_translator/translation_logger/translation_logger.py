import boto3
import datetime
import random
import string

S3_CLIENT = boto3.client("s3")


# noinspection DuplicatedCode
def lambda_handler(event, context):

    # search for strings, original language code and logging bucket in request
    original_sentence = None
    original_language_code = None
    translated_sentence = None
    logging_bucket_name = None

    if event.get('queryStringParameters') is not None:
        if 'original_sentence' in event['queryStringParameters']:
            original_sentence = (event['queryStringParameters']['original_sentence'])
    elif event.get('original_sentence') is not None:
        original_sentence = event['original_sentence']
    else:
        return "Error"

    if event.get('queryStringParameters') is not None:
        if 'original_language_code' in event['queryStringParameters']:
            original_language_code = (event['queryStringParameters']['original_language_code'])
    elif event.get('original_language_code') is not None:
        original_language_code = event['original_language_code']
    else:
        return "Error"

    if event.get('queryStringParameters') is not None:
        if 'translated_sentence' in event['queryStringParameters']:
            translated_sentence = (event['queryStringParameters']['translated_sentence'])
    elif event.get('translated_sentence') is not None:
        translated_sentence = event['translated_sentence']
    else:
        return "Error"

    if event.get('queryStringParameters') is not None:
        if 'logging_bucket_name' in event['queryStringParameters']:
            logging_bucket_name = (event['queryStringParameters']['logging_bucket_name'])
    elif event.get('logging_bucket_name') is not None:
        logging_bucket_name = event['logging_bucket_name']
    else:
        return "Error"

    log_translation(original_sentence, original_language_code, translated_sentence, "en", logging_bucket_name)

    # prepare and return response
    return "Logged"


def log_translation(original_sentence, original_language_code, translated_sentence, destination_language_code,
                    logging_bucket_name):

    # timestamp
    timestamp = datetime.datetime.utcnow()

    # create filename
    filename = "Translation " + str(timestamp.isoformat(sep=' ', timespec='milliseconds')) + make_id() + ".log"
    filename = filename.replace(" ", "_")

    # create body
    body = "Translation info:" + "\n\n" + "original sentence: " + original_sentence + "\n" + "original language: " + \
           original_language_code + "\n" + "translated sentence: " + translated_sentence + "\n" + \
           "destination language: " + destination_language_code + "\n" + "log date: " + str(timestamp.date()) + "\n" + \
           "log time: " + str(timestamp.time())

    # prepare and perform request
    S3_CLIENT.put_object(
        Bucket=logging_bucket_name,
        Key=filename,
        Body=body
    )


def make_id() -> str:
    letters = string.ascii_letters
    fingerprint = ''.join(random.choice(letters) for _ in range(8))
    return "[PythonRuntime_" + fingerprint + "]"
