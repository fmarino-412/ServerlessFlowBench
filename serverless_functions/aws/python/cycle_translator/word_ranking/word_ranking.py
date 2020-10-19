import boto3
import re

DYNAMODB = boto3.resource("dynamodb")


# noinspection DuplicatedCode
def lambda_handler(event, context):
    # search for string and table in request
    sentence = None
    ranking_table_name = None

    if event.get('queryStringParameters') is not None:
        if 'sentence' in event['queryStringParameters']:
            sentence = (event['queryStringParameters']['sentence'])
    elif event.get('sentence') is not None:
        sentence = event['sentence']
    else:
        return "Error"

    if event.get('queryStringParameters') is not None:
        if 'ranking_table_name' in event['queryStringParameters']:
            ranking_table_name = (event['queryStringParameters']['ranking_table_name'])
    elif event.get('ranking_table_name') is not None:
        ranking_table_name = event['ranking_table_name']
    else:
        return "Error"

    # isolate words
    for word in re.findall("[a-zA-Z]+", sentence):
        rank_word(word.lower(), ranking_table_name)

    # prepare and return response
    return "Updated"


def rank_word(word, table_name):

    # prepare and perform request
    table = DYNAMODB.Table(table_name)
    table.update_item(
        Key={
            'word': word
        },
        UpdateExpression="ADD word_counter :word_counter",
        ExpressionAttributeValues={
            ':word_counter': 1
        },
        ReturnValues="NONE"
    )
